package org.xydra.core.model.impl.memory;

import java.util.LinkedList;
import java.util.List;

import org.xydra.annotations.ModificationOperation;
import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.change.XAtomicEvent;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XEvent;
import org.xydra.base.change.XFieldCommand;
import org.xydra.base.change.XFieldEvent;
import org.xydra.base.change.XModelCommand;
import org.xydra.base.change.XModelEvent;
import org.xydra.base.change.XObjectCommand;
import org.xydra.base.change.XObjectEvent;
import org.xydra.base.change.XRepositoryCommand;
import org.xydra.base.change.XRepositoryEvent;
import org.xydra.base.change.XTransactionEvent;
import org.xydra.base.change.impl.memory.MemoryFieldEvent;
import org.xydra.base.change.impl.memory.MemoryModelEvent;
import org.xydra.base.change.impl.memory.MemoryObjectEvent;
import org.xydra.base.change.impl.memory.MemoryRepositoryEvent;
import org.xydra.base.change.impl.memory.MemoryTransactionEvent;
import org.xydra.base.rmof.XRevWritableField;
import org.xydra.base.rmof.XRevWritableModel;
import org.xydra.base.rmof.XRevWritableObject;
import org.xydra.base.rmof.XRevWritableRepository;
import org.xydra.base.value.XValue;
import org.xydra.core.XX;
import org.xydra.core.change.XRMOFChangeListener;
import org.xydra.index.XI;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.sharedutils.XyAssert;


/**
 * Does not deal with synchronisation.
 * 
 * Does not deal with entity-does-not-exist.
 * 
 * @author xamde
 * 
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
        long oldFieldRev = fieldState.getRevisionNumber();
        if(!command.isForced() && command.getRevisionNumber() != oldFieldRev) {
            return XCommand.FAILED;
        }
        
        /* Standard execution */
        XValue currentValue = fieldState.getValue();
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
                    return XCommand.FAILED;
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
                    return XCommand.FAILED;
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
                    return XCommand.FAILED;
                }
            }
            break;
        default:
            throw new IllegalArgumentException("Unknown field command type: " + command);
        }
        
        if(XI.equals(currentValue, newValue)) {
            return XCommand.NOCHANGE;
        }
        
        /* Changes happen */
        // rev nrs
        long currentObjectRev = objectState == null ? XEvent.RevisionOfEntityNotSet : objectState
                .getRevisionNumber();
        long currentModelRev = modelState == null ? XEvent.RevisionOfEntityNotSet : modelState
                .getRevisionNumber();
        long newFieldRev = Math.max(currentObjectRev, currentModelRev) + 1;
        
        // state updates
        fieldState.setValue(newValue);
        fieldState.setRevisionNumber(newFieldRev);
        if(objectState != null) {
            objectState.setRevisionNumber(newFieldRev);
            if(modelState != null) {
                modelState.setRevisionNumber(newFieldRev);
            }
        }
        
        // event creation
        XFieldEvent event = null;
        XAddress fieldAddress = fieldState.getAddress();
        switch(command.getChangeType()) {
        case ADD:
            event = MemoryFieldEvent.createAddEvent(actorId, fieldAddress, newValue,
                    currentModelRev, currentObjectRev, oldFieldRev, false);
            break;
        case REMOVE:
            event = MemoryFieldEvent.createRemoveEvent(actorId, fieldAddress, currentModelRev,
                    currentObjectRev, oldFieldRev, false, false);
            break;
        case CHANGE:
            event = MemoryFieldEvent.createChangeEvent(actorId, fieldAddress, newValue,
                    currentModelRev, currentObjectRev, oldFieldRev, false);
            break;
        default:
            assert false;
        }
        assert event != null;
        // event logging
        // FIXME why log here?
        root.getWritableChangeLog().appendEvent(event);
        root.getLocalChanges().append(command, event);
        // event sending
        changeEventListener.onChangeEvent(event);
        // FIXME use XField, XModel etc objects to fire events
        // these events here will never arrive. ALTERNATIVE: let listener
        // re-fire the events
        root.fireFieldEvent(fieldState.getAddress(), event);
        
        return newFieldRev;
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
        /* Assertions and failure cases */
        XyAssert.xyAssert(!root.isTransactionInProgess());
        
        XAddress objectAddress = objectState.getAddress();
        XId fieldId = command.getFieldId();
        
        if(!objectAddress.equals(command.getTarget())) {
            return XCommand.FAILED;
        }
        
        /* Check failed commands */
        XRevWritableField fieldState = null;
        switch(command.getChangeType()) {
        case ADD:
            if(objectState.hasField(fieldId)) {
                // ID already taken
                if(command.isForced()) {
                    /*
                     * the forced event only cares about the postcondition -
                     * that there is a field with the given ID, not about that
                     * there was no such field before
                     */
                    return XCommand.NOCHANGE;
                }
                return XCommand.FAILED;
            }
            break;
        case REMOVE:
            fieldState = objectState.getField(fieldId);
            if(fieldState == null) {
                // ID not taken
                if(command.isForced()) {
                    /*
                     * the forced event only cares about the postcondition -
                     * that there is no field with the given ID, not about that
                     * there was such a field before
                     */
                    return XCommand.NOCHANGE;
                }
                return XCommand.FAILED;
            } else {
                if(!command.isForced()
                        && fieldState.getRevisionNumber() != command.getRevisionNumber()) {
                    return XCommand.FAILED;
                }
            }
            break;
        default:
            throw new IllegalArgumentException("Unknown object command type: " + command);
        }
        
        /* Change success */
        // rev nrs
        long currentObjectRev = objectState.getRevisionNumber();
        long currentModelRev = modelState == null ? XEvent.RevisionOfEntityNotSet : modelState
                .getRevisionNumber();
        long newObjectRev = Math.max(currentObjectRev, currentModelRev) + 1;
        
        // state updates
        objectState.setRevisionNumber(newObjectRev);
        if(modelState != null) {
            modelState.setRevisionNumber(newObjectRev);
        }
        
        // event creation
        XEvent event = null;
        
        switch(command.getChangeType()) {
        case ADD:
            event = MemoryObjectEvent.createAddEvent(actorId, objectAddress, fieldId,
                    currentModelRev, currentObjectRev, false);
            break;
        case REMOVE:
            assert fieldState != null;
            
            // recursive removes?
            XValue currentValue = fieldState.getValue();
            if(currentValue != null) {
                // create txn event
                List<XAtomicEvent> events = new LinkedList<XAtomicEvent>();
                events.add(MemoryFieldEvent.createRemoveEvent(actorId,
                        XX.resolveField(objectAddress, fieldId), currentModelRev, currentObjectRev,
                        fieldState.getRevisionNumber(), true, true));
                events.add(MemoryObjectEvent.createRemoveEvent(actorId, objectAddress, fieldId,
                        currentModelRev, currentObjectRev, fieldState.getRevisionNumber(), true,
                        true));
                event = MemoryTransactionEvent.createTransactionEvent(actorId, objectAddress,
                        events, currentModelRev, currentObjectRev);
            } else {
                event = MemoryObjectEvent.createRemoveEvent(actorId, objectAddress, fieldId,
                        currentModelRev, currentObjectRev, fieldState.getRevisionNumber(), false,
                        false);
            }
            break;
        default:
            assert false;
        }
        assert (event != null) : "exactly one must be true";
        
        // event logging
        // FIXME why log here?
        root.getWritableChangeLog().appendEvent(event);
        root.getLocalChanges().append(command, event);
        fireEvents(root, changeEventListener, event);
        
        return newObjectRev;
    }
    
    private static void fireEvents(Root root, XRMOFChangeListener changeEventListener, XEvent event) {
        if(event instanceof XTransactionEvent) {
            XTransactionEvent txnEvent = (XTransactionEvent)event;
            
            // event sending
            for(int i = 0; i < txnEvent.size(); i++) {
                XAtomicEvent ae = txnEvent.getEvent(i);
                
            }
            
        } else {
            XAtomicEvent atomicEvent = (XAtomicEvent)event;
            
            // event sending
            changeEventListener.onChangeEvent((XObjectEvent)atomicEvent);
            
            if(atomicEvent instanceof XFieldEvent) {
                root.fireFieldEvent(event.getTarget(), (XFieldEvent)event);
            } else if(atomicEvent instanceof XObjectEvent) {
                root.fireObjectEvent(event.getTarget(), (XObjectEvent)event);
            } else if(atomicEvent instanceof XModelEvent) {
                root.fireModelEvent(event.getTarget(), (XModelEvent)event);
            } else if(atomicEvent instanceof XRepositoryEvent) {
                root.fireRepositoryEvent(event.getTarget(), (XRepositoryEvent)event);
            }
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
        XyAssert.xyAssert(!root.isTransactionInProgess());
        XAddress modelAddress = modelState.getAddress();
        if(!modelAddress.equals(command.getTarget())) {
            log.warn("Command target (" + command.getTarget() + ")  does not fit this model "
                    + modelAddress);
            return XCommand.FAILED;
        }
        
        long currentModelRev = modelState.getRevisionNumber();
        XId objectId = command.getObjectId();
        XRevWritableObject currentObjectState = null;
        switch(command.getChangeType()) {
        case ADD: {
            if(modelState.hasObject(objectId)) {
                // ID already taken
                if(command.isForced()) {
                    /*
                     * the forced event only cares about the postcondition -
                     * that there is an object with the given ID, not about that
                     * there was no such object before
                     */
                    return XCommand.NOCHANGE;
                } else {
                    return XCommand.FAILED;
                }
            }
            // change state
            modelState.createObject(objectId);
        }
            break;
        case REMOVE: {
            currentObjectState = modelState.getObject(objectId);
            if(currentObjectState == null) {
                // ID not taken
                if(command.isForced()) {
                    /*
                     * the forced event only cares about the postcondition -
                     * that there is no object with the given ID, not about that
                     * there was such an object before
                     */
                    return XCommand.NOCHANGE;
                } else {
                    return XCommand.FAILED;
                }
            }
            if(!command.isForced()
                    && currentObjectState.getRevisionNumber() != command.getRevisionNumber()) {
                return XCommand.FAILED;
            }
            // change state
            modelState.removeObject(objectId);
        }
            break;
        default:
            throw new IllegalArgumentException("Unknown model command type: " + command);
        }
        
        /* Apply changes */
        // rev nrs
        long newModelRev = currentModelRev + 1;
        
        // state updates
        modelState.setRevisionNumber(newModelRev);
        
        // event creation
        XModelEvent event = null;
        switch(command.getChangeType()) {
        case ADD:
            event = MemoryModelEvent.createAddEvent(actorId, modelAddress, objectId,
                    currentModelRev, false);
            break;
        case REMOVE:
            assert currentObjectState != null;
            event = MemoryModelEvent.createRemoveEvent(actorId, modelAddress, objectId,
                    currentModelRev, currentObjectState.getRevisionNumber(), false, false);
            break;
        default:
            assert false;
        }
        assert event != null;
        
        // FIXME ... add or remove object really
        
        // event logging
        // FIXME why log here?
        root.getWritableChangeLog().appendEvent(event);
        root.getLocalChanges().append(command, event);
        // event sending
        changeEventListener.onChangeEvent(event);
        // FIXME use XField, XModel etc objects to fire events
        // these events here will never arrive. ALTERNATIVE: let listener
        // re-fire the events
        root.fireModelEvent(modelState, event);
        
        return newModelRev;
    }
    
    /**
     * @param actorId
     * @param command
     * @param repositoryState
     * @param root
     * @param changeEventListener @CanBeNull
     * @return command result
     */
    @ModificationOperation
    public static long executeRepositoryCommand(XId actorId, XRepositoryCommand command,
            XRevWritableRepository repositoryState, Root root,
            XRMOFChangeListener changeEventListener) {
        assert command != null;
        
        XyAssert.xyAssert(!root.isTransactionInProgess());
        XAddress repositoryAddress = repositoryState.getAddress();
        if(!repositoryAddress.equals(command.getChangedEntity())) {
            log.warn("Command target (" + command.getTarget() + ")  does not fit this repository "
                    + repositoryAddress);
            return XCommand.FAILED;
        }
        
        XId modelId = command.getModelId();
        XRevWritableModel modelState = null;
        long currentModelRev;
        
        switch(command.getChangeType()) {
        case ADD: {
            if(repositoryState.hasModel(modelId)) {
                // model exists already with same id
                if(command.isForced()) {
                    /*
                     * the forced event only cares about the postcondition -
                     * that there is a model with the given ID, not about that
                     * there was no such model before
                     */
                    return XCommand.NOCHANGE;
                } else {
                    return XCommand.FAILED;
                }
            }
            // change state
            repositoryState.createModel(modelId);
            // TODO or re-use some higher number via root?
            currentModelRev = 0;
        }
        case REMOVE: {
            modelState = repositoryState.getModel(modelId);
            if(modelState == null) {
                if(command.isForced()) {
                    /*
                     * the forced event only cares about the postcondition -
                     * that there is no model with the given ID, not about that
                     * there was such a model before
                     */
                    return XCommand.NOCHANGE;
                } else {
                    return XCommand.FAILED;
                }
            }
            // model is present, command is removeModel
            
            // check safe conditions
            if(!command.isForced() && modelState.getRevisionNumber() != command.getRevisionNumber()) {
                return XCommand.FAILED;
            }
            
            // change state
            repositoryState.removeModel(modelId);
            currentModelRev = modelState.getRevisionNumber();
        }
            break;
        default:
            throw new IllegalArgumentException("unknown command type: " + command);
        }
        
        /* Apply changes */
        // rev nrs
        long newModelRev = currentModelRev + 1;
        
        // state updates
        modelState.setRevisionNumber(newModelRev);
        
        // event creation
        XRepositoryEvent event = null;
        switch(command.getChangeType()) {
        case ADD:
            event = MemoryRepositoryEvent.createAddEvent(actorId, repositoryAddress, modelId,
                    currentModelRev, false);
            break;
        case REMOVE:
            assert modelState != null;
            event = MemoryRepositoryEvent.createRemoveEvent(actorId, repositoryAddress, modelId,
                    currentModelRev, false);
            break;
        default:
            assert false;
        }
        assert event != null;
        
        // FIXME ... add or remove object really
        
        // event logging
        // FIXME why log here?
        root.getWritableChangeLog().appendEvent(event);
        root.getLocalChanges().append(command, event);
        // event sending
        changeEventListener.onChangeEvent(event);
        // FIXME use XField, XModel etc objects to fire events
        // these events here will never arrive. ALTERNATIVE: let listener
        // re-fire the events
        root.fireRepositoryEvent(null, event);
        
        return newModelRev;
        
        // --------
        
        // execute!
        synchronized(this.root) {
            int since = this.syncState.eventQueue.getNextPosition();
            boolean inTrans = enqueueModelRemoveEvents(givenActorId);
            if(inTrans) {
                this.syncState.eventQueue.createTransactionEvent(givenActorId, this, null, since);
            }
            
            delete();
            
            this.syncState.eventQueue.newLocalChange(command, callback);
            this.syncState.eventQueue.sendEvents();
            this.syncState.eventQueue.setBlockSending(true);
            
            // change local rev
            this.state.setRevisionNumber(this.syncState.getChangeLog().getCurrentRevisionNumber());
            
            // change father
            if(this.father != null) {
                this.father.removeModelInternal(getId());
            }
            
            return getRevisionNumber();
        }
        
    }
    
}
