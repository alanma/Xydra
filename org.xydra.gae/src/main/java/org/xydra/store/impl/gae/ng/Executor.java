package org.xydra.store.impl.gae.ng;

import java.util.LinkedList;
import java.util.List;

import org.xydra.annotations.NeverNull;
import org.xydra.base.XId;
import org.xydra.base.XType;
import org.xydra.base.change.ChangeType;
import org.xydra.base.change.XAtomicCommand;
import org.xydra.base.change.XAtomicCommand.Intent;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XFieldCommand;
import org.xydra.base.change.XModelCommand;
import org.xydra.base.change.XObjectCommand;
import org.xydra.base.change.XRepositoryCommand;
import org.xydra.base.change.XTransaction;
import org.xydra.base.rmof.XRevWritableField;
import org.xydra.base.rmof.XStateWritableField;
import org.xydra.base.rmof.XStateWritableObject;
import org.xydra.base.value.XValue;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;
import org.xydra.sharedutils.ReflectionUtils;
import org.xydra.sharedutils.XyAssert;
import org.xydra.store.impl.gae.changes.GaeChange;
import org.xydra.store.impl.gae.changes.GaeChange.Status;


/**
 * Checks preconditions and computes events. Decides if execution of a command
 * fails or succeeds.
 * 
 * Change in semantics on 2013-05-28: SafeRevBound ADD commands eliminated. Was
 * checking the revision of the container before.
 * 
 * @author xamde
 */
public class Executor {
    
    private static final Logger log = LoggerFactory.getLogger(Executor.class);
    
    /**
     * @param command may also be a {@link XRepositoryCommand}
     * @param change
     * @param inTransaction
     * @param ctxBeforeCmd to read from
     * @param ctxInTxn to change
     * @return ..
     */
    private static CheckResult checkAtomic(XAtomicCommand command, GaeChange change,
            boolean inTransaction, @NeverNull ContextBeforeCommand ctxBeforeCmd,
            ContextInTxn ctxInTxn) {
        XyAssert.xyAssert(ctxBeforeCmd != null);
        assert ctxBeforeCmd != null;
        
        switch(command.getTarget().getAddressedType()) {
        case XREPOSITORY:
            return Executor.checkRepositoryCommand((XRepositoryCommand)command, change,
                    ctxBeforeCmd, ctxInTxn);
        case XMODEL:
            return Executor.checkModelCommand((XModelCommand)command, change, ctxBeforeCmd,
                    ctxInTxn, inTransaction);
        case XOBJECT:
            return Executor.checkObjectCommand((XObjectCommand)command, change, ctxBeforeCmd,
                    ctxInTxn, inTransaction);
        case XFIELD:
            return Executor.checkFieldCommand((XFieldCommand)command, change, ctxBeforeCmd,
                    ctxInTxn, inTransaction);
        default:
            throw new AssertionError("Cannot happen");
        }
    }
    
    /**
     * @param command to be checked
     * @param change just carried over to the result
     * @param ctxBeforeCmd used to look up revision numbers for safe REMOVE
     *            command
     * @param ctxInTxn the running state, queried to decide of command is legal
     * @param inTransaction just carried over to the result
     * @return a {@link CheckResult} with a changed {@link ContextInTxn} and the
     *         resulting {@link Status}
     */
    private static CheckResult checkFieldCommand(XFieldCommand command, GaeChange change,
            @NeverNull ContextBeforeCommand ctxBeforeCmd, ContextInTxn ctxInTxn,
            boolean inTransaction) {
        
        if(!ctxInTxn.exists()) {
            return CheckResult.failed("Model '" + command.getModelId() + "' does not exist");
        }
        
        XId objectId = command.getChangedEntity().getObject();
        XStateWritableObject objectInTxn = ctxInTxn.getObject(objectId);
        if(objectInTxn == null) {
            return CheckResult.failed("Object '" + objectId
                    + "' does not exist, no field command can succeed");
        }
        
        XStateWritableField fieldInTxn = objectInTxn.getField(command.getFieldId());
        if(fieldInTxn == null) {
            return CheckResult.failed("Command { " + command + "} is invalid. Field '"
                    + command.getFieldId() + "' not found in object '" + command.getObjectId()
                    + ", no field command can succeed");
        }
        
        /* model, object and field exist in the transaction context */
        boolean valueExists = !fieldInTxn.isEmpty();
        switch(command.getChangeType()) {
        
        case ADD: {
            if(valueExists) {
                if(command.getIntent() == Intent.Forced) {
                    // success or nochange
                    /*
                     * forced command ADDs a value, but there is already another
                     * one? success.
                     */
                    XValue oldValueInTxn = fieldInTxn.getValue();
                    XValue newValue = command.getValue();
                    XyAssert.xyAssert(newValue != null);
                    boolean sameValue = oldValueInTxn != null && oldValueInTxn.equals(newValue);
                    if(sameValue) {
                        return CheckResult.successNoChange("had already the same value");
                    } else {
                        return CheckResult.successValue(command, change, ctxInTxn, inTransaction);
                    }
                } else {
                    // fail
                    return CheckResult
                            .failed("Could not safely add field value, there was already one");
                }
            } else {
                if(command.getIntent() == Intent.SafeRevBound) {
                    long fieldRevBeforeCmd = getFieldRevBeforeCmd(ctxBeforeCmd, objectId,
                            command.getFieldId());
                    if(command.getRevisionNumber() != fieldRevBeforeCmd) {
                        return CheckResult.failed("Expected revNr " + command.getRevisionNumber()
                                + " but found " + fieldRevBeforeCmd);
                    }
                }
                // success
                return CheckResult.successValue(command, change, ctxInTxn, inTransaction);
            }
        }
        
        case REMOVE: {
            if(!valueExists) {
                if(command.getIntent() == Intent.Forced) {
                    return CheckResult.successNoChange("was empty before, nothing to remove");
                } else {
                    return CheckResult
                            .failed("Could not safely remove field value, there was none");
                }
            } else {
                if(command.getIntent() == Intent.SafeRevBound) {
                    long fieldRevBeforeCmd = getFieldRevBeforeCmd(ctxBeforeCmd, objectId,
                            command.getFieldId());
                    if(command.getRevisionNumber() != fieldRevBeforeCmd) {
                        return CheckResult.failed("Expected revNr " + command.getRevisionNumber()
                                + " but found " + fieldRevBeforeCmd);
                    }
                }
                return CheckResult.successValue(command, change, ctxInTxn, inTransaction);
            }
        }
        
        /* semantics similar to 'remove followed by add' */
        case CHANGE: {
            if(!valueExists) {
                if(command.getIntent() == Intent.Forced) {
                    return CheckResult.successValue(command, change, ctxInTxn, inTransaction);
                } else {
                    return CheckResult
                            .failed("Could not safely change field value, there was none");
                }
            } else {
                if(command.getIntent() == Intent.Forced) {
                    // success or nochange
                    /*
                     * forced command ADDs a value, but there is already another
                     * one? success.
                     */
                    XValue oldValueInTxn = fieldInTxn.getValue();
                    XValue newValue = command.getValue();
                    XyAssert.xyAssert(newValue != null);
                    boolean sameValue = oldValueInTxn != null && oldValueInTxn.equals(newValue);
                    if(sameValue) {
                        return CheckResult.successNoChange("had already the same value");
                    } else {
                        return CheckResult.successValue(command, change, ctxInTxn, inTransaction);
                    }
                } else {
                    if(command.getIntent() == Intent.SafeRevBound) {
                        long fieldRevBeforeCmd = getFieldRevBeforeCmd(ctxBeforeCmd, objectId,
                                command.getFieldId());
                        if(command.getRevisionNumber() != fieldRevBeforeCmd) {
                            return CheckResult.failed("Expected revNr "
                                    + command.getRevisionNumber() + " but found "
                                    + fieldRevBeforeCmd);
                        }
                    }
                    return CheckResult.successValue(command, change, ctxInTxn, inTransaction);
                }
            }
        }
        
        default:
            throw new AssertionError("illegal command");
        }
    }
    
    /**
     * @param ctxBeforeCmd
     * @param objectId
     * @param fieldId
     * @return the field rev before a command can also be the object or model
     *         revision, depending on what existed before the command.
     */
    private static long getFieldRevBeforeCmd(ContextBeforeCommand ctxBeforeCmd, XId objectId,
            XId fieldId) {
        long fieldRevBeforeCmd;
        TentativeObjectState objectBeforeCmd = ctxBeforeCmd.getObject(objectId);
        if(objectBeforeCmd == null) {
            fieldRevBeforeCmd = ctxBeforeCmd.getRevisionNumber();
        } else {
            XRevWritableField fieldBeforeCmd = objectBeforeCmd.getField(fieldId);
            if(fieldBeforeCmd == null) {
                fieldRevBeforeCmd = objectBeforeCmd.getRevisionNumber();
            } else {
                fieldRevBeforeCmd = fieldBeforeCmd.getRevisionNumber();
            }
        }
        return fieldRevBeforeCmd;
    }
    
    private static long getObjectRevBeforeCmd(ContextBeforeCommand ctxBeforeCmd, XId objectId) {
        long objectRevBeforeCmd;
        TentativeObjectState objectBeforeCmd = ctxBeforeCmd.getObject(objectId);
        if(objectBeforeCmd == null) {
            objectRevBeforeCmd = ctxBeforeCmd.getRevisionNumber();
        } else {
            objectRevBeforeCmd = objectBeforeCmd.getRevisionNumber();
        }
        return objectRevBeforeCmd;
    }
    
    private static long getModelRevBeforeCmd(ContextBeforeCommand ctxBeforeCmd) {
        return ctxBeforeCmd.getRevisionNumber();
    }
    
    /**
     * Checks if the given {@link XModelCommand} is valid and can be
     * successfully executed on this ChangedModel or if the attempt to execute
     * it will fail.
     * 
     * @param command The {@link XModelCommand} which is to be checked.
     * @param change
     * @param beforeContext to read from
     * @param ctxInTxn to change
     * @param inTransaction
     * @return true, if the {@link XModelCommand} is valid and can be executed,
     *         false otherwise
     */
    private static CheckResult checkModelCommand(XModelCommand command, GaeChange change,
            @NeverNull ContextBeforeCommand ctxBeforeCmd, @NeverNull ContextInTxn ctxInTxn,
            boolean inTransaction) {
        
        if(!ctxInTxn.exists()) {
            return CheckResult.failed("Model '" + command.getModelId() + "' does not exist");
        }
        
        XId objectId = command.getChangedEntity().getObject();
        /*
         * TODO might be faster to look for TOS instead via
         * ctxInTxn.getObject(objectId);
         */
        boolean objectExists = ctxInTxn.hasObject(objectId);
        switch(command.getChangeType()) {
        case ADD:
            if(objectExists) {
                switch(command.getIntent()) {
                case Forced:
                    // nochange
                    return CheckResult
                            .successNoChange("objectExists " + command.getChangedEntity());
                case SafeStateBound:
                case SafeRevBound:
                    // fail
                    return CheckResult.failed("objectExists " + command.getChangedEntity());
                default:
                    throw new AssertionError();
                }
            } else {
                if(command.getIntent() == Intent.SafeRevBound) {
                    if(command.getIntent() == Intent.SafeRevBound) {
                        long modelRevBeforeCmd = getModelRevBeforeCmd(ctxBeforeCmd);
                        if(command.getRevisionNumber() != modelRevBeforeCmd) {
                            return CheckResult.failed("ModelRevision number expected "
                                    + command.getRevisionNumber() + " but found "
                                    + modelRevBeforeCmd);
                        }
                    }
                }
                // success
                return CheckResult.successCreatedObject(command, change, ctxInTxn, inTransaction);
            }
        case REMOVE:
            if(!objectExists) {
                switch(command.getIntent()) {
                case Forced:
                    // nochange
                    return CheckResult.successNoChange("!tos.objectExists");
                case SafeStateBound:
                case SafeRevBound:
                    // fail
                    return CheckResult.failed("!objectExists");
                default:
                    throw new AssertionError();
                }
            } else {
                if(command.getIntent() == Intent.SafeRevBound) {
                    long objectRevBeforeCmd = getObjectRevBeforeCmd(ctxBeforeCmd, objectId);
                    if(command.getRevisionNumber() != objectRevBeforeCmd) {
                        return CheckResult.failed("ObjectRevision number expected "
                                + command.getRevisionNumber() + " but found " + objectRevBeforeCmd);
                    }
                }
                // success
                return CheckResult.successRemovedObject(command, change, ctxInTxn, inTransaction);
            }
        default:
            throw new AssertionError("impossible type for model command " + command);
        }
    }
    
    private static CheckResult checkObjectCommand(XObjectCommand command, GaeChange change,
            @NeverNull ContextBeforeCommand ctxBeforeCmd, ContextInTxn ctxInTxn,
            boolean inTransaction) {
        
        if(!ctxInTxn.exists()) {
            return CheckResult.failed("Model '" + command.getModelId() + "' does not exist");
        }
        
        XId objectId = command.getChangedEntity().getObject();
        XId fieldId = command.getFieldId();
        
        XStateWritableObject objectInTxn = ctxInTxn.getObject(objectId);
        
        boolean fieldExists = objectInTxn.hasField(fieldId);
        
        switch(command.getChangeType()) {
        case ADD: {
            if(fieldExists) {
                if(command.getIntent() == Intent.Forced) {
                    // nochange
                    return CheckResult.successNoChange("tos '" + objectInTxn.getAddress()
                            + "' hasField '" + fieldId + "'");
                } else {
                    // fail
                    return CheckResult.failed("tos '" + objectInTxn.getAddress() + "' hasField '"
                            + fieldId + "'");
                }
            } else {
                if(command.getIntent() == Intent.SafeRevBound) {
                    long objectRevBeforeCmd = getObjectRevBeforeCmd(ctxBeforeCmd, objectId);
                    if(command.getRevisionNumber() != objectRevBeforeCmd) {
                        return CheckResult.failed("Revision number expected "
                                + command.getRevisionNumber() + " but found " + objectRevBeforeCmd);
                    }
                }
                // success
                return CheckResult.successCreatedField(command, ctxBeforeCmd.getRevisionNumber(),
                        ctxInTxn, change, inTransaction);
            }
        }
        case REMOVE: {
            if(!fieldExists) {
                if(command.getIntent() == Intent.Forced) {
                    // nochange
                    return CheckResult.successNoChange("tos '" + objectInTxn.getAddress()
                            + "' hasField '" + fieldId + "'");
                } else {
                    // fail
                    return CheckResult.failed("tos '" + objectInTxn.getAddress()
                            + "' has no field '" + fieldId + "'");
                }
            } else {
                if(command.getIntent() == Intent.SafeRevBound) {
                    // check
                    long fieldRevBeforeCmd = getFieldRevBeforeCmd(ctxBeforeCmd, objectId, fieldId);
                    if(command.getRevisionNumber() != fieldRevBeforeCmd) {
                        return CheckResult.failed("FieldRevision number expected "
                                + command.getRevisionNumber() + " but found " + fieldRevBeforeCmd);
                    }
                }
                // success
                return CheckResult.successRemovedField(command, change, ctxInTxn, inTransaction);
            }
        }
        default:
            throw new AssertionError("impossible type for object command " + command);
        }
    }
    
    /**
     * Main entry method.
     * 
     * Phase 2: Depending on the command, fetch the required information to
     * compute if the command is legal -- and if so -- what events results from
     * executing it. I.e. there are implied events to be considered.
     * 
     * As the locks synchronised access, the currently locked parts are stable,
     * i.e. not being changed by other parts. They reflect the state before this
     * command and can be considered the current rev -- even if other command
     * are still working in parallel on irrelevant parts.
     * 
     * @param executionContext
     * 
     * @param command
     * @param gaeLocks
     * @return
     */
    static CheckResult checkPreconditions(ContextBeforeCommand executionContext, XCommand command,
            GaeChange change) {
        CheckResult result;
        ContextInTxn inTxnContext = executionContext.forkTxn();
        
        if(command.getChangeType() == ChangeType.TRANSACTION) {
            result = checkTransaction((XTransaction)command, change, executionContext, inTxnContext);
        } else {
            if(command.getTarget().getAddressedType() == XType.XREPOSITORY) {
                result = checkRepositoryCommand((XRepositoryCommand)command, change,
                        executionContext, inTxnContext);
            } else {
                result = Executor.checkAtomic((XAtomicCommand)command, change, false,
                        executionContext, inTxnContext);
            }
        }
        
        /* just add some additional logging */
        if(result.getStatus() == Status.FailedPreconditions) {
            log.debug("Status=" + result.getStatus() + " hint: " + result.getDebugHint());
        }
        
        return result;
    }
    
    /**
     * @param repoCmd
     * @param change
     * @param ctxBeforeCmd
     * @param ctxInTxn
     * @return
     */
    private static CheckResult checkRepositoryCommand(XRepositoryCommand repoCmd, GaeChange change,
            ContextBeforeCommand ctxBeforeCmd, ContextInTxn ctxInTxn) {
        
        GaeModelRevInfo infoBeforeCmd = ctxBeforeCmd.getInfo();
        boolean modelExistsBeforeCmd = infoBeforeCmd.isModelExists();
        
        switch(repoCmd.getChangeType()) {
        case ADD:
            if(!ctxInTxn.exists()) {
                // check
                if(repoCmd.getIntent() == Intent.SafeRevBound) {
                    long modelRevBeforeCmd = infoBeforeCmd.getLastStableSuccessChange();
                    if(modelRevBeforeCmd != repoCmd.getRevisionNumber()) {
                        return CheckResult
                                .failed("SafeRevBound RepositoryCommand ADD failed. Reason: "
                                        + ("modelRevNr=" + modelRevBeforeCmd + " cmdRevNr=" + repoCmd
                                                .getRevisionNumber()));
                    }
                }
                return CheckResult.successCreatedModel(repoCmd, change, ctxInTxn);
            } else if(repoCmd.isForced()) {
                return CheckResult.successNoChange("Model exists");
            } else {
                return CheckResult
                        .failed("Safe RepositoryCommand ADD failed; model existed already");
            }
        case REMOVE:
            long modelRevBeforeCmd = infoBeforeCmd.getLastStableSuccessChange();
            
            if(!modelExistsBeforeCmd) {
                if(repoCmd.getIntent() == Intent.Forced) {
                    return CheckResult.successNoChange("Model did not exist");
                } else {
                    return CheckResult
                            .failed("Safe-X RepositoryCommand REMOVE failed. Reason: "
                                    + ("model did not exist; modelRevNr=" + modelRevBeforeCmd
                                            + " cmdRevNr=" + repoCmd.getRevisionNumber()
                                            + " intent:" + repoCmd.getIntent()));
                }
            } else {
                assert modelExistsBeforeCmd;
                if(repoCmd.getIntent() == Intent.SafeRevBound) {
                    if(modelRevBeforeCmd != repoCmd.getRevisionNumber()) {
                        return CheckResult
                                .failed("SafeRevBound RepositoryCommand REMOVE failed. Reason: "
                                        + ("modelRevNr=" + modelRevBeforeCmd + " cmdRevNr=" + repoCmd
                                                .getRevisionNumber()));
                    }
                }
                // change
                log.debug("Removing model " + repoCmd.getChangedEntity() + " " + modelRevBeforeCmd);
                return CheckResult.successRemovedModel(repoCmd, change, ctxInTxn);
            }
            
        default:
            throw new AssertionError("XRepositoryCommand with unexpected type: " + repoCmd);
        }
    }
    
    /**
     * Apply the {@link XCommand XCommands} contained in the given
     * {@link XTransaction}. If one of the {@link XCommand XCommands} failed,
     * the {@link XTransaction} will remain partially applied, already executed
     * {@link XCommand XCommands} will not be rolled back.
     * 
     * @param transaction The {@link XTransaction} which is to be executed
     * @param change
     * @param ctxBeforeCmd state before the txn started (outside view)
     * @param ctxInTxn state while txn is running (inside view)
     * @return the {@link CheckResult}
     * 
     *         TODO it might be a good idea to tell the caller of this method
     *         which commands of the transaction were executed and not only
     *         return false
     */
    private static CheckResult checkTransaction(XTransaction transaction, GaeChange change,
            ContextBeforeCommand ctxBeforeCmd, ContextInTxn ctxInTxn) {
        
        List<CheckResult> results = new LinkedList<CheckResult>();
        for(int i = 0; i < transaction.size(); i++) {
            XAtomicCommand command = transaction.getCommand(i);
            try {
                CheckResult atomicResult = Executor.checkAtomic(command, change, true,
                        ctxBeforeCmd, ctxInTxn);
                if(atomicResult.getStatus().isFailure()) {
                    return CheckResult.failed("txn failed at command " + command + " Reason: "
                            + atomicResult.getDebugHint());
                }
                results.add(atomicResult);
            } catch(Throwable e) {
                log.warn("Txn failed on exception", e);
                return CheckResult.failed("txn failed at command " + command + " Reason: "
                        + e.getClass().getName() + ": " + ReflectionUtils.firstNLines(e, 200));
            }
        }
        
        return CheckResult.successTransaction(transaction, change, ctxInTxn);
    }
    
}
