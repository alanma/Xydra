package org.xydra.core.model.impl.memory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.xydra.annotations.ReadOperation;
import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.XType;
import org.xydra.base.XX;
import org.xydra.base.change.ChangeType;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XEvent;
import org.xydra.base.change.XFieldCommand;
import org.xydra.base.change.XObjectCommand;
import org.xydra.base.change.XObjectEvent;
import org.xydra.base.change.XReversibleFieldEvent;
import org.xydra.base.change.XTransaction;
import org.xydra.base.change.impl.memory.MemoryObjectCommand;
import org.xydra.base.change.impl.memory.MemoryObjectEvent;
import org.xydra.base.change.impl.memory.MemoryReversibleFieldEvent;
import org.xydra.base.rmof.XReadableModel;
import org.xydra.base.rmof.XRevWritableField;
import org.xydra.base.rmof.XRevWritableObject;
import org.xydra.base.rmof.impl.memory.SimpleObject;
import org.xydra.core.XCopyUtils;
import org.xydra.core.model.XChangeLog;
import org.xydra.core.model.XChangeLogState;
import org.xydra.core.model.XField;
import org.xydra.core.model.XLocalChangeCallback;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;
import org.xydra.core.model.XRepository;
import org.xydra.core.model.delta.ReadableModelWithOneObject;


/**
 * An implementation of {@link XObject}.
 * 
 * @author voelkel
 * @author Kaidel
 * 
 */
public class MemoryObject extends SynchronizesChangesImpl implements XObject {
	
	private static final long serialVersionUID = -808702139986657842L;
	
	/** The father-model of this MemoryObject */
	private final MemoryModel father;
	
	private final Map<XID,MemoryField> loadedFields = new HashMap<XID,MemoryField>();
	
	private final XRevWritableObject state;
	
	/**
	 * Creates a new MemoryObject with the given {@link MemoryModel} as its
	 * father.
	 * 
	 * @param parent The father-{@link MemoryModel} of this MemoryObject.
	 * @param eventQueue The {@link MemoryEventManager} which will be used by
	 *            this MemoryObject.
	 * @param objectState A {@link XRevWritableObject} representing the initial
	 *            state of this object. The {@link XObject} will continue using
	 *            this state object, so it must not be modified directly after
	 *            wrapping it in an {@link XObject}.
	 */
	protected MemoryObject(MemoryModel parent, MemoryEventManager eventQueue,
	        XRevWritableObject objectState) {
		super(eventQueue);
		assert eventQueue != null;
		
		if(objectState == null) {
			throw new IllegalArgumentException("objectState may not be null");
		}
		this.state = objectState;
		
		if(parent == null && objectState.getAddress().getModel() != null) {
			throw new IllegalArgumentException("must load object through containing model");
		}
		this.father = parent;
	}
	
	/**
	 * Creates a new MemoryObject without a father-{@link XModel}.
	 * 
	 * @param actorId The actor to be used in events generated by this object.
	 * @param passwordHash
	 * @param objectId The {@link XID} for this MemoryObject
	 */
	public MemoryObject(XID actorId, String passwordHash, XID objectId) {
		this(actorId, passwordHash, new SimpleObject(XX.toAddress(null, null, objectId, null)));
	}
	
	/**
	 * Creates a new MemoryObject without a father-{@link XModel}.
	 * 
	 * @param actorId The actor to be used in events generated by this object.
	 * @param passwordHash
	 * @param objectState A {@link XRevWritableObject} representing the initial
	 *            state of this object. The {@link XObject} will continue using
	 *            this state object, so it must not be modified directly after
	 *            wrapping it in an {@link XObject}.
	 */
	public MemoryObject(XID actorId, String passwordHash, XRevWritableObject objectState) {
		this(actorId, passwordHash, objectState, createChangeLog(objectState));
	}
	
	private static XChangeLogState createChangeLog(XRevWritableObject objectState) {
		XChangeLogState log = new MemoryChangeLogState(objectState.getAddress());
		log.setFirstRevisionNumber(objectState.getRevisionNumber() + 1);
		return log;
	}
	
	/**
	 * Creates a new MemoryObject without a father-{@link XModel}.
	 * 
	 * @param actorId The actor to be used in events generated by this object.
	 * @param passwordHash
	 * @param objectState A {@link XRevWritableObject} representing the initial
	 *            state of this object. The {@link XObject} will continue using
	 *            this state object, so it must not be modified directly after
	 *            wrapping it in an {@link XObject}.
	 * @param log
	 */
	public MemoryObject(XID actorId, String passwordHash, XRevWritableObject objectState,
	        XChangeLogState log) {
		this(null, createEventQueue(actorId, passwordHash, objectState, log), objectState);
	}
	
	private static MemoryEventManager createEventQueue(XID actorId, String passwordHash,
	        XRevWritableObject objectState, XChangeLogState logState) {
		if(logState.getCurrentRevisionNumber() != objectState.getRevisionNumber()) {
			throw new IllegalArgumentException("object state and log revision mismatch");
		}
		MemoryChangeLog log = new MemoryChangeLog(logState);
		return new MemoryEventManager(actorId, passwordHash, log, objectState.getRevisionNumber());
	}
	
	/**
	 * @throws IllegalStateException if this method is called after this
	 *             MemoryObject was already removed
	 */
	@Override
	protected void checkRemoved() throws IllegalStateException {
		if(this.removed) {
			throw new IllegalStateException("this object has been removed: " + getAddress());
		}
	}
	
	@Override
	protected void checkSync() {
		if(this.father != null) {
			throw new IllegalStateException(
			        "an object that is part of a model cannot be rolled abck / synchronized individualy");
		}
	}
	
	@Override
	public MemoryField createField(XID fieldId) {
		
		XObjectCommand command = MemoryObjectCommand.createAddCommand(getAddress(), true, fieldId);
		
		// synchronize so that return is never null if command succeeded
		synchronized(this.eventQueue) {
			long result = executeObjectCommand(command);
			MemoryField field = getField(fieldId);
			assert result == XCommand.FAILED || field != null;
			return field;
		}
	}
	
	/**
	 * Create a new field, increase revision (if not in a transaction) and
	 * enqueue the corresponding event.
	 * 
	 * The caller is responsible for handling synchronization, for checking that
	 * this object has not been removed and for checking that the field doesn't
	 * already exist.
	 */
	protected MemoryField createFieldInternal(XID fieldId) {
		
		assert !hasField(fieldId);
		
		boolean inTrans = this.eventQueue.transactionInProgess;
		
		MemoryField field = null;
		Orphans orphans = this.eventQueue.orphans;
		if(orphans != null) {
			XAddress fieldAddr = XX.resolveField(getAddress(), fieldId);
			field = orphans.fields.remove(fieldAddr);
		}
		if(field == null) {
			XRevWritableField fieldState = this.state.createField(fieldId);
			assert getAddress().contains(fieldState.getAddress());
			field = new MemoryField(this, this.eventQueue, fieldState);
		} else {
			this.state.addField(field.getState());
		}
		
		assert field.getObject() == this;
		
		this.loadedFields.put(field.getId(), field);
		
		XObjectEvent event = MemoryObjectEvent
		        .createAddEvent(this.eventQueue.getActor(), getAddress(), field.getId(),
		                getModelRevisionNumber(), getRevisionNumber(), inTrans);
		
		this.eventQueue.enqueueObjectEvent(this, event);
		
		// event propagation and revision number increasing happens after
		// all events were successful
		if(!inTrans) {
			
			field.incrementRevision();
			
			// propagate events
			this.eventQueue.sendEvents();
			
		}
		
		return field;
	}
	
	@Override
	protected MemoryObject createObjectInternal(XID objectId) {
		throw new AssertionError("object transactions cannot create objects");
	}
	
	/**
	 * Deletes the state information of this MemoryObject from the currently
	 * used persistence layer
	 * 
	 * @param transaction
	 */
	protected void delete() {
		for(XID fieldId : this) {
			MemoryField field = getField(fieldId);
			field.delete();
		}
		for(XID fieldId : this.loadedFields.keySet()) {
			this.state.removeField(fieldId);
		}
		this.loadedFields.clear();
		this.removed = true;
	}
	
	/**
	 * Builds a transaction that first removes the value of the given field and
	 * then the given field itself.
	 * 
	 * @param actor The actor for this transaction
	 * @param field The field which should be removed by the transaction
	 * @param inTrans true, if the removal of this {@link MemoryField} occurs
	 *            during an {@link XTransaction}.
	 * @param implied true if this object is also removed in the same
	 *            transaction
	 */
	protected void enqueueFieldRemoveEvents(XID actor, MemoryField field, boolean inTrans,
	        boolean implied) {
		
		if(field == null) {
			throw new NullPointerException("field must not be null");
		}
		
		long modelRev = getModelRevisionNumber();
		
		if(field.getValue() != null) {
			assert inTrans;
			XReversibleFieldEvent event = MemoryReversibleFieldEvent.createRemoveEvent(actor,
			        field.getAddress(), field.getValue(), modelRev, getRevisionNumber(),
			        field.getRevisionNumber(), inTrans, true);
			this.eventQueue.enqueueFieldEvent(field, event);
		}
		
		XObjectEvent event = MemoryObjectEvent.createRemoveEvent(actor, getAddress(),
		        field.getId(), modelRev, getRevisionNumber(), field.getRevisionNumber(), inTrans,
		        implied);
		this.eventQueue.enqueueObjectEvent(this, event);
		
	}
	
	@ReadOperation
	@Override
	public boolean equals(Object object) {
		synchronized(this.eventQueue) {
			return super.equals(object);
		}
	}
	
	@Override
	public long executeCommand(XCommand command) {
		return executeCommand(command, null);
	}
	
	@Override
	public long executeCommand(XCommand command, XLocalChangeCallback callback) {
		if(command instanceof XTransaction) {
			return executeTransaction((XTransaction)command, callback);
		}
		if(command instanceof XObjectCommand) {
			return executeObjectCommand((XObjectCommand)command, callback);
		}
		if(command instanceof XFieldCommand) {
			MemoryField field = getField(command.getTarget().getField());
			if(field != null) {
				return field.executeFieldCommand((XFieldCommand)command, callback);
			} else {
				return XCommand.FAILED;
			}
		}
		throw new IllegalArgumentException("Unknown command type: " + command);
	}
	
	@Override
	public long executeObjectCommand(XObjectCommand command) {
		return executeObjectCommand(command, null);
	}
	
	protected long executeObjectCommand(XObjectCommand command, XLocalChangeCallback callback) {
		synchronized(this.eventQueue) {
			checkRemoved();
			
			assert !this.eventQueue.transactionInProgess;
			
			if(!getAddress().equals(command.getTarget())) {
				if(callback != null) {
					callback.onFailure();
				}
				return XCommand.FAILED;
			}
			
			long oldRev = getCurrentRevisionNumber();
			
			if(command.getChangeType() == ChangeType.ADD) {
				if(hasField(command.getFieldId())) {
					// ID already taken
					if(command.isForced()) {
						/*
						 * the forced event only cares about the postcondition -
						 * that there is a field with the given ID, not about
						 * that there was no such field before
						 */
						if(callback != null) {
							callback.onSuccess(XCommand.NOCHANGE);
						}
						return XCommand.NOCHANGE;
					}
					if(callback != null) {
						callback.onFailure();
					}
					return XCommand.FAILED;
				}
				
				this.eventQueue.newLocalChange(command, callback);
				
				createFieldInternal(command.getFieldId());
				
			} else if(command.getChangeType() == ChangeType.REMOVE) {
				XField oldField = getField(command.getFieldId());
				
				if(oldField == null) {
					// ID not taken
					if(command.isForced()) {
						/*
						 * the forced event only cares about the postcondition -
						 * that there is no field with the given ID, not about
						 * that there was such a field before
						 */
						if(callback != null) {
							callback.onSuccess(XCommand.NOCHANGE);
						}
						return XCommand.NOCHANGE;
					}
					if(callback != null) {
						callback.onFailure();
					}
					return XCommand.FAILED;
				}
				
				if(!command.isForced()
				        && oldField.getRevisionNumber() != command.getRevisionNumber()) {
					if(callback != null) {
						callback.onFailure();
					}
					return XCommand.FAILED;
				}
				
				this.eventQueue.newLocalChange(command, callback);
				
				removeFieldInternal(command.getFieldId());
				
			} else {
				throw new IllegalArgumentException("Unknown object command type: " + command);
			}
			
			return oldRev + 1;
		}
	}
	
	@Override
	public XAddress getAddress() {
		synchronized(this.eventQueue) {
			return this.state.getAddress();
		}
	}
	
	@Override
	protected long getCurrentRevisionNumber() {
		if(this.father != null)
			return this.father.getRevisionNumber();
		else
			return getRevisionNumber();
	}
	
	@Override
	public MemoryField getField(XID fieldId) {
		synchronized(this.eventQueue) {
			checkRemoved();
			
			MemoryField field = this.loadedFields.get(fieldId);
			if(field != null) {
				return field;
			}
			
			XRevWritableField fieldState = this.state.getField(fieldId);
			if(fieldState == null) {
				return null;
			}
			
			field = new MemoryField(this, this.eventQueue, fieldState);
			this.loadedFields.put(fieldId, field);
			
			return field;
		}
	}
	
	@Override
	public XID getId() {
		synchronized(this.eventQueue) {
			return this.state.getId();
		}
	}
	
	@Override
	protected MemoryModel getModel() {
		return this.father;
	}
	
	/**
	 * @return the {@link XID} of the father-{@link XModel} of this MemoryObject
	 *         or null, if this object has no father.
	 */
	protected XID getModelId() {
		return this.father == null ? null : this.father.getId();
	}
	
	/**
	 * @return the current revision number of the father-{@link MemoryModel} of
	 *         this MemoryObject or {@link XEvent#RevisionOfEntityNotSet} if
	 *         this MemoryObject has no father.
	 */
	protected long getModelRevisionNumber() {
		if(this.father != null)
			return this.father.getRevisionNumber();
		else
			return XEvent.RevisionOfEntityNotSet;
	}
	
	@Override
	protected MemoryObject getObject() {
		return this;
	}
	
	@Override
	protected MemoryObject getObject(XID objectId) {
		if(getId().equals(objectId)) {
			return this;
		}
		return null;
	}
	
	/**
	 * @return the {@link XID} of the father-{@link XRepository} of this
	 *         MemoryObject or null, if this object has no father.
	 */
	protected XID getRepositoryId() {
		return this.father == null ? null : this.father.getRepositoryId();
	}
	
	@Override
	public long getRevisionNumber() {
		synchronized(this.eventQueue) {
			return this.state.getRevisionNumber();
		}
	}
	
	protected XRevWritableObject getState() {
		return this.state;
	}
	
	@Override
	protected XReadableModel getTransactionTarget() {
		if(this.father != null) {
			return this.father;
		}
		return new ReadableModelWithOneObject(this);
	}
	
	@Override
	public boolean hasField(XID id) {
		synchronized(this.eventQueue) {
			checkRemoved();
			return this.loadedFields.containsKey(id) || this.state.hasField(id);
		}
	}
	
	@Override
	public MemoryModel getFather() {
		return this.father;
	}
	
	@ReadOperation
	@Override
	public int hashCode() {
		synchronized(this.eventQueue) {
			return super.hashCode();
		}
	}
	
	protected boolean hasObject(XID objectId) {
		return getId().equals(objectId);
	}
	
	@Override
	protected void incrementRevision() {
		assert !this.eventQueue.transactionInProgess;
		if(this.father != null) {
			// this increments the revisionNumber of the father and sets
			// this revNr to the revNr of the father
			this.father.incrementRevision();
			setRevisionNumber(this.father.getRevisionNumber());
		} else {
			XChangeLog log = this.eventQueue.getChangeLog();
			if(log != null) {
				assert log.getCurrentRevisionNumber() > getRevisionNumber();
				setRevisionNumber(log.getCurrentRevisionNumber());
			} else {
				setRevisionNumber(getRevisionNumber() + 1);
			}
		}
	}
	
	@Override
	public boolean isEmpty() {
		synchronized(this.eventQueue) {
			checkRemoved();
			return this.state.isEmpty();
		}
	}
	
	@Override
	public Iterator<XID> iterator() {
		synchronized(this.eventQueue) {
			checkRemoved();
			return this.state.iterator();
		}
	}
	
	@Override
	public boolean removeField(XID fieldId) {
		
		// no synchronization necessary here (except that in
		// executeObjectCommand())
		
		XObjectCommand command = MemoryObjectCommand.createRemoveCommand(getAddress(),
		        XCommand.FORCED, fieldId);
		
		long result = executeObjectCommand(command);
		assert result >= 0 || result == XCommand.NOCHANGE;
		return result != XCommand.NOCHANGE;
	}
	
	/**
	 * Remove an existing field, increase revision (if not in a transaction) and
	 * enqueue the corresponding event(s).
	 * 
	 * The caller is responsible for handling synchronization, for checking that
	 * this object has not been removed and for checking that the field actually
	 * exists.
	 */
	protected void removeFieldInternal(XID fieldId) {
		
		assert hasField(fieldId);
		
		MemoryField field = getField(fieldId);
		assert field != null : "checked by caller";
		
		boolean inTrans = this.eventQueue.transactionInProgess;
		
		boolean makeTrans = !field.isEmpty();
		int since = this.eventQueue.getNextPosition();
		enqueueFieldRemoveEvents(this.eventQueue.getActor(), field, makeTrans || inTrans, false);
		
		// actually remove the field
		this.state.removeField(field.getId());
		this.loadedFields.remove(field.getId());
		
		Orphans orphans = this.eventQueue.orphans;
		if(orphans != null) {
			assert !orphans.fields.containsKey(field.getAddress());
			field.getState().setValue(null);
			orphans.fields.put(field.getAddress(), field);
		} else {
			field.delete();
		}
		
		// event propagation and revision number increasing happens after
		// all events were successful
		if(!inTrans) {
			
			if(makeTrans) {
				this.eventQueue.createTransactionEvent(this.eventQueue.getActor(), this.father,
				        this, since);
			}
			
			incrementRevision();
			
			// propagate events
			this.eventQueue.sendEvents();
			
		}
		
	}
	
	/**
	 * Removes all {@link XField XFields} of this MemoryObject from the
	 * persistence layer and the MemoryObject itself.
	 */
	protected void removeInternal() {
		// all fields are already loaded for creating events
		
		for(MemoryField field : this.loadedFields.values()) {
			field.getState().setValue(null);
			assert !this.eventQueue.orphans.fields.containsKey(field.getAddress());
			this.eventQueue.orphans.fields.put(field.getAddress(), field);
			this.state.removeField(field.getId());
		}
		
		this.loadedFields.clear();
		
		assert !this.eventQueue.orphans.objects.containsKey(getId());
		this.eventQueue.orphans.objects.put(getId(), this);
	}
	
	@Override
	protected void removeObjectInternal(XID objectId) {
		throw new AssertionError("object transactions cannot remove objects");
	}
	
	/**
	 * Sets the revision number of this MemoryObject
	 * 
	 * @param newRevision the new revision number
	 */
	protected void setRevisionNumber(long newRevision) {
		this.state.setRevisionNumber(newRevision);
	}
	
	@Override
	protected void setRevisionNumberIfModel(long modelRevisionNumber) {
		// not a model, so nothing to do here
	}
	
	@ReadOperation
	@Override
	public String toString() {
		return this.getId() + " rev[" + this.getRevisionNumber() + "]" + " "
		        + this.state.toString();
	}
	
	@Override
	public XRevWritableObject createSnapshot() {
		synchronized(this.eventQueue) {
			if(this.removed) {
				return null;
			}
			return XCopyUtils.createSnapshot(this);
		}
	}
	
	@Override
	public XType getType() {
		return XType.XOBJECT;
	}
	
}
