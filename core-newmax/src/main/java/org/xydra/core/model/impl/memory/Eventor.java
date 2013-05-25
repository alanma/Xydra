package org.xydra.core.model.impl.memory;

import java.util.List;

import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.change.XAtomicEvent;
import org.xydra.base.change.XEvent;
import org.xydra.base.change.XFieldEvent;
import org.xydra.base.change.XModelEvent;
import org.xydra.base.change.XObjectEvent;
import org.xydra.base.change.XRepositoryEvent;
import org.xydra.base.change.XTransactionEvent;
import org.xydra.base.change.impl.memory.MemoryFieldEvent;
import org.xydra.base.change.impl.memory.MemoryModelEvent;
import org.xydra.base.change.impl.memory.MemoryObjectEvent;
import org.xydra.base.change.impl.memory.MemoryRepositoryEvent;
import org.xydra.base.change.impl.memory.MemoryTransactionEvent;
import org.xydra.base.rmof.XReadableField;
import org.xydra.base.rmof.XReadableObject;
import org.xydra.base.rmof.XRevWritableField;
import org.xydra.base.rmof.XRevWritableModel;
import org.xydra.base.rmof.XRevWritableObject;
import org.xydra.base.rmof.XWritableField;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.base.rmof.XWritableObject;
import org.xydra.base.rmof.impl.XExistsRevWritableModel;
import org.xydra.base.rmof.impl.XExistsRevWritableObject;
import org.xydra.base.value.XValue;
import org.xydra.core.change.RevisionConstants;
import org.xydra.core.model.delta.ChangedField;
import org.xydra.core.model.delta.ChangedModel;
import org.xydra.core.model.delta.ChangedObject;
import org.xydra.core.model.delta.DeltaUtils.ModelChange;


/**
 * Compute Changed...MOF entities into a list of {@link XEvent}.
 * 
 * Applies events to XWritable...MOF entities.
 * 
 * @author xamde
 */
class Eventor {
    
    /**
     * @param events @NeverNull
     * @param actorId @NeverNull
     * @param target @NeverNull
     * @param modelRevision
     * @param objectRevision
     * @return the single event within the events list or a
     *         {@link XTransactionEvent} containing all given events
     */
    static XEvent createSingleEvent(List<XAtomicEvent> events, XId actorId, XAddress target,
            long modelRevision, long objectRevision) {
        assert !events.isEmpty() : "no events in list";
        if(events.size() == 1) {
            return events.get(0);
        }
        XTransactionEvent txnEvent = MemoryTransactionEvent.createTransactionEvent(actorId, target,
                events, modelRevision, objectRevision);
        return txnEvent;
    }
    
    /**
     * Can create events from stand-alone atomic commands or from within a
     * transaction.
     * 
     * @param events where to add new events @NeverNull
     * @param actorId @NeverNull
     * @param modelRevBeforeTxn
     * @param objectRevBeforeTxn
     * @param fieldRevBeforeTxn
     * @param fieldStateInTxn representing state after a command @NeverNull
     * @param inTransaction
     */
    static void createEventsFromChangedField(List<XAtomicEvent> events, XId actorId,
            long modelRevBeforeTxn, long objectRevBeforeTxn, XReadableField fieldBeforeTxn,
            ChangedField fieldStateInTxn, boolean inTransaction) {
        XValue newValue = fieldStateInTxn.getValue();
        XValue oldValue = fieldStateInTxn.getOldValue();
        assert newValue != null || oldValue != null;
        XAddress fieldAddress = fieldStateInTxn.getAddress();
        long fieldRevBeforeTxn = fieldBeforeTxn.getRevisionNumber();
        XFieldEvent event = null;
        if(oldValue == null && newValue != null) {
            // add value
            event = MemoryFieldEvent.createAddEvent(actorId, fieldAddress, newValue,
                    modelRevBeforeTxn, objectRevBeforeTxn, fieldRevBeforeTxn, inTransaction);
        } else if(oldValue != null && newValue == null) {
            // remove value
            event = MemoryFieldEvent.createRemoveEvent(actorId, fieldAddress, modelRevBeforeTxn,
                    objectRevBeforeTxn, fieldRevBeforeTxn, false, inTransaction);
        } else if(oldValue != null && newValue != null) {
            // change value
            event = MemoryFieldEvent.createChangeEvent(actorId, fieldAddress, newValue,
                    modelRevBeforeTxn, objectRevBeforeTxn, fieldRevBeforeTxn, inTransaction);
        }
        events.add(event);
    }
    
    /**
     * Creates REMOVE-field and REMOVE-value events
     * 
     * @param events where to add resulting events @NeverNull
     * @param actorId @NeverNull
     * @param modelRevBeforeTxn
     * @param objectRevBeforeTxn
     * @param removedFieldBeforeTxn @NeverNull
     * @param inTransaction
     * @param implied true if this call is part of a larger recursive remove
     *            call
     */
    private static void createEventsFromRemovedField(List<XAtomicEvent> events, XId actorId,
            long modelRevBeforeTxn, long objectRevBeforeTxn, XReadableField removedFieldBeforeTxn,
            boolean inTransaction, boolean implied) {
        boolean txnFlag = inTransaction;
        long fieldRevBeforeTxn = removedFieldBeforeTxn.getRevisionNumber();
        if(!removedFieldBeforeTxn.isEmpty()) {
            txnFlag = true;
            events.add(MemoryFieldEvent.createRemoveEvent(actorId,
                    removedFieldBeforeTxn.getAddress(), modelRevBeforeTxn, objectRevBeforeTxn,
                    fieldRevBeforeTxn, txnFlag, true));
        }
        events.add(MemoryObjectEvent.createRemoveEvent(actorId, removedFieldBeforeTxn.getAddress()
                .getParent(), removedFieldBeforeTxn.getId(), modelRevBeforeTxn, objectRevBeforeTxn,
                fieldRevBeforeTxn, txnFlag, implied));
    }
    
    /**
     * Creates ADD-field and ADD-value events
     * 
     * @param events where to add resulting events @NeverNull
     * @param actorId @NeverNull
     * @param modelRevBeforeTxn
     * @param objectRevBeforeTxn
     * @param addedFieldInTxn @NeverNull
     * @param inTransaction
     */
    private static void createEventsFromAddedField(List<XAtomicEvent> events, XId actorId,
            long modelRevBeforeTxn, long objectRevBeforeTxn, XReadableField addedFieldInTxn,
            boolean inTransaction) {
        
        if(!addedFieldInTxn.isEmpty()) {
            events.add(MemoryFieldEvent.createAddEvent(actorId, addedFieldInTxn.getAddress(),
                    addedFieldInTxn.getValue(), modelRevBeforeTxn, objectRevBeforeTxn,
                    RevisionConstants.REVISION_OF_ENTITY_NOT_SET, inTransaction));
        }
        events.add(MemoryObjectEvent.createAddEvent(actorId, addedFieldInTxn.getAddress()
                .getParent(), addedFieldInTxn.getId(), modelRevBeforeTxn, objectRevBeforeTxn,
                inTransaction));
    }
    
    /**
     * Can create events from stand-alone atomic commands or from within a
     * transaction. In both cases, the result may be a transactionEvent, in the
     * case of REMOVE.
     * 
     * @param events
     * @param actorId
     * @param modelRevBeforeTxn
     * @param objectBeforeTxn
     * @param objectInTxn @NeverNull
     * @param inTransaction
     */
    static void createEventsFromChangedObject(List<XAtomicEvent> events, XId actorId,
            long modelRevBeforeTxn, XReadableObject objectBeforeTxn, ChangedObject objectInTxn,
            boolean inTransaction) {
        assert objectInTxn != null;
        assert objectInTxn.hasChanges();
        
        long objectRevBeforeTxn = objectBeforeTxn.getRevisionNumber();
        
        // removed
        for(XId removedFieldIdInTxn : objectInTxn.getRemoved()) {
            XReadableField removedFieldBeforeTxn = objectBeforeTxn.getField(removedFieldIdInTxn);
            assert removedFieldBeforeTxn != null;
            // recursive remove
            createEventsFromRemovedField(events, actorId, modelRevBeforeTxn, objectRevBeforeTxn,
                    removedFieldBeforeTxn, inTransaction, false);
        }
        
        // added
        for(XReadableField addedFieldInTxn : objectInTxn.getAdded()) {
            assert !objectBeforeTxn.hasField(addedFieldInTxn.getId());
            XAtomicEvent event = MemoryObjectEvent.createAddEvent(actorId,
                    objectInTxn.getAddress(), addedFieldInTxn.getId(), objectRevBeforeTxn,
                    inTransaction);
            events.add(event);
            // transform field content to events
            if(!addedFieldInTxn.isEmpty()) {
                createEventsFromAddedField(events, actorId, modelRevBeforeTxn, objectRevBeforeTxn,
                        addedFieldInTxn, inTransaction);
            }
        }
        // changed
        for(ChangedField fieldInTxn : objectInTxn.getChangedFields()) {
            if(fieldInTxn.isChanged()) {
                assert objectBeforeTxn.hasField(fieldInTxn.getId());
                XReadableField fieldBeforeTxn = objectBeforeTxn.getField(fieldInTxn.getId());
                createEventsFromChangedField(events, actorId, modelRevBeforeTxn,
                        objectRevBeforeTxn, fieldBeforeTxn, fieldInTxn, inTransaction);
            }
        }
    }
    
    /**
     * Creates REMOVE-object, REMOVE-field and REMOVE-value events
     * 
     * @param events where to add resulting events @NeverNull
     * @param actorId @NeverNull
     * @param modelRevBeforeTxn
     * @param removedObjectInTxn @NeverNull
     * @param inTransaction
     * @param implied true if this call is part of a larger recursive remove
     *            call
     */
    private static void createEventsFromRemovedObject(List<XAtomicEvent> events, XId actorId,
            long modelRevBeforeTxn, XReadableObject removedObjectInTxn, boolean inTransaction,
            boolean implied) {
        long objectRevBeforeTxn = removedObjectInTxn.getRevisionNumber();
        if(!removedObjectInTxn.isEmpty()) {
            for(XId removedFieldIdInTxn : removedObjectInTxn) {
                XReadableField removedFieldInTxn = removedObjectInTxn.getField(removedFieldIdInTxn);
                createEventsFromRemovedField(events, actorId, modelRevBeforeTxn,
                        objectRevBeforeTxn, removedFieldInTxn, inTransaction, implied);
            }
        }
        events.add(MemoryModelEvent.createRemoveEvent(actorId, removedObjectInTxn.getAddress()
                .getParent(), removedObjectInTxn.getId(), modelRevBeforeTxn, objectRevBeforeTxn,
                inTransaction, implied));
    }
    
    private static void createEventsFromAddedObject(List<XAtomicEvent> events, XId actorId,
            long modelRevBeforeTxn, XReadableObject addedObjectInTxn, boolean inTransaction) {
        XModelEvent event = MemoryModelEvent.createAddEvent(actorId, addedObjectInTxn.getAddress()
                .getParent(), addedObjectInTxn.getId(), modelRevBeforeTxn, inTransaction);
        events.add(event);
        for(XId addedFieldId : addedObjectInTxn) {
            createEventsFromAddedField(events, actorId, modelRevBeforeTxn,
                    RevisionConstants.REVISION_OF_ENTITY_NOT_SET,
                    addedObjectInTxn.getField(addedFieldId), inTransaction);
        }
    }
    
    /**
     * Can create events from stand-alone atomic commands or from within a
     * transaction.
     * 
     * @param events @NeverNull
     * @param actorId
     * @param modelBeforeTxn
     * @param modelInTxn @NeverNull
     * @param inTransaction
     */
    static void createEventsFromChangedModel(List<XAtomicEvent> events, XId actorId,
            XRevWritableModel modelBeforeTxn, ChangedModel modelInTxn, boolean inTransaction) {
        assert modelInTxn != null;
        assert modelInTxn.hasChanges();
        
        // model event
        if(modelInTxn.modelWasCreated()) {
            createEventsFromModelChange(events, actorId, modelBeforeTxn, ModelChange.CREATED,
                    inTransaction);
        } else if(modelInTxn.modelWasRemoved()) {
            createEventsFromModelChange(events, actorId, modelBeforeTxn, ModelChange.REMOVED,
                    inTransaction);
        }
        
        long modelRevBeforeTxn = modelInTxn.getRevisionNumber();
        // removed
        for(XId removedObjectIdInTxn : modelInTxn.getRemoved()) {
            XReadableObject removedObjectBeforeTxn = modelBeforeTxn.getObject(removedObjectIdInTxn);
            assert removedObjectBeforeTxn != null;
            
            // recursive remove
            createEventsFromRemovedObject(events, actorId, modelRevBeforeTxn,
                    removedObjectBeforeTxn, inTransaction, false);
        }
        
        // added
        for(XReadableObject addedObjectInTxn : modelInTxn.getAdded()) {
            assert !modelBeforeTxn.hasObject(addedObjectInTxn.getId());
            // transform object content to events
            createEventsFromAddedObject(events, actorId, modelRevBeforeTxn, addedObjectInTxn,
                    inTransaction);
        }
        // changed
        for(ChangedObject objectInTxn : modelInTxn.getChangedObjects()) {
            if(objectInTxn.isChanged()) {
                assert modelBeforeTxn.hasObject(objectInTxn.getId());
                XReadableObject objectBeforeTxn = modelBeforeTxn.getObject(objectInTxn.getId());
                createEventsFromChangedObject(events, actorId, modelRevBeforeTxn, objectBeforeTxn,
                        objectInTxn, inTransaction);
            }
        }
    }
    
    /**
     * Can create events from stand-alone atomic commands or from within a
     * transaction.
     * 
     * @param events
     * @param actorId
     * @param modelBeforeTxn
     * @param modelChangeInTxn
     * @param inTransaction
     */
    static void createEventsFromModelChange(List<XAtomicEvent> events, XId actorId,
            XRevWritableModel modelBeforeTxn, ModelChange modelChangeInTxn, boolean inTransaction) {
        switch(modelChangeInTxn) {
        case CREATED: {
            XRepositoryEvent event = MemoryRepositoryEvent.createAddEvent(actorId, modelBeforeTxn
                    .getAddress().getParent(), modelBeforeTxn.getId(), modelBeforeTxn
                    .getRevisionNumber(), inTransaction);
            events.add(event);
        }
            break;
        case REMOVED: {
            XRepositoryEvent event = MemoryRepositoryEvent.createRemoveEvent(actorId,
                    modelBeforeTxn.getAddress().getParent(), modelBeforeTxn.getId(),
                    modelBeforeTxn.getRevisionNumber(), inTransaction);
            events.add(event);
        }
            break;
        default:
            throw new IllegalArgumentException();
        }
    }
    
    /**
     * Apply revision- and state-changes
     * 
     * @param event @NeverNull
     * @param modelState @CanBeNull
     * @param objectState @CanBeNull
     * @param fieldState @NeverNull
     */
    static void applyEventOnFieldAndParents(XFieldEvent event, XWritableModel modelState,
            XWritableObject objectState, XWritableField fieldState) {
        assert event != null;
        assert fieldState != null;
        
        if(modelState != null && modelState instanceof XRevWritableModel) {
            ((XRevWritableModel)modelState).setRevisionNumber(event.getRevisionNumber());
        }
        if(objectState != null && objectState instanceof XRevWritableObject) {
            ((XRevWritableObject)objectState).setRevisionNumber(event.getRevisionNumber());
        }
        
        applyFieldEventOnFieldAndChildren(event, fieldState);
    }
    
    /**
     * Change fieldState according to given event
     * 
     * @param event @NeverNull
     * @param fieldState @NeverNull
     */
    private static void applyFieldEventOnFieldAndChildren(XFieldEvent event,
            XWritableField fieldState) {
        if(fieldState instanceof XRevWritableField) {
            ((XRevWritableField)fieldState).setRevisionNumber(event.getRevisionNumber());
        }
        
        switch(event.getChangeType()) {
        case ADD:
            assert fieldState.getValue() == null;
            fieldState.setValue(event.getNewValue());
            break;
        case REMOVE:
            assert fieldState.getValue() != null;
            fieldState.setValue(null);
            break;
        case CHANGE:
            assert fieldState.getValue() != null;
            fieldState.setValue(event.getNewValue());
            break;
        default:
            assert false;
        }
    }
    
    /**
     * @param event @NeverNull
     * @param modelState @CanBeNull
     * @param objectState @NeverNull
     */
    static void applyEventOnObjectAndParents(XEvent event, XRevWritableModel modelState,
            XRevWritableObject objectState) {
        // apply rev nr changes, if possible
        if(modelState != null && modelState instanceof XRevWritableModel) {
            ((XRevWritableModel)modelState).setRevisionNumber(event.getRevisionNumber());
        }
        
        // apply state changes
        switch(event.getTarget().getAddressedType()) {
        case XREPOSITORY:
            throw new IllegalArgumentException();
        case XMODEL:
            assert modelState == null;
            assert objectState instanceof XExistsRevWritableObject : "otherwise this event cannot be applied to a stand-alone object";
            applyModelEventOnObject((XModelEvent)event, (XExistsRevWritableObject)objectState);
            break;
        case XOBJECT:
            if(event instanceof XTransactionEvent) {
                applyTransactionEventOnObjectAndChildren((XTransactionEvent)event, objectState);
            } else {
                applyObjectEventOnObjectAndChildren((XObjectEvent)event, objectState);
            }
            break;
        case XFIELD:
            if(objectState instanceof XRevWritableObject) {
                ((XRevWritableObject)objectState).setRevisionNumber(event.getRevisionNumber());
            }
            XFieldEvent fieldEvent = (XFieldEvent)event;
            XRevWritableField fieldState = objectState.getField(fieldEvent.getFieldId());
            applyFieldEventOnFieldAndChildren(fieldEvent, fieldState);
        }
    }
    
    private static void applyTransactionEventOnObjectAndChildren(XTransactionEvent txnEvent,
            XRevWritableObject objectState) {
        for(XAtomicEvent atomicEvent : txnEvent) {
            applyEventOnObjectAndParents(atomicEvent, null, objectState);
        }
    }
    
    /**
     * @param event @NeverNull
     * @param objectState @NeverNull
     */
    private static void applyModelEventOnObject(XModelEvent event,
            XExistsRevWritableObject objectState) {
        assert event.getChangedEntity().equals(objectState.getAddress());
        if(objectState instanceof XRevWritableObject) {
            ((XRevWritableObject)objectState).setRevisionNumber(event.getRevisionNumber());
        }
        switch(event.getChangeType()) {
        case ADD:
            objectState.setExists(true);
            break;
        case REMOVE:
            objectState.setExists(false);
            break;
        default:
            throw new IllegalArgumentException();
        }
    }
    
    /**
     * @param event can be a fieldEvent, objectEvent or transactionEvent @NeverNull
     * @param objectState @NeverNull
     */
    private static void applyObjectEventOnObjectAndChildren(XObjectEvent event,
            XRevWritableObject objectState) {
        if(objectState instanceof XRevWritableObject) {
            ((XRevWritableObject)objectState).setRevisionNumber(event.getRevisionNumber());
        }
        switch(event.getChangeType()) {
        case ADD:
            XRevWritableField field = objectState.createField(event.getFieldId());
            field.setRevisionNumber(event.getRevisionNumber());
            break;
        case REMOVE:
            objectState.removeField(event.getFieldId());
            break;
        default:
            throw new IllegalArgumentException();
        }
    }
    
    /**
     * @param event @NeverNull
     * @param modelState @NeverNull
     */
    static void applyEventOnModel(XEvent event, XExistsRevWritableModel modelState) {
        assert modelState != null;
        switch(event.getTarget().getAddressedType()) {
        case XREPOSITORY:
            applyRepositoryEventOnModel((XRepositoryEvent)event, modelState);
            break;
        case XMODEL:
            if(event instanceof XTransactionEvent) {
                applyTransactionEventOnModelAndChildren((XTransactionEvent)event, modelState);
            } else {
                applyModelEventOnModelAndChildren((XModelEvent)event, modelState);
            }
            break;
        case XOBJECT:
        case XFIELD:
            XId objectId;
            if(event instanceof XTransactionEvent) {
                XTransactionEvent txnEvent = (XTransactionEvent)event;
                objectId = txnEvent.getTarget().getObject();
            } else {
                XObjectEvent objectEvent = (XObjectEvent)event;
                objectId = objectEvent.getObjectId();
            }
            
            XRevWritableObject objectState = modelState.getObject(objectId);
            if(objectState == null)
                throw new IllegalArgumentException("Cannot execut event on non-existing object");
            applyEventOnObjectAndParents(event, modelState, objectState);
        }
    }
    
    private static void applyTransactionEventOnModelAndChildren(XTransactionEvent txnEvent,
            XExistsRevWritableModel modelState) {
        for(XAtomicEvent atomicEvent : txnEvent) {
            applyEventOnModel(atomicEvent, modelState);
        }
    }
    
    /**
     * @param event @NeverNull
     * @param modelState @NeverNull
     */
    private static void applyModelEventOnModelAndChildren(XModelEvent event,
            XExistsRevWritableModel modelState) {
        assert event.getTarget().equals(modelState.getAddress());
        if(modelState != null && modelState instanceof XRevWritableModel) {
            ((XRevWritableModel)modelState).setRevisionNumber(event.getRevisionNumber());
        }
        switch(event.getChangeType()) {
        case ADD:
            XRevWritableObject object = modelState.createObject(event.getObjectId());
            object.setRevisionNumber(event.getRevisionNumber());
            break;
        case REMOVE:
            modelState.removeObject(event.getObjectId());
            break;
        default:
            throw new IllegalArgumentException();
        }
    }
    
    /**
     * @param event @NeverNull
     * @param modelState @NeverNull
     */
    private static void applyRepositoryEventOnModel(XRepositoryEvent event,
            XExistsRevWritableModel modelState) {
        assert modelState != null;
        modelState.setRevisionNumber(event.getRevisionNumber());
        switch(event.getChangeType()) {
        case ADD:
            modelState.setExists(true);
            break;
        case REMOVE:
            modelState.setExists(false);
            break;
        default:
            throw new IllegalArgumentException();
        }
    }
    
    static void applyModelChange(ModelChange modelChange, XExistsRevWritableModel modelState) {
        switch(modelChange) {
        case CREATED:
            modelState.setExists(true);
            break;
        case REMOVED:
            modelState.setExists(false);
            break;
        default:
            throw new AssertionError();
        }
    }
    
}
