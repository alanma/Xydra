package org.xydra.core.model.delta;

import java.util.ArrayList;
import java.util.List;

import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.change.XAtomicCommand.Intent;
import org.xydra.base.change.XAtomicEvent;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XRepositoryCommand;
import org.xydra.base.change.XRepositoryEvent;
import org.xydra.base.change.impl.memory.MemoryFieldEvent;
import org.xydra.base.change.impl.memory.MemoryModelEvent;
import org.xydra.base.change.impl.memory.MemoryObjectEvent;
import org.xydra.base.change.impl.memory.MemoryRepositoryEvent;
import org.xydra.base.change.impl.memory.RevisionConstants;
import org.xydra.base.rmof.XReadableField;
import org.xydra.base.rmof.XReadableModel;
import org.xydra.base.rmof.XReadableObject;
import org.xydra.base.rmof.XRevWritableField;
import org.xydra.base.rmof.XRevWritableModel;
import org.xydra.base.rmof.XRevWritableObject;
import org.xydra.base.rmof.impl.XExistsReadableModel;
import org.xydra.base.rmof.impl.XExistsWritableModel;
import org.xydra.base.rmof.impl.memory.SimpleModel;
import org.xydra.base.value.XValue;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.sharedutils.XyAssert;


/**
 * Helper class for executing commands and generating matching events.
 * 
 * @author dscharrer
 */
public abstract class DeltaUtils {
    
    private static final Logger log = LoggerFactory.getLogger(DeltaUtils.class);
    
    /**
     * A description of what happened to the model itself.
     * 
     * Changes to individual objects and fields are described by
     * {@link ChangedModel}.
     */
    @Deprecated
    public enum ModelChange {
        CREATED, NOCHANGE, REMOVED
    }
    
    private static void applyChanges(XRevWritableModel model, ChangedModel changedModel, long rev) {
        
        for(XId objectId : changedModel.getRemovedObjects()) {
            XyAssert.xyAssert(model.hasObject(objectId));
            model.removeObject(objectId);
        }
        
        for(XReadableObject object : changedModel.getNewObjects()) {
            XyAssert.xyAssert(!model.hasObject(object.getId()));
            XRevWritableObject newObject = model.createObject(object.getId());
            for(XId fieldId : object) {
                applyChanges(newObject, object.getField(fieldId), rev);
            }
            newObject.setRevisionNumber(rev);
        }
        
        for(ChangedObject changedObject : changedModel.getChangedObjects()) {
            
            boolean objectChanged = false;
            
            XRevWritableObject object = model.getObject(changedObject.getId());
            XyAssert.xyAssert(object != null);
            assert object != null;
            
            for(XId fieldId : changedObject.getRemovedFields()) {
                XyAssert.xyAssert(object.hasField(fieldId));
                object.removeField(fieldId);
                objectChanged = true;
            }
            
            for(XReadableField field : changedObject.getNewFields()) {
                applyChanges(object, field, rev);
                objectChanged = true;
            }
            
            for(ChangedField changedField : changedObject.getChangedFields()) {
                if(changedField.isChanged()) {
                    XRevWritableField field = object.getField(changedField.getId());
                    XyAssert.xyAssert(field != null);
                    assert field != null;
                    boolean valueChanged = field.setValue(changedField.getValue());
                    XyAssert.xyAssert(valueChanged);
                    field.setRevisionNumber(rev);
                    objectChanged = true;
                }
            }
            
            if(objectChanged) {
                object.setRevisionNumber(rev);
            }
            
        }
        
        model.setRevisionNumber(rev);
        
    }
    
    /**
     * Apply the given changes to a {@link XRevWritableModel}.
     * 
     * @param modelAddr The address of the model to change. This is used if the
     *            model needs to be created first (modelToChange is null).
     * @param modelToChange The model to change. This may be null if the model
     *            currently exists.
     * @param changedModel The changes to apply as returned by
     *            {@link #executeCommand(XExistsReadableModel, XCommand)}.
     * @param rev The revision number of the change.
     * @return a model with the changes applied or null if model has been
     *         removed by the changes.
     */
    public static XRevWritableModel applyChanges(XAddress modelAddr,
            XRevWritableModel modelToChange, ChangedModel changedModel, long rev) {
        
        XRevWritableModel model = modelToChange;
        
        if(changedModel.modelWasRemoved()) {
            return null;
        } else if(changedModel.modelWasCreated()) {
            assert model != null;
            assert !model.exists();
            model.setRevisionNumber(rev);
            if(model instanceof XExistsWritableModel) {
                ((XExistsWritableModel)model).setExists(true);
            }
        }
        
        if(changedModel != null) {
            XyAssert.xyAssert(model != null);
            assert model != null;
            applyChanges(model, changedModel, rev);
        }
        
        return model;
    }
    
    private static void applyChanges(XRevWritableObject object, XReadableField field, long rev) {
        XyAssert.xyAssert(!object.hasField(field.getId()));
        XRevWritableField newField = object.createField(field.getId());
        newField.setValue(field.getValue());
        newField.setRevisionNumber(rev);
    }
    
    /**
     * Calculated the events describing the given change.
     * 
     * @param modelAddr The model the change applies to.
     * @param changedModel A change as created by
     *            {@link #executeCommand(XExistsReadableModel, XCommand)}. @NeverNull
     * @param actorId The actor that initiated the change.
     * @param rev The revision number of the change.
     * @param forceTxnEvent if true, a txn is created even if there is only 1
     *            change and thus no transaction necessary
     * @return the appropriate events for the change (as returned by
     *         {@link #executeCommand(XExistsReadableModel, XCommand)}
     */
    public static List<XAtomicEvent> createEvents(XAddress modelAddr, ChangedModel changedModel,
            XId actorId, long rev, boolean forceTxnEvent) {
        XyAssert.xyAssert(changedModel != null);
        assert changedModel != null;
        
        /* we count only 0, 1 or 2 = many */
        int nChanges = changedModel.countCommandsNeeded(2);
        
        List<XAtomicEvent> events = new ArrayList<XAtomicEvent>();
        
        if(nChanges == 0) {
            return events;
        }
        
        XyAssert.xyAssert(nChanges > 0);
        
        /*
         * A transaction command always creates a transaction event; a single
         * command might create a transaction, i.e. when there are more than 1
         * resulting event
         */
        boolean inTxn = (nChanges > 1) || forceTxnEvent;
        
        XyAssert.xyAssert(changedModel.getAddress().equals(modelAddr));
        
        createEventsForChangedModel(events, actorId, changedModel, inTxn);
        
        assert nChanges == 1 ? events.size() == 1 :
        
        events.size() >= 2 :
        
        "1 change must result in 1 event, more changes result in more events. Got changes="
                + nChanges + " events=" + events.size();
        
        return events;
    }
    
    /**
     * @param events
     * @param actorId
     * @param changedModel
     * @param forceTransaction should always be true if there are more than 1
     *            events
     */
    public static void createEventsForChangedModel(List<XAtomicEvent> events, XId actorId,
            ChangedModel changedModel, boolean forceTransaction) {
        
        boolean implied = changedModel.modelWasRemoved();
        long rev = changedModel.getRevisionNumber();
        int nChanges = changedModel.countEventsNeeded(2);
        boolean inTransaction = forceTransaction || nChanges > 1;
        
        /* Repository ADD commands handled first */
        if(changedModel.modelWasCreated()) {
            XAddress target = changedModel.getAddress().getParent();
            XRepositoryEvent repositoryEvent = MemoryRepositoryEvent.createAddEvent(actorId,
                    target, changedModel.getId(), rev, inTransaction);
            events.add(repositoryEvent);
        }
        
        for(XId objectId : changedModel.getRemovedObjects()) {
            XReadableObject removedObject = changedModel.getOldObject(objectId);
            DeltaUtils.createEventsForRemovedObject(events, rev, actorId, removedObject,
                    inTransaction, implied);
        }
        
        for(XReadableObject object : changedModel.getNewObjects()) {
            events.add(MemoryModelEvent.createAddEvent(actorId, changedModel.getAddress(),
                    object.getId(), rev, inTransaction));
            for(XId fieldId : object) {
                DeltaUtils.createEventsForNewField(events, rev, actorId, object,
                        object.getField(fieldId), inTransaction);
            }
        }
        
        for(ChangedObject object : changedModel.getChangedObjects()) {
            createEventsForChangedObject(events, actorId, object, forceTransaction, rev);
        }
        
        /* Repository REMOVE commands handled last */
        if(changedModel.modelWasRemoved()) {
            XAddress target = changedModel.getAddress().getParent();
            XRepositoryEvent repositoryEvent = MemoryRepositoryEvent.createRemoveEvent(actorId,
                    target, changedModel.getId(), rev, inTransaction);
            events.add(repositoryEvent);
        }
        
    }
    
    /**
     * @param events
     * @param actorId
     * @param object
     * @param forceTransaction
     * @param currentModelRev the new resulting model revision
     */
    public static void createEventsForChangedObject(List<XAtomicEvent> events, XId actorId,
            ChangedObject object, boolean forceTransaction, long currentModelRev) {
        boolean inTransaction = forceTransaction || object.countCommandsNeeded(2) > 1;
        
        for(XId fieldId : object.getRemovedFields()) {
            DeltaUtils.createEventsForRemovedField(events, currentModelRev, actorId, object,
                    object.getOldField(fieldId), inTransaction, false);
        }
        
        for(XReadableField field : object.getNewFields()) {
            DeltaUtils.createEventsForNewField(events, currentModelRev, actorId, object, field,
                    inTransaction);
        }
        
        for(ChangedField field : object.getChangedFields()) {
            long objectRev = object.getRevisionNumber();
            DeltaUtils.createEventsForChangedField(events, currentModelRev, actorId, objectRev,
                    field, inTransaction);
        }
    }
    
    public static void createEventsForChangedField(List<XAtomicEvent> events, long currentModelRev,
            XId actorId, long currentObjectRev, ChangedField field, boolean inTransaction) {
        if(field.isChanged()) {
            // IMPROVE we only need to know if the old value exists
            XValue oldValue = field.getOldValue();
            XValue newValue = field.getValue();
            XAddress target = field.getAddress();
            long currentFieldRev = field.getRevisionNumber();
            if(newValue == null) {
                XyAssert.xyAssert(oldValue != null);
                assert oldValue != null;
                events.add(MemoryFieldEvent.createRemoveEvent(actorId, target, currentModelRev,
                        currentObjectRev, currentFieldRev, inTransaction, false));
            } else if(oldValue == null) {
                events.add(MemoryFieldEvent.createAddEvent(actorId, target, newValue,
                        currentModelRev, currentObjectRev, currentFieldRev, inTransaction));
            } else {
                events.add(MemoryFieldEvent.createChangeEvent(actorId, target, newValue,
                        currentModelRev, currentObjectRev, currentFieldRev, inTransaction));
            }
        }
    }
    
    private static void createEventsForNewField(List<XAtomicEvent> events, long rev, XId actorId,
            XReadableObject object, XReadableField field, boolean inTransaction) {
        long objectRev = object.getRevisionNumber();
        events.add(MemoryObjectEvent.createAddEvent(actorId, object.getAddress(), field.getId(),
                rev, objectRev, inTransaction));
        if(!field.isEmpty()) {
            events.add(MemoryFieldEvent.createAddEvent(actorId, field.getAddress(),
                    field.getValue(), rev, objectRev, field.getRevisionNumber(), inTransaction));
        }
    }
    
    private static void createEventsForRemovedField(List<XAtomicEvent> events, long modelRev,
            XId actorId, XReadableObject object, XReadableField field, boolean inTransaction,
            boolean implied) {
        long objectRev = object.getRevisionNumber();
        long fieldRev = field.getRevisionNumber();
        if(!field.isEmpty()) {
            events.add(MemoryFieldEvent.createRemoveEvent(actorId, field.getAddress(), modelRev,
                    objectRev, fieldRev, inTransaction, true));
        }
        events.add(MemoryObjectEvent.createRemoveEvent(actorId, object.getAddress(), field.getId(),
                modelRev, objectRev, fieldRev, inTransaction, implied));
    }
    
    private static void createEventsForRemovedObject(List<XAtomicEvent> events, long modelRev,
            XId actorId, XReadableObject object, boolean inTransaction, boolean implied) {
        for(XId fieldId : object) {
            DeltaUtils.createEventsForRemovedField(events, modelRev, actorId, object,
                    object.getField(fieldId), inTransaction, true);
        }
        events.add(MemoryModelEvent.createRemoveEvent(actorId, object.getAddress().getParent(),
                object.getId(), modelRev, object.getRevisionNumber(), inTransaction, implied));
    }
    
    /**
     * @param modelAddress
     * @param model @CanBeNull
     * @return the given model, if not null, or create a new one
     */
    private static XReadableModel createNonExistingModel(XAddress modelAddress, XReadableModel model) {
        if(model != null) {
            return model;
        }
        
        SimpleModel nonExistingModel = new SimpleModel(modelAddress);
        nonExistingModel.setExists(false);
        nonExistingModel.setRevisionNumber(RevisionConstants.NOT_EXISTING);
        return nonExistingModel;
    }
    
    /**
     * Calculate the changes resulting from executing the given command on the
     * given model.
     * 
     * @param model The model to modify. Null if the model currently doesn't
     *            exist. This instance is modified. @CanBeNull
     * @param command
     * @return The changed model after executing the command. Returns null if
     *         the command failed.
     */
    public static ChangedModel executeCommand(XExistsReadableModel model, XCommand command) {
        
        boolean modelExists = model != null && model.exists();
        
        if(command instanceof XRepositoryCommand) {
            XRepositoryCommand rc = (XRepositoryCommand)command;
            
            switch(rc.getChangeType()) {
            case ADD:
                if(modelExists) {
                    if(rc.getIntent() != Intent.Forced) {
                        log.warn("Safe-X RepositoryCommand ADD failed; modelExists=" + modelExists
                                + " model==null?" + (model == null));
                        return null;
                    }
                    log.info("Command is forced, but there is no change");
                    return new ChangedModel(createNonExistingModel(rc.getChangedEntity(), model));
                } else {
                    if(rc.getIntent() == Intent.SafeRevBound) {
                        if(model != null) {
                            long currentModelRev = model.getRevisionNumber();
                            if(((XRepositoryCommand)command).getRevisionNumber() != currentModelRev) {
                                log.warn("Safe RepositoryCommand ADD failed; modelRev="
                                        + currentModelRev + " command=" + command);
                                return null;
                            }
                        } else {
                            log.warn("SafeRevBound model-ADD, but we cannot check rev, model is null");
                        }
                    }
                    XAddress modelAddress = rc.getChangedEntity();
                    ChangedModel changedModel = new ChangedModel(createNonExistingModel(
                            modelAddress, model));
                    changedModel.setExists(true);
                    return changedModel;
                }
            case REMOVE:
                if(!modelExists) {
                    // which kind of SAFE command?
                    if(rc.getIntent() == Intent.Forced) {
                        log.info("There is no change");
                        return new ChangedModel(
                                createNonExistingModel(rc.getChangedEntity(), model));
                    } else {
                        log.warn("Safe-X RepositoryCommand REMOVE failed, model was already removed");
                        return null;
                    }
                } else {
                    assert model != null;
                    if(rc.getIntent() == Intent.SafeRevBound) {
                        if(model.getRevisionNumber() != rc.getRevisionNumber()) {
                            log.warn("SafeRevBound RepositoryCommand REMOVE failed. Reason: "
                                    + (model == null ? "model is null" : "modelRevNr:"
                                            + model.getRevisionNumber() + " cmdRevNr:"
                                            + rc.getRevisionNumber() + " intent:" + rc.getIntent()));
                            return null;
                        }
                    }
                    // do change
                    log.debug("Removing model " + model.getAddress() + " "
                            + model.getRevisionNumber());
                    ChangedModel changedModel = new ChangedModel(model);
                    changedModel.setExists(false);
                    return changedModel;
                }
                
            default:
                throw new AssertionError("XRepositoryCommand with unexpected type: " + rc);
            }
            
        } else {
            if(model == null) {
                log.warn("Safe Non-RepositoryCommand '" + command + "' failed on null-model");
                return null;
            }
            
            ChangedModel changedModel = new ChangedModel(model);
            
            // apply changes to the delta-model
            if(!changedModel.executeCommand(command)) {
                log.info("Could not execute command on ChangedModel");
                return null;
            }
            
            return changedModel;
        }
    }
}
