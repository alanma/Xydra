package org.xydra.core.model.delta;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.xydra.base.IHasXId;
import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.change.XAtomicEvent;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XRepositoryCommand;
import org.xydra.base.change.impl.memory.MemoryFieldEvent;
import org.xydra.base.change.impl.memory.MemoryModelEvent;
import org.xydra.base.change.impl.memory.MemoryObjectEvent;
import org.xydra.base.change.impl.memory.MemoryRepositoryEvent;
import org.xydra.base.rmof.XReadableField;
import org.xydra.base.rmof.XReadableModel;
import org.xydra.base.rmof.XReadableObject;
import org.xydra.base.rmof.XRevWritableField;
import org.xydra.base.rmof.XRevWritableModel;
import org.xydra.base.rmof.XRevWritableObject;
import org.xydra.base.rmof.impl.memory.SimpleModel;
import org.xydra.base.value.XValue;
import org.xydra.index.query.Pair;
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
     * Apply the given changes to a {@link SimpleModel}.
     * 
     * @param modelAddr The address of the model to change. This is used if the
     *            model needs to be created first (modelToChange is null).
     * @param modelToChange The model to change. This may be null if the model
     *            currently exists.
     * @param change The changes to apply as returned by
     *            {@link #executeCommand(XReadableModel, XCommand)}.
     * @param rev The revision number of the change.
     * @return a model with the changes applied or null if model has been
     *         removed by the changes.
     */
    public static XRevWritableModel applyChanges(XAddress modelAddr,
            XRevWritableModel modelToChange, Pair<ChangedModel,ModelChange> change, long rev) {
        
        XRevWritableModel model = modelToChange;
        ChangedModel changedModel = change.getFirst();
        ModelChange mc = change.getSecond();
        
        if(mc == ModelChange.REMOVED) {
            return null;
        } else if(mc == ModelChange.CREATED) {
            XyAssert.xyAssert(model == null);
            model = new SimpleModel(modelAddr);
            model.setRevisionNumber(rev);
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
     * @param change A change as created by
     *            {@link #executeCommand(XReadableModel, XCommand)}.
     * @param actorId The actor that initiated the change.
     * @param rev The revision number of the change.
     * @param forceTxnEvent if true, a txn is created even if there is only 1
     *            change and thus no transaction necessary
     * @return the appropriate events for the change (as returned by
     *         {@link #executeCommand(XReadableModel, XCommand)}
     */
    public static List<XAtomicEvent> createEvents(XAddress modelAddr,
            Pair<ChangedModel,ModelChange> change, XId actorId, long rev, boolean forceTxnEvent) {
        XyAssert.xyAssert(change != null);
        assert change != null;
        
        ChangedModel changedModel = change.getFirst();
        ModelChange modelChangeOperation = change.getSecond();
        
        assert changedModel == null || (rev - 1 == changedModel.getRevisionNumber()) : ("rev="
                + rev + " modelRev=" + changedModel.getRevisionNumber());
        
        /* we count only 0, 1 or 2 = many */
        int nChanges;
        switch(modelChangeOperation) {
        case NOCHANGE:
            if(changedModel == null) {
                nChanges = 0;
            } else {
                nChanges = changedModel.countCommandsNeeded(2);
            }
            break;
        case CREATED:
        case REMOVED: {
            nChanges = 1;
            if(changedModel != null) {
                nChanges += changedModel.countCommandsNeeded(1);
            }
            break;
        }
        default:
            throw new AssertionError("unreachable");
        }
        
        //
        // int nChanges = (modelChange == ModelChange.NOCHANGE ? 0 : 1);
        // if(model != null) {
        // nChanges += model.countEventsNeeded(2 - nChanges);
        // }
        
        List<XAtomicEvent> events = new ArrayList<XAtomicEvent>();
        
        if(nChanges == 0) {
            return events;
        }
        
        XyAssert.xyAssert(nChanges > 0);
        
        if(modelChangeOperation == ModelChange.CREATED) {
            events.add(MemoryRepositoryEvent.createAddEvent(actorId, modelAddr.getParent(),
                    modelAddr.getModel(), rev - 1,
                    /* creating a model is never part of a txn */
                    false));
        }
        
        // FIXME
        /*
         * FIXME is this the correct way to check if the events are supposed to
         * be in a transaction or not? We can construct transaction which
         * actually contain a lot of commands, but every command but one is
         * cancelled out by another command (i.e. adds get undone by removes in
         * the same transaction) and the transaction actually only changes one
         * single thing. Should we think of this case as a transaction (and
         * create a transaction event) or as a single command (and create a
         * single atomic event). I suppose a single, atomic event is preferable,
         * but we should document this thoroughly, since this behavior seems
         * logical, but actually is quite strange. (we execute a transaction,
         * but do not create a transaction event)
         * 
         * ~Kaidel
         */
        boolean inTxn = (nChanges > 1) || forceTxnEvent;
        
        if(changedModel != null) {
            XyAssert.xyAssert(changedModel.getAddress().equals(modelAddr));
            createEventsForChangedModel(events, actorId, changedModel, inTxn,
                    modelChangeOperation == ModelChange.REMOVED);
        }
        
        if(modelChangeOperation == ModelChange.REMOVED) {
            events.add(MemoryRepositoryEvent.createRemoveEvent(actorId, modelAddr.getParent(),
                    modelAddr.getModel(), rev - 1, inTxn));
        }
        
        assert nChanges == 1 ? events.size() == 1 : events.size() >= 2 : "1 change must result in 1 event, more changes result in more events";
        
        return events;
    }
    
    /**
     * @param events
     * @param actorId
     * @param changedModel
     * @param inTrans should always be true if there are more than 1 events
     * @param implied
     */
    public static void createEventsForChangedModel(List<XAtomicEvent> events, XId actorId,
            ChangedModel changedModel, boolean inTrans, boolean implied) {
        
        long rev = changedModel.getRevisionNumber();
        
        for(XId objectId : changedModel.getRemovedObjects()) {
            XReadableObject removedObject = changedModel.getOldObject(objectId);
            DeltaUtils.createEventsForRemovedObject(events, rev, actorId, removedObject, inTrans,
                    implied);
        }
        
        for(XReadableObject object : changedModel.getNewObjects()) {
            events.add(MemoryModelEvent.createAddEvent(actorId, changedModel.getAddress(),
                    object.getId(), rev, inTrans));
            for(XId fieldId : object) {
                DeltaUtils.createEventsForNewField(events, rev, actorId, object,
                        object.getField(fieldId), inTrans);
            }
        }
        
        for(ChangedObject object : changedModel.getChangedObjects()) {
            
            XyAssert.xyAssert(!implied);
            
            for(XId fieldId : object.getRemovedFields()) {
                DeltaUtils.createEventsForRemovedField(events, rev, actorId, object,
                        object.getOldField(fieldId), inTrans, false);
            }
            
            for(XReadableField field : object.getNewFields()) {
                DeltaUtils.createEventsForNewField(events, rev, actorId, object, field, inTrans);
            }
            
            for(ChangedField field : object.getChangedFields()) {
                if(field.isChanged()) {
                    XValue oldValue = field.getOldValue();
                    // IMPROVE we only need to know if the old value exists
                    XValue newValue = field.getValue();
                    XAddress target = field.getAddress();
                    long objectRev = object.getRevisionNumber();
                    long fieldRev = field.getRevisionNumber();
                    if(newValue == null) {
                        XyAssert.xyAssert(oldValue != null);
                        assert oldValue != null;
                        events.add(MemoryFieldEvent.createRemoveEvent(actorId, target, rev,
                                objectRev, fieldRev, inTrans, false));
                    } else if(oldValue == null) {
                        events.add(MemoryFieldEvent.createAddEvent(actorId, target, newValue, rev,
                                objectRev, fieldRev, inTrans));
                    } else {
                        events.add(MemoryFieldEvent.createChangeEvent(actorId, target, newValue,
                                rev, objectRev, fieldRev, inTrans));
                    }
                    
                }
            }
            
        }
        
    }
    
    private static void createEventsForNewField(List<XAtomicEvent> events, long rev, XId actorId,
            XReadableObject object, XReadableField field, boolean inTrans) {
        long objectRev = object.getRevisionNumber();
        events.add(MemoryObjectEvent.createAddEvent(actorId, object.getAddress(), field.getId(),
                rev, objectRev, inTrans));
        if(!field.isEmpty()) {
            events.add(MemoryFieldEvent.createAddEvent(actorId, field.getAddress(),
                    field.getValue(), rev, objectRev, field.getRevisionNumber(), inTrans));
        }
    }
    
    private static void createEventsForRemovedField(List<XAtomicEvent> events, long modelRev,
            XId actorId, XReadableObject object, XReadableField field, boolean inTrans,
            boolean implied) {
        long objectRev = object.getRevisionNumber();
        long fieldRev = field.getRevisionNumber();
        if(!field.isEmpty()) {
            events.add(MemoryFieldEvent.createRemoveEvent(actorId, field.getAddress(), modelRev,
                    objectRev, fieldRev, inTrans, true));
        }
        events.add(MemoryObjectEvent.createRemoveEvent(actorId, object.getAddress(), field.getId(),
                modelRev, objectRev, fieldRev, inTrans, implied));
    }
    
    private static void createEventsForRemovedObject(List<XAtomicEvent> events, long modelRev,
            XId actorId, XReadableObject object, boolean inTrans, boolean implied) {
        for(XId fieldId : object) {
            DeltaUtils.createEventsForRemovedField(events, modelRev, actorId, object,
                    object.getField(fieldId), inTrans, true);
        }
        events.add(MemoryModelEvent.createRemoveEvent(actorId, object.getAddress().getParent(),
                object.getId(), modelRev, object.getRevisionNumber(), inTrans, implied));
    }
    
    /**
     * Calculate the changes resulting from executing the given command on the
     * given model.
     * 
     * @param model The model to modify. Null if the model currently doesn't
     *            exist. This instance is modified.
     * @param command
     * @return The changed model after executing the command (may be null if
     *         there are no other changes except creating/removing the model)
     *         (Pair#getFirst()) and if the model was added or removed by the
     *         command (Pair#getSecond()). Returns null if the command failed.
     */
    public static Pair<ChangedModel,ModelChange> executeCommand(XReadableModel model,
            XCommand command) {
        
        if(command instanceof XRepositoryCommand) {
            
            XRepositoryCommand rc = (XRepositoryCommand)command;
            
            switch(rc.getChangeType()) {
            case ADD:
                if(model == null) {
                    return new Pair<ChangedModel,ModelChange>(null, ModelChange.CREATED);
                } else if(rc.isForced()) {
                    log.info("Command is forced, but there is no change");
                    return new Pair<ChangedModel,ModelChange>(null, ModelChange.NOCHANGE);
                } else {
                    log.warn("Safe RepositoryCommand ADD failed; model!=null");
                    return null;
                }
                
            case REMOVE:
                if((model == null || model.getRevisionNumber() != rc.getRevisionNumber())
                        && !rc.isForced()) {
                    log.warn("Safe RepositoryCommand REMOVE failed. Reason: "
                            + (model == null ? "model is null" : "modelRevNr:"
                                    + model.getRevisionNumber() + " cmdRevNr:"
                                    + rc.getRevisionNumber() + " forced:" + rc.isForced()));
                    return null;
                } else if(model != null) {
                    log.debug("Removing model " + model.getAddress() + " "
                            + model.getRevisionNumber());
                    ChangedModel changedModel = new ChangedModel(model);
                    changedModel.clear();
                    return new Pair<ChangedModel,ModelChange>(changedModel, ModelChange.REMOVED);
                } else {
                    log.info("There is no change");
                    return new Pair<ChangedModel,ModelChange>(null, ModelChange.NOCHANGE);
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
            
            return new Pair<ChangedModel,ModelChange>(changedModel, ModelChange.NOCHANGE);
            
        }
        
    }
    
    public static interface IModelDiff {
        Collection<? extends XReadableObject> getAdded();
        
        Collection<? extends IObjectDiff> getPotentiallyChanged();
        
        Collection<XId> getRemoved();
    }
    
    public static interface IObjectDiff extends IHasXId {
        Collection<? extends XReadableField> getAdded();
        
        Collection<? extends IFieldDiff> getPotentiallyChanged();
        
        Collection<XId> getRemoved();
        
        boolean hasChanges();
        
        @Override
        XId getId();
    }
    
    public static interface IFieldDiff extends IHasXId {
        XValue getInitialValue();
        
        // same signature as XReadableField
        XValue getValue();
        
        boolean isChanged();
        
        @Override
        XId getId();
    }
    
}
