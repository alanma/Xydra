package org.xydra.store.impl.gae.ng;

import org.xydra.annotations.CanBeNull;
import org.xydra.annotations.NeverNull;
import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.change.XFieldCommand;
import org.xydra.base.change.XModelCommand;
import org.xydra.base.change.XObjectCommand;
import org.xydra.base.change.XRepositoryCommand;
import org.xydra.base.change.XTransaction;
import org.xydra.sharedutils.XyAssert;
import org.xydra.store.impl.gae.changes.GaeChange;
import org.xydra.store.impl.gae.changes.GaeChange.Status;


public class CheckResult {
	
	/**
	 * Failed preconditions.
	 * 
	 * @param explanation helps to debug
	 * @return the {@link CheckResult} with Status = failed
	 */
	public static CheckResult failed(String explanation) {
		GaeModelPersistenceNG.log.info("Command failed. Reason: '" + explanation + "'");
		return new CheckResult(Status.FailedPreconditions, null, null, false, explanation);
	}
	
	public static CheckResult successCreatedField(XObjectCommand command, ContextInTxn ctxInTxn,
	        GaeChange change, boolean inTransaction) {
		ctxInTxn.getObject(command.getChangedEntity().getObject()).createField(
		        command.getChangedEntity().getField());
		return new CheckResult(Status.SuccessExecuted, change.getActorId(), ctxInTxn,
		        inTransaction, null);
	}
	
	/**
	 * @param command
	 * @param change
	 * @param ctxInTxn
	 * @return ..
	 */
	public static CheckResult successCreatedModel(XRepositoryCommand command, GaeChange change,
	        ContextInTxn ctxInTxn) {
		ctxInTxn.setModelExists(true);
		return new CheckResult(Status.SuccessExecuted, change.getActorId(), ctxInTxn,
		/* Add/remove model is never part of a txn */
		false, null);
	}
	
	/**
	 * @param command
	 * @param change
	 * @param ctxInTxn
	 * @param inTransaction
	 * @return ..
	 */
	public static CheckResult successCreatedObject(XModelCommand command, GaeChange change,
	        ContextInTxn ctxInTxn, boolean inTransaction) {
		ctxInTxn.createObject(command.getChangedEntity().getObject());
		return new CheckResult(Status.SuccessExecuted, change.getActorId(), ctxInTxn,
		        inTransaction, null);
	}
	
	public static CheckResult successNoChange(String explanation) {
		GaeModelPersistenceNG.log.info("+++ NoChange. Reason: " + explanation);
		return new CheckResult(Status.SuccessNochange, null, null, false, explanation);
	}
	
	public static CheckResult successRemovedField(XObjectCommand objectCommand, GaeChange change,
	        ContextInTxn ctxInTxn, boolean inTransaction) {
		ctxInTxn.getObject(objectCommand.getChangedEntity().getObject()).removeField(
		        objectCommand.getChangedEntity().getField());
		return new CheckResult(Status.SuccessExecuted, change.getActorId(), ctxInTxn,
		        inTransaction, null);
	}
	
	public static CheckResult successRemovedModel(XRepositoryCommand command, GaeChange change,
	        ContextInTxn ctxInTxn) {
		ctxInTxn.setModelExists(false);
		return new CheckResult(Status.SuccessExecuted, change.getActorId(), ctxInTxn,
		/* Add / remove model is never part of a txn */
		false, null);
	}
	
	public static CheckResult successRemovedObject(XModelCommand command, GaeChange change,
	        ContextInTxn ctxInTxn, boolean inTransaction) {
		ctxInTxn.removeObject(command.getChangedEntity().getObject());
		return new CheckResult(Status.SuccessExecuted, change.getActorId(), ctxInTxn,
		        inTransaction, null);
	}
	
	/**
	 * 
	 * @param transaction
	 * @param change
	 * @param ctxInTxn
	 * @return ..
	 */
	public static CheckResult successTransaction(XTransaction transaction, GaeChange change,
	        ContextInTxn ctxInTxn) {
		return new CheckResult(ctxInTxn.hasChanges() ? Status.SuccessExecuted
		        : Status.SuccessNochange, change.getActorId(), ctxInTxn, true, null);
	}
	
	/**
	 * @param command
	 * @param change
	 * @param ctxInTxn
	 * @param inTransaction
	 * @return ..
	 */
	public static CheckResult successValue(XFieldCommand command, GaeChange change,
	        ContextInTxn ctxInTxn, boolean inTransaction) {
		XAddress a = command.getChangedEntity();
		ctxInTxn.getObject(a.getObject()).getField(a.getField()).setValue(command.getValue());
		return new CheckResult(Status.SuccessExecuted, change.getActorId(), ctxInTxn,
		        inTransaction, null);
	}
	
	private String debugHint;
	
	private ContextInTxn ctxInTxn;
	
	private Status status;
	
	private XID actorId;
	
	private boolean inTransaction;
	
	/**
	 * @param status
	 * @param actorId @CanBeNull if command failed
	 * @param contextInTxn @CanBeNull if command failed
	 * @param inTransaction
	 * @param explanation
	 */
	public CheckResult(@NeverNull Status status, XID actorId, @CanBeNull ContextInTxn contextInTxn,
	        boolean inTransaction, @CanBeNull String explanation) {
		XyAssert.xyAssert(status != null);
		assert status != null;
		XyAssert.xyAssert(status.isCommitted(), status);
		this.status = status;
		this.actorId = actorId;
		this.ctxInTxn = contextInTxn;
		this.inTransaction = inTransaction;
		this.debugHint = explanation;
	}
	
	public @CanBeNull
	String getDebugHint() {
		return this.debugHint;
	}
	
	public ContextInTxn getExecutionContextInTxn() {
		return this.ctxInTxn;
	}
	
	public Status getStatus() {
		return this.status;
	}
	
	@Override
	public String toString() {
		return this.status + " " + (this.debugHint == null ? "" : this.debugHint) + " -> TBD";
	}
	
	public XID getActorId() {
		return this.actorId;
	}
	
	public boolean inTransaction() {
		return this.inTransaction;
	}
	
}
