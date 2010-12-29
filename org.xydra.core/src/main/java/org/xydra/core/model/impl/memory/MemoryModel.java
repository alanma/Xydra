package org.xydra.core.model.impl.memory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.xydra.annotations.ReadOperation;
import org.xydra.core.XX;
import org.xydra.core.change.ChangeType;
import org.xydra.core.change.XCommand;
import org.xydra.core.change.XEvent;
import org.xydra.core.change.XModelCommand;
import org.xydra.core.change.XModelEvent;
import org.xydra.core.change.XModelEventListener;
import org.xydra.core.change.XObjectEvent;
import org.xydra.core.change.XTransaction;
import org.xydra.core.change.impl.memory.MemoryModelCommand;
import org.xydra.core.change.impl.memory.MemoryModelEvent;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XBaseModel;
import org.xydra.core.model.XID;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;
import org.xydra.core.model.XRepository;
import org.xydra.core.model.XSynchronizationCallback;
import org.xydra.core.model.state.XChangeLogState;
import org.xydra.core.model.state.XModelState;
import org.xydra.core.model.state.XObjectState;
import org.xydra.core.model.state.impl.memory.MemoryChangeLogState;
import org.xydra.core.model.state.impl.memory.TemporaryModelState;


/**
 * An implementation of {@link XModel}.
 * 
 * @author voelkel
 * @author Kaidel
 * 
 */
public class MemoryModel extends SynchronizesChangesImpl implements XModel {
	
	private static final long serialVersionUID = -2969189978307340483L;
	
	private final XModelState state;
	private final Map<XID,MemoryObject> loadedObjects = new HashMap<XID,MemoryObject>();
	
	/** The father-repository of this MemoryModel */
	private final MemoryRepository father;
	
	/** Has this MemoryModel been removed? */
	boolean removed = false;
	
	private Set<XModelEventListener> modelChangeListenerCollection;
	
	/**
	 * Creates a new MemoryModel without father-{@link XRepository}.
	 * 
	 * @param actorId TODO
	 * @param modelId The {@link XID} for this MemoryModel.
	 */
	public MemoryModel(XID actorId, String passwordHash, XID modelId) {
		this(actorId, passwordHash, null, createModelState(modelId));
	}
	
	private static XModelState createModelState(XID modelId) {
		XAddress modelAddr = XX.toAddress(null, modelId, null, null);
		XChangeLogState changeLogState = new MemoryChangeLogState(modelAddr);
		// Bump the log revision since we're missing this object's create event.
		changeLogState.setFirstRevisionNumber(1);
		return new TemporaryModelState(modelAddr, changeLogState);
	}
	
	/**
	 * Creates a new MemoryModel without father-{@link XRepository}.
	 * 
	 * @param actorId TODO
	 * @param modelState The initial {@link XModelState} of this MemoryModel.
	 */
	public MemoryModel(XID actorId, String passwordHash, XModelState modelState) {
		this(actorId, passwordHash, null, modelState);
	}
	
	/**
	 * Creates a new MemoryModel with the given {@link MemoryRepository} as its
	 * father.
	 * 
	 * @param actorId TODO
	 * @param father The father-{@link MemoryRepository} for this MemoryModel
	 * @param modelState The initial {@link XModelState} of this MemoryModel.
	 */
	protected MemoryModel(XID actorId, String passwordHash, MemoryRepository father,
	        XModelState modelState) {
		super(new MemoryEventManager(actorId, passwordHash,
		        modelState.getChangeLogState() == null ? null : new MemoryChangeLog(modelState
		                .getChangeLogState()), modelState.getRevisionNumber()));
		
		this.state = modelState;
		this.father = father;
		
		this.modelChangeListenerCollection = new HashSet<XModelEventListener>();
		
		assert this.eventQueue.getChangeLog() == null
		        || this.eventQueue.getChangeLog().getCurrentRevisionNumber() == getRevisionNumber();
	}
	
	/**
	 * @throws IllegalStateException if this method is called after this
	 *             MemoryModel was already removed
	 */
	@Override
	protected void checkRemoved() throws IllegalStateException {
		if(this.removed) {
			throw new IllegalStateException("this model has been removed");
		}
	}
	
	/**
	 * @return the {@link XID} of the father-{@link XRepository} of this
	 *         MemoryModel or null, if this object has no father.
	 */
	protected XID getRepositoryId() {
		return this.father == null ? null : this.father.getID();
	}
	
	@Override
	public MemoryObject getObject(XID objectID) {
		synchronized(this.eventQueue) {
			checkRemoved();
			
			MemoryObject object = this.loadedObjects.get(objectID);
			if(object != null) {
				return object;
			}
			
			if(!this.state.hasObjectState(objectID)) {
				return null;
			}
			
			XObjectState objectState = this.state.getObjectState(objectID);
			assert objectState != null : "The state '" + getAddress()
			        + "' has a child with objectID '" + objectID + "' but the objectState '"
			        + XX.resolveObject(getAddress(), objectID)
			        + "' is not in the XStateStore. Most likely it has not been save()d.";
			object = new MemoryObject(this, this.eventQueue, objectState);
			this.loadedObjects.put(objectID, object);
			
			return object;
		}
	}
	
	public Iterator<XID> iterator() {
		synchronized(this.eventQueue) {
			checkRemoved();
			return this.state.iterator();
		}
	}
	
	public MemoryObject createObject(XID objectId) {
		
		XModelCommand command = MemoryModelCommand.createAddCommand(getAddress(), true, objectId);
		
		// synchronize so that return is never null if command succeeded
		synchronized(this.eventQueue) {
			long result = executeModelCommand(command);
			MemoryObject object = getObject(objectId);
			assert result == XCommand.FAILED || object != null;
			return object;
		}
	}
	
	public boolean removeObject(XID objectId) {
		
		// no synchronization necessary here (except that in
		// executeModelCommand())
		
		XModelCommand command = MemoryModelCommand.createRemoveCommand(getAddress(),
		        XCommand.FORCED, objectId);
		
		long result = executeModelCommand(command);
		assert result >= 0 || result == XCommand.NOCHANGE;
		return result != XCommand.NOCHANGE;
	}
	
	@Override
	protected MemoryObject createObjectInternal(XID objectId) {
		assert getRevisionNumber() >= 0;
		
		assert !hasObject(objectId);
		
		boolean inTrans = this.eventQueue.transactionInProgess;
		Orphans orphans = getOrphans();
		
		if(!inTrans && orphans == null) {
			beginStateTransaction();
		}
		
		MemoryObject object = null;
		if(orphans != null) {
			object = orphans.objects.remove(objectId);
		}
		if(object == null) {
			XObjectState objectState = this.state.createObjectState(objectId);
			assert getAddress().contains(objectState.getAddress());
			object = new MemoryObject(this, this.eventQueue, objectState);
		}
		
		this.state.addObjectState(object.getState());
		this.loadedObjects.put(object.getID(), object);
		
		XModelEvent event = MemoryModelEvent.createAddEvent(this.eventQueue.getActor(),
		        getAddress(), objectId, getRevisionNumber(), inTrans);
		
		this.eventQueue.enqueueModelEvent(this, event);
		
		// event propagation and revision number increasing happens after
		// all events were successful
		if(!inTrans) {
			
			object.incrementRevisionAndSave();
			
			if(orphans == null) {
				endStateTransaction();
			}
			
			// propagate events
			this.eventQueue.sendEvents();
			
		}
		
		return object;
	}
	
	@Override
	protected void removeObjectInternal(XID objectId) {
		
		MemoryObject object = getObject(objectId);
		assert object != null;
		
		boolean inTrans = this.eventQueue.transactionInProgess;
		boolean makeTrans = !object.isEmpty();
		int since = this.eventQueue.getNextPosition();
		enqueueObjectRemoveEvents(this.eventQueue.getActor(), object, makeTrans || inTrans, false);
		
		Orphans orphans = getOrphans();
		if(!inTrans && orphans == null) {
			beginStateTransaction();
		}
		
		// remove the object
		this.loadedObjects.remove(object.getID());
		this.state.removeObjectState(object.getID());
		if(orphans != null) {
			object.removeInternal(orphans);
			orphans.objects.put(object.getID(), object);
		} else {
			object.delete();
		}
		
		// event propagation and revision number increasing for transactions
		// happens after all events of a transaction were successful
		if(!inTrans) {
			
			if(makeTrans) {
				this.eventQueue.createTransactionEvent(this.eventQueue.getActor(), this, null,
				        since);
			}
			
			incrementRevisionAndSave();
			
			if(orphans == null) {
				endStateTransaction();
			}
			
			// propagate events
			this.eventQueue.sendEvents();
			
		}
		
	}
	
	public long executeModelCommand(XModelCommand command) {
		return executeModelCommand(command, null);
	}
	
	protected long executeModelCommand(XModelCommand command, XSynchronizationCallback callback) {
		synchronized(this.eventQueue) {
			checkRemoved();
			
			assert !this.eventQueue.transactionInProgess;
			
			if(!getAddress().equals(command.getTarget())) {
				return XCommand.FAILED;
			}
			
			long oldRev = getRevisionNumber();
			
			if(command.getChangeType() == ChangeType.ADD) {
				if(hasObject(command.getObjectID())) {
					// ID already taken
					if(command.isForced()) {
						/*
						 * the forced event only cares about the postcondition -
						 * that there is an object with the given ID, not about
						 * that there was no such object before
						 */
						return XCommand.NOCHANGE;
					}
					return XCommand.FAILED;
				}
				
				this.eventQueue.newLocalChange(command, callback);
				
				createObjectInternal(command.getObjectID());
				
			} else if(command.getChangeType() == ChangeType.REMOVE) {
				XObject oldObject = getObject(command.getObjectID());
				
				if(oldObject == null) {
					// ID not taken
					if(command.isForced()) {
						/*
						 * the forced event only cares about the postcondition -
						 * that there is no object with the given ID, not about
						 * that there was such an object before
						 */
						return XCommand.NOCHANGE;
					}
					return XCommand.FAILED;
				}
				
				if(!command.isForced()
				        && oldObject.getRevisionNumber() != command.getRevisionNumber()) {
					return XCommand.FAILED;
				}
				
				this.eventQueue.newLocalChange(command, callback);
				
				removeObjectInternal(command.getObjectID());
				
			} else {
				throw new IllegalArgumentException("Unknown model command type: " + command);
			}
			
			return oldRev + 1;
		}
	}
	
	public XID getID() {
		synchronized(this.eventQueue) {
			checkRemoved();
			return this.state.getID();
		}
	}
	
	public boolean hasObject(XID id) {
		synchronized(this.eventQueue) {
			checkRemoved();
			return this.loadedObjects.containsKey(id) || this.state.hasObjectState(id);
		}
	}
	
	@Override
	protected void incrementRevisionAndSave() {
		assert !this.eventQueue.transactionInProgess;
		long newRevision = getRevisionNumber() + 1;
		this.state.setRevisionNumber(newRevision);
		save();
		this.eventQueue.saveLog();
	}
	
	/**
	 * Saves the current state information of this MemoryModel with the
	 * currently used persistence layer
	 */
	protected void save() {
		this.state.save(this.eventQueue.stateTransaction);
	}
	
	/**
	 * Returns the father-{@link MemoryRepository} of this MemoryModel.
	 * 
	 * @return The father of this MemoryModel (may be null).
	 */
	@ReadOperation
	protected MemoryRepository getFather() {
		return this.father;
	}
	
	/**
	 * Checks whether this MemoryModel has a father-{@link XRepository} or not.
	 * 
	 * @return true, if this MemoryModel has a father-{@link XRepository}, false
	 *         otherwise.
	 */
	@ReadOperation
	protected boolean hasFather() {
		return this.father != null;
	}
	
	@Override
	@ReadOperation
	public boolean equals(Object object) {
		if(!(object instanceof MemoryModel)) {
			return false;
		}
		
		MemoryModel model = (MemoryModel)object;
		
		// compare revision number, repository ID & modelID
		if(this.father != null) {
			if(model.father == null) {
				return false;
			}
			
			return (getRevisionNumber() == model.getRevisionNumber())
			        && (this.father.getID().equals(model.father.getID()))
			        && (getID().equals(model.getID()));
		} else {
			if(model.father != null) {
				return false;
			}
			
			return (getRevisionNumber() == model.getRevisionNumber())
			        && (getID().equals(model.getID()));
		}
	}
	
	@Override
	@ReadOperation
	public int hashCode() {
		int hashCode = getID().hashCode() + (int)getRevisionNumber();
		
		if(this.father != null) {
			hashCode += this.father.getID().hashCode();
		}
		
		return hashCode;
	}
	
	public long getRevisionNumber() {
		synchronized(this.eventQueue) {
			checkRemoved();
			return this.state.getRevisionNumber();
		}
	}
	
	@Override
	protected long executeTransaction(XTransaction transaction, XSynchronizationCallback callback) {
		synchronized(this.eventQueue) {
			checkRemoved();
			
			if(transaction.getTarget().getObject() != null) {
				
				// try to get the object the given transaction actually refers
				// to
				MemoryObject object = getObject(transaction.getTarget().getObject());
				
				if(object == null) {
					// object does not exist -> transaction fails
					return XCommand.FAILED;
				} else {
					// let the object handle the transaction execution
					/*
					 * TODO using the actor set on the object instead of the one
					 * set on the model (on which the user called the
					 * #executeTransaction() method) - this is counter-intuitive
					 * for an API user
					 */
					return object.executeTransaction(transaction, callback);
				}
			}
			
			return super.executeTransaction(transaction, callback);
			
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
			checkRemoved();
			return this.state.getAddress();
		}
	}
	
	/**
	 * Notifies all listeners that have registered interest for notification on
	 * {@link XModelEvent XModelEvents} happening on this MemoryModel.
	 * 
	 * @param event The {@link XModelEvent} which will be propagated to the
	 *            registered listeners.
	 */
	protected void fireModelEvent(XModelEvent event) {
		for(XModelEventListener listener : this.modelChangeListenerCollection) {
			listener.onChangeEvent(event);
		}
	}
	
	public boolean addListenerForModelEvents(XModelEventListener changeListener) {
		synchronized(this.eventQueue) {
			return this.modelChangeListenerCollection.add(changeListener);
		}
	}
	
	public boolean removeListenerForModelEvents(XModelEventListener changeListener) {
		synchronized(this.eventQueue) {
			return this.modelChangeListenerCollection.remove(changeListener);
		}
	}
	
	/**
	 * Creates all {@link XEvent XEvents} which will represent the removal of
	 * the given {@link MemoryObject}. All necessary {@link XObjectEvent
	 * XObjectEvents} of the REMOVE-type will and lastly the {@link XModelEvent}
	 * representing the actual removal of the {@link MemoryObject} will be
	 * created to accurately represent the removal. The created {@link XEvent
	 * XEvents} will then be enqueued into the {@link MemoryEventManager} used
	 * by this MemoryModel and then be propagated to the interested listeners.
	 * 
	 * @param actor The {@link XID} of the actor
	 * @param object The {@link MemoryObject} which is to be removed (must not
	 *            be null)
	 * @param inTrans true, if the removal of this {@link MemoryObject} occurs
	 *            during an {@link XTransaction}.
	 * @param implied true if this model is also removed in the same transaction
	 * @throws IllegalArgumentException if the given {@link MemoryOjbect} equals
	 *             null
	 */
	protected void enqueueObjectRemoveEvents(XID actor, MemoryObject object, boolean inTrans,
	        boolean implied) {
		
		if(object == null) {
			throw new IllegalArgumentException("object must not be null");
		}
		
		for(XID fieldID : object) {
			assert inTrans;
			MemoryField field = object.getField(fieldID);
			object.enqueueFieldRemoveEvents(actor, field, inTrans, true);
		}
		
		// add event to remove the object
		XModelEvent event = MemoryModelEvent.createRemoveEvent(actor, getAddress(), object.getID(),
		        getRevisionNumber(), object.getRevisionNumber(), inTrans, implied);
		this.eventQueue.enqueueModelEvent(this, event);
		
	}
	
	/**
	 * Deletes the state information of this MemoryModel from the currently used
	 * persistence layer
	 */
	protected void delete() {
		for(XID objectId : this) {
			MemoryObject object = getObject(objectId);
			object.delete();
		}
		this.state.delete(this.eventQueue.stateTransaction);
		this.eventQueue.deleteLog();
		this.removed = true;
	}
	
	public long executeCommand(XCommand command) {
		return executeCommand(command, null);
	}
	
	public long executeCommand(XCommand command, XSynchronizationCallback callback) {
		
		if(command instanceof XTransaction) {
			return executeTransaction((XTransaction)command, callback);
		} else if(command instanceof XModelCommand) {
			return executeModelCommand((XModelCommand)command, callback);
		}
		MemoryObject object = getObject(command.getTarget().getObject());
		if(object == null) {
			return XCommand.FAILED;
		}
		return object.executeCommand(command, callback);
	}
	
	@Override
	protected long getOldRevisionNumber() {
		return getRevisionNumber();
	}
	
	@Override
	protected MemoryModel getModel() {
		return this;
	}
	
	@Override
	protected MemoryObject getObject() {
		// this is not an object
		return null;
	}
	
	@Override
	protected XBaseModel getTransactionTarget() {
		return this;
	}
	
	@Override
	protected void saveIfModel() {
		save();
	}
	
	@Override
	protected void setRevisionNumberIfModel(long modelRevisionNumber) {
		this.state.setRevisionNumber(modelRevisionNumber);
	}
	
	@Override
	protected void checkSync() {
		// models can always sync
	}
	
	@Override
	protected void beginStateTransaction() {
		assert this.eventQueue.stateTransaction == null : "multiple state transactions detected";
		this.eventQueue.stateTransaction = this.state.beginTransaction();
	}
	
	@Override
	protected void endStateTransaction() {
		this.state.endTransaction(this.eventQueue.stateTransaction);
		this.eventQueue.stateTransaction = null;
	}
	
	@Override
	public String toString() {
		return this.state.toString();
	}
	
}
