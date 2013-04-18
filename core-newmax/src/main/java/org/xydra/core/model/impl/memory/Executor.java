package org.xydra.core.model.impl.memory;

import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XEvent;
import org.xydra.base.change.XFieldCommand;
import org.xydra.base.change.XFieldEvent;
import org.xydra.base.change.XObjectCommand;
import org.xydra.base.change.XObjectEvent;
import org.xydra.base.change.impl.memory.MemoryFieldEvent;
import org.xydra.base.change.impl.memory.MemoryObjectEvent;
import org.xydra.base.rmof.XRevWritableField;
import org.xydra.base.rmof.XRevWritableModel;
import org.xydra.base.rmof.XRevWritableObject;
import org.xydra.base.value.XValue;
import org.xydra.core.change.XRMOFChangeListener;
import org.xydra.index.XI;
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
        root.fireFieldEvent(fieldState, event);
        
        return newFieldRev;
    }
    
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
        XObjectEvent event = null;
        switch(command.getChangeType()) {
        case ADD:
            event = MemoryObjectEvent.createAddEvent(actorId, objectAddress, fieldId,
                    currentModelRev, currentObjectRev, false);
            break;
        case REMOVE:
            assert fieldState != null;
            event = MemoryObjectEvent
                    .createRemoveEvent(actorId, objectAddress, fieldId, currentModelRev,
                            currentObjectRev, fieldState.getRevisionNumber(), false, false);
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
        root.fireObjectEvent(objectState, event);
        
        return newObjectRev;
    }
}
