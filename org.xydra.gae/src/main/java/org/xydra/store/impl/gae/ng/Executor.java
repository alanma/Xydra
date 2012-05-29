package org.xydra.store.impl.gae.ng;

import java.util.LinkedList;
import java.util.List;

import org.xydra.annotations.NeverNull;
import org.xydra.base.XID;
import org.xydra.base.XType;
import org.xydra.base.change.ChangeType;
import org.xydra.base.change.XAtomicCommand;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XFieldCommand;
import org.xydra.base.change.XModelCommand;
import org.xydra.base.change.XObjectCommand;
import org.xydra.base.change.XRepositoryCommand;
import org.xydra.base.change.XTransaction;
import org.xydra.base.rmof.XReadableField;
import org.xydra.base.rmof.XRevWritableObject;
import org.xydra.base.rmof.XStateWritableField;
import org.xydra.base.rmof.XStateWritableObject;
import org.xydra.base.value.XValue;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.sharedutils.ReflectionUtils;
import org.xydra.sharedutils.XyAssert;
import org.xydra.store.impl.gae.changes.GaeChange;
import org.xydra.store.impl.gae.changes.GaeChange.Status;


/**
 * Checks preconditions and computes events. Decides if execution of a command
 * fails or succeeds.
 * 
 * @author xamde
 */
public class Executor {
	
	private static final Logger log = LoggerFactory.getLogger(Executor.class);
	
	/**
	 * @param command may NOT be a {@link XRepositoryCommand}
	 * @param change
	 * @param inTransaction
	 * @param ctxBeforeCmd to read from
	 * @param ctxInTxn to change
	 * @return ..
	 */
	private static CheckResult checkAtomic(XAtomicCommand command, GaeChange change,
	        boolean inTransaction, @NeverNull ContextBeforeCommand ctxBeforeCmd,
	        ContextInTxn ctxInTxn) {
		XyAssert.xyAssert(command.getTarget().getAddressedType() != XType.XREPOSITORY);
		XyAssert.xyAssert(command.getChangedEntity().getObject() != null,
		        "MOF commands each must have an objectId in their address");
		XyAssert.xyAssert(ctxBeforeCmd != null);
		assert ctxBeforeCmd != null;
		
		switch(command.getTarget().getAddressedType()) {
		case XMODEL:
			return Executor.checkModelCommand((XModelCommand)command, change, ctxBeforeCmd,
			        ctxInTxn, inTransaction);
		case XOBJECT:
			return Executor.checkObjectCommand((XObjectCommand)command, change, ctxBeforeCmd,
			        ctxInTxn, inTransaction);
		case XFIELD:
			return Executor.checkFieldCommand((XFieldCommand)command, change, ctxBeforeCmd,
			        ctxInTxn, inTransaction);
		case XREPOSITORY:
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
		
		if(!ctxInTxn.isModelExists()) {
			return CheckResult.failed("Model does not exist, no field command can succeed");
		}
		XID objectId = command.getChangedEntity().getObject();
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
		
		if(command.isForced()) {
			switch(command.getChangeType()) {
			case ADD:
			case CHANGE:
				/*
				 * forced & isEmpty: success. forced & sameValue: noChange.
				 * forced & differentValue: success.
				 */
				if(fieldInTxn.isEmpty()) {
					return CheckResult.successValue(command, change, ctxInTxn, inTransaction);
				} else {
					XValue oldValueInTxn = fieldInTxn.getValue();
					XValue newValue = command.getValue();
					XyAssert.xyAssert(newValue != null);
					boolean sameValue = oldValueInTxn != null && oldValueInTxn.equals(newValue);
					if(sameValue) {
						return CheckResult.successNoChange("had already the same value");
					} else {
						return CheckResult.successValue(command, change, ctxInTxn, inTransaction);
					}
				}
			case REMOVE:
				/*
				 * forced & isEmpty: noChange. forced & !empty: success.
				 */
				if(fieldInTxn.isEmpty()) {
					return CheckResult.successNoChange("was empty before, nothing to remove");
				} else {
					return CheckResult.successValue(command, change, ctxInTxn, inTransaction);
				}
			default:
			case TRANSACTION:
				throw new AssertionError();
			}
			
		} else {
			/* safe command */
			switch(command.getChangeType()) {
			case ADD:
				/* safe & isEmpty: success. safe & !empty: fail. */
				if(fieldInTxn.isEmpty()) {
					return CheckResult.successValue(command, change, ctxInTxn, inTransaction);
				} else {
					return CheckResult.failed("field has a value, cannot safely add it");
				}
			case CHANGE: {
				/*
				 * safe & isEmpty: fail.
				 * 
				 * safe & !empty: CHECK REV (or value at that rev)
				 */
				if(fieldInTxn.isEmpty()) {
					return CheckResult.failed("field has no value, cannot change it");
				}
				XRevWritableObject objectBeforeCmd = ctxBeforeCmd.getObject(objectId);
				long fieldRevBeforeCmd = objectBeforeCmd.getField(command.getFieldId())
				        .getRevisionNumber();
				if(fieldRevBeforeCmd == command.getRevisionNumber()) {
					return CheckResult.successValue(command, change, ctxInTxn, inTransaction);
				} else {
					return CheckResult
					        .failed("Cannot safely change value (wrong revision) fieldRev="
					                + fieldRevBeforeCmd + " commandSafeRev="
					                + command.getRevisionNumber());
				}
			}
			case REMOVE: {
				/*
				 * safe & isEmpty: fail.
				 * 
				 * safe & !empty: CHECK REV (or value at that rev)
				 */
				if(fieldInTxn.isEmpty()) {
					return CheckResult.failed("field has no value, cannot remove it");
				}
				XRevWritableObject objectBeforeCmd = ctxBeforeCmd.getObject(objectId);
				long fieldRevBeforeCmd = objectBeforeCmd.getField(command.getFieldId())
				        .getRevisionNumber();
				if(fieldRevBeforeCmd == command.getRevisionNumber()) {
					return CheckResult.successValue(command, change, ctxInTxn, inTransaction);
				} else {
					return CheckResult
					        .failed("Cannot safely remove value (wrong revision) fieldRev="
					                + fieldRevBeforeCmd + " commandSafeRev="
					                + command.getRevisionNumber());
				}
			}
			default:
			case TRANSACTION:
				throw new AssertionError();
			}
			
		}
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
		
		XID objectId = command.getChangedEntity().getObject();
		XStateWritableObject objectInTxn = ctxInTxn.getObject(objectId);
		XRevWritableObject objectBeforeCmd = ctxBeforeCmd.getObject(objectId);
		
		switch(command.getChangeType()) {
		case ADD:
			if(objectInTxn != null) {
				// command is invalid or doesn't change anything
				return command.isForced() ? CheckResult.successNoChange("tos.objectExists "
				        + command.getChangedEntity()) : CheckResult.failed("XModelCommand "
				        + command + " ADDs object which is already there");
			}
			// command is OK and adds a new object
			return CheckResult.successCreatedObject(command, change, ctxInTxn, inTransaction);
			
		case REMOVE:
			if(objectInTxn == null) {
				// command is invalid or doesn't change anything
				return command.isForced() ? CheckResult.successNoChange("!tos.objectExists")
				        : CheckResult.failed("XModelCommand REMOVE " + command
				                + " is invalid or doesn't change anything");
			} else if(objectBeforeCmd.getRevisionNumber() != command.getRevisionNumber()
			        && !command.isForced()) {
				// command is invalid
				return CheckResult.failed("Safe XModelCommand " + command
				        + " is invalid (revNr mismatch)");
			}
			// command is OK and removes an existing object
			return CheckResult.successRemovedObject(command, change, ctxInTxn, inTransaction
			        || !objectInTxn.isEmpty());
		default:
			throw new AssertionError("impossible type for model command " + command);
		}
	}
	
	private static CheckResult checkObjectCommand(XObjectCommand command, GaeChange change,
	        @NeverNull ContextBeforeCommand ctxBeforeCmd, ContextInTxn ctxInTxn,
	        boolean inTransaction) {
		
		if(!ctxInTxn.isModelExists()) {
			return CheckResult.failed("Model does not exist");
		}
		XID objectId = command.getChangedEntity().getObject();
		XStateWritableObject objectInTxn = ctxInTxn.getObject(objectId);
		XRevWritableObject objectBeforeCmd = ctxBeforeCmd.getObject(objectId);
		
		XyAssert.xyAssert(objectInTxn != null, "XObjectCommand is invalid: " + command
		        + ", object is null");
		assert objectInTxn != null;
		if(objectInTxn == null) {
			return CheckResult.failed("TOS says, object '" + command.getChangedEntity()
			        + "' does not exist");
		}
		XID fieldId = command.getFieldId();
		switch(command.getChangeType()) {
		case ADD:
			if(objectInTxn.hasField(fieldId)) {
				return command.isForced() ? CheckResult.successNoChange("tos '"
				        + objectInTxn.getAddress() + "' hasField '" + fieldId + "'") : CheckResult
				        .failed(command + " object has already field '" + fieldId + "' and foced="
				                + command.isForced());
			}
			// command is OK and adds a new field
			return CheckResult.successCreatedField(command, ctxInTxn, change, inTransaction);
			
		case REMOVE:
			XReadableField fieldBeforeCmd = objectBeforeCmd.getField(fieldId);
			if(fieldBeforeCmd == null) {
				// command is invalid or doesn't change anything
				return command.isForced() ? CheckResult.successNoChange("field==null")
				        : CheckResult.failed("XObjectCommand REMOVE '" + command
				                + "'is invalid or doesn't change anything, forced="
				                + command.isForced());
			}
			XyAssert.xyAssert(fieldBeforeCmd != null);
			XyAssert.xyAssert(objectBeforeCmd.hasField(fieldId));
			if(fieldBeforeCmd.getRevisionNumber() != command.getRevisionNumber()
			        && !command.isForced()) {
				// command is invalid
				return CheckResult.failed("Safe XObjectCommand REMOVE " + command
				        + " revNr mismatch");
			}
			// command is OK and removes an existing field
			return CheckResult.successRemovedField(command, change, ctxInTxn, inTransaction
			        || !fieldBeforeCmd.isEmpty());
			
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
		
		return result;
	}
	
	/**
	 * @param rc
	 * @return
	 */
	private static CheckResult checkRepositoryCommand(XRepositoryCommand rc, GaeChange change,
	        ContextBeforeCommand ctxBeforeCmd, ContextInTxn ctxInTxn) {
		
		GaeModelRevInfo info = ctxBeforeCmd.getRevisionManager().getInfo();
		boolean modelExists = info.isModelExists();
		
		switch(rc.getChangeType()) {
		case ADD:
			if(!ctxInTxn.isModelExists()) {
				return CheckResult.successCreatedModel(rc, change, ctxInTxn);
			} else if(rc.isForced()) {
				return CheckResult.successNoChange("Model exists");
			} else {
				return CheckResult
				        .failed("Safe RepositoryCommand ADD failed; model existed already");
			}
		case REMOVE:
			long modelRev = ctxBeforeCmd.getRevisionManager().getInfo()
			        .getLastStableSuccessChange();
			if((!modelExists || modelRev != rc.getRevisionNumber()) && !rc.isForced()) {
				return CheckResult.failed("Safe RepositoryCommand REMOVE failed. Reason: "
				        + (!modelExists ? "model is null" : "modelRevNr:" + modelRev + " cmdRevNr:"
				                + rc.getRevisionNumber() + " forced:" + rc.isForced()));
			} else if(modelExists) {
				log.debug("Removing model " + rc.getChangedEntity() + " " + modelRev);
				return CheckResult.successRemovedModel(rc, change, ctxInTxn);
			} else {
				return CheckResult.successNoChange("Model did not exist");
			}
			
		default:
			throw new AssertionError("XRepositoryCommand with unexpected type: " + rc);
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
