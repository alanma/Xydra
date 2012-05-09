package org.xydra.store.impl.gae.ng;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.xydra.annotations.CanBeNull;
import org.xydra.annotations.NeverNull;
import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.XX;
import org.xydra.base.change.XAtomicEvent;
import org.xydra.base.change.XFieldCommand;
import org.xydra.base.change.XFieldEvent;
import org.xydra.base.change.XModelCommand;
import org.xydra.base.change.XModelEvent;
import org.xydra.base.change.XObjectEvent;
import org.xydra.base.change.XRepositoryCommand;
import org.xydra.base.change.XRepositoryEvent;
import org.xydra.base.change.XTransaction;
import org.xydra.base.change.impl.memory.MemoryFieldEvent;
import org.xydra.base.change.impl.memory.MemoryModelEvent;
import org.xydra.base.change.impl.memory.MemoryObjectEvent;
import org.xydra.base.change.impl.memory.MemoryRepositoryEvent;
import org.xydra.base.rmof.XReadableField;
import org.xydra.base.rmof.XReadableModel;
import org.xydra.base.rmof.XReadableObject;
import org.xydra.base.rmof.XRevWritableField;
import org.xydra.base.rmof.impl.memory.SimpleObject;
import org.xydra.base.value.XValue;
import org.xydra.sharedutils.XyAssert;
import org.xydra.store.impl.gae.changes.GaeChange;
import org.xydra.store.impl.gae.changes.GaeChange.Status;


/**
 * Preliminary, unsaved result of executing a command.
 * 
 * Static methods gather all information required to generate events.
 * 
 */
class ExecutionResult {
	/**
	 * Failed preconditions.
	 * 
	 * @param explanation helps to debug
	 * @return the {@link ExecutionResult} with Status = failed
	 */
	public static ExecutionResult failed(String explanation) {
		GaeModelPersistenceNG.log.info("Command failed. Reason: '" + explanation + "'");
		return new ExecutionResult(Status.FailedPreconditions, explanation);
	}
	
	public static ExecutionResult successNoChange(String explanation) {
		GaeModelPersistenceNG.log.info("+++ NoChange. Reason: " + explanation);
		return new ExecutionResult(Status.SuccessNochange, Collections.EMPTY_LIST);
	}
	
	public @CanBeNull
	String getDebugHint() {
		return this.debugHint;
	}
	
	public static ExecutionResult successCreatedField(GaeChange change,
	        @NeverNull TentativeObjectSnapshot tos, XID fieldId, boolean inTransaction) {
		// calc event
		XObjectEvent event = MemoryObjectEvent.createAddEvent(change.getActorId(),
		        tos.getAddress(), fieldId, tos.getModelRevision(), tos.getRevisionNumber(),
		        inTransaction);
		// change object
		XRevWritableField field = tos.asRevWritableObject().createField(fieldId);
		field.setRevisionNumber(change.rev);
		tos.asRevWritableObject().setRevisionNumber(change.rev);
		tos.setModelRev(change.rev);
		XyAssert.xyAssert(tos.getField(fieldId).getRevisionNumber() == change.rev);
		
		return new ExecutionResult(Status.SuccessExecuted,
		        Collections.singletonList((XAtomicEvent)event));
	}
	
	/**
	 * @param command
	 * @param change
	 * @param modelRev before the change happened
	 * @return ..
	 */
	public static ExecutionResult successCreatedModel(XRepositoryCommand command, GaeChange change,
	        long modelRev) {
		// calc event
		XRepositoryEvent event = MemoryRepositoryEvent.createAddEvent(change.getActorId(),
		        command.getTarget(), command.getModelId(), modelRev, false);
		// no object to change
		return new ExecutionResult(Status.SuccessExecuted,
		        Collections.singletonList((XAtomicEvent)event));
	}
	
	/**
	 * @param command
	 * @param change
	 * @param tsm
	 * @param modelRev before the change happened
	 * @param inTransaction
	 * @return ..
	 */
	public static ExecutionResult successCreatedObject(XModelCommand command, GaeChange change,
	        ITentativeSnapshotManager tsm, long modelRev, boolean inTransaction) {
		// calc event
		XModelEvent event = MemoryModelEvent.createAddEvent(change.getActorId(),
		        command.getTarget(), command.getObjectId(), modelRev, inTransaction);
		// change object
		SimpleObject tosObject = new SimpleObject(command.getChangedEntity());
		tosObject.setRevisionNumber(change.rev);
		TentativeObjectSnapshot tos = new TentativeObjectSnapshot(tosObject,
		        command.getChangedEntity(), change.rev);
		tsm.saveTentativeObjectSnapshot(tos);
		
		return new ExecutionResult(Status.SuccessExecuted,
		        Collections.singletonList((XAtomicEvent)event));
	}
	
	public static ExecutionResult successRemovedField(GaeChange change,
	        @NeverNull TentativeObjectSnapshot tos, XID fieldId, boolean inTransaction) {
		XyAssert.xyAssert(tos.hasField(fieldId), "tos.hasField(%s) rev=%s", fieldId,
		        tos.getRevisionNumber());
		XReadableField field = tos.getField(fieldId);
		long fieldRevision = field.getRevisionNumber();
		boolean hasValue = field.getValue() != null;
		// calc events
		List<XAtomicEvent> events = new ArrayList<XAtomicEvent>();
		if(hasValue) {
			XFieldEvent valueRemoveEvent = MemoryFieldEvent.createRemoveEvent(change.getActorId(),
			        XX.resolveField(tos.getAddress(), fieldId), tos.getModelRevision(),
			        tos.getRevisionNumber(), fieldRevision, true, true);
			events.add(valueRemoveEvent);
		}
		XObjectEvent event = MemoryObjectEvent.createRemoveEvent(change.getActorId(),
		        tos.getAddress(), fieldId, tos.getModelRevision(), tos.getRevisionNumber(),
		        fieldRevision,
		        /* its only a txn if we indeed did more than 1 thing */
		        hasValue, false);
		events.add(event);
		// change object
		tos.asRevWritableObject().removeField(fieldId);
		tos.asRevWritableObject().setRevisionNumber(change.rev);
		tos.setModelRev(change.rev);
		return new ExecutionResult(Status.SuccessExecuted, events);
	}
	
	public static ExecutionResult successRemovedModel(GaeChange change, XRepositoryCommand command,
	        ITentativeSnapshotManager tsm) {
		List<XAtomicEvent> events = new ArrayList<XAtomicEvent>();
		
		XReadableModel modelSnapshot = tsm.getModelSnapshot();
		
		// implied events, delete all objects
		for(XID objectId : modelSnapshot) {
			
			// calc events
			XReadableObject object = modelSnapshot.getObject(objectId);
			for(XID fieldId : object) {
				XReadableField field = object.getField(fieldId);
				events.add(MemoryObjectEvent.createRemoveEvent(change.getActorId(),
				        XX.resolveObject(command.getChangedEntity(), objectId), fieldId,
				        object.getRevisionNumber(), field.getRevisionNumber(), true, true));
			}
			events.add(MemoryModelEvent.createRemoveEvent(change.getActorId(),
			        command.getChangedEntity(), objectId, modelSnapshot.getRevisionNumber(),
			        object.getRevisionNumber(), true, true));
			
			// change objects
			XAddress objectAddress = XX.resolveObject(modelSnapshot.getAddress(), objectId);
			TentativeObjectSnapshot tosRemoved = new TentativeObjectSnapshot(null, objectAddress,
			        change.rev);
			tsm.saveTentativeObjectSnapshot(tosRemoved);
		}
		
		XRepositoryEvent event = MemoryRepositoryEvent.createRemoveEvent(change.getActorId(),
		        command.getTarget(), command.getModelId(), change.rev,
		        /*
				 * final remove is only in a txn if there were other implied
				 * events
				 */
		        events.size() > 0);
		events.add(event);
		
		return new ExecutionResult(Status.SuccessExecuted, events);
	}
	
	public static ExecutionResult successRemovedObject(XModelCommand command, GaeChange change,
	        @NeverNull TentativeObjectSnapshot tos, @NeverNull ITentativeSnapshotManager tsm,
	        boolean inTransaction) {
		// calc events
		List<XAtomicEvent> events = new ArrayList<XAtomicEvent>();
		for(XID fieldId : tos) {
			XReadableField field = tos.getField(fieldId);
			events.add(MemoryObjectEvent.createRemoveEvent(change.getActorId(),
			        command.getChangedEntity(), fieldId, tos.getModelRevision(),
			        tos.getRevisionNumber(), field.getRevisionNumber(), true, true));
		}
		XModelEvent event = MemoryModelEvent.createRemoveEvent(change.getActorId(),
		        command.getTarget(), command.getObjectId(),
		        
		        tos.getModelRevision(), tos.getRevisionNumber(),
		        /*
				 * final remove is only in a txn if there were other implied
				 * events
				 */
		        events.size() > 0, false);
		events.add(event);
		
		// change object
		TentativeObjectSnapshot tosRemoved = new TentativeObjectSnapshot(null,
		        command.getChangedEntity(), change.rev);
		tsm.saveTentativeObjectSnapshot(tosRemoved);
		
		return new ExecutionResult(Status.SuccessExecuted, events);
	}
	
	public static ExecutionResult successTransaction(XTransaction transaction, GaeChange change,
	        List<ExecutionResult> results) {
		// compute aggregate result
		boolean anyChange = false;
		List<XAtomicEvent> events = new LinkedList<XAtomicEvent>();
		for(ExecutionResult atomicResult : results) {
			if(atomicResult.getStatus() == Status.SuccessExecuted) {
				anyChange = true;
			}
			events.addAll(atomicResult.getAtomicEvents());
		}
		
		return new ExecutionResult(anyChange ? Status.SuccessExecuted : Status.SuccessNochange,
		        events);
	}
	
	/**
	 * @param command
	 * @param change
	 * @param tos
	 * @param inTransaction
	 * @return ..
	 */
	public static ExecutionResult successValue(XFieldCommand command, GaeChange change,
	        TentativeObjectSnapshot tos, boolean inTransaction) {
		
		XValue value = command.getValue();
		XFieldEvent event;
		XRevWritableField field = tos.asRevWritableObject()
		        .getField(command.getTarget().getField());
		long oldFieldRev = field.getRevisionNumber();
		long oldObjectRev = tos.getRevisionNumber();
		long oldModelRev = tos.getModelRevision();
		// calc event
		switch(command.getChangeType()) {
		case ADD:
			XyAssert.xyAssert(value != null);
			event = MemoryFieldEvent.createAddEvent(change.getActorId(),
			        command.getChangedEntity(), value, oldModelRev, oldObjectRev, oldFieldRev,
			        inTransaction);
			break;
		case REMOVE:
			XyAssert.xyAssert(value == null);
			event = MemoryFieldEvent.createRemoveEvent(change.getActorId(),
			        command.getChangedEntity(), oldModelRev, oldObjectRev, oldFieldRev,
			        inTransaction, false);
			break;
		case CHANGE:
			XyAssert.xyAssert(value != null);
			event = MemoryFieldEvent.createChangeEvent(change.getActorId(),
			        command.getChangedEntity(), value, oldModelRev, oldObjectRev, oldFieldRev,
			        inTransaction);
			break;
		default:
			throw new AssertionError("cannot happen");
		}
		
		// change object
		field.setRevisionNumber(change.rev);
		tos.asRevWritableObject().setRevisionNumber(change.rev);
		tos.setModelRev(change.rev);
		field.setValue(value);
		
		return new ExecutionResult(Status.SuccessExecuted,
		        Collections.singletonList((XAtomicEvent)event));
	}
	
	private List<XAtomicEvent> events = new LinkedList<XAtomicEvent>();
	
	private Status status;
	
	private String debugHint;
	
	@Override
	public String toString() {
		return this.status + " " + (this.debugHint == null ? "" : this.debugHint) + " -> "
		        + this.events.size() + " events";
	}
	
	/* worker */
	public ExecutionResult(@NeverNull Status status, List<XAtomicEvent> events) {
		XyAssert.xyAssert(status != null);
		assert status != null;
		XyAssert.xyAssert(status.isCommitted());
		
		this.status = status;
		if(events != null) {
			this.events.addAll(events);
		}
	}
	
	public ExecutionResult(Status status, String explanation) {
		this(status, Collections.EMPTY_LIST);
		this.debugHint = explanation;
	}
	
	public List<XAtomicEvent> getAtomicEvents() {
		return this.events;
	}
	
	public Status getStatus() {
		return this.status;
	}
	
}
