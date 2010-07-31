package org.xydra.core.model.impl.memory;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.xydra.annotations.ReadOperation;
import org.xydra.core.X;
import org.xydra.core.XX;
import org.xydra.core.change.ChangeType;
import org.xydra.core.change.XCommand;
import org.xydra.core.change.XFieldEvent;
import org.xydra.core.change.XFieldEventListener;
import org.xydra.core.change.XModelCommand;
import org.xydra.core.change.XModelEvent;
import org.xydra.core.change.XModelEventListener;
import org.xydra.core.change.XObjectEvent;
import org.xydra.core.change.XObjectEventListener;
import org.xydra.core.change.XTransaction;
import org.xydra.core.change.XTransactionEvent;
import org.xydra.core.change.XTransactionEventListener;
import org.xydra.core.change.impl.memory.MemoryModelEvent;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XBaseModel;
import org.xydra.core.model.XID;
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
public class MemoryModel extends SynchronizesChangesImpl implements XModel, Serializable {
	
	private static final long serialVersionUID = -2969189978307340483L;
	
	private final XModelState state;
	private final Map<XID,MemoryObject> loadedObjects = new HashMap<XID,MemoryObject>();
	
	/** The father-repository of this MemoryModel */
	private final MemoryRepository father;
	
	/** Has this MemoryModel been removed? */
	boolean removed = false;
	
	private Set<XModelEventListener> modelChangeListenerCollection;
	private Set<XObjectEventListener> objectChangeListenerCollection;
	private Set<XFieldEventListener> fieldChangeListenerCollection;
	private Set<XTransactionEventListener> transactionListenerCollection;
	
	/**
	 * Creates a new MemoryModel without father-{@link XRepository}.
	 * 
	 * @param modelId The {@link XID} for this MemoryModel.
	 */
	public MemoryModel(XID modelId) {
		this(null, createModelState(modelId));
	}
	
	private static XModelState createModelState(XID modelId) {
		XAddress modelAddr = X.getIDProvider().fromComponents(null, modelId, null, null);
		XChangeLogState changeLogState = new MemoryChangeLogState(modelAddr, 0L);
		return new TemporaryModelState(modelAddr, changeLogState);
	}
	
	/**
	 * Creates a new MemoryModel without father-{@link XRepository}.
	 * 
	 * @param modelState The initial {@link XModelState} of this MemoryModel.
	 */
	public MemoryModel(XModelState modelState) {
		this(null, modelState);
	}
	
	/**
	 * Creates a new MemoryModel with the given {@link MemoryRepository} as its
	 * father.
	 * 
	 * @param father The father-{@link MemoryRepository} for this MemoryModel
	 * @param modelState The initial {@link XModelState} of this MemoryModel.
	 */
	protected MemoryModel(MemoryRepository father, XModelState modelState) {
		super(new MemoryEventQueue(modelState.getChangeLogState() == null ? null
		        : new MemoryChangeLog(modelState.getChangeLogState())));
		
		this.state = modelState;
		this.father = father;
		
		this.modelChangeListenerCollection = new HashSet<XModelEventListener>();
		this.objectChangeListenerCollection = new HashSet<XObjectEventListener>();
		this.fieldChangeListenerCollection = new HashSet<XFieldEventListener>();
		this.transactionListenerCollection = new HashSet<XTransactionEventListener>();
		
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
	 * @throws IllegalStateException if this method is called after this
	 *             MemoryModel was already removed
	 */
	@Override
	public MemoryObject createObject(XID actor, XID objectID) {
		assert getRevisionNumber() >= 0;
		synchronized(this.eventQueue) {
			checkRemoved();
			
			if(hasObject(objectID)) {
				return getObject(objectID);
			}
			
			MemoryObject object = null;
			
			boolean inTrans = this.eventQueue.transactionInProgess;
			Orphans orphans = getOrphans();
			if(orphans != null) {
				object = orphans.objects.remove(objectID);
			} else if(!inTrans) {
				beginStateTransaction();
			}
			
			if(object == null) {
				XObjectState objectState = this.state.createObjectState(objectID);
				assert XX.contains(getAddress(), objectState.getAddress());
				object = new MemoryObject(this, this.eventQueue, objectState);
			}
			
			this.state.addObjectState(object.getState());
			this.loadedObjects.put(object.getID(), object);
			
			XModelEvent event = MemoryModelEvent.createAddEvent(actor, getAddress(), objectID,
			        getRevisionNumber(), inTrans);
			
			this.eventQueue.enqueueModelEvent(this, event);
			
			// event propagation and revision number increasing happens after
			// all events were successful
			if(!inTrans) {
				
				// increment revision number
				// only increment if this event is no subevent of a transaction
				// (needs to be handled differently)
				object.incrementRevisionAndSave();
				
				if(orphans == null) {
					endStateTransaction();
				}
				
				// propagate events
				this.eventQueue.sendEvents();
				
			}
			
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
	
	/**
	 * @throws IllegalStateException if this method is called after this
	 *             MemoryModel was already removed
	 */
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
	
	/**
	 * @throws IllegalStateException if this method is called after this
	 *             MemoryModel was already removed
	 */
	@ReadOperation
	public Iterator<XID> iterator() {
		synchronized(this.eventQueue) {
			checkRemoved();
			return this.state.iterator();
		}
	}
	
	/**
	 * @throws IllegalStateException if this method is called after this
	 *             MemoryModel was already removed
	 */
	@Override
	public boolean removeObject(XID actor, XID objectID) {
		synchronized(this.eventQueue) {
			checkRemoved();
			
			if(!hasObject(objectID)) {
				return false;
			}
			
			MemoryObject object = getObject(objectID);
			
			assert object != null;
			
			boolean inTrans = this.eventQueue.transactionInProgess;
			boolean makeTrans = !object.isEmpty();
			int since = this.eventQueue.getNextPosition();
			enqueueObjectRemoveEvents(actor, object, makeTrans || inTrans);
			
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
					this.eventQueue.createTransactionEvent(actor, this, null, since);
				}
				
				// increment revision number
				// only increment if this event is no subevent of a
				// transaction (needs to be handled differently)
				incrementRevisionAndSave();
				
				if(orphans == null) {
					endStateTransaction();
				}
				
				// propagate events
				this.eventQueue.sendEvents();
				
			}
			
			return true;
		}
	}
	
	/**
	 * @throws IllegalStateException if this method is called after this
	 *             MemoryModel was already removed
	 */
	public long executeModelCommand(XID actor, XModelCommand command) {
		synchronized(this.eventQueue) {
			checkRemoved();
			
			if(!getAddress().equals(command.getTarget())) {
				return XCommand.FAILED;
			}
			
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
				
				long oldRev = getRevisionNumber();
				
				createObject(actor, command.getObjectID());
				
				return oldRev;
			}
			
			if(command.getChangeType() == ChangeType.REMOVE) {
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
				
				long oldRev = getRevisionNumber();
				
				removeObject(actor, command.getObjectID());
				
				return oldRev;
			}
			
			return XCommand.FAILED;
		}
	}
	
	/**
	 * @throws IllegalStateException if this method is called after this
	 *             MemoryModel was already removed
	 */
	public XID getID() {
		synchronized(this.eventQueue) {
			checkRemoved();
			return this.state.getID();
		}
	}
	
	/**
	 * @throws IllegalStateException if this method is called after this
	 *             MemoryModel was already removed
	 */
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
	protected void save(Object transaction) {
		assert this.eventQueue.stateTransaction == null : "double state transaction detected";
		this.state.save(transaction);
	}
	
	/**
	 * Saves the current state information of this MemoryModel with the
	 * currently used persistence layer
	 */
	private void save() {
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
	
	/**
	 * @throws IllegalStateException if this method is called after this
	 *             MemoryModel was already removed
	 */
	@ReadOperation
	public long getRevisionNumber() {
		synchronized(this.eventQueue) {
			checkRemoved();
			return this.state.getRevisionNumber();
		}
	}
	
	/**
	 * @throws IllegalStateException if this method is called after this
	 *             MemoryModel was already removed
	 */
	@Override
	public long executeTransaction(XID actor, XTransaction transaction) {
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
					return object.executeTransaction(actor, transaction);
				}
			}
			
			return super.executeTransaction(actor, transaction);
			
		}
	}
	
	/**
	 * @throws IllegalStateException if this method is called after this
	 *             MemoryModel was already removed
	 */
	public boolean isEmpty() {
		synchronized(this.eventQueue) {
			checkRemoved();
			return this.state.isEmpty();
		}
	}
	
	/**
	 * @throws IllegalStateException if this method is called after this
	 *             MemoryModel was already removed
	 */
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
	
	/**
	 * Notifies all listeners that have registered interest for notification on
	 * {@link XObjectEvent XObjectEvents} happening on child-
	 * {@link MemoryObject MemoryObjects} of this MemoryModel.
	 * 
	 * @param event The {@link XObjectEvent} which will be propagated to the
	 *            registered listeners.
	 */
	protected void fireObjectEvent(XObjectEvent event) {
		for(XObjectEventListener listener : this.objectChangeListenerCollection) {
			listener.onChangeEvent(event);
		}
	}
	
	/**
	 * Notifies all listeners that have registered interest for notification on
	 * {@link XFieldEvent XFieldEvents} happening on child-{@link MemoryField
	 * MemoryFields} of this MemoryModel.
	 * 
	 * @param event The {@link XFieldEvent} which will be propagated to the
	 *            registered listeners.
	 */
	protected void fireFieldEvent(XFieldEvent event) {
		for(XFieldEventListener listener : this.fieldChangeListenerCollection) {
			listener.onChangeEvent(event);
		}
	}
	
	/**
	 * Notifies all listeners that have registered interest for notification on
	 * {@link XTransactionEvent XTransactionEvents} happening on this
	 * MemoryModel.
	 * 
	 * @param event The {@link XTransactonEvent} which will be propagated to the
	 *            registered listeners.
	 */
	protected void fireTransactionEvent(XTransactionEvent event) {
		for(XTransactionEventListener listener : this.transactionListenerCollection) {
			listener.onChangeEvent(event);
		}
	}
	
	/**
	 * Adds the given {@link XModelEventListener} to this MemoryModel, if
	 * possible.
	 * 
	 * @param changeListener The {@link XModelEventListener} which is to be
	 *            added
	 * @return false, if the given {@link XModelEventListener} was already
	 *         registered on this MemoryModel, true otherwise
	 */
	public boolean addListenerForModelEvents(XModelEventListener changeListener) {
		synchronized(this.eventQueue) {
			return this.modelChangeListenerCollection.add(changeListener);
		}
	}
	
	/**
	 * Removes the given {@link XModelEventListener} from this MemoryModel.
	 * 
	 * @param changeListener The {@link XModelEventListener} which is to be
	 *            removed
	 * @return true, if the given {@link XModelEventListener} was registered on
	 *         this MemoryModel, false otherwise
	 */
	public boolean removeListenerForModelEvents(XModelEventListener changeListener) {
		synchronized(this.eventQueue) {
			return this.modelChangeListenerCollection.remove(changeListener);
		}
	}
	
	/**
	 * Adds the given {@link XObjectEventListener} to this MemoryModel, if
	 * possible.
	 * 
	 * @param changeListener The {@link XObjectEventListener} which is to be
	 *            added
	 * @return false, if the given {@link XObjectEventListener} was already
	 *         registered on this MemoryModel, true otherwise
	 */
	public boolean addListenerForObjectEvents(XObjectEventListener changeListener) {
		synchronized(this.eventQueue) {
			return this.objectChangeListenerCollection.add(changeListener);
		}
	}
	
	/**
	 * Removes the given {@link XObjectEventListener} from this MemoryModel.
	 * 
	 * @param changeListener The {@link XObjectEventListener} which is to be
	 *            removed
	 * @return true, if the given {@link XObjectEventListener} was registered on
	 *         this MemoryModel, false otherwise
	 */
	public boolean removeListenerForObjectEvents(XObjectEventListener changeListener) {
		synchronized(this.eventQueue) {
			return this.objectChangeListenerCollection.remove(changeListener);
		}
	}
	
	/**
	 * Adds the given {@link XFieldEventListener} to this MemoryModel, if
	 * possible.
	 * 
	 * @param changeListener The {@link XFieldEventListener} which is to be
	 *            added
	 * @return false, if the given {@link XFieldEventListener} was already
	 *         registered on this MemoryModel, true otherwise
	 */
	public boolean addListenerForFieldEvents(XFieldEventListener changeListener) {
		synchronized(this.eventQueue) {
			return this.fieldChangeListenerCollection.add(changeListener);
		}
	}
	
	/**
	 * Removes the given {@link XFieldEventListener} from this MemoryModel.
	 * 
	 * @param changeListener The {@link XFieldEventListener} which is to be
	 *            removed
	 * @return true, if the given {@link XFieldEventListener} was registered on
	 *         this MemoryModel, false otherwise
	 */
	public boolean removeListenerForFieldEvents(XFieldEventListener changeListener) {
		synchronized(this.eventQueue) {
			return this.fieldChangeListenerCollection.remove(changeListener);
		}
	}
	
	/**
	 * Adds the given {@link XTransactionEventListener} to this MemoryModel, if
	 * possible.
	 * 
	 * @param changeListener The {@link XTransactionEventListener} which is to
	 *            be added
	 * @return false, if the given {@link XTransactionEventListener} was already
	 *         registered on this MemoryModel, true otherwise
	 */
	public boolean addListenerForTransactionEvents(XTransactionEventListener changeListener) {
		synchronized(this.eventQueue) {
			return this.transactionListenerCollection.add(changeListener);
		}
	}
	
	/**
	 * Removes the given {@link XTransactionEventListener} from this
	 * MemoryModel.
	 * 
	 * @param changeListener The {@link XTransactionEventListener} which is to
	 *            be removed
	 * @return true, if the given {@link XTransactionEventListener} was
	 *         registered on this MemoryModel, false otherwise
	 */
	public boolean removeListenerForTransactionEvents(XTransactionEventListener changeListener) {
		synchronized(this.eventQueue) {
			return this.transactionListenerCollection.remove(changeListener);
		}
	}
	
	/**
	 * Creates all {@link XEvent XEvents} which will represent the removal of
	 * the given {@link MemoryObject}. All necessary {@link XObjectEvent
	 * XObjectEvents} of the REMOVE-type will and lastly the {@link XModelEvent}
	 * representing the actual removal of the {@link MemoryObject} will be
	 * created to accurately represent the removal. The created {@link XEvent
	 * XEvents} will then be enqueued into the {@link MemoryEventQueue} used by
	 * this MemoryModel and then be propagated to the interested listeners.
	 * 
	 * @param actor The {@link XID} of the actor
	 * @param object The {@link MemoryObject} which is to be removed (must not
	 *            be null)
	 * @param inTrans true, if the removal of this {@link MemoryObject} occurs
	 *            during an {@link XTransaction}.
	 * @throws IllegalArgumentException if the given {@link MemoryOjbect} equals
	 *             null
	 */
	protected void enqueueObjectRemoveEvents(XID actor, MemoryObject object, boolean inTrans) {
		
		if(object == null) {
			throw new IllegalArgumentException("object must not be null");
		}
		
		for(XID fieldID : object) {
			assert inTrans;
			MemoryField field = object.getField(fieldID);
			object.enqueueFieldRemoveEvents(actor, field, inTrans);
		}
		
		// add event to remove the object
		XModelEvent event = MemoryModelEvent.createRemoveEvent(actor, getAddress(), object.getID(),
		        getRevisionNumber(), object.getRevisionNumber(), inTrans);
		this.eventQueue.enqueueModelEvent(this, event);
		
	}
	
	/**
	 * Deletes the state information of this MemoryModel from the currently used
	 * persistence layer
	 */
	protected void delete(Object transaction) {
		assert this.eventQueue.stateTransaction == null : "double state transaction detected";
		this.eventQueue.stateTransaction = transaction;
		delete();
		this.eventQueue.stateTransaction = null;
	}
	
	/**
	 * Deletes the state information of this MemoryModel from the currently used
	 * persistence layer
	 */
	private void delete() {
		for(XID objectId : this) {
			MemoryObject object = getObject(objectId);
			object.delete();
		}
		this.state.delete(this.eventQueue.stateTransaction);
		this.eventQueue.setBlockSending(true);
		this.eventQueue.deleteLog();
		this.removed = true;
	}
	
	/**
	 * @throws IllegalStateException if this method is called after this
	 *             MemoryModel was already removed
	 */
	public long executeCommand(XID actor, XCommand command) {
		synchronized(this.eventQueue) {
			checkRemoved();
			
			if(command instanceof XTransaction) {
				return executeTransaction(actor, (XTransaction)command);
			}
			if(command instanceof XModelCommand) {
				return executeModelCommand(actor, (XModelCommand)command);
			}
			MemoryObject object = getObject(command.getTarget().getObject());
			if(object == null) {
				return XCommand.FAILED;
			}
			return object.executeCommand(actor, command);
		}
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
	
}
