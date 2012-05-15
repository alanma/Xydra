package org.xydra.store.impl.gae.ng;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.xydra.annotations.NeverNull;
import org.xydra.base.change.XAtomicEvent;
import org.xydra.sharedutils.XyAssert;
import org.xydra.store.impl.gae.changes.GaeChange.Status;


/**
 * Preliminary, unsaved result of executing a command.
 * 
 * Static methods gather all information required to generate events.
 * 
 */
class ExecutionResult {
	
	public static ExecutionResult createEventsFrom(CheckResult checkResult,
	        @NeverNull ContextBeforeCommand ctxBeforeCommand) {
		XyAssert.xyAssert(ctxBeforeCommand != null);
		
		switch(checkResult.getStatus()) {
		case FailedPreconditions:
		case FailedTimeout:
		case SuccessNochange:
			return new ExecutionResult(checkResult.getStatus(), Collections.EMPTY_LIST,
			        checkResult.getDebugHint());
		case SuccessExecuted:
			List<XAtomicEvent> events = checkResult.getExecutionContextInTxn().toEvents(
			        checkResult.getActorId(), ctxBeforeCommand, checkResult.inTransaction());
			return new ExecutionResult(checkResult.getStatus(), events, null);
		default:
		case Creating:
			throw new AssertionError();
		}
	}
	
	// /**
	// * Failed preconditions.
	// *
	// * @param explanation helps to debug
	// * @return the {@link ExecutionResult} with Status = failed
	// */
	// public static ExecutionResult failed(String explanation) {
	// GaeModelPersistenceNG.log.info("Command failed. Reason: '" + explanation
	// + "'");
	// return new ExecutionResult(Status.FailedPreconditions, (XEvent)null,
	// explanation);
	// }
	//
	// public static ExecutionResult successNoChange(String explanation) {
	// GaeModelPersistenceNG.log.info("+++ NoChange. Reason: " + explanation);
	// return new ExecutionResult(Status.SuccessNochange, (XEvent)null, null);
	// }
	//
	// public static ExecutionResult successCreatedField(GaeChange change, XID
	// fieldId,
	// TentativeObjectState outerTos, TentativeObjectState innerTos,
	// ContextInTxn innerContext, boolean inTransaction) {
	// XyAssert.xyAssert(innerTos.isObjectExists(), "innerTos: object " +
	// outerTos.getAddress()
	// + " does not exist");
	// // calc event
	// XObjectEvent event =
	// MemoryObjectEvent.createAddEvent(change.getActorId(),
	// innerTos.getAddress(), fieldId, innerTos.getModelRevision(),
	// innerTos.getRevisionNumber(), inTransaction);
	// // change object
	// XRevWritableField innerField =
	// innerTos.asRevWritableObject().createField(fieldId);
	// innerField.setRevisionNumber(change.rev);
	// innerTos.asRevWritableObject().setRevisionNumber(change.rev);
	// innerTos.setModelRev(change.rev);
	// innerContext.saveTentativeObjectState(innerTos);
	// XyAssert.xyAssert(innerTos.getField(fieldId).getRevisionNumber() ==
	// change.rev);
	//
	// return new ExecutionResult(Status.SuccessExecuted, event, null);
	// }
	//
	// /**
	// * @param command
	// * @param change
	// * @param modelRev before the change happened
	// * @return ..
	// */
	// public static ExecutionResult successCreatedModel(XRepositoryCommand
	// command, GaeChange change,
	// long modelRev) {
	// // calc event
	// XRepositoryEvent event =
	// MemoryRepositoryEvent.createAddEvent(change.getActorId(),
	// command.getTarget(), command.getModelId(), modelRev, false);
	// // no object to change
	// return new ExecutionResult(Status.SuccessExecuted, event, null);
	// }
	//
	// /**
	// * @param command
	// * @param change
	// * @param innerContext
	// * @param modelRev before the change happened
	// * @param inTransaction
	// * @return ..
	// */
	// public static ExecutionResult successCreatedObject(XModelCommand command,
	// GaeChange change,
	// ContextInTxn innerContext, long modelRev, boolean inTransaction) {
	// // calc event
	// XModelEvent event = MemoryModelEvent.createAddEvent(change.getActorId(),
	// command.getTarget(), command.getObjectId(), modelRev, inTransaction);
	// // change object
	// SimpleObject tosObject = new SimpleObject(command.getChangedEntity());
	// tosObject.setRevisionNumber(change.rev);
	// TentativeObjectState tos = new TentativeObjectState(tosObject,
	// command.getChangedEntity(),
	// change.rev);
	// innerContext.saveTentativeObjectState(tos);
	//
	// return new ExecutionResult(Status.SuccessExecuted, event, null);
	// }
	//
	// public static ExecutionResult successRemovedField(GaeChange change, XID
	// fieldId,
	//
	// TentativeObjectState outerTos, TentativeObjectState innerTos,
	//
	// boolean inTransaction) {
	// // calc preconditions
	// XyAssert.xyAssert(outerTos.hasField(fieldId), "tos.hasField(%s) rev=%s",
	// fieldId,
	// outerTos.getRevisionNumber());
	// XReadableField outerField = outerTos.getField(fieldId);
	// long fieldRevision = outerField.getRevisionNumber();
	// boolean hasValue = outerField.getValue() != null;
	// // calc events
	// List<XAtomicEvent> events = new ArrayList<XAtomicEvent>();
	// if(hasValue) {
	// XFieldEvent valueRemoveEvent =
	// MemoryFieldEvent.createRemoveEvent(change.getActorId(),
	// XX.resolveField(outerTos.getAddress(), fieldId),
	// outerTos.getModelRevision(),
	// outerTos.getRevisionNumber(), fieldRevision, true, true);
	// events.add(valueRemoveEvent);
	// }
	// XObjectEvent event =
	// MemoryObjectEvent.createRemoveEvent(change.getActorId(),
	// outerTos.getAddress(), fieldId, outerTos.getModelRevision(),
	// outerTos.getRevisionNumber(), fieldRevision,
	// /* its only a txn if we indeed did more than 1 thing */
	// hasValue, false);
	// events.add(event);
	// // change object
	// innerTos.asRevWritableObject().removeField(fieldId);
	// innerTos.asRevWritableObject().setRevisionNumber(change.rev);
	// innerTos.setModelRev(change.rev);
	// return new ExecutionResult(Status.SuccessExecuted, events, null);
	// }
	//
	// public static ExecutionResult successRemovedModel(GaeChange change,
	// XRepositoryCommand command,
	// ContextBeforeCommand ctxBeforeCmd, ContextInTxn innerContext) {
	// List<XAtomicEvent> events = new LinkedList<XAtomicEvent>();
	//
	// XReadableModel outerModelSnapshot = ctxBeforeCmd.getModelSnapshot();
	//
	// // implied events, delete all objects
	// for(XID objectId : outerModelSnapshot) {
	//
	// // calc events
	// XAddress objectAddress =
	// XX.resolveObject(outerModelSnapshot.getAddress(), objectId);
	// XReadableObject innerTos =
	// innerContext.getTentativeObjectState(objectAddress);
	// XReadableObject outerTos =
	// ctxBeforeCmd.getTentativeObjectState(objectAddress);
	// for(XID fieldId : innerTos) {
	// XReadableField innerField = innerTos.getField(fieldId);
	// XReadableField outerField = outerTos.getField(fieldId);
	//
	// if(!innerField.isEmpty()) {
	// events.add(MemoryFieldEvent.createRemoveEvent(change.getActorId(),
	// XX.resolveField(command.getChangedEntity(), objectId, fieldId),
	// outerModelSnapshot.getRevisionNumber(), outerTos.getRevisionNumber(),
	// outerField.getRevisionNumber(), true, true));
	// }
	// events.add(MemoryObjectEvent.createRemoveEvent(change.getActorId(),
	// XX.resolveObject(command.getChangedEntity(), objectId), fieldId,
	// outerTos.getRevisionNumber(), outerField.getRevisionNumber(), true,
	// true));
	// }
	// events.add(MemoryModelEvent.createRemoveEvent(change.getActorId(),
	// command.getChangedEntity(), objectId,
	// outerModelSnapshot.getRevisionNumber(),
	// outerTos.getRevisionNumber(), true, true));
	//
	// // change objects
	// TentativeObjectState tosRemoved = new TentativeObjectState(null,
	// objectAddress,
	// change.rev);
	// ctxBeforeCmd.saveTentativeObjectState(tosRemoved);
	// }
	//
	// XRepositoryEvent event =
	// MemoryRepositoryEvent.createRemoveEvent(change.getActorId(),
	// command.getTarget(), command.getModelId(), change.rev,
	// /*
	// * final remove is only in a txn if there were other implied
	// * events
	// */
	// events.size() > 0);
	// events.add(event);
	//
	// return new ExecutionResult(Status.SuccessExecuted, events, null);
	// }
	//
	// public static ExecutionResult successRemovedObject(XModelCommand command,
	// GaeChange change,
	// TentativeObjectState outerTos, TentativeObjectState innerTos,
	// ContextInTxn outerContect, ContextInTxn innerContext, boolean
	// inTransaction) {
	// // calc events
	// List<XAtomicEvent> events = new ArrayList<XAtomicEvent>();
	// for(XID fieldId : innerTos) {
	// XReadableField outerField = outerTos.getField(fieldId);
	// events.add(MemoryObjectEvent.createRemoveEvent(change.getActorId(),
	// command.getChangedEntity(), fieldId, outerTos.getModelRevision(),
	// outerTos.getRevisionNumber(), outerField.getRevisionNumber(), true,
	// true));
	// }
	// XModelEvent event =
	// MemoryModelEvent.createRemoveEvent(change.getActorId(),
	// command.getTarget(), command.getObjectId(), outerTos.getModelRevision(),
	// outerTos.getRevisionNumber(),
	// /*
	// * final remove is only in a txn if there were other implied
	// * events
	// */
	// events.size() > 0, false);
	// events.add(event);
	//
	// // change object
	// TentativeObjectState tosRemoved = new TentativeObjectState(null,
	// command.getChangedEntity(), change.rev);
	// innerContext.saveTentativeObjectState(tosRemoved);
	//
	// return new ExecutionResult(Status.SuccessExecuted, events, null);
	// }
	//
	// /**
	// * @param transaction
	// * @param change
	// * @param results
	// * @return ..
	// */
	// public static ExecutionResult successTransaction(XTransaction
	// transaction, GaeChange change,
	// List<ExecutionResult> results) {
	// XyAssert.xyAssert(results.size() > 0);
	//
	// // compute aggregate result
	// List<XAtomicEvent> events = new LinkedList<XAtomicEvent>();
	// boolean anyChange = false;
	// for(ExecutionResult partialResult : results) {
	// if(partialResult.getStatus() == Status.SuccessExecuted) {
	// anyChange = true;
	// }
	//
	// List<XAtomicEvent> partialEvents = partialResult.getEvents();
	// events.addAll(partialEvents);
	// }
	//
	// return new ExecutionResult(anyChange ? Status.SuccessExecuted :
	// Status.SuccessNochange,
	// events, null);
	// }
	//
	// /**
	// * @param command
	// * @param change
	// * @param outerTos
	// * @param innerTos
	// * @param outerContect to read from
	// * @param innerContext to change
	// * @param inTransaction
	// * @return ..
	// */
	// public static ExecutionResult successValue(XFieldCommand command,
	// GaeChange change,
	// TentativeObjectState outerTos, TentativeObjectState innerTos,
	// ContextInTxn outerContect, ContextInTxn innerContext, boolean
	// inTransaction) {
	//
	// XValue value = command.getValue();
	// XFieldEvent event;
	// XRevWritableField outerField = outerTos.asRevWritableObject().getField(
	// command.getTarget().getField());
	// long oldFieldRev = outerField.getRevisionNumber();
	// long oldObjectRev = outerTos.getRevisionNumber();
	// long oldModelRev = outerTos.getModelRevision();
	// // calc event
	// switch(command.getChangeType()) {
	// case ADD:
	// XyAssert.xyAssert(value != null);
	// event = MemoryFieldEvent.createAddEvent(change.getActorId(),
	// command.getChangedEntity(), value, oldModelRev, oldObjectRev,
	// oldFieldRev,
	// inTransaction);
	// break;
	// case REMOVE:
	// XyAssert.xyAssert(value == null);
	// event = MemoryFieldEvent.createRemoveEvent(change.getActorId(),
	// command.getChangedEntity(), oldModelRev, oldObjectRev, oldFieldRev,
	// inTransaction, false);
	// break;
	// case CHANGE:
	// XyAssert.xyAssert(value != null);
	// event = MemoryFieldEvent.createChangeEvent(change.getActorId(),
	// command.getChangedEntity(), value, oldModelRev, oldObjectRev,
	// oldFieldRev,
	// inTransaction);
	// break;
	// default:
	// throw new AssertionError("cannot happen");
	// }
	//
	// // change object
	// XRevWritableField innerField = innerTos.asRevWritableObject().getField(
	// command.getTarget().getField());
	// innerField.setRevisionNumber(change.rev);
	// innerField.setValue(value);
	// innerTos.asRevWritableObject().setRevisionNumber(change.rev);
	// innerTos.setModelRev(change.rev);
	// innerContext.saveTentativeObjectState(innerTos);
	//
	// return new ExecutionResult(Status.SuccessExecuted, event, null);
	// }
	
	private List<XAtomicEvent> events = new LinkedList<XAtomicEvent>();
	
	private Status status;
	
	private String debugHint;
	
	@Override
	public String toString() {
		return this.status
		        + " "
		        + (this.debugHint == null ? "" : this.debugHint)
		        + " -> "
		        + (this.events.size() == 1 ? this.events.get(0) + " event" : this.events.size()
		                + " events");
	}
	
	/**
	 * @param status
	 * @param events @NeverNull if status != SuccessExecuted.
	 * @param explanation @CanBeNull if status != failed
	 */
	public ExecutionResult(Status status, List<XAtomicEvent> events, String explanation) {
		XyAssert.xyAssert(status != null);
		assert status != null;
		XyAssert.xyAssert(status.isCommitted());
		
		this.status = status;
		this.events = events;
		this.debugHint = explanation;
	}
	
	public List<XAtomicEvent> getEvents() {
		return this.events;
	}
	
	public Status getStatus() {
		return this.status;
	}
	
	public String getDebugHint() {
		return this.debugHint;
	}
	
}
