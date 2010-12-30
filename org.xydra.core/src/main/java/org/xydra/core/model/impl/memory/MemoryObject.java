package org.xydra.core.model.impl.memory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.xydra.annotations.ReadOperation;
import org.xydra.core.XX;
import org.xydra.core.change.ChangeType;
import org.xydra.core.change.XCommand;
import org.xydra.core.change.XEvent;
import org.xydra.core.change.XFieldCommand;
import org.xydra.core.change.XFieldEvent;
import org.xydra.core.change.XObjectCommand;
import org.xydra.core.change.XObjectEvent;
import org.xydra.core.change.XTransaction;
import org.xydra.core.change.impl.memory.MemoryFieldEvent;
import org.xydra.core.change.impl.memory.MemoryObjectCommand;
import org.xydra.core.change.impl.memory.MemoryObjectEvent;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XBaseModel;
import org.xydra.core.model.XChangeLog;
import org.xydra.core.model.XField;
import org.xydra.core.model.XID;
import org.xydra.core.model.XLocalChangeCallback;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;
import org.xydra.core.model.XRepository;
import org.xydra.core.model.delta.WrapperModel;
import org.xydra.core.model.state.XChangeLogState;
import org.xydra.core.model.state.XFieldState;
import org.xydra.core.model.state.XObjectState;
import org.xydra.core.model.state.impl.memory.MemoryChangeLogState;
import org.xydra.core.model.state.impl.memory.TemporaryObjectState;


/**
 * An implementation of {@link XObject}.
 * 
 * @author voelkel
 * @author Kaidel
 * 
 */
public class MemoryObject extends SynchronizesChangesImpl implements XObject {
	
	private static final long serialVersionUID = -808702139986657842L;
	
	private final XObjectState state;
	private final Map<XID,MemoryField> loadedFields = new HashMap<XID,MemoryField>();
	
	/** The father-model of this MemoryObject */
	private final MemoryModel father;
	
	/**
	 * Creates a new MemoryObject without a father-{@link XModel}.
	 * 
	 * @param actorId TODO
	 * @param objectId The {@link XID} for this MemoryObject
	 */
	public MemoryObject(XID actorId, String passwordHash, XID objectId) {
		this(actorId, passwordHash, createObjectState(objectId));
	}
	
	private static XObjectState createObjectState(XID objectId) {
		XAddress objectAddr = XX.toAddress(null, null, objectId, null);
		XChangeLogState changeLogState = new MemoryChangeLogState(objectAddr);
		// Bump the log revision since we're missing this object's create event.
		changeLogState.setFirstRevisionNumber(1);
		return new TemporaryObjectState(objectAddr, changeLogState);
	}
	
	/**
	 * Creates a new MemoryObject without a father-{@link XModel}.
	 * 
	 * @param actorId TODO
	 * @param objectState The {@link XObjectState} for this MemoryObject
	 */
	public MemoryObject(XID actorId, String passwordHash, XObjectState objectState) {
		this(null, createEventQueue(actorId, passwordHash, objectState), objectState);
	}
	
	private static MemoryEventManager createEventQueue(XID actorId, String passwordHash,
	        XObjectState objectState) {
		XChangeLogState logState = objectState.getChangeLogState();
		MemoryChangeLog log = logState == null ? null : new MemoryChangeLog(logState);
		return new MemoryEventManager(actorId, passwordHash, log, objectState.getRevisionNumber());
	}
	
	/**
	 * Creates a new MemoryObject with the given {@link MemoryModel} as its
	 * father.
	 * 
	 * @param actorId TODO
	 * @param parent The father-{@link MemoryModel} of this MemoryObject.
	 * @param eventQueue The {@link MemoryEventManager} which will be used by
	 *            this MemoryObject.
	 * @param objectState The initial {@link XObjectState} of this MemoryObject.
	 */
	protected MemoryObject(MemoryModel parent, MemoryEventManager eventQueue,
	        XObjectState objectState) {
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
	 * @throws IllegalStateException if this method is called after this
	 *             MemoryObject was already removed
	 */
	@Override
	protected void checkRemoved() throws IllegalStateException {
		if(this.removed) {
			throw new IllegalStateException("this object has been removed");
		}
	}
	
	@ReadOperation
	@Override
	public int hashCode() {
		int hashCode = this.getID().hashCode() + (int)this.getRevisionNumber();
		
		if(this.father != null) {
			hashCode += this.father.getID().hashCode();
			
			XRepository repoFather = this.father.getFather();
			
			if(repoFather != null) {
				hashCode += repoFather.getID().hashCode();
			}
		}
		return hashCode;
	}
	
	@ReadOperation
	@Override
	public boolean equals(Object object) {
		if(!(object instanceof MemoryObject)) {
			return false;
		}
		
		MemoryObject memoryObject = (MemoryObject)object;
		
		// compare revision number, father-model id (if it exists),
		// father-repository id (if it exists)
		boolean result = (this.getRevisionNumber() == memoryObject.getRevisionNumber())
		        && (this.getID().equals(memoryObject.getID()));
		
		if(this.father != null) {
			if(memoryObject.father == null) {
				return false;
			}
			
			result = result && (this.father.getID().equals(memoryObject.father.getID()));
			
			if(this.father.hasFather()) {
				if(memoryObject.father.getFather() == null) {
					return false;
				}
				
				result = result
				        && (this.father.getFather().getID().equals(memoryObject.father.getFather()
				                .getID()));
			}
		}
		
		return result;
	}
	
	@ReadOperation
	@Override
	public String toString() {
		return this.getID() + "-v" + this.getRevisionNumber() + " " + this.state.toString();
	}
	
	public XID getID() {
		synchronized(this.eventQueue) {
			return this.state.getID();
		}
	}
	
	/**
	 * Removes all {@link XField XFields} of this MemoryObject from the
	 * persistence layer and the MemoryObject itself.
	 * 
	 * @param orphans The collection to which the removed {@link XField XFields}
	 *            are to be added to for server-client synchronization purposes
	 */
	protected void removeInternal(Orphans orphans) {
		// all fields are already loaded for creating events
		
		for(MemoryField field : this.loadedFields.values()) {
			field.getState().setValue(null);
			orphans.fields.put(field.getAddress(), field);
			this.state.removeFieldState(field.getID());
		}
		
		this.loadedFields.clear();
	}
	
	/**
	 * Saves the current state information of this MemoryObject with the
	 * currently used persistence layer
	 */
	protected void save() {
		this.state.save(this.eventQueue.stateTransaction);
	}
	
	/**
	 * @return the {@link XID} of the father-{@link XModel} of this MemoryObject
	 *         or null, if this object has no father.
	 */
	protected XID getModelId() {
		return this.father == null ? null : this.father.getID();
	}
	
	/**
	 * @return the {@link XID} of the father-{@link XRepository} of this
	 *         MemoryObject or null, if this object has no father.
	 */
	protected XID getRepositoryId() {
		return this.father == null ? null : this.father.getRepositoryId();
	}
	
	public MemoryField getField(XID fieldID) {
		synchronized(this.eventQueue) {
			checkRemoved();
			
			MemoryField field = this.loadedFields.get(fieldID);
			if(field != null) {
				return field;
			}
			
			if(!this.state.hasFieldState(fieldID)) {
				return null;
			}
			
			XFieldState fieldState = this.state.getFieldState(fieldID);
			assert fieldState != null;
			field = new MemoryField(this, this.eventQueue, fieldState);
			this.loadedFields.put(fieldID, field);
			
			return field;
		}
	}
	
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
	 * Create a new field, increase revision (if not in a transaction) and
	 * enqueue the corresponding event.
	 * 
	 * The caller is responsible for handling synchronization, for checking that
	 * this object has not been removed and for checking that the field doesn't
	 * already exist.
	 */
	protected MemoryField createFieldInternal(XID fieldId) {
		
		assert !hasField(fieldId);
		
		Orphans orphans = getOrphans();
		boolean inTrans = this.eventQueue.transactionInProgess;
		if(!inTrans && orphans == null) {
			beginStateTransaction();
		}
		
		MemoryField field = null;
		if(orphans != null) {
			XAddress fieldAddr = XX.resolveField(getAddress(), fieldId);
			field = orphans.fields.remove(fieldAddr);
		}
		if(field == null) {
			XFieldState fieldState = this.state.createFieldState(fieldId);
			assert getAddress().contains(fieldState.getAddress());
			field = new MemoryField(this, this.eventQueue, fieldState);
		}
		
		this.state.addFieldState(field.getState());
		this.loadedFields.put(field.getID(), field);
		
		XObjectEvent event = MemoryObjectEvent
		        .createAddEvent(this.eventQueue.getActor(), getAddress(), field.getID(),
		                getModelRevisionNumber(), getRevisionNumber(), inTrans);
		
		this.eventQueue.enqueueObjectEvent(this, event);
		
		// event propagation and revision number increasing happens after
		// all events were successful
		if(!inTrans) {
			
			field.incrementRevisionAndSave();
			
			if(orphans == null) {
				endStateTransaction();
			}
			
			// propagate events
			this.eventQueue.sendEvents();
			
		}
		
		return field;
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
		Orphans orphans = getOrphans();
		if(!inTrans && orphans == null) {
			beginStateTransaction();
		}
		
		boolean makeTrans = !field.isEmpty();
		int since = this.eventQueue.getNextPosition();
		enqueueFieldRemoveEvents(this.eventQueue.getActor(), field, makeTrans || inTrans, false);
		
		// actually remove the field
		this.state.removeFieldState(field.getID());
		this.loadedFields.remove(field.getID());
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
			
			incrementRevisionAndSave();
			
			if(orphans == null) {
				endStateTransaction();
			}
			
			// propagate events
			this.eventQueue.sendEvents();
			
		}
		
	}
	
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
				if(hasField(command.getFieldID())) {
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
				
				createFieldInternal(command.getFieldID());
				
			} else if(command.getChangeType() == ChangeType.REMOVE) {
				XField oldField = getField(command.getFieldID());
				
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
				
				removeFieldInternal(command.getFieldID());
				
			} else {
				throw new IllegalArgumentException("Unknown object command type: " + command);
			}
			
			return oldRev + 1;
		}
	}
	
	@Override
	protected void incrementRevisionAndSave() {
		assert !this.eventQueue.transactionInProgess;
		if(this.father != null) {
			// this increments the revisionNumber of the father and sets
			// this revNr to the revNr of the father
			this.father.incrementRevisionAndSave();
			setRevisionNumber(this.father.getRevisionNumber());
		} else {
			XChangeLog log = this.eventQueue.getChangeLog();
			if(log != null) {
				assert log.getCurrentRevisionNumber() > getRevisionNumber();
				setRevisionNumber(log.getCurrentRevisionNumber());
				this.eventQueue.saveLog();
			} else {
				setRevisionNumber(getRevisionNumber() + 1);
			}
		}
		save();
	}
	
	/**
	 * Sets the revision number of this MemoryObject
	 * 
	 * @param newRevision the new revision number
	 */
	protected void setRevisionNumber(long newRevision) {
		this.state.setRevisionNumber(newRevision);
	}
	
	public Iterator<XID> iterator() {
		synchronized(this.eventQueue) {
			checkRemoved();
			return this.state.iterator();
		}
	}
	
	public boolean hasField(XID id) {
		synchronized(this.eventQueue) {
			checkRemoved();
			return this.loadedFields.containsKey(id) || this.state.hasFieldState(id);
		}
	}
	
	public long getRevisionNumber() {
		synchronized(this.eventQueue) {
			return this.state.getRevisionNumber();
		}
	}
	
	public boolean isEmpty() {
		synchronized(this.eventQueue) {
			checkRemoved();
			return this.state.isEmpty();
		}
	}
	
	public XAddress getAddress() {
		synchronized(this.eventQueue) {
			return this.state.getAddress();
		}
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
	protected long getCurrentRevisionNumber() {
		if(this.father != null)
			return this.father.getRevisionNumber();
		else
			return getRevisionNumber();
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
	 * @return An {@link ObjectTransaction} that first removes the value of the
	 *         given field and then the given field itself.
	 */
	protected void enqueueFieldRemoveEvents(XID actor, MemoryField field, boolean inTrans,
	        boolean implied) {
		
		if(field == null) {
			throw new NullPointerException("field must not be null");
		}
		
		long modelRev = getModelRevisionNumber();
		
		if(field.getValue() != null) {
			assert inTrans;
			XFieldEvent event = MemoryFieldEvent.createRemoveEvent(actor, field.getAddress(), field
			        .getValue(), modelRev, getRevisionNumber(), field.getRevisionNumber(), inTrans,
			        true);
			this.eventQueue.enqueueFieldEvent(field, event);
		}
		
		XObjectEvent event = MemoryObjectEvent.createRemoveEvent(actor, getAddress(),
		        field.getID(), modelRev, getRevisionNumber(), field.getRevisionNumber(), inTrans,
		        implied);
		this.eventQueue.enqueueObjectEvent(this, event);
		
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
			this.state.removeFieldState(fieldId);
		}
		this.state.delete(this.eventQueue.stateTransaction);
		this.loadedFields.clear();
		if(this.father == null) {
			this.eventQueue.deleteLog();
		}
		this.removed = true;
	}
	
	public long executeCommand(XCommand command) {
		return executeCommand(command, null);
	}
	
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
			}
		}
		throw new IllegalArgumentException("Unknown command type: " + command);
	}
	
	@Override
	protected MemoryObject createObjectInternal(XID objectId) {
		throw new AssertionError("object transactions cannot create objects");
	}
	
	@Override
	protected MemoryObject getObject(XID objectId) {
		if(getID().equals(objectId)) {
			return this;
		}
		return null;
	}
	
	protected boolean hasObject(XID objectId) {
		return getID().equals(objectId);
	}
	
	@Override
	protected void removeObjectInternal(XID objectId) {
		throw new AssertionError("object transactions cannot remove objects");
	}
	
	@Override
	protected MemoryModel getModel() {
		return this.father;
	}
	
	@Override
	protected MemoryObject getObject() {
		return this;
	}
	
	@Override
	protected XBaseModel getTransactionTarget() {
		if(this.father != null) {
			return this.father;
		}
		return new WrapperModel(this);
	}
	
	protected XObjectState getState() {
		return this.state;
	}
	
	@Override
	protected void saveIfModel() {
		// not a model, so nothing to do here
	}
	
	@Override
	protected void setRevisionNumberIfModel(long modelRevisionNumber) {
		// not a model, so nothing to do here
	}
	
	@Override
	protected void checkSync() {
		if(this.father != null) {
			throw new IllegalStateException(
			        "an object that is part of a model cannot be rolled abck / synchronized individualy");
		}
	}
	
	@Override
	protected void beginStateTransaction() {
		if(this.father != null) {
			this.father.beginStateTransaction();
		} else {
			assert this.eventQueue.stateTransaction == null : "multiple state transactions detected";
			this.eventQueue.stateTransaction = this.state.beginTransaction();
		}
	}
	
	@Override
	protected void endStateTransaction() {
		if(this.father != null) {
			this.father.endStateTransaction();
		} else {
			this.state.endTransaction(this.eventQueue.stateTransaction);
			this.eventQueue.stateTransaction = null;
		}
	}
	
}
