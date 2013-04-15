package org.xydra.core.model.impl.memory;

import java.util.List;

import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.change.ChangeType;
import org.xydra.base.change.XAtomicEvent;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XFieldCommand;
import org.xydra.base.rmof.XRevWritableModel;
import org.xydra.core.model.XLocalChangeCallback;
import org.xydra.core.model.delta.ChangedModel;
import org.xydra.core.model.delta.DeltaUtils;
import org.xydra.index.XI;
import org.xydra.index.query.Pair;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.sharedutils.XyAssert;


// TODO look in backend for similar stuff
/**
 * 
 * TODO move execution out of MemoryXXX
 * 
 * @author xamde
 * 
 */
public class CommandExecutor {
    
    private static final Logger log = LoggerFactory.getLogger(CommandExecutor.class);
    
    /**
     * Check the preconditions required to execute the given command
     * 
     * @param modelAddress
     * 
     * 
     * @param command
     * @param actorId The actor to log in the created events.
     * @param modelState
     * @param eventListener @CanBeNull
     * @param localChangeCallback @CanBeNull
     * @return a copy of the created events or null if the command cannot be
     *         applied.
     */
    public static long checkPreconditionsChangeStateSendEvents(XAddress modelAddress,
            XCommand command, XId actorId, XRevWritableModel modelState,
            IGenericEventListener eventListener, XLocalChangeCallback localChangeCallback) {
        Pair<ChangedModel,DeltaUtils.ModelChange> c = DeltaUtils
                .executeCommand(modelState, command);
        if(c == null) {
            log.info("Failed preconditions");
            if(localChangeCallback != null)
                localChangeCallback.onFailure();
            return XCommand.FAILED;
        }
        
        long nextRev = modelState.getRevisionNumber() + 1;
        
        List<XAtomicEvent> events = DeltaUtils.createEvents(modelAddress, c, actorId, nextRev,
                command.getChangeType() == ChangeType.TRANSACTION);
        log.debug("[r" + nextRev + "] DeltaUtils generated " + events.size() + " events");
        if(events.size() > 1000) {
            log.warn("Created over 1000 events (" + events.size()
                    + ") GA?category=xydra&action=saveManyEvents&label=events&value="
                    + events.size());
            try {
                throw new RuntimeException("Over 1000 events");
            } catch(Exception e) {
                log.warn("Over 1000 events", e);
            }
        }
        XyAssert.xyAssert(events != null);
        if(events.isEmpty()) {
            log.debug("No change");
            if(localChangeCallback != null)
                localChangeCallback.onSuccess(XCommand.NOCHANGE);
            return XCommand.NOCHANGE;
        }
        
        // apply changes to state
        DeltaUtils.applyChanges(modelAddress, modelState, c, nextRev);
        // send events
        if(eventListener != null) {
            for(XAtomicEvent ae : events) {
                eventListener.onEvent(ae);
            }
        }
        
        if(localChangeCallback != null)
            localChangeCallback.onSuccess(nextRev);
        return nextRev;
    }
    
    /**
     * @throws IllegalStateException if this method is called after this
     *             MemoryField was already removed
     */
    private static void assertExists(IMemoryMOFEntity entity) throws IllegalStateException {
        if(!entity.exists()) {
            throw new IllegalStateException("this entity has been removed");
        }
    }
    
    /**
     * @param field
     * @param command
     * @param callback
     * @return ...
     * @throws IllegalStateException if this method is called after this
     *             MemoryField was already removed
     */
    public long executeFieldCommand(IMemoryField field, XFieldCommand command,
            XLocalChangeCallback callback) {
        synchronized(field.getRoot()) {
            assertExists(field);
            
            // FIXME MONKEY
            // XyAssert.xyAssert(!field.eventQueue.transactionInProgess);
            
            // check whether the given event actually refers to this field
            if(!field.getAddress().equals(command.getTarget())) {
                if(callback != null) {
                    callback.onFailure();
                }
                return XCommand.FAILED;
            }
            
            if(!command.isForced() && field.getRevisionNumber() != command.getRevisionNumber()) {
                if(callback != null) {
                    callback.onFailure();
                }
                return XCommand.FAILED;
            }
            
            long oldRev = field.getFatherRevisionNumber();
            
            if(command.getChangeType() == ChangeType.ADD) {
                if(field.getValue() != null) {
                    /*
                     * the forced event only cares about the postcondition -
                     * that there is the given value set, not about that there
                     * was no value before
                     */
                    if(!command.isForced()) {
                        // value already set
                        if(callback != null) {
                            callback.onFailure();
                        }
                        return XCommand.FAILED;
                    }
                }
                
            } else if(command.getChangeType() == ChangeType.REMOVE) {
                if(field.getValue() == null) {
                    /*
                     * the forced event only cares about the postcondition -
                     * that there is no value set, not about that there was a
                     * value before
                     */
                    if(!command.isForced()) {
                        // value is not set and can not be removed or the given
                        // value is not current anymore
                        if(callback != null) {
                            callback.onFailure();
                        }
                        return XCommand.FAILED;
                    }
                }
                
                XyAssert.xyAssert(command.getValue() == null);
                
            } else if(command.getChangeType() == ChangeType.CHANGE) {
                if(field.getValue() == null) {
                    /*
                     * the forced event only cares about the postcondition -
                     * that there is the given value set, not about that there
                     * was no value before
                     */
                    if(!command.isForced()) {
                        // given old value does not concur with the current
                        // value
                        if(callback != null) {
                            callback.onFailure();
                        }
                        return XCommand.FAILED;
                    }
                }
                
            } else {
                throw new IllegalArgumentException("Unknown field command type: " + command);
            }
            
            if(XI.equals(field.getValue(), command.getValue())) {
                if(callback != null) {
                    callback.onSuccess(XCommand.NOCHANGE);
                }
                return XCommand.NOCHANGE;
            }
            
            // FIXME BIG MONKEY field.eventQueue.newLocalChange(command,
            // callback);
            
            field.setValueInternal(command.getValue());
            
            return oldRev + 1;
        }
    }
    
}
