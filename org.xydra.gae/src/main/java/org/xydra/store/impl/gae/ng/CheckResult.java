package org.xydra.store.impl.gae.ng;

import org.xydra.annotations.CanBeNull;
import org.xydra.annotations.NeverNull;
import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.change.XFieldCommand;
import org.xydra.base.change.XModelCommand;
import org.xydra.base.change.XObjectCommand;
import org.xydra.base.change.XRepositoryCommand;
import org.xydra.base.change.XTransaction;
import org.xydra.base.rmof.XRevWritableField;
import org.xydra.base.rmof.XStateWritableField;
import org.xydra.base.rmof.XStateWritableObject;
import org.xydra.sharedutils.XyAssert;
import org.xydra.store.impl.gae.changes.GaeChange;
import org.xydra.store.impl.gae.changes.GaeChange.Status;

/**
 * This class changes the TOS (Tentative Object Snapshot)
 *
 * @author xamde
 */
public class CheckResult {

	/**
	 * Failed preconditions.
	 *
	 * @param explanation
	 *            helps to debug
	 * @return the {@link CheckResult} with Status = failed
	 */
	public static CheckResult failed(final String explanation) {
		GaeModelPersistenceNG.log.info("Command failed. Reason: '" + explanation + "'");
		return new CheckResult(Status.FailedPreconditions, null, null, false, explanation);
	}

	public static CheckResult successCreatedField(final XObjectCommand command, final long revBeforeTxn,
			final ContextInTxn ctxInTxn, final GaeChange change, final boolean inTransaction) {
		final XStateWritableObject objectInTxn = ctxInTxn.getObject(command.getChangedEntity()
				.getObject());
		final XStateWritableField newField = objectInTxn.createField(command.getChangedEntity()
				.getField());
		if (newField instanceof XRevWritableField) {
			((XRevWritableField) newField).setRevisionNumber(revBeforeTxn);
		}
		return new CheckResult(Status.SuccessExecuted, change.getActorId(), ctxInTxn,
				inTransaction, null);
	}

	/**
	 * @param command
	 * @param change
	 * @param ctxInTxn
	 * @return ..
	 */
	public static CheckResult successCreatedModel(final XRepositoryCommand command, final GaeChange change,
			final ContextInTxn ctxInTxn) {
		ctxInTxn.setExists(true);
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
	public static CheckResult successCreatedObject(final XModelCommand command, final GaeChange change,
			final ContextInTxn ctxInTxn, final boolean inTransaction) {
		ctxInTxn.createObject(command.getChangedEntity().getObject());
		return new CheckResult(Status.SuccessExecuted, change.getActorId(), ctxInTxn,
				inTransaction, null);
	}

	public static CheckResult successNoChange(final String explanation) {
		GaeModelPersistenceNG.log.info("+++ NoChange. Reason: " + explanation);
		return new CheckResult(Status.SuccessNochange, null, null, false, explanation);
	}

	public static CheckResult successRemovedField(final XObjectCommand objectCommand, final GaeChange change,
			final ContextInTxn ctxInTxn, final boolean inTransaction) {
		ctxInTxn.getObject(objectCommand.getChangedEntity().getObject()).removeField(
				objectCommand.getChangedEntity().getField());
		return new CheckResult(Status.SuccessExecuted, change.getActorId(), ctxInTxn,
				inTransaction, null);
	}

	public static CheckResult successRemovedModel(final XRepositoryCommand command, final GaeChange change,
			final ContextInTxn ctxInTxn) {
		ctxInTxn.setExists(false);
		return new CheckResult(Status.SuccessExecuted, change.getActorId(), ctxInTxn,
		/* Add / remove model is never part of a txn */
		false, null);
	}

	public static CheckResult successRemovedObject(final XModelCommand command, final GaeChange change,
			final ContextInTxn ctxInTxn, final boolean inTransaction) {
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
	public static CheckResult successTransaction(final XTransaction transaction, final GaeChange change,
			final ContextInTxn ctxInTxn) {
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
	public static CheckResult successValue(final XFieldCommand command, final GaeChange change,
			final ContextInTxn ctxInTxn, final boolean inTransaction) {
		final XAddress a = command.getChangedEntity();
		ctxInTxn.getObject(a.getObject()).getField(a.getField()).setValue(command.getValue());
		return new CheckResult(Status.SuccessExecuted, change.getActorId(), ctxInTxn,
				inTransaction, null);
	}

	private final String debugHint;

	private final ContextInTxn ctxInTxn;

	private final Status status;

	private final XId actorId;

	private final boolean inTransaction;

	/**
	 * @param status
	 * @param actorId
	 *            @CanBeNull if command failed
	 * @param contextInTxn
	 *            @CanBeNull if command failed
	 * @param inTransaction
	 * @param explanation
	 */
	public CheckResult(@NeverNull final Status status, final XId actorId, @CanBeNull final ContextInTxn contextInTxn,
			final boolean inTransaction, @CanBeNull final String explanation) {
		XyAssert.xyAssert(status != null);
		assert status != null;
		XyAssert.xyAssert(status.isCommitted(), status);
		this.status = status;
		this.actorId = actorId;
		this.ctxInTxn = contextInTxn;
		this.inTransaction = inTransaction;
		this.debugHint = explanation;
	}

	public @CanBeNull String getDebugHint() {
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
		return this.status + " " + (this.debugHint == null ? "" : this.debugHint) + " actor:"
				+ this.actorId + " inTxn?" + this.inTransaction + " see also ctxInTxn";
	}

	public XId getActorId() {
		return this.actorId;
	}

	public boolean inTransaction() {
		return this.inTransaction;
	}

}
