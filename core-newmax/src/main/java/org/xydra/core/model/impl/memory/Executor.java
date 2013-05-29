package org.xydra.core.model.impl.memory;

import java.util.LinkedList;
import java.util.List;

import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.XType;
import org.xydra.base.change.XAtomicEvent;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XEvent;
import org.xydra.base.change.XFieldCommand;
import org.xydra.base.change.XFieldEvent;
import org.xydra.base.change.XModelEvent;
import org.xydra.base.change.XObjectEvent;
import org.xydra.base.change.XRepositoryEvent;
import org.xydra.base.change.XTransactionEvent;
import org.xydra.base.change.impl.memory.MemoryTransactionEvent;
import org.xydra.base.rmof.XRevWritableField;
import org.xydra.base.rmof.XRevWritableModel;
import org.xydra.base.rmof.XRevWritableObject;
import org.xydra.base.rmof.XWritableRepository;
import org.xydra.base.rmof.impl.XExistsRevWritableModel;
import org.xydra.base.rmof.impl.XExistsRevWritableRepository;
import org.xydra.core.XX;
import org.xydra.core.change.EventUtils;
import org.xydra.core.change.RevisionConstants;
import org.xydra.core.change.XRMOFChangeListener;
import org.xydra.core.model.delta.ChangedField;
import org.xydra.core.model.delta.ChangedModel;
import org.xydra.core.model.delta.ChangedObject;
import org.xydra.core.model.delta.DeltaUtils;
import org.xydra.core.model.impl.memory.sync.Root;
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
 * 
 * 
 * === Transaction handling ===
 * 
 * execute XTransaction on model; exists? => delegate executing txn to object
 * 
 * 
 * execute XTransaction on object; exists?
 * 
 * 
 * === Model & Repo handling === Some cases that can happen:
 * 
 * model has father, model exists =
 * 
 * model has father, model does not exist
 * 
 * 
 * model has no father, model exists
 * 
 * model has no father, model does not exist
 * 
 * === Strategy for executing commands ===
 * 
 * Cases:
 * 
 * <pre>
 * Type              | Executed on         | Result is
 * ------------------+---------------------+---------------------------------
 *      FieldCommand | Field               | FieldEvent  or TransactionEvent
 *     ObjectCommand | Object              | ObjectEvent or TransactionEvent
 *      ModelCommand | Model               | ModelEvent  or TransactionEvent
 * RepositoryCommand | Repository or Model | ModelEvent  or TransactionEvent
 *       Transaction | Model or Object     |                TransactionEvent
 * </pre>
 * 
 * Strategy:
 * 
 * <pre>
 * For txn: Wrap in ChangedEntity to preserve revision numbers before txn
 * 
 * For each atomic command:
 * 
 *     Check some assertions
 *     
 *     try {
 *       ChangedMOF changedMOF = evaluateRMOFCommand(XAtomicCommand, revNrs before txn, stateWithinTxn)
 *       if(changedMOF==null) return NOCHANGE;
 *       // change happens, represented in ChangedMOF
 *       List(XAtomicEvent) events = ...
 *       createEvents(events, revNr before txn, changedMOF)
 *       applyEvents to state from before txn
 *       fireEvents
 *     } catch (ExecutionException) {
 *       return FAILED;
 *     }
 *     
 *     Command + State before txn -> 
 *         ChangedEntity (Success) or 
 *         Exception (Failed) or 
 *         null (NoChange) 
 * 
 * if changes:
 * ChangedEntity -> Events + Apply changes (have all the same nextRev)
 * </pre>
 * 
 * 
 * Command -evaluate-> ChangedField -create-> Event -apply->
 * 
 * @author xamde
 */
public class Executor {
    
    private static final Logger log = LoggerFactory.getLogger(Executor.class);
    
    /**
     * Execute a single field command, not part of a transaction.
     * 
     * Events are fired to listener and root in an intermixed fashion.
     * 
     * @param actorId @NeverNull
     * @param fieldCommand @NeverNull
     * @param modelState @CanBeNull
     * @param objectState @CanBeNull if modelState is also null
     * @param fieldState @NeverNull
     * @param changeEventListener usually the field implementation in order to
     *            update its internal state, @CanBeNull
     * @param root used to fire events @NeverNull
     * @return result of executing command
     */
    public static long executeCommandOnField(XId actorId, XFieldCommand fieldCommand,
    
    XRevWritableModel modelState, XRevWritableObject objectState, XRevWritableField fieldState,
    
    Root root, XRMOFChangeListener changeEventListener) {
        /* Assertions */
        assert !(objectState == null && modelState != null);
        XyAssert.xyAssert(!root.isTransactionInProgess());
        
        /* Command -run-> ChangedField -create-> Events -apply-> -fire-> */
        ChangedField fieldInTxn = new ChangedField(fieldState);
        boolean success = fieldInTxn.executeCommand(fieldCommand);
        if(!success) {
            log.warn("command " + fieldCommand + " failed");
            return XCommand.FAILED;
        }
        if(!fieldInTxn.isChanged()) {
            return XCommand.NOCHANGE;
        }
        
        // create event
        long currentModelRev = modelState == null ? XEvent.REVISION_OF_ENTITY_NOT_SET : modelState
                .getRevisionNumber();
        long currentObjectRev = objectState == null ? XEvent.REVISION_OF_ENTITY_NOT_SET
                : objectState.getRevisionNumber();
        
        List<XAtomicEvent> events = new LinkedList<XAtomicEvent>();
        DeltaUtils.createEventsForChangedField(events, currentModelRev, actorId, currentObjectRev,
                fieldInTxn, root.isTransactionInProgess());
        assert events.size() == 1;
        XEvent event = createSingleEvent(events, actorId, fieldCommand.getTarget(),
                currentModelRev, currentObjectRev);
        
        // apply event
        if(modelState != null) {
            EventUtils.applyEvent(modelState, event);
        } else {
            if(objectState != null) {
                EventUtils.applyEvent(objectState, event);
            } else {
                EventUtils.applyEvent(fieldState, event);
            }
        }
        
        root.getSyncLog().appendSyncLogEntry(fieldCommand, event);
        fireEvents(root, changeEventListener, event);
        
        return event.getRevisionNumber();
    }
    
    /**
     * Execute a field, object or model command or a transaction.
     * 
     * Events are fired to listener and root in an intermixed fashion.
     * 
     * @param actorId @NeverNull
     * @param command @NeverNull
     * @param repositoryState @CanBeNull
     * @param modelState @CanBeNull
     * @param objectState @NeverNull
     * @param root @NeverNull
     * @param changeEventListener @CanBenNull
     * @return resulting revision number or error code
     */
    public static long executeCommandOnObject(XId actorId, XCommand command,
    
    XWritableRepository repositoryState, XRevWritableModel modelState,
            XRevWritableObject objectState,
            
            Root root, XRMOFChangeListener changeEventListener) {
        /* Assertions */
        assert objectState != null;
        XyAssert.xyAssert(!root.isTransactionInProgess());
        
        /* Command -run-> ChangedField -create-> Events -apply-> -fire-> */
        ChangedObject objectInTxn = new ChangedObject(objectState);
        boolean success = objectInTxn.executeCommand(command);
        if(!success) {
            log.warn("command " + command + " failed");
            return XCommand.FAILED;
        }
        if(!objectInTxn.hasChanges()) {
            return XCommand.NOCHANGE;
        }
        
        // create event
        long currentModelRev = modelState == null ? XEvent.REVISION_OF_ENTITY_NOT_SET : modelState
                .getRevisionNumber();
        List<XAtomicEvent> events = new LinkedList<XAtomicEvent>();
        DeltaUtils.createEventsForChangedObject(events, actorId, objectInTxn,
                root.isTransactionInProgess(), currentModelRev);
        XEvent event = createSingleEvent(events, actorId, command.getTarget(), currentModelRev,
                objectState.getRevisionNumber());
        
        // apply event
        EventUtils.applyEvent(objectState, event);
        modelState.setRevisionNumber(event.getRevisionNumber());
        root.getSyncLog().appendSyncLogEntry(command, event);
        fireEvents(root, changeEventListener, event);
        
        return event.getRevisionNumber();
    }
    
    /**
     * Execute a field, object, model or repository command or a transaction.
     * 
     * Events are fired to listener and root in an intermixed fashion.
     * 
     * @param actorId @NeverNull
     * @param command @NeverNull
     * @param root @NeverNull
     * @param repositoryState @CanBeNull
     * @param modelState @NeverNull
     * @param changeEventListener @CanBeNull
     * @return resulting revision number or error code
     */
    public static long executeCommandOnModel(XId actorId, XCommand command,
    
    XExistsRevWritableRepository repositoryState, XExistsRevWritableModel modelState,
    
    Root root, XRMOFChangeListener changeEventListener) {
        /* Assertions */
        assert modelState != null;
        XyAssert.xyAssert(!root.isTransactionInProgess());
        
        /* Command -run-> ChangedField -create-> Events -apply-> -fire-> */
        ChangedModel modelInTxn = new ChangedModel(modelState);
        boolean success = modelInTxn.executeCommand(command);
        if(!success) {
            log.warn("command " + command + " failed");
            return XCommand.FAILED;
        }
        if(!modelInTxn.hasChanges()) {
            return XCommand.NOCHANGE;
        }
        
        // create event
        long currentModelRev = modelState.getRevisionNumber();
        List<XAtomicEvent> events = new LinkedList<XAtomicEvent>();
        DeltaUtils.createEventsForChangedModel(events, actorId, modelInTxn,
                root.isTransactionInProgess());
        long currentObjectRev;
        if(command.getTarget().getObject() != null) {
            // object txn
            XRevWritableObject objectState = modelState.getObject(command.getTarget().getObject());
            if(objectState == null) {
                throw new IllegalArgumentException("Cannot execute an objectCommand (" + command
                        + ") on a non-existing object");
            } else {
                currentObjectRev = objectState.getRevisionNumber();
            }
        } else {
            // model txn
            currentObjectRev = RevisionConstants.REVISION_OF_ENTITY_NOT_SET;
        }
        
        XEvent event = createSingleEvent(events, actorId, command.getTarget(), currentModelRev,
                currentObjectRev);
        
        // apply event
        EventUtils.applyEvent(modelState, event);
        root.getSyncLog().appendSyncLogEntry(command, event);
        fireEvents(root, changeEventListener, event);
        
        return event.getRevisionNumber();
    }
    
    /**
     * @param events @NeverNull
     * @param actorId @NeverNull
     * @param target @NeverNull
     * @param modelRevision
     * @param objectRevision
     * @return the single event within the events list or a
     *         {@link XTransactionEvent} containing all given events
     */
    private static XEvent createSingleEvent(List<XAtomicEvent> events, XId actorId,
            XAddress target, long modelRevision, long objectRevision) {
        assert !events.isEmpty() : "no events in list";
        if(events.size() == 1) {
            return events.get(0);
        }
        XAddress txnTarget = target;
        if(target.getAddressedType() == XType.XREPOSITORY) {
            // need to construct the model address
            txnTarget = XX.resolveModel(events.get(0).getTarget());
        }
        
        XTransactionEvent txnEvent = MemoryTransactionEvent.createTransactionEvent(actorId,
                txnTarget, events, modelRevision, objectRevision);
        return txnEvent;
    }
    
    private static void fireAtomicEvent(Root root, XRMOFChangeListener changeEventListener,
            XAtomicEvent atomicEvent) {
        if(atomicEvent instanceof XFieldEvent) {
            if(changeEventListener != null) {
                changeEventListener.onChangeEvent((XFieldEvent)atomicEvent);
            }
            root.fireFieldEvent(atomicEvent.getTarget(), (XFieldEvent)atomicEvent);
        } else if(atomicEvent instanceof XObjectEvent) {
            if(changeEventListener != null) {
                changeEventListener.onChangeEvent((XObjectEvent)atomicEvent);
            }
            root.fireObjectEvent(atomicEvent.getTarget(), (XObjectEvent)atomicEvent);
        } else if(atomicEvent instanceof XModelEvent) {
            if(changeEventListener != null) {
                changeEventListener.onChangeEvent((XModelEvent)atomicEvent);
            }
            root.fireModelEvent(atomicEvent.getTarget(), (XModelEvent)atomicEvent);
        } else if(atomicEvent instanceof XRepositoryEvent) {
            if(changeEventListener != null) {
                changeEventListener.onChangeEvent((XRepositoryEvent)atomicEvent);
            }
            root.fireRepositoryEvent(atomicEvent.getTarget(), (XRepositoryEvent)atomicEvent);
        }
    }
    
    /**
     * Fires the transaction events correctly: fires first atomic events, then
     * the transaction event itself
     * 
     * @param root
     * @param changeEventListener @CanBeNull An additional changeEventListener
     * @param event
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
    
}
