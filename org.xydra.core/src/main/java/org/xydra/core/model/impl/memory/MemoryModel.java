package org.xydra.core.model.impl.memory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.xydra.annotations.ReadOperation;
import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.XType;
import org.xydra.base.XX;
import org.xydra.base.change.ChangeType;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XEvent;
import org.xydra.base.change.XModelCommand;
import org.xydra.base.change.XModelEvent;
import org.xydra.base.change.XObjectEvent;
import org.xydra.base.change.XRepositoryEvent;
import org.xydra.base.change.XTransaction;
import org.xydra.base.change.impl.memory.MemoryModelCommand;
import org.xydra.base.change.impl.memory.MemoryModelEvent;
import org.xydra.base.change.impl.memory.MemoryRepositoryCommand;
import org.xydra.base.change.impl.memory.MemoryRepositoryEvent;
import org.xydra.base.rmof.XReadableModel;
import org.xydra.base.rmof.XRevWritableModel;
import org.xydra.core.XCopyUtils;
import org.xydra.core.change.XModelEventListener;
import org.xydra.core.model.XField;
import org.xydra.core.model.XLocalChangeCallback;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;
import org.xydra.core.model.XRepository;
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
	
	private static XModelState createModelState(XAddress modelAddr) {
		XChangeLogState changeLogState = new MemoryChangeLogState(modelAddr);
		// Bump the log revision if we're missing this object's create event.
		changeLogState.setFirstRevisionNumber(modelAddr.getRepository() == null ? 1 : 0);
		return new TemporaryModelState(modelAddr, changeLogState);
	}
	
	/** The father-repository of this MemoryModel */
	private final MemoryRepository father;
	
	private final Map<XID,MemoryObject> loadedObjects = new HashMap<XID,MemoryObject>();
	
	private Set<XModelEventListener> modelChangeListenerCollection;
	
	private final XModelState state;
	
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
		
		if(father == null && getAddress().getRepository() != null
		        && this.eventQueue.getChangeLog() != null
		        && this.eventQueue.getChangeLog().getCurrentRevisionNumber() == -1) {
			XAddress repoAddr = getAddress().getParent();
			XCommand createCommand = MemoryRepositoryCommand.createAddCommand(repoAddr, true,
			        getID());
			this.eventQueue.newLocalChange(createCommand, null);
			XRepositoryEvent createEvent = MemoryRepositoryEvent.createAddEvent(actorId, repoAddr,
			        getID());
			this.eventQueue.enqueueRepositoryEvent(null, createEvent);
			this.eventQueue.sendEvents();
		}
		
		assert this.eventQueue.getChangeLog() == null
		        || this.eventQueue.getChangeLog().getCurrentRevisionNumber() == getRevisionNumber();
		
	}
	
	/**
	 * Creates a new MemoryModel without father-{@link XRepository} but with a
	 * parent repository XID so it can be synchronized.
	 * 
	 * @param actorId TODO
	 * @param modelAddr The {@link XAddress} for this MemoryModel.
	 */
	public MemoryModel(XID actorId, String passwordHash, XAddress modelAddr) {
		this(actorId, passwordHash, null, createModelState(modelAddr));
		if(modelAddr.getAddressedType() != XType.XMODEL) {
			throw new IllegalArgumentException("modelAddr must be a model Adress, was: "
			        + modelAddr);
		}
	}
	
	/**
	 * Creates a new MemoryModel without father-{@link XRepository}.
	 * 
	 * @param actorId TODO
	 * @param modelId The {@link XID} for this MemoryModel.
	 */
	public MemoryModel(XID actorId, String passwordHash, XID modelId) {
		this(actorId, passwordHash, null, createModelState(XX.toAddress(null, modelId, null, null)));
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
	
	public boolean addListenerForModelEvents(XModelEventListener changeListener) {
		synchronized(this.eventQueue) {
			return this.modelChangeListenerCollection.add(changeListener);
		}
	}
	
	@Override
	protected void beginStateTransaction() {
		assert this.eventQueue.stateTransaction == null : "multiple state transactions detected";
		this.eventQueue.stateTransaction = this.state.beginTransaction();
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
	
	@Override
	protected void checkSync() {
		// models can always sync
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
	
	@Override
	protected MemoryObject createObjectInternal(XID objectId) {
		assert getRevisionNumber() >= 0;
		
		assert !hasObject(objectId);
		
		boolean inTrans = this.eventQueue.transactionInProgess;
		Orphans orphans = this.eventQueue.orphans;
		
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
		assert object.getModel() == this;
		
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
	
	/**
	 * Deletes the state information of this MemoryModel from the currently used
	 * persistence layer
	 */
	protected void delete() {
		for(XID objectId : this) {
			MemoryObject object = getObject(objectId);
			object.delete();
		}
		for(XID objectId : this.loadedObjects.keySet()) {
			this.state.removeObjectState(objectId);
		}
		this.state.setRevisionNumber(this.state.getRevisionNumber() + 1);
		this.state.delete(this.eventQueue.stateTransaction);
		this.eventQueue.deleteLog();
		this.loadedObjects.clear();
		this.removed = true;
	}
	
	@Override
	protected void endStateTransaction() {
		this.state.endTransaction(this.eventQueue.stateTransaction);
		this.eventQueue.stateTransaction = null;
	}
	
	protected boolean enqueueModelRemoveEvents(XID actorId) {
		
		boolean inTrans = false;
		
		for(XID objectId : this) {
			MemoryObject object = getObject(objectId);
			enqueueObjectRemoveEvents(actorId, object, true, true);
			inTrans = true;
		}
		
		XAddress repoAdrr = hasFather() ? getFather().getAddress() : getAddress().getParent();
		XRepositoryEvent event = MemoryRepositoryEvent.createRemoveEvent(actorId, repoAdrr,
		        getID(), getCurrentRevisionNumber(), inTrans);
		this.eventQueue.enqueueRepositoryEvent(getFather(), event);
		
		return inTrans;
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
		
		for(XID fieldId : object) {
			assert inTrans;
			MemoryField field = object.getField(fieldId);
			object.enqueueFieldRemoveEvents(actor, field, inTrans, true);
		}
		
		// add event to remove the object
		XModelEvent event = MemoryModelEvent.createRemoveEvent(actor, getAddress(), object.getID(),
		        getRevisionNumber(), object.getRevisionNumber(), inTrans, implied);
		this.eventQueue.enqueueModelEvent(this, event);
		
	}
	
	@Override
	@ReadOperation
	public boolean equals(Object object) {
		if(!(object instanceof MemoryModel)) {
			return false;
		}
		
		MemoryModel model = (MemoryModel)object;
		
		// compare revision number, repository ID & modelId
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
	
	public long executeCommand(XCommand command) {
		return executeCommand(command, null);
	}
	
	public long executeCommand(XCommand command, XLocalChangeCallback callback) {
		
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
	
	public long executeModelCommand(XModelCommand command) {
		return executeModelCommand(command, null);
	}
	
	protected long executeModelCommand(XModelCommand command, XLocalChangeCallback callback) {
		synchronized(this.eventQueue) {
			checkRemoved();
			
			assert !this.eventQueue.transactionInProgess;
			
			if(!getAddress().equals(command.getTarget())) {
				if(callback != null) {
					callback.onFailure();
				}
				return XCommand.FAILED;
			}
			
			long oldRev = getRevisionNumber();
			
			if(command.getChangeType() == ChangeType.ADD) {
				if(hasObject(command.getObjectId())) {
					// ID already taken
					if(command.isForced()) {
						/*
						 * the forced event only cares about the postcondition -
						 * that there is an object with the given ID, not about
						 * that there was no such object before
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
				
				createObjectInternal(command.getObjectId());
				
			} else if(command.getChangeType() == ChangeType.REMOVE) {
				XObject oldObject = getObject(command.getObjectId());
				
				if(oldObject == null) {
					// ID not taken
					if(command.isForced()) {
						/*
						 * the forced event only cares about the postcondition -
						 * that there is no object with the given ID, not about
						 * that there was such an object before
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
				        && oldObject.getRevisionNumber() != command.getRevisionNumber()) {
					return XCommand.FAILED;
				}
				
				this.eventQueue.newLocalChange(command, callback);
				
				removeObjectInternal(command.getObjectId());
				
			} else {
				throw new IllegalArgumentException("Unknown model command type: " + command);
			}
			
			return oldRev + 1;
		}
	}
	
	@Override
	protected long executeTransaction(XTransaction transaction, XLocalChangeCallback callback) {
		synchronized(this.eventQueue) {
			checkRemoved();
			
			if(transaction.getTarget().getObject() != null) {
				
				// try to get the object the given transaction actually refers
				// to
				MemoryObject object = getObject(transaction.getTarget().getObject());
				
				if(object == null) {
					// object does not exist -> transaction fails
					if(callback != null) {
						callback.onFailure();
					}
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
	
	public XAddress getAddress() {
		return this.state.getAddress();
	}
	
	@Override
	protected long getCurrentRevisionNumber() {
		return this.state.getRevisionNumber();
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
	
	public XID getID() {
		synchronized(this.eventQueue) {
			return this.state.getID();
		}
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
	public MemoryObject getObject(XID objectId) {
		synchronized(this.eventQueue) {
			checkRemoved();
			
			MemoryObject object = this.loadedObjects.get(objectId);
			if(object != null) {
				return object;
			}
			
			if(!this.state.hasObjectState(objectId)) {
				return null;
			}
			
			XObjectState objectState = this.state.getObjectState(objectId);
			assert objectState != null : "The state '" + getAddress()
			        + "' has a child with objectId '" + objectId + "' but the objectState '"
			        + XX.resolveObject(getAddress(), objectId)
			        + "' is not in the XStateStore. Most likely it has not been save()d.";
			object = new MemoryObject(this, this.eventQueue, objectState);
			this.loadedObjects.put(objectId, object);
			
			return object;
		}
	}
	
	/**
	 * @return the {@link XID} of the father-{@link XRepository} of this
	 *         MemoryModel or null, if this object has no father.
	 */
	protected XID getRepositoryId() {
		return this.father == null ? null : this.father.getID();
	}
	
	public long getRevisionNumber() {
		synchronized(this.eventQueue) {
			return this.state.getRevisionNumber();
		}
	}
	
	protected XModelState getState() {
		return this.state;
	}
	
	@Override
	protected XReadableModel getTransactionTarget() {
		return this;
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
	public int hashCode() {
		int hashCode = getID().hashCode() + (int)getRevisionNumber();
		
		if(this.father != null) {
			hashCode += this.father.getID().hashCode();
		}
		
		return hashCode;
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
	
	public boolean isEmpty() {
		synchronized(this.eventQueue) {
			checkRemoved();
			return this.state.isEmpty();
		}
	}
	
	public Iterator<XID> iterator() {
		synchronized(this.eventQueue) {
			checkRemoved();
			return this.state.iterator();
		}
	}
	
	/**
	 * Removes all {@link XField XFields} of this MemoryObject from the
	 * persistence layer and the MemoryObject itself.
	 */
	protected void removeInternal() {
		// all fields are already loaded for creating events
		
		for(MemoryObject object : this.loadedObjects.values()) {
			object.removeInternal();
			this.state.removeObjectState(object.getID());
		}
		
		this.state.setRevisionNumber(this.state.getRevisionNumber() + 1);
		
		this.loadedObjects.clear();
		
		this.removed = true;
	}
	
	public boolean removeListenerForModelEvents(XModelEventListener changeListener) {
		synchronized(this.eventQueue) {
			return this.modelChangeListenerCollection.remove(changeListener);
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
	protected void removeObjectInternal(XID objectId) {
		
		MemoryObject object = getObject(objectId);
		assert object != null;
		
		boolean inTrans = this.eventQueue.transactionInProgess;
		boolean makeTrans = !object.isEmpty();
		int since = this.eventQueue.getNextPosition();
		enqueueObjectRemoveEvents(this.eventQueue.getActor(), object, makeTrans || inTrans, false);
		
		Orphans orphans = this.eventQueue.orphans;
		if(!inTrans && orphans == null) {
			beginStateTransaction();
		}
		
		// remove the object
		this.loadedObjects.remove(object.getID());
		this.state.removeObjectState(object.getID());
		if(orphans != null) {
			object.removeInternal();
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
	
	/**
	 * Saves the current state information of this MemoryModel with the
	 * currently used persistence layer
	 */
	protected void save() {
		this.state.save(this.eventQueue.stateTransaction);
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
	public String toString() {
		return this.state.toString();
	}
	
	@Override
	public XRevWritableModel createSnapshot() {
		if(this.removed) {
			return null;
		}
		return XCopyUtils.createSnapshot(this);
	}
	
}
