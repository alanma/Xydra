package org.xydra.core.model.impl.memory;

import java.util.LinkedList;
import java.util.List;

import org.xydra.annotations.CanBeNull;
import org.xydra.annotations.ModificationOperation;
import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.change.XAtomicCommand;
import org.xydra.base.change.XAtomicEvent;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XCommandUtils;
import org.xydra.base.change.XEvent;
import org.xydra.base.change.XFieldCommand;
import org.xydra.base.change.XFieldEvent;
import org.xydra.base.change.XModelCommand;
import org.xydra.base.change.XModelEvent;
import org.xydra.base.change.XObjectCommand;
import org.xydra.base.change.XObjectEvent;
import org.xydra.base.change.XRepositoryCommand;
import org.xydra.base.change.XRepositoryEvent;
import org.xydra.base.change.XTransaction;
import org.xydra.base.change.XTransactionEvent;
import org.xydra.base.change.impl.memory.MemoryFieldEvent;
import org.xydra.base.change.impl.memory.MemoryModelEvent;
import org.xydra.base.change.impl.memory.MemoryObjectEvent;
import org.xydra.base.change.impl.memory.MemoryRepositoryEvent;
import org.xydra.base.change.impl.memory.MemoryTransactionEvent;
import org.xydra.base.rmof.XReadableField;
import org.xydra.base.rmof.XReadableModel;
import org.xydra.base.rmof.XReadableObject;
import org.xydra.base.rmof.XReadableRepository;
import org.xydra.base.rmof.XRevWritableField;
import org.xydra.base.rmof.XRevWritableModel;
import org.xydra.base.rmof.XRevWritableObject;
import org.xydra.base.rmof.XWritableField;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.base.rmof.XWritableObject;
import org.xydra.base.rmof.impl.XExistsReadableModel;
import org.xydra.base.rmof.impl.XExistsRevWritableModel;
import org.xydra.base.rmof.impl.XExistsRevWritableRepository;
import org.xydra.base.rmof.impl.memory.SimpleModel;
import org.xydra.base.value.XValue;
import org.xydra.core.XX;
import org.xydra.core.change.XRMOFChangeListener;
import org.xydra.core.model.delta.ChangedModel;
import org.xydra.core.model.impl.memory.sync.Root;
import org.xydra.index.XI;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.sharedutils.XyAssert;


/**
 * Does not deal with synchronisation.
 * 
 * Does not deal with entity-does-not-exist.
 * 
 * Implementation notes: Transaction locking is controlled by root.
 * 
 * @author xamde
 */
public class Executor {
    
    /**
     * Events are fired to listener and root in an intermixed fashion.
     * 
     * @param actorId @NeverNull
     * @param command @NeverNull
     * @param modelState @CanBeNull
     * @param objectState @CanBeNull if modelState is also null
     * @param fieldState @NeverNull
     * @param changeEventListener usually the field implementation in order to
     *            update its internal state, @CanBeNull
     * @param root used to fire events @NeverNull
     * @return result of executing command
     */
    public static long executeFieldCommand(XId actorId, XFieldCommand command,
    
    XRevWritableModel modelState, XRevWritableObject objectState, XRevWritableField fieldState,
    
    Root root, XRMOFChangeListener changeEventListener) {
        
        /* Assertions and failure cases */
        assert !(objectState == null && modelState != null);
        XyAssert.xyAssert(!root.isTransactionInProgess());
        
        // check whether the given event actually refers to this field
        if(!fieldState.getAddress().equals(command.getTarget())) {
            return XCommand.FAILED;
        }
        long currentModelRev = modelState == null ? XEvent.RevisionOfEntityNotSet : modelState
                .getRevisionNumber();
        long currentObjectRev = objectState == null ? XEvent.RevisionOfEntityNotSet : objectState
                .getRevisionNumber();
        long currentFieldRev = fieldState.getRevisionNumber();
        XValue currentValue = fieldState.getValue();
        
        EventResult eventResult = createEventFromFieldCommand(actorId, command, currentModelRev,
                currentObjectRev, currentFieldRev, currentValue, changeEventListener,
                root.isTransactionInProgess());
        
        if(!eventResult.changedSomething())
            return eventResult.getResult();
        
        // apply event
        XEvent event = eventResult.getEvent();
        applyFieldEvent((XFieldEvent)event, modelState, objectState, fieldState);
        root.getSyncLog().appendSyncLogEntry(command, event);
        fireEvents(root, changeEventListener, event);
        
        return event.getRevisionNumber();
    }
    
    private static class EventResult {
        
        private EventResult(long result, XEvent event) {
            this.result = result;
            this.event = event;
        }
        
        public boolean changedSomething() {
            return XCommandUtils.changedSomething(this.result);
        }
        
        public static EventResult failed() {
            return new EventResult(XCommand.FAILED, null);
        }
        
        public static EventResult noChange() {
            return new EventResult(XCommand.NOCHANGE, null);
        }
        
        public static EventResult success(XEvent event) {
            return new EventResult(event.getRevisionNumber(), event);
        }
        
        public long getResult() {
            return this.result;
        }
        
        public XEvent getEvent() {
            return this.event;
        }
        
        private long result;
        /** is null if result is XCommand.FAILED or XCommand.NOCHANGE */
        private XEvent event;
        
        public boolean isFailed() {
            return XCommandUtils.failed(getResult());
        }
    }
    
    private static EventResult createEventFromFieldCommand(XId actorId, XFieldCommand command,
    
    long currentModelRev, long currentObjectRev, long currentFieldRev, XValue currentValue,
    
    XRMOFChangeListener changeEventListener, boolean inTransaction) {
        
        /* Assertions and failure cases */
        if(!command.isForced() && command.getRevisionNumber() != currentFieldRev) {
            return EventResult.failed();
        }
        
        /* Standard execution */
        XValue newValue = command.getValue();
        switch(command.getChangeType()) {
        case ADD:
            if(currentValue != null) {
                /*
                 * the forced event only cares about the postcondition - that
                 * there is the given value set, not about that there was no
                 * value before
                 */
                if(!command.isForced()) {
                    // value already set
                    return EventResult.failed();
                }
            }
            break;
        case REMOVE:
            if(currentValue == null) {
                /*
                 * the forced event only cares about the postcondition - that
                 * there is no value set, not about that there was a value
                 * before
                 */
                if(!command.isForced()) {
                    /*
                     * value is not set and can not be removed or the given
                     * value is not current anymore
                     */
                    return EventResult.failed();
                }
            }
            XyAssert.xyAssert(newValue == null);
            break;
        case CHANGE:
            if(currentValue == null) {
                /*
                 * the forced event only cares about the postcondition - that
                 * there is the given value set, not about that there was no
                 * value before
                 */
                if(!command.isForced()) {
                    /*
                     * given old value does not concur with the current value
                     */
                    return EventResult.failed();
                }
            }
            break;
        default:
            throw new IllegalArgumentException("Unknown field command type: " + command);
        }
        
        if(XI.equals(currentValue, newValue)) {
            return EventResult.noChange();
        }
        
        /* Changes happen, create event */
        XFieldEvent event = null;
        XAddress fieldAddress = command.getChangedEntity();
        switch(command.getChangeType()) {
        case ADD:
            if(currentValue == null) {
                event = MemoryFieldEvent.createAddEvent(actorId, fieldAddress, newValue,
                        currentModelRev, currentObjectRev, currentFieldRev, inTransaction);
            } else {
                assert command.isForced();
                event = MemoryFieldEvent.createChangeEvent(actorId, fieldAddress, newValue,
                        currentModelRev, currentObjectRev, currentFieldRev, inTransaction);
            }
            break;
        case REMOVE:
            event = MemoryFieldEvent.createRemoveEvent(actorId, fieldAddress, currentModelRev,
                    currentObjectRev, currentFieldRev, false, inTransaction);
            break;
        case CHANGE:
            event = MemoryFieldEvent.createChangeEvent(actorId, fieldAddress, newValue,
                    currentModelRev, currentObjectRev, currentFieldRev, inTransaction);
            break;
        default:
            assert false;
        }
        assert event != null;
        
        return EventResult.success(event);
    }
    
    /**
     * @param actorId
     * @param command
     * @param currentModelRev
     * @param currentObjectRev
     * @param currentFieldState @CanBeNull
     * @param changeEventListener
     * @param inTransaction
     * @return
     */
    private static EventResult createEventFromObjectCommand(XId actorId, XObjectCommand command,
            long currentModelRev, long currentObjectRev,
            @CanBeNull XReadableField currentFieldState, XRMOFChangeListener changeEventListener,
            boolean inTransaction) {
        
        boolean fieldExists = currentFieldState != null;
        
        /* Assertions and failure cases */
        XAddress objectAddress = command.getTarget();
        XId fieldId = command.getFieldId();
        switch(command.getChangeType()) {
        case ADD:
            if(fieldExists) {
                // ID already taken
                if(command.isForced()) {
                    /*
                     * the forced event only cares about the postcondition -
                     * that there is a field with the given ID, not about that
                     * there was no such field before
                     */
                    return EventResult.noChange();
                }
                return EventResult.failed();
            }
            break;
        case REMOVE:
            if(!fieldExists) {
                // ID not taken
                if(command.isForced()) {
                    /*
                     * the forced event only cares about the postcondition -
                     * that there is no field with the given ID, not about that
                     * there was such a field before
                     */
                    return EventResult.noChange();
                }
                return EventResult.failed();
            } else {
                if(!command.isForced() && currentObjectRev != command.getRevisionNumber()) {
                    return EventResult.failed();
                }
            }
            break;
        default:
            throw new IllegalArgumentException("Unknown object command type: " + command);
        }
        
        // create event
        XEvent event = null;
        switch(command.getChangeType()) {
        case ADD:
            assert currentFieldState == null;
            event = MemoryObjectEvent.createAddEvent(actorId, objectAddress, fieldId,
                    currentModelRev, currentObjectRev, inTransaction);
            break;
        case REMOVE:
            assert currentFieldState != null;
            long currentFieldRev = currentFieldState.getRevisionNumber();
            // recursive removes?
            XValue currentValue = currentFieldState.getValue();
            if(currentValue != null) {
                // create txn event
                List<XAtomicEvent> events = new LinkedList<XAtomicEvent>();
                events.add(MemoryFieldEvent.createRemoveEvent(actorId,
                        XX.resolveField(objectAddress, fieldId), currentModelRev, currentObjectRev,
                        currentFieldRev, true, true));
                events.add(MemoryObjectEvent.createRemoveEvent(actorId, objectAddress, fieldId,
                        currentModelRev, currentObjectRev, currentFieldRev, true, inTransaction));
                event = MemoryTransactionEvent.createTransactionEvent(actorId, objectAddress,
                        events, currentModelRev, currentObjectRev);
            } else {
                event = MemoryObjectEvent.createRemoveEvent(actorId, objectAddress, fieldId,
                        currentModelRev, currentObjectRev, currentFieldRev, false, inTransaction);
            }
            break;
        default:
            assert false;
        }
        assert event != null;
        return EventResult.success(event);
    }
    
    /**
     * @param actorId
     * @param command
     * @param modelState
     * @param objectState
     * @param root
     * @param changeEventListener
     * @return command result
     */
    public static long executeObjectCommand(XId actorId, XObjectCommand command,
            XRevWritableModel modelState, XRevWritableObject objectState, Root root,
            XRMOFChangeListener changeEventListener) {
        // pre-checks
        XyAssert.xyAssert(!root.isTransactionInProgess());
        if(!objectState.getAddress().equals(command.getTarget())) {
            log.warn("Event address '" + command.getTarget() + "' does not match '"
                    + objectState.getAddress() + "'");
            return XCommand.FAILED;
        }
        
        // create event
        long currentModelRev = modelState == null ? XEvent.RevisionOfEntityNotSet : modelState
                .getRevisionNumber();
        long currentObjectRev = objectState.getRevisionNumber();
        XRevWritableField currentFieldState = objectState.getField(command.getFieldId());
        EventResult eventResult = createEventFromObjectCommand(actorId, command, currentModelRev,
                currentObjectRev, currentFieldState, changeEventListener,
                root.isTransactionInProgess());
        
        if(!eventResult.changedSomething())
            return eventResult.getResult();
        
        // apply event
        XEvent event = eventResult.getEvent();
        applyObjectEventOrTransaction(event, modelState, objectState);
        root.getSyncLog().appendSyncLogEntry(command, event);
        fireEvents(root, changeEventListener, event);
        
        return event.getRevisionNumber();
    }
    
    /**
     * Fires the transaction events correctly: fires first atomic events, then
     * the transaction event itself
     */
    private static void fireEvents(Root root, XRMOFChangeListener changeEventListener, XEvent event) {
        if(event instanceof XTransactionEvent) {
            XTransactionEvent txnEvent = (XTransactionEvent)event;
            for(int i = 0; i < txnEvent.size(); i++) {
                XAtomicEvent atomicEvent = txnEvent.getEvent(i);
                fireAtomicEvent(root, changeEventListener, atomicEvent);
            }
            /*
             * fire txn event only to event listeners, changeListener needs to
             * get only the individual changes to adjust the loaded... stuff
             * correctly
             */
            root.fireTransactionEvent(txnEvent.getTarget(), txnEvent);
        } else {
            XAtomicEvent atomicEvent = (XAtomicEvent)event;
            fireAtomicEvent(root, changeEventListener, atomicEvent);
        }
    }
    
    private static void fireAtomicEvent(Root root, XRMOFChangeListener changeEventListener,
            XAtomicEvent atomicEvent) {
        if(atomicEvent instanceof XFieldEvent) {
            if(changeEventListener != null)
                changeEventListener.onChangeEvent((XFieldEvent)atomicEvent);
            root.fireFieldEvent(atomicEvent.getTarget(), (XFieldEvent)atomicEvent);
        } else if(atomicEvent instanceof XObjectEvent) {
            if(changeEventListener != null)
                changeEventListener.onChangeEvent((XObjectEvent)atomicEvent);
            root.fireObjectEvent(atomicEvent.getTarget(), (XObjectEvent)atomicEvent);
        } else if(atomicEvent instanceof XModelEvent) {
            if(changeEventListener != null)
                changeEventListener.onChangeEvent((XModelEvent)atomicEvent);
            root.fireModelEvent(atomicEvent.getTarget(), (XModelEvent)atomicEvent);
        } else if(atomicEvent instanceof XRepositoryEvent) {
            if(changeEventListener != null)
                changeEventListener.onChangeEvent((XRepositoryEvent)atomicEvent);
            root.fireRepositoryEvent(atomicEvent.getTarget(), (XRepositoryEvent)atomicEvent);
        }
    }
    
    private static final Logger log = LoggerFactory.getLogger(Executor.class);
    
    /**
     * @param actorId
     * @param command
     * @param modelState
     * @param root
     * @param changeEventListener
     * @return command result
     */
    @ModificationOperation
    public static long executeModelCommand(XId actorId, XModelCommand command,
            XRevWritableModel modelState, Root root, XRMOFChangeListener changeEventListener) {
        // pre-checks
        XAddress modelAddress = modelState.getAddress();
        if(!modelAddress.equals(command.getTarget())) {
            log.warn("Command target (" + command.getTarget() + ")  does not fit this model "
                    + modelAddress);
            return XCommand.FAILED;
        }
        XyAssert.xyAssert(!root.isTransactionInProgess());
        
        // create event
        long currentModelRev = modelState.getRevisionNumber();
        XRevWritableObject currentObjectState = modelState.getObject(command.getObjectId());
        EventResult eventResult = createEventFromModelCommand(actorId, command, currentModelRev,
                currentObjectState, changeEventListener, root.isTransactionInProgess());
        
        if(!eventResult.changedSomething())
            return eventResult.getResult();
        
        // apply event
        XEvent event = eventResult.getEvent();
        assert event != null;
        applyModelEvent((XModelEvent)event, modelState);
        root.getSyncLog().appendSyncLogEntry(command, event);
        fireEvents(root, changeEventListener, event);
        
        return event.getRevisionNumber();
    }
    
    /**
     * @param actorId
     * @param command
     * @param currentModelRev
     * @param currentObjectState @CanBeNull
     * @param changeEventListener
     * @param inTransaction
     * @return
     */
    private static EventResult createEventFromModelCommand(XId actorId, XModelCommand command,
            long currentModelRev, XReadableObject currentObjectState,
            XRMOFChangeListener changeEventListener, boolean inTransaction) {
        /* Check if failure */
        boolean objectExists = currentObjectState != null;
        switch(command.getChangeType()) {
        case ADD: {
            if(objectExists) {
                // ID already taken
                if(command.isForced()) {
                    /*
                     * the forced event only cares about the postcondition -
                     * that there is an object with the given ID, not about that
                     * there was no such object before
                     */
                    return EventResult.noChange();
                } else {
                    return EventResult.failed();
                }
            }
        }
            break;
        case REMOVE: {
            
            // TODO create txn event if the object had fields ...
            
            if(!objectExists) {
                // ID not taken
                if(command.isForced()) {
                    /*
                     * the forced event only cares about the postcondition -
                     * that there is no object with the given ID, not about that
                     * there was such an object before
                     */
                    return EventResult.noChange();
                } else {
                    return EventResult.failed();
                }
            }
            assert currentObjectState != null;
            if(!command.isForced()
                    && currentObjectState.getRevisionNumber() != command.getRevisionNumber()) {
                return EventResult.failed();
            }
        }
            break;
        default:
            throw new IllegalArgumentException("Unknown model command type: " + command);
        }
        
        // create event
        XModelEvent event = null;
        XAddress modelAddress = command.getTarget();
        XId objectId = command.getObjectId();
        switch(command.getChangeType()) {
        case ADD:
            assert currentObjectState == null;
            event = MemoryModelEvent.createAddEvent(actorId, modelAddress, objectId,
                    currentModelRev, inTransaction);
            break;
        case REMOVE:
            assert currentObjectState != null;
            event = MemoryModelEvent.createRemoveEvent(actorId, modelAddress, objectId,
                    currentModelRev, currentObjectState.getRevisionNumber(), inTransaction, false);
            break;
        default:
            assert false;
        }
        
        return EventResult.success(event);
    }
    
    private static void applyModelEvent(XModelEvent event, XWritableModel modelState) {
        if(modelState instanceof XRevWritableModel) {
            ((XRevWritableModel)modelState).setRevisionNumber(event.getRevisionNumber());
        }
        
        switch(event.getChangeType()) {
        case ADD:
            assert !modelState.hasObject(event.getObjectId());
            XWritableObject objectState = modelState.createObject(event.getObjectId());
            if(objectState instanceof XRevWritableObject) {
                ((XRevWritableObject)objectState).setRevisionNumber(event.getRevisionNumber());
            }
            break;
        case REMOVE:
            assert modelState.hasObject(event.getObjectId());
            modelState.removeObject(event.getObjectId());
            break;
        default:
            assert false;
        }
    }
    
    private static void applyObjectEvent(XObjectEvent event, XWritableModel modelState,
            XWritableObject objectState) {
        if(modelState instanceof XRevWritableModel) {
            ((XRevWritableModel)modelState).setRevisionNumber(event.getRevisionNumber());
        }
        if(objectState instanceof XRevWritableObject) {
            ((XRevWritableObject)objectState).setRevisionNumber(event.getRevisionNumber());
        }
        
        XId fieldId = event.getFieldId();
        switch(event.getChangeType()) {
        case ADD:
            assert !objectState.hasField(fieldId);
            XWritableField fieldState = objectState.createField(fieldId);
            if(fieldState instanceof XRevWritableField) {
                ((XRevWritableField)fieldState).setRevisionNumber(event.getRevisionNumber());
            }
            break;
        case REMOVE:
            assert objectState.hasField(fieldId);
            objectState.removeField(fieldId);
            break;
        default:
            assert false;
        }
    }
    
    private static void applyObjectEventOrTransaction(XEvent event, XRevWritableModel modelState,
            XRevWritableObject objectState) {
        if(event instanceof XObjectEvent) {
            applyObjectEvent((XObjectEvent)event, modelState, objectState);
        } else {
            assert event instanceof XTransactionEvent;
            applyObjectTransaction((XTransactionEvent)event, modelState, objectState);
        }
    }
    
    @SuppressWarnings("unused")
    private static void applyModelEventOrTransaction(XEvent event,
            XExistsRevWritableRepository repositoryState, XExistsRevWritableModel modelState) {
        if(event instanceof XModelEvent) {
            applyModelEvent((XModelEvent)event, modelState);
        } else {
            assert event instanceof XTransactionEvent;
            applyModelTransaction((XTransactionEvent)event, repositoryState, modelState);
        }
    }
    
    /**
     * @param txn
     * @param repositoryState @CanBeNull
     * @param modelState
     */
    private static void applyModelTransaction(XTransactionEvent txn,
            XExistsRevWritableRepository repositoryState, XExistsRevWritableModel modelState) {
        for(int i = 0; i < txn.size(); i++) {
            XAtomicEvent atomicEvent = txn.getEvent(i);
            switch(atomicEvent.getTarget().getAddressedType()) {
            case XREPOSITORY: {
                applyRepositoryEvent((XRepositoryEvent)atomicEvent, repositoryState);
            }
                break;
            case XMODEL: {
                applyModelEvent((XModelEvent)atomicEvent, modelState);
            }
                break;
            case XOBJECT: {
                XRevWritableObject objectState = modelState.getObject(atomicEvent
                        .getChangedEntity().getObject());
                applyObjectEvent((XObjectEvent)atomicEvent, modelState, objectState);
            }
                break;
            case XFIELD: {
                XRevWritableObject objectState = modelState.getObject(atomicEvent
                        .getChangedEntity().getObject());
                XRevWritableField fieldState = objectState.getField(atomicEvent.getChangedEntity()
                        .getField());
                assert fieldState != null;
                applyFieldEvent((XFieldEvent)atomicEvent, modelState, objectState, fieldState);
            }
                break;
            default:
                throw new AssertionError(
                        "ModelTransaction can only contain model/object/field events, found:"
                                + atomicEvent);
            }
        }
    }
    
    private static void applyRepositoryEvent(XRepositoryEvent repositoryEvent,
            XExistsRevWritableRepository repositoryState, XExistsRevWritableModel currentModelState) {
        applyRepositoryEvent(repositoryEvent, repositoryState);
        if(currentModelState != null) {
            switch(repositoryEvent.getChangeType()) {
            case ADD:
                currentModelState.setExists(true);
                break;
            case REMOVE:
                currentModelState.setExists(false);
                break;
            default:
                throw new AssertionError();
            }
            currentModelState.setRevisionNumber(repositoryEvent.getRevisionNumber());
        }
    }
    
    /**
     * @param atomicEvent
     * @param repositoryState @CanBeNull
     */
    private static void applyRepositoryEvent(XRepositoryEvent repositoryEvent,
            XExistsRevWritableRepository repositoryState) {
        if(repositoryState == null)
            return;
        
        switch(repositoryEvent.getChangeType()) {
        case ADD:
            XExistsRevWritableModel modelState = repositoryState.createModel(repositoryEvent
                    .getChangedEntity().getModel());
            modelState.setRevisionNumber(repositoryEvent.getRevisionNumber());
            modelState.setExists(true);
            break;
        case REMOVE:
            repositoryState.removeModel(repositoryEvent.getChangedEntity().getModel());
            break;
        default:
            throw new AssertionError();
        }
    }
    
    private static void applyObjectTransaction(XTransactionEvent txn, XRevWritableModel modelState,
            XRevWritableObject objectState) {
        for(int i = 0; i < txn.size(); i++) {
            XAtomicEvent atomicEvent = txn.getEvent(i);
            switch(atomicEvent.getTarget().getAddressedType()) {
            case XOBJECT:
                applyObjectEvent((XObjectEvent)atomicEvent, modelState, objectState);
                break;
            case XFIELD:
                XRevWritableField fieldState = objectState.getField(atomicEvent.getChangedEntity()
                        .getField());
                assert fieldState != null;
                applyFieldEvent((XFieldEvent)atomicEvent, modelState, objectState, fieldState);
                break;
            default:
                throw new AssertionError(
                        "ObjectTransaction can only contain object/field events, found:"
                                + atomicEvent);
            }
        }
    }
    
    /**
     * @param event
     * @param modelState
     * @param objectState
     * @param fieldState
     */
    private static void applyFieldEvent(XFieldEvent event, XWritableModel modelState,
            XWritableObject objectState, XWritableField fieldState) {
        assert event != null;
        assert fieldState != null;
        if(modelState != null && modelState instanceof XRevWritableModel) {
            ((XRevWritableModel)modelState).setRevisionNumber(event.getRevisionNumber());
        }
        if(objectState != null && objectState instanceof XRevWritableObject) {
            ((XRevWritableObject)objectState).setRevisionNumber(event.getRevisionNumber());
        }
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
    
    public static long executeRepositoryCommand(XId actorId, XRepositoryCommand command,
            XExistsRevWritableRepository repositoryState, Root root,
            XRMOFChangeListener changeEventListener) {
        XExistsRevWritableModel modelState = repositoryState.getModel(command.getModelId());
        return executeRepositoryCommand_complex(actorId, command, repositoryState, modelState,
                root, changeEventListener);
    }
    
    /*
     * Some cases that can happen:
     * 
     * model has father, model exists =
     * 
     * model has father, model does not exist
     * 
     * 
     * model has no father, model exists
     * 
     * model has no father, model does not exist
     */
    
    /**
     * @param actorId
     * @param command
     * @param repositoryState @CanBeNull
     * @param modelState @CanBeNull
     * @param root
     * @param changeEventListener @CanBeNull
     * @return command result
     */
    @ModificationOperation
    public static long executeRepositoryCommand_complex(XId actorId, XRepositoryCommand command,
            XExistsRevWritableRepository repositoryState, XExistsRevWritableModel modelState,
            Root root, XRMOFChangeListener changeEventListener) {
        assert command != null;
        XId modelId = command.getModelId();
        if(repositoryState != null)
            assert repositoryState.getModel(modelId) == modelState;
        
        // pre-checks
        XAddress repositoryAddress = command.getTarget();
        if(repositoryState != null) {
            if(!repositoryState.getAddress().equals(repositoryAddress)) {
                log.warn("Command target (" + command.getTarget()
                        + ")  does not fit this repository " + repositoryAddress);
                return XCommand.FAILED;
            }
        }
        
        // create event
        XyAssert.xyAssert(!root.isTransactionInProgess());
        XExistsRevWritableModel currentModelState;
        if(repositoryState != null) {
            currentModelState = repositoryState.getModel(modelId);
            assert modelState == currentModelState;
        } else {
            assert modelState != null;
            currentModelState = modelState;
        }
        // currentModelState can still be null if repository didn't have it
        if(currentModelState == null) {
            currentModelState = new SimpleModel(command.getChangedEntity());
            currentModelState.setExists(false);
        }
        
        EventResult eventResult = createEventFromRepositoryCommand(actorId, command,
                currentModelState, root, changeEventListener, root.isTransactionInProgess());
        
        if(!eventResult.changedSomething())
            return eventResult.getResult();
        
        // apply event
        XEvent event = eventResult.getEvent();
        root.getSyncLog().appendSyncLogEntry(command, event);
        if(event instanceof XTransactionEvent) {
            applyModelTransaction((XTransactionEvent)event, repositoryState, currentModelState);
        } else {
            assert event instanceof XRepositoryEvent;
            applyRepositoryEvent((XRepositoryEvent)event, repositoryState, currentModelState);
        }
        fireEvents(root, changeEventListener, event);
        
        assert event.getRevisionNumber() >= 0;
        assert currentModelState.getRevisionNumber() >= 0;
        
        return event.getRevisionNumber();
    }
    
    /**
     * @param actorId
     * @param command
     * @param currentModelState @NeverNull
     * @param root @CanBeNull
     * @param changeEventListener @CanBeNull
     * @param inTransaction
     * @return
     */
    private static EventResult createEventFromRepositoryCommand(XId actorId,
            XRepositoryCommand command, XExistsReadableModel currentModelState, Root root,
            XRMOFChangeListener changeEventListener, boolean inTransaction) {
        XId modelId = command.getModelId();
        assert currentModelState != null;
        long currentModelRev = currentModelState.getRevisionNumber();
        
        switch(command.getChangeType()) {
        case ADD: {
            if(currentModelState.exists()) {
                // model exists already with same id
                if(command.isForced()) {
                    /*
                     * the forced event only cares about the postcondition -
                     * that there is a model with the given ID, not about that
                     * there was no such model before
                     */
                    return EventResult.noChange();
                } else {
                    return EventResult.failed();
                }
            }
            break;
        }
        case REMOVE: {
            // TODO create txn event if the model had objects ...
            
            if(!currentModelState.exists()) {
                if(command.isForced()) {
                    /*
                     * the forced event only cares about the postcondition -
                     * that there is no model with the given ID, not about that
                     * there was such a model before
                     */
                    return EventResult.noChange();
                } else {
                    return EventResult.failed();
                }
            }
            // model is present, command is removeModel
            
            // check safe conditions
            if(!command.isForced()
                    && currentModelState.getRevisionNumber() != command.getRevisionNumber()) {
                return EventResult.failed();
            }
            
        }
            break;
        default:
            throw new IllegalArgumentException("unknown command type: " + command);
        }
        
        // event creation
        XRepositoryEvent event = null;
        XAddress repositoryAddress = command.getTarget();
        switch(command.getChangeType()) {
        case ADD:
            assert !currentModelState.exists();
            event = MemoryRepositoryEvent.createAddEvent(actorId, repositoryAddress, modelId,
                    currentModelRev, inTransaction);
            break;
        case REMOVE:
            assert currentModelState.exists();
            event = MemoryRepositoryEvent.createRemoveEvent(actorId, repositoryAddress, modelId,
                    currentModelRev, inTransaction);
            break;
        default:
            assert false;
        }
        assert event != null;
        return EventResult.success(event);
    }
    
    /**
     * @param actorId
     * @param txn
     * @param repositoryState @CanBeNull
     * @param modelState @NeverNull
     * @param root
     * @param changeEventListener
     * @return ...
     */
    public static long executeModelTransaction(XId actorId, XTransaction txn,
            XExistsRevWritableRepository repositoryState, XExistsRevWritableModel modelState,
            Root root, XRMOFChangeListener changeEventListener) {
        assert modelState != null;
        synchronized(root) {
            root.startExecutingTransaction();
            EventResult eventResult = createEventFromModelTransaction(actorId, txn,
                    repositoryState, modelState);
            if(!eventResult.changedSomething()) {
                root.stopExecutingTransaction();
                return eventResult.getResult();
            }
            
            // apply event
            XEvent event = eventResult.getEvent();
            assert event != null;
            applyModelTransaction((XTransactionEvent)event, repositoryState, modelState);
            root.getSyncLog().appendSyncLogEntry(txn, event);
            fireEvents(root, changeEventListener, event);
            
            root.stopExecutingTransaction();
            return event.getRevisionNumber();
        }
    }
    
    /**
     * @param actorId
     * @param txn
     * @param repositoryState
     * @param currentModelState @NeverNull
     * @return
     */
    private static EventResult createEventFromModelTransaction(XId actorId, XTransaction txn,
            XReadableRepository repositoryState, XReadableModel currentModelState) {
        // make sure changes are not visible to outside yet
        assert currentModelState != null;
        
        ChangedModel tempModelState = new ChangedModel(currentModelState);
        List<XAtomicEvent> changingEvents = new LinkedList<XAtomicEvent>();
        
        for(int i = 0; i < txn.size(); i++) {
            XAtomicCommand atomicCommand = txn.getCommand(i);
            EventResult eventResult;
            switch(atomicCommand.getTarget().getAddressedType()) {
            /*
             * We always read from the currentXXX state to check the command
             * mode (force,safe) and then we apply the event to the tempXXX
             * state
             */
            case XREPOSITORY: {
                XRepositoryCommand repoCommand = (XRepositoryCommand)atomicCommand;
                eventResult = createEventFromRepositoryCommand(actorId, repoCommand,
                        tempModelState, null, null, true);
            }
                break;
            case XMODEL: {
                XModelCommand modelCommand = (XModelCommand)atomicCommand;
                long currentModelRev = currentModelState.getRevisionNumber();
                XWritableObject tempObjectState = tempModelState.getObject(modelCommand
                        .getObjectId());
                eventResult = createEventFromModelCommand(actorId, modelCommand, currentModelRev,
                        tempObjectState, null, true);
                if(eventResult.changedSomething()) {
                    applyModelEvent((XModelEvent)eventResult.getEvent(), tempModelState);
                }
                // FIXME if result is txn, apply the individual events instead
            }
                break;
            case XOBJECT: {
                XObjectCommand objectCommand = (XObjectCommand)atomicCommand;
                long currentModelRev = currentModelState.getRevisionNumber();
                XId objectId = objectCommand.getObjectId();
                XWritableObject tempObjectState = tempModelState.getObject(objectId);
                if(tempObjectState == null) {
                    eventResult = EventResult.failed();
                } else {
                    XReadableObject currentObjectState = currentModelState.getObject(objectId);
                    long currentObjectRev = currentObjectState == null ? currentModelRev
                            : currentObjectState.getRevisionNumber();
                    XWritableField tempFieldState = tempObjectState.getField(objectId);
                    eventResult = createEventFromObjectCommand(actorId, objectCommand,
                            currentModelRev, currentObjectRev, tempFieldState, null, true);
                    if(eventResult.changedSomething()) {
                        applyObjectEvent((XObjectEvent)eventResult.getEvent(), tempModelState,
                                tempObjectState);
                    }
                }
            }
                break;
            case XFIELD: {
                XFieldCommand fieldCommand = (XFieldCommand)atomicCommand;
                long currentModelRev = currentModelState.getRevisionNumber();
                XId objectId = fieldCommand.getObjectId();
                XWritableObject tempObjectState = tempModelState.getObject(objectId);
                if(tempObjectState == null) {
                    eventResult = EventResult.failed();
                } else {
                    XReadableObject currentObjectState = currentModelState.getObject(objectId);
                    long currentObjectRev = currentObjectState == null ? currentModelRev
                            : currentObjectState.getRevisionNumber();
                    XId fieldId = fieldCommand.getFieldId();
                    XWritableField tempFieldState = tempObjectState.getField(fieldId);
                    if(tempFieldState == null) {
                        eventResult = EventResult.failed();
                    } else {
                        XReadableField currentFieldState = currentObjectState == null ? null
                                : currentObjectState.getField(fieldId);
                        long currentFieldRev = currentFieldState == null ? currentObjectRev
                                : currentFieldState.getRevisionNumber();
                        XValue tempValue = tempFieldState == null ? null : tempFieldState
                                .getValue();
                        eventResult = createEventFromFieldCommand(actorId, fieldCommand,
                                currentModelRev, currentObjectRev, currentFieldRev, tempValue,
                                null, true);
                        if(eventResult.changedSomething()) {
                            applyFieldEvent((XFieldEvent)eventResult.getEvent(), tempModelState,
                                    tempObjectState, tempFieldState);
                        }
                    }
                }
            }
                break;
            default:
                throw new AssertionError();
            }
            // process atomic event
            if(eventResult.isFailed()) {
                // txn failed
                return EventResult.failed();
            } else if(eventResult.changedSomething()) {
                XEvent event = eventResult.getEvent();
                if(event instanceof XTransactionEvent) {
                    // add parts from remove-txn
                    XTransactionEvent subTxn = (XTransactionEvent)event;
                    for(int j = 0; j < subTxn.size(); j++) {
                        XAtomicEvent subAtomicEvent = subTxn.getEvent(j);
                        changingEvents.add(subAtomicEvent);
                    }
                } else {
                    changingEvents.add((XAtomicEvent)event);
                }
                
            }
        }
        if(changingEvents.isEmpty()) {
            // txn did not fail, but did not change anything either
            return EventResult.noChange();
        }
        
        // create txn event
        XTransactionEvent txnEvent = MemoryTransactionEvent.createTransactionEvent(actorId,
                txn.getTarget(), changingEvents, tempModelState.getRevisionNumber(),
                XEvent.RevisionNotAvailable);
        return EventResult.success(txnEvent);
    }
    
    // FIXME impl.
    public static long executeObjectTransaction(XId actorId, XTransaction txn,
            XRevWritableObject objectState, Root root, XRMOFChangeListener changeEventListener) {
        
        synchronized(root) {
            root.startExecutingTransaction();
            root.stopExecutingTransaction();
        }
        
        // TODO Auto-generated method stub
        return 0;
    }
    
}
