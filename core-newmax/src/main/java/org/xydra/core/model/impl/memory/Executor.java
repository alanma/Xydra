package org.xydra.core.model.impl.memory;

import java.util.LinkedList;
import java.util.List;

import org.xydra.annotations.ModificationOperation;
import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.change.XAtomicCommand;
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
import org.xydra.base.change.XTransaction;
import org.xydra.base.change.XTransactionEvent;
import org.xydra.base.rmof.XRevWritableField;
import org.xydra.base.rmof.XRevWritableModel;
import org.xydra.base.rmof.XRevWritableObject;
import org.xydra.base.rmof.XWritableField;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.base.rmof.XWritableObject;
import org.xydra.base.rmof.XWritableRepository;
import org.xydra.base.rmof.impl.XExistsReadableModel;
import org.xydra.base.rmof.impl.XExistsRevWritableModel;
import org.xydra.base.rmof.impl.XExistsRevWritableRepository;
import org.xydra.base.rmof.impl.XExistsWritableModel;
import org.xydra.base.rmof.impl.memory.SimpleModel;
import org.xydra.base.value.XValue;
import org.xydra.core.change.RevisionConstants;
import org.xydra.core.change.XRMOFChangeListener;
import org.xydra.core.model.delta.ChangedField;
import org.xydra.core.model.delta.ChangedModel;
import org.xydra.core.model.delta.ChangedObject;
import org.xydra.core.model.delta.DeltaUtils;
import org.xydra.core.model.delta.DeltaUtils.ModelChange;
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
    
    /**
     * Thrown by run...methods if command fails
     */
    private static class ExecutionException extends RuntimeException {
        
        private static final long serialVersionUID = 1L;
        
        /**
         * @param msg reasons for failing command
         */
        public ExecutionException(String msg) {
            super(msg);
        }
        
    }
    
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
        try {
            ChangedField fieldInTxn = new ChangedField(fieldState);
            runCommandOnField(fieldCommand, fieldState, fieldInTxn);
            if(!fieldInTxn.isChanged()) {
                return XCommand.NOCHANGE;
            }
            
            // create event
            long currentModelRev = modelState == null ? XEvent.REVISION_OF_ENTITY_NOT_SET
                    : modelState.getRevisionNumber();
            long currentObjectRev = objectState == null ? XEvent.REVISION_OF_ENTITY_NOT_SET
                    : objectState.getRevisionNumber();
            List<XAtomicEvent> events = new LinkedList<XAtomicEvent>();
            Eventor.createEventsFromChangedField(events, actorId, currentModelRev,
                    currentObjectRev, fieldState, fieldInTxn, root.isTransactionInProgess());
            assert events.size() == 1;
            XEvent event = Eventor.createSingleEvent(events, actorId, fieldCommand.getTarget(),
                    currentModelRev, currentObjectRev);
            
            // apply event
            Eventor.applyEventOnFieldAndParents((XFieldEvent)event, modelState, objectState,
                    fieldState);
            root.getSyncLog().appendSyncLogEntry(fieldCommand, event);
            fireEvents(root, changeEventListener, event);
            
            return event.getRevisionNumber();
        } catch(ExecutionException e) {
            log.warn("command " + fieldCommand + " failed", e);
            return XCommand.FAILED;
        }
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
        try {
            ChangedObject objectInTxn = new ChangedObject(objectState);
            runCommandOnObject(command, objectState, objectInTxn);
            if(!objectInTxn.hasChanges()) {
                return XCommand.NOCHANGE;
            }
            
            // create event
            long currentModelRev = modelState == null ? XEvent.REVISION_OF_ENTITY_NOT_SET
                    : modelState.getRevisionNumber();
            List<XAtomicEvent> events = new LinkedList<XAtomicEvent>();
            Eventor.createEventsFromChangedObject(events, actorId, currentModelRev, objectState,
                    objectInTxn, root.isTransactionInProgess());
            XEvent event = Eventor.createSingleEvent(events, actorId, command.getTarget(),
                    currentModelRev, objectState.getRevisionNumber());
            
            // apply event
            Eventor.applyEventOnObjectAndParents(event, modelState, objectState);
            root.getSyncLog().appendSyncLogEntry(command, event);
            fireEvents(root, changeEventListener, event);
            
            return event.getRevisionNumber();
        } catch(ExecutionException e) {
            log.warn("command " + command + " failed", e);
            return XCommand.FAILED;
        }
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
        try {
            ChangedModel modelInTxn = new ChangedModel(modelState);
            runCommandOnModel(command, modelState, modelInTxn);
            if(!modelInTxn.hasChanges()) {
                return XCommand.NOCHANGE;
            }
            
            // create event
            long currentModelRev = modelState.getRevisionNumber();
            List<XAtomicEvent> events = new LinkedList<XAtomicEvent>();
            Eventor.createEventsFromChangedModel(events, actorId, modelState, modelInTxn,
                    root.isTransactionInProgess());
            XEvent event = Eventor.createSingleEvent(events, actorId, command.getTarget(),
                    currentModelRev, RevisionConstants.REVISION_OF_ENTITY_NOT_SET);
            
            // apply event
            Eventor.applyEventOnModel(event, modelState);
            root.getSyncLog().appendSyncLogEntry(command, event);
            fireEvents(root, changeEventListener, event);
            
            return event.getRevisionNumber();
        } catch(ExecutionException e) {
            log.warn("command " + command + " failed", e);
            return XCommand.FAILED;
        }
    }
    
    /**
     * Worker. Can also be used within a transaction. Run... methods define
     * command execution semantics.
     * 
     * @param fieldCommand @NeverNull can be an atomic field command or part of
     *            a transaction
     * @param fieldBeforeTxn @CanBeNull
     * @param fieldInTxn @NeverNull
     * @throws ExecutionException if the command fails
     */
    private static void runCommandOnField(XFieldCommand fieldCommand,
            XRevWritableField fieldBeforeTxn, XWritableField fieldInTxn) throws ExecutionException {
        assert fieldCommand != null;
        assert fieldInTxn != null;
        
        // check whether the given event actually refers to this field
        if(!fieldInTxn.getAddress().equals(fieldCommand.getTarget())) {
            throw new ExecutionException("Address of command does not match fieldState");
        }
        
        XValue currentValue = fieldInTxn.getValue();
        long fieldRevBeforeTxn = fieldBeforeTxn == null ? RevisionConstants.NOT_EXISTING
                : fieldBeforeTxn.getRevisionNumber();
        
        /* Assertions and failure cases */
        if(!fieldCommand.isForced() && fieldCommand.getRevisionNumber() != fieldRevBeforeTxn) {
            throw new ExecutionException("Safe rev-bound-command fails, expected rev="
                    + fieldCommand.getRevisionNumber() + " found rev=" + fieldRevBeforeTxn);
        }
        
        /* Standard execution */
        XValue newValue = fieldCommand.getValue();
        switch(fieldCommand.getChangeType()) {
        case ADD:
            if(currentValue != null) {
                /*
                 * the forced event only cares about the postcondition - that
                 * there is the given value set, not about that there was no
                 * value before
                 */
                if(!fieldCommand.isForced()) {
                    throw new ExecutionException("Cmd not forced & value was already set");
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
                if(!fieldCommand.isForced()) {
                    throw new ExecutionException(
                            "value is not set and can not be removed or the given value is not current anymore");
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
                if(!fieldCommand.isForced()) {
                    throw new ExecutionException(
                            "given old value does not concur with the current value");
                }
            }
            break;
        default:
            throw new IllegalArgumentException("Unknown field command type: " + fieldCommand);
        }
        
        if(XI.equals(currentValue, newValue)) {
            // no change
            return;
        }
        
        /* Changes happen, materialise them */
        switch(fieldCommand.getChangeType()) {
        case ADD:
        case CHANGE:
            fieldInTxn.setValue(newValue);
            break;
        case REMOVE:
            fieldInTxn.setValue(null);
            break;
        default:
            assert false;
        }
    }
    
    /**
     * Worker. Can also be used within a transaction. Run... methods define
     * command execution semantics.
     * 
     * @param command @NeverNull
     * @param objectBeforeTxn @CanBeNull
     * @param objectInTxn @NeverNull
     */
    private static void runCommandOnObject(XCommand command, XRevWritableObject objectBeforeTxn,
            ChangedObject objectInTxn) {
        if(command instanceof XTransaction) {
            for(XAtomicCommand atomicCommand : ((XTransaction)command)) {
                runAtomicCommandOnObject(atomicCommand, objectBeforeTxn, objectInTxn);
            }
        } else {
            runAtomicCommandOnObject((XAtomicCommand)command, objectBeforeTxn, objectInTxn);
        }
    }
    
    /**
     * Worker. Can also be used within a transaction. Run... methods define
     * command execution semantics.
     * 
     * @param command @NeverNull
     * @param modelBeforeTxn @CanBeNull
     * @param modelInTxn @NeverNull
     */
    private static void runCommandOnModel(XCommand command, XExistsRevWritableModel modelBeforeTxn,
            ChangedModel modelInTxn) throws ExecutionException {
        if(command instanceof XTransaction) {
            for(XAtomicCommand atomicCommand : ((XTransaction)command)) {
                runAtomicCommandOnModel(atomicCommand, modelBeforeTxn, modelInTxn);
            }
        } else {
            runAtomicCommandOnModel((XAtomicCommand)command, modelBeforeTxn, modelInTxn);
        }
    }
    
    /**
     * Worker. Can also be used within a transaction. Run... methods define
     * command execution semantics.
     * 
     * @param atomicCommand
     * @param objectBeforeTxn @CanBeNull
     * @param objectInTxn
     */
    private static void runAtomicCommandOnObject(XAtomicCommand atomicCommand,
            XRevWritableObject objectBeforeTxn, XWritableObject objectInTxn) {
        switch(atomicCommand.getTarget().getAddressedType()) {
        case XREPOSITORY:
        case XMODEL:
            // TODO support model commands?
            throw new IllegalArgumentException();
        case XOBJECT:
            runObjectCommandOnObject((XObjectCommand)atomicCommand, objectBeforeTxn, objectInTxn);
            break;
        case XFIELD:
            XFieldCommand fieldCommand = (XFieldCommand)atomicCommand;
            XId fieldId = fieldCommand.getFieldId();
            XWritableField fieldInTxn = objectInTxn.getField(fieldId);
            assert fieldInTxn != null;
            runCommandOnField(fieldCommand,
                    objectBeforeTxn == null ? null : objectBeforeTxn.getField(fieldId), fieldInTxn);
        }
    }
    
    /**
     * Worker. Can also be used within a transaction. Run... methods define
     * command execution semantics.
     * 
     * @param atomicCommand @NeverNull
     * @param modelBeforeTxn @CanBeNull
     * @param modelInTxn @NeverNukk
     */
    private static void runAtomicCommandOnModel(XAtomicCommand atomicCommand,
            XRevWritableModel modelBeforeTxn, XExistsWritableModel modelInTxn) {
        assert atomicCommand != null;
        assert modelInTxn != null;
        switch(atomicCommand.getTarget().getAddressedType()) {
        case XREPOSITORY:
            runRepositoryCommandOnModel((XRepositoryCommand)atomicCommand, modelBeforeTxn,
                    modelInTxn);
            break;
        case XMODEL:
            runModelCommandOnModel((XModelCommand)atomicCommand, modelBeforeTxn, modelInTxn);
            break;
        case XOBJECT:
        case XFIELD:
            XId objectId;
            if(atomicCommand instanceof XObjectCommand) {
                objectId = ((XObjectCommand)atomicCommand).getObjectId();
            } else if(atomicCommand instanceof XFieldCommand) {
                objectId = ((XFieldCommand)atomicCommand).getObjectId();
            } else {
                throw new AssertionError();
            }
            XWritableObject objectInTxn = modelInTxn.getObject(objectId);
            assert objectInTxn != null;
            runAtomicCommandOnObject(atomicCommand, modelBeforeTxn.getObject(objectId), objectInTxn);
            break;
        }
    }
    
    /**
     * Worker. Can also be used within a transaction. Run... methods define
     * command execution semantics.
     * 
     * @param objectCommand @NeverNull
     * @param objectBeforeTxn @CanBeNull
     * @param objectInTxn @NeverNull
     * @throws ExecutionException
     */
    private static void runObjectCommandOnObject(XObjectCommand objectCommand,
            XRevWritableObject objectBeforeTxn, XWritableObject objectInTxn)
            throws ExecutionException {
        /* Assertions and failure cases */
        if(!objectInTxn.getAddress().equals(objectCommand.getTarget())) {
            throw new ExecutionException("Event address '" + objectCommand.getTarget()
                    + "' does not match '" + objectInTxn.getAddress() + "'");
        }
        
        XId fieldId = objectCommand.getFieldId();
        boolean fieldExists = objectInTxn.hasField(fieldId);
        long objectRevBeforeTxn = objectBeforeTxn == null ? RevisionConstants.NOT_EXISTING
                : objectBeforeTxn.getRevisionNumber();
        
        switch(objectCommand.getChangeType()) {
        case ADD:
            if(fieldExists) {
                // ID already taken
                if(objectCommand.isForced()) {
                    /*
                     * the forced event only cares about the postcondition -
                     * that there is a field with the given ID, not about that
                     * there was no such field before
                     */
                    return;
                }
                throw new ExecutionException("field exists already, command not forced");
            }
            break;
        case REMOVE:
            if(!fieldExists) {
                // ID not taken
                if(objectCommand.isForced()) {
                    /*
                     * the forced event only cares about the postcondition -
                     * that there is no field with the given ID, not about that
                     * there was such a field before
                     */
                    return;
                }
                throw new ExecutionException("Field did not exist, command not forced");
            } else {
                if(!objectCommand.isForced()
                        && objectRevBeforeTxn != objectCommand.getRevisionNumber()) {
                    throw new ExecutionException("Safe-rev command found wrong rev-nr");
                }
            }
            break;
        default:
            throw new IllegalArgumentException("Unknown object command type: " + objectCommand);
        }
        
        // create ChangedObject
        switch(objectCommand.getChangeType()) {
        case ADD:
            assert !objectInTxn.hasField(fieldId);
            objectInTxn.createField(fieldId);
            break;
        case REMOVE:
            assert objectInTxn.hasField(fieldId);
            objectInTxn.removeField(fieldId);
            break;
        default:
            assert false;
        }
    }
    
    /**
     * Worker. Can also be used within a transaction. Run... methods define
     * command execution semantics.
     * 
     * @param atomicCommand @NeverNull
     * @param modelBeforeTxn @CanBeNull
     * @param modelInTxn @NeverNull
     * @throws ExecutionException
     */
    private static void runModelCommandOnModel(XModelCommand modelCommand,
            XRevWritableModel modelBeforeTxn, XWritableModel modelInTxn) throws ExecutionException {
        assert modelCommand != null;
        assert modelInTxn != null;
        
        /* Check if failure */
        XId objectId = modelCommand.getObjectId();
        boolean objectExists = modelInTxn.hasObject(objectId);
        switch(modelCommand.getChangeType()) {
        case ADD: {
            if(objectExists) {
                // ID already taken
                if(modelCommand.isForced()) {
                    /*
                     * the forced event only cares about the postcondition -
                     * that there is an object with the given ID, not about that
                     * there was no such object before
                     */
                    return;
                } else {
                    throw new ExecutionException(
                            "Cannot add object - exists already; command not forced.");
                }
            }
        }
            break;
        case REMOVE: {
            if(!objectExists) {
                // ID not taken
                if(modelCommand.isForced()) {
                    /*
                     * the forced event only cares about the postcondition -
                     * that there is no object with the given ID, not about that
                     * there was such an object before
                     */
                    return;
                } else {
                    throw new ExecutionException(
                            "Cannot removed non-existing object in non-forced mode");
                }
            }
            XWritableObject objectBeforeTxn = modelBeforeTxn.getObject(objectId);
            assert objectBeforeTxn != null;
            if(!modelCommand.isForced()
                    && objectBeforeTxn.getRevisionNumber() != modelCommand.getRevisionNumber()) {
                throw new ExecutionException("Safe-rev command found wrong rev");
            }
        }
            break;
        default:
            throw new IllegalArgumentException("Unknown model command type: " + modelCommand);
        }
        
        // create changed model
        switch(modelCommand.getChangeType()) {
        case ADD:
            assert !objectExists;
            modelInTxn.createObject(objectId);
            break;
        case REMOVE:
            assert objectExists;
            modelInTxn.removeObject(objectId);
            break;
        default:
            assert false;
        }
    }
    
    /**
     * @param atomicCommand @NeverNull
     * @param modelBeforeTxn @NeverNull
     * @param modelInTxn @NeverNull
     */
    private static void runRepositoryCommandOnModel(XRepositoryCommand repositoryCommand,
            XRevWritableModel modelBeforeTxn, XExistsWritableModel modelInTxn) {
        assert repositoryCommand != null;
        assert modelBeforeTxn != null; // TODO verify this assertion makes sense
        assert modelInTxn != null;
        
        /* Check if failure */
        boolean modelExists = modelInTxn.exists();
        switch(repositoryCommand.getChangeType()) {
        case ADD: {
            if(modelExists) {
                // ID already taken
                if(repositoryCommand.isForced()) {
                    /*
                     * the forced event only cares about the postcondition -
                     * that there is an object with the given ID, not about that
                     * there was no such object before
                     */
                    return;
                } else {
                    throw new ExecutionException(
                            "Cannot add model - exists already; command not forced.");
                }
            }
        }
            break;
        case REMOVE: {
            if(!modelExists) {
                // ID not taken
                if(repositoryCommand.isForced()) {
                    /*
                     * the forced event only cares about the postcondition -
                     * that there is no object with the given ID, not about that
                     * there was such an object before
                     */
                    return;
                } else {
                    throw new ExecutionException(
                            "Cannot removed non-existing model in non-forced mode");
                }
            }
            assert modelBeforeTxn != null;
            if(!repositoryCommand.isForced()
                    && modelBeforeTxn.getRevisionNumber() != repositoryCommand.getRevisionNumber()) {
                throw new ExecutionException("Safe-rev command found wrong rev");
            }
        }
            break;
        default:
            throw new IllegalArgumentException("Unknown repo command type: " + repositoryCommand);
        }
        
        // create changed model
        switch(repositoryCommand.getChangeType()) {
        case ADD:
            assert !modelExists;
            modelInTxn.setExists(true);
            break;
        case REMOVE:
            assert modelExists;
            modelInTxn.setExists(false);
            break;
        default:
            assert false;
        }
        
    }
    
    /**
     * Execute an {@link XRepositoryCommand} on a model that may or may not
     * exist and may or may not have a repositoryState
     * 
     * @param actorId @NeverNull
     * @param command @NeverNull
     * @param repositoryState @CanBeNull
     * @param modelState @CanBeNull
     * @param root @NeverNull
     * @param changeEventListener @CanBeNull
     * @return command result
     */
    @ModificationOperation
    private static long executeRepositoryCommand(XId actorId, XRepositoryCommand command,
            XExistsRevWritableRepository repositoryState, XExistsRevWritableModel modelState,
            Root root, XRMOFChangeListener changeEventListener) {
        /* pre-checks */
        assert command != null;
        XId modelId = command.getModelId();
        assert repositoryState == null || repositoryState.getModel(modelId) == modelState;
        XAddress repositoryAddress = command.getTarget();
        if(repositoryState != null) {
            if(!repositoryState.getAddress().equals(repositoryAddress)) {
                log.warn("Command target (" + command.getTarget()
                        + ")  does not fit this repository " + repositoryAddress);
                return XCommand.FAILED;
            }
        }
        XyAssert.xyAssert(!root.isTransactionInProgess());
        
        /* Command -evaluate-> ChangedModel -create-> Event -apply-> */
        try {
            // find or create current model state
            XExistsRevWritableModel currentModelState;
            long modelRevBeforeTxn;
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
                modelRevBeforeTxn = RevisionConstants.NOT_EXISTING;
            } else {
                modelRevBeforeTxn = currentModelState.getRevisionNumber();
            }
            
            // create change state
            DeltaUtils.ModelChange modelChange = createModelChangedFromRepositoryCommand(command,
                    currentModelState);
            if(modelChange == null) {
                return XCommand.NOCHANGE;
            }
            assert modelChange != null;
            
            // create events
            List<XAtomicEvent> events = new LinkedList<XAtomicEvent>();
            Eventor.createEventsFromModelChange(events, actorId, modelState, modelChange,
                    root.isTransactionInProgess());
            
            // apply change to state
            Eventor.applyModelChange(modelChange, currentModelState);
            
            // fire events
            XEvent event = Eventor.createSingleEvent(events, actorId, command.getTarget(),
                    modelRevBeforeTxn, RevisionConstants.REVISION_OF_ENTITY_NOT_SET);
            root.getSyncLog().appendSyncLogEntry(command, event);
            fireEvents(root, changeEventListener, event);
            return event.getRevisionNumber();
        } catch(ExecutionException e) {
            log.warn("command " + command + " failed", e);
            return XCommand.FAILED;
        }
    }
    
    /**
     * @param command
     * @param currentModelState
     * @return null or {@link ChangedModel}
     * @throws ExecutionException
     */
    private static DeltaUtils.ModelChange createModelChangedFromRepositoryCommand(
            XRepositoryCommand command, XExistsReadableModel currentModelState)
            throws ExecutionException {
        assert currentModelState != null;
        
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
                    return null;
                } else {
                    throw new ExecutionException("Cannot add existing model as non-forced command");
                }
            }
            break;
        }
        case REMOVE: {
            if(currentModelState.exists()) {
                // FIXME create txn event if the model had objects ...
            } else {
                if(command.isForced()) {
                    /*
                     * the forced event only cares about the postcondition -
                     * that there is no model with the given ID, not about that
                     * there was such a model before
                     */
                    return null;
                } else {
                    throw new ExecutionException(
                            "Cannot remove non-existing model as non-forced command");
                }
            }
            // model is present, command is removeModel
            
            // check safe conditions
            if(!command.isForced()
                    && currentModelState.getRevisionNumber() != command.getRevisionNumber()) {
                throw new ExecutionException(
                        "Cannot remove model with safe-rev-command and wrong current rev nr");
            }
            
        }
            break;
        default:
            throw new IllegalArgumentException("unknown command type: " + command);
        }
        
        // materialise change
        switch(command.getChangeType()) {
        case ADD:
            assert !currentModelState.exists();
            return ModelChange.CREATED;
        case REMOVE:
            assert currentModelState.exists();
            return ModelChange.REMOVED;
        default:
            throw new AssertionError();
        }
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
