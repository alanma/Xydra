package org.xydra.core.model.impl.memory;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.xydra.annotations.ReadOperation;
import org.xydra.core.X;
import org.xydra.core.XX;
import org.xydra.core.change.ChangeType;
import org.xydra.core.change.XAtomicCommand;
import org.xydra.core.change.XAtomicEvent;
import org.xydra.core.change.XCommand;
import org.xydra.core.change.XEvent;
import org.xydra.core.change.XFieldCommand;
import org.xydra.core.change.XFieldEvent;
import org.xydra.core.change.XFieldEventListener;
import org.xydra.core.change.XModelCommand;
import org.xydra.core.change.XModelEvent;
import org.xydra.core.change.XModelEventListener;
import org.xydra.core.change.XObjectCommand;
import org.xydra.core.change.XObjectEvent;
import org.xydra.core.change.XObjectEventListener;
import org.xydra.core.change.XTransaction;
import org.xydra.core.change.XTransactionEvent;
import org.xydra.core.change.XTransactionEventListener;
import org.xydra.core.change.impl.memory.MemoryFieldCommand;
import org.xydra.core.change.impl.memory.MemoryModelCommand;
import org.xydra.core.change.impl.memory.MemoryModelEvent;
import org.xydra.core.change.impl.memory.MemoryObjectCommand;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XBaseModel;
import org.xydra.core.model.XID;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;
import org.xydra.core.model.state.XModelState;
import org.xydra.core.model.state.XObjectState;
import org.xydra.core.model.state.impl.memory.TemporaryModelState;


/**
 * An implementation of {@link XModel}.
 * 
 * @author voelkel
 * @author Kaidel
 * 
 */
public class MemoryModel extends TransactionManager implements XModel, Serializable {
	
	private static final long serialVersionUID = -2969189978307340483L;
	
	private final XModelState state;
	private final Map<XID,MemoryObject> loadedObjects = new HashMap<XID,MemoryObject>();
	
	/** The father-repository of this XModel */
	private final MemoryRepository father;
	
	/** Has this object been removed? */
	boolean removed = false;
	
	private Set<XModelEventListener> modelChangeListenerCollection;
	private Set<XObjectEventListener> objectChangeListenerCollection;
	private Set<XFieldEventListener> fieldChangeListenerCollection;
	private Set<XTransactionEventListener> transactionListenerCollection;
	
	/**
	 * Creates a new MemoryModel without father.
	 * 
	 * @param modelId The {@link XID} for the model.
	 */
	public MemoryModel(XID modelId) {
		this(null, new TemporaryModelState(X.getIDProvider().fromComponents(null, modelId, null,
		        null), 0));
	}
	
	/**
	 * Creates a new MemoryModel without father.
	 * 
	 * @param modelState The {@link XModelState} for the model.
	 */
	public MemoryModel(XModelState modelState) {
		this(null, modelState);
	}
	
	/**
	 * Creates a new MemoryModel with the given repository as its father.
	 * 
	 * @param father The father-repository of this model
	 * @param modelState initial state
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
	 * @throws IllegalStateException if this model has already been removed
	 */
	@Override
	protected void checkRemoved() throws IllegalStateException {
		if(this.removed) {
			throw new IllegalStateException("this model has been removed");
		}
	}
	
	public MemoryObject createObject(XID actor, XID objectID) {
		return createObject(actor, objectID, null);
	}
	
	@Override
	protected MemoryObject createObject(XID actor, XID objectID, Orphans orphans) {
		assert getRevisionNumber() >= 0;
		synchronized(this.eventQueue) {
			checkRemoved();
			
			if(hasObject(objectID)) {
				return getObject(objectID);
			}
			
			MemoryObject object = null;
			
			if(orphans != null) {
				object = orphans.objects.remove(objectID);
			}
			
			if(object == null) {
				XObjectState objectState = this.state.createObjectState(objectID);
				assert XX.contains(getAddress(), objectState.getAddress());
				object = new MemoryObject(this, this.eventQueue, objectState);
			}
			
			this.state.addObjectState(object.getState());
			this.loadedObjects.put(object.getID(), object);
			
			XModelEvent event = MemoryModelEvent.createAddEvent(actor, getAddress(), objectID,
			        getRevisionNumber(), transactionInProgress());
			
			this.eventQueue.enqueueModelEvent(this, event);
			
			// event propagation and revision number increasing happens after
			// all events were successful
			if(!transactionInProgress()) {
				
				// increment revision number
				// only increment if this event is no subevent of a transaction
				// (needs to be handled differently)
				object.incrementRevisionAndSave();
				
				// propagate events
				this.eventQueue.sendEvents();
				
			}
			
			return object;
		}
	}
	
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
	
	@ReadOperation
	public Iterator<XID> iterator() {
		synchronized(this.eventQueue) {
			checkRemoved();
			return this.state.iterator();
		}
	}
	
	public boolean removeObject(XID actor, XID objectID) {
		return removeObject(actor, objectID, null);
	}
	
	@Override
	protected boolean removeObject(XID actor, XID objectID, Orphans orphans) {
		synchronized(this.eventQueue) {
			checkRemoved();
			
			if(!hasObject(objectID)) {
				return false;
			}
			
			MemoryObject object = getObject(objectID);
			
			assert object != null;
			
			boolean makeTrans = !object.isEmpty();
			int since = this.eventQueue.getNextPosition();
			enqueueObjectRemoveEvents(actor, object, makeTrans || transactionInProgress());
			
			// remove the object
			this.loadedObjects.remove(object.getID());
			this.state.removeObjectState(object.getID());
			if(orphans != null) {
				orphans.objects.put(object.getID(), object);
			} else {
				object.delete();
			}
			
			// event propagation and revision number increasing for transactions
			// happens after all events of a transaction were successful
			if(!transactionInProgress()) {
				
				if(makeTrans) {
					this.eventQueue.createTransactionEvent(actor, this, null, since);
				}
				
				// increment revision number
				// only increment if this event is no subevent of a
				// transaction (needs to be handled differently)
				incrementRevisionAndSave();
				
				// propagate events
				this.eventQueue.sendEvents();
				
			}
			
			return true;
		}
	}
	
	public long executeModelCommand(XID actor, XModelCommand command) {
		return executeModelCommand(actor, command, null);
	}
	
	public long executeModelCommand(XID actor, XModelCommand command, Orphans orphans) {
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
				
				createObject(actor, command.getObjectID(), orphans);
				
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
				
				removeObject(actor, command.getObjectID(), orphans);
				
				return oldRev;
			}
			
			return XCommand.FAILED;
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
		assert !transactionInProgress();
		long newRevision = getRevisionNumber() + 1;
		this.state.setRevisionNumber(newRevision);
		save();
	}
	
	protected void save() {
		this.state.save();
	}
	
	/**
	 * Returns the father of this model.
	 * 
	 * @return The father of this model (may be null).
	 */
	@ReadOperation
	protected MemoryRepository getFather() {
		checkRemoved();
		return this.father;
	}
	
	/**
	 * Checks whether this model has a father or not.
	 * 
	 * @return true, if this model has a father, false otherwise.
	 */
	@ReadOperation
	protected boolean hasFather() {
		checkRemoved();
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
	
	@ReadOperation
	public long getRevisionNumber() {
		synchronized(this.eventQueue) {
			checkRemoved();
			return this.state.getRevisionNumber();
		}
	}
	
	@Override
	public long executeTransaction(XID actor, XTransaction transaction, Orphans orphans) {
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
			
			return super.executeTransaction(actor, transaction, orphans);
			
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
	 * ModelEvents.
	 * 
	 * @param event The event object.
	 */
	protected void fireModelEvent(XModelEvent event) {
		for(XModelEventListener listener : this.modelChangeListenerCollection) {
			listener.onChangeEvent(event);
		}
	}
	
	/**
	 * Notifies all listeners that have registered interest for notification on
	 * ObjectEvents.
	 * 
	 * @param event The event object.
	 */
	protected void fireObjectEvent(XObjectEvent event) {
		for(XObjectEventListener listener : this.objectChangeListenerCollection) {
			listener.onChangeEvent(event);
		}
	}
	
	/**
	 * Notifies all listeners of the object this model holds that have
	 * registered interest for notification on ObjectEvents.
	 * 
	 * @param event The event object.
	 */
	protected void fireFieldEvent(XFieldEvent event) {
		for(XFieldEventListener listener : this.fieldChangeListenerCollection) {
			listener.onChangeEvent(event);
		}
	}
	
	/**
	 * Notifies all listeners of the object this model holds that have
	 * registered interest for notification on TransactionEvents.
	 * 
	 * @param event The event object.
	 */
	protected void fireTransactionEvent(XTransactionEvent event) {
		for(XTransactionEventListener listener : this.transactionListenerCollection) {
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
	
	public boolean addListenerForObjectEvents(XObjectEventListener changeListener) {
		synchronized(this.eventQueue) {
			return this.objectChangeListenerCollection.add(changeListener);
		}
	}
	
	public boolean removeListenerForObjectEvents(XObjectEventListener changeListener) {
		synchronized(this.eventQueue) {
			return this.objectChangeListenerCollection.remove(changeListener);
		}
	}
	
	public boolean addListenerForFieldEvents(XFieldEventListener changeListener) {
		synchronized(this.eventQueue) {
			return this.fieldChangeListenerCollection.add(changeListener);
		}
	}
	
	public boolean removeListenerForFieldEvents(XFieldEventListener changeListener) {
		synchronized(this.eventQueue) {
			return this.fieldChangeListenerCollection.remove(changeListener);
		}
	}
	
	public boolean addListenerForTransactionEvents(XTransactionEventListener changeListener) {
		synchronized(this.eventQueue) {
			return this.transactionListenerCollection.add(changeListener);
		}
	}
	
	public boolean removeListenerForTransactionEvents(XTransactionEventListener changeListener) {
		synchronized(this.eventQueue) {
			return this.transactionListenerCollection.remove(changeListener);
		}
	}
	
	/**
	 * Builds a transaction that first removes all values of the given object,
	 * then all fields of the given object and finally the given object itself.
	 * 
	 * @param actor The actor for this transaction
	 * @param object The object which should be removed by the transaction
	 * @return An {@link ModelTransaction} that first removes all values of the
	 *         given object, then all fields of the given object and finally the
	 *         given object itself.
	 */
	private void enqueueObjectRemoveEvents(XID actor, MemoryObject object, boolean inTrans) {
		
		if(object == null) {
			throw new NullPointerException("object must not be null");
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
	
	protected void delete() {
		for(XID objectId : this) {
			MemoryObject object = getObject(objectId);
			object.delete();
		}
		this.state.delete();
		this.removed = true;
	}
	
	public long executeCommand(XID actor, XCommand command) {
		return executeCommand(actor, command, null);
	}
	
	private long executeCommand(XID actor, XCommand command, Orphans orphans) {
		synchronized(this.eventQueue) {
			checkRemoved();
			
			if(command instanceof XTransaction) {
				return executeTransaction(actor, (XTransaction)command, orphans);
			}
			if(command instanceof XModelCommand) {
				return executeModelCommand(actor, (XModelCommand)command, orphans);
			}
			MemoryObject object = getObject(command.getTarget().getObject());
			if(object == null) {
				return XCommand.FAILED;
			}
			return object.executeCommand(actor, command, orphans);
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
	
	public MemoryChangeLog getChangeLog() {
		return this.eventQueue.getChangeLog();
	}
	
	public void rollback(long revision) {
		rollback(revision, null);
	}
	
	private void rollback(long revision, Orphans orphans) {
		
		if(revision < 0) {
			throw new RuntimeException("invalid revision number: " + revision);
		}
		
		if(revision == getRevisionNumber()) {
			return;
		}
		
		// stop the change log to prevent the rollback events from being logged
		boolean oldLogging = this.eventQueue.setLogging(false);
		
		// rollback each event individually
		for(long i = getRevisionNumber() - 1; i >= revision; i--) {
			XEvent event = getChangeLog().getEventAt(i);
			if(event instanceof XAtomicEvent) {
				rollbackEvent((XAtomicEvent)event, orphans);
			} else {
				assert event instanceof XTransactionEvent;
				XTransactionEvent trans = (XTransactionEvent)event;
				for(int j = trans.size() - 1; j >= 0; j--) {
					XAtomicEvent atomicEvent = trans.getEvent(j);
					rollbackEvent(atomicEvent, orphans);
				}
			}
			
		}
		
		// reset the change log
		getChangeLog().truncateToRevision(getRevisionNumber());
		this.eventQueue.setLogging(oldLogging);
		
		save();
		
		assert getRevisionNumber() == revision;
	}
	
	private void rollbackEvent(XAtomicEvent event, Orphans orphans) {
		XAtomicCommand command = XX.createForcedUndoCommand(event);
		long result = executeCommand(null, command, orphans);
		assert result > 0 : "rollback command " + command + " for event " + event + " failed";
		XAddress target = event.getTarget();
		
		// fix revision numbers
		this.state.setRevisionNumber(event.getModelRevisionNumber());
		if(target.getObject() != null) {
			MemoryObject object = getObject(target.getObject());
			object.setRevisionNumber(event.getObjectRevisionNumber());
			object.save();
			if(target.getField() != null) {
				MemoryField field = object.getField(target.getField());
				field.setRevisionNumber(event.getFieldRevisionNumber());
				field.save();
			} else if(event.getChangeType() == ChangeType.REMOVE) {
				MemoryField field = object.getField(((XObjectEvent)event).getFieldID());
				field.setRevisionNumber(event.getFieldRevisionNumber());
				field.save();
			}
		} else if(event.getChangeType() == ChangeType.REMOVE) {
			MemoryObject field = getObject(((XModelEvent)event).getObjectID());
			field.setRevisionNumber(event.getObjectRevisionNumber());
			field.save();
		}
	}
	
	public long[] syncChanges(List<XEvent> remoteChanges, long lastRevision, XID actor,
	        List<XCommand> localChanges) {
		
		long[] results = new long[localChanges.size()];
		
		boolean oldBlock = this.eventQueue.setBlockSending(true);
		
		try {
			
			Orphans orphans = new Orphans();
			
			int pos = this.eventQueue.getNextPosition();
			
			// Roll back to the old revision and save removed entities.
			rollback(lastRevision, orphans);
			
			// Apply the remote changes.
			for(XEvent remoteChange : remoteChanges) {
				long result = executeCommand(remoteChange.getActor(), XX
				        .createReplayCommand(remoteChange), orphans);
				if(result < 0) {
					throw new IllegalStateException("could not apply remote change: "
					        + remoteChange);
				}
			}
			
			// Re-apply the local changes.
			long nRemote = remoteChanges.size();
			for(int i = 0; i < localChanges.size(); i++) {
				XCommand command = localChanges.get(i);
				
				// Adapt the command if needed.
				if(command instanceof XModelCommand) {
					XModelCommand mc = (XModelCommand)command;
					if(mc.getChangeType() == ChangeType.REMOVE
					        && mc.getRevisionNumber() > lastRevision) {
						command = MemoryModelCommand.createRemoveCommand(mc.getTarget(), mc
						        .getRevisionNumber()
						        + nRemote, mc.getObjectID());
						localChanges.set(i, command);
					}
				} else if(command instanceof XObjectCommand) {
					XObjectCommand oc = (XObjectCommand)command;
					if(oc.getChangeType() == ChangeType.REMOVE
					        && oc.getRevisionNumber() > lastRevision) {
						command = MemoryObjectCommand.createRemoveCommand(oc.getTarget(), oc
						        .getRevisionNumber()
						        + nRemote, oc.getFieldID());
						localChanges.set(i, command);
					}
				} else if(command instanceof XFieldCommand) {
					XFieldCommand fc = (XFieldCommand)command;
					if(fc.getRevisionNumber() > lastRevision) {
						switch(command.getChangeType()) {
						case ADD:
							command = MemoryFieldCommand.createAddCommand(fc.getTarget(), fc
							        .getRevisionNumber()
							        + nRemote, fc.getValue());
							break;
						case REMOVE:
							command = MemoryFieldCommand.createRemoveCommand(fc.getTarget(), fc
							        .getRevisionNumber()
							        + nRemote);
							break;
						case CHANGE:
							command = MemoryFieldCommand.createChangeCommand(fc.getTarget(), fc
							        .getRevisionNumber()
							        + nRemote, fc.getValue());
							break;
						default:
							assert false : "Invalid command: " + fc;
						}
						localChanges.set(i, command);
					}
				}
				
				results[i] = executeCommand(actor, command, orphans);
			}
			
			// Clean unneeded events.
			this.eventQueue.cleanEvents(pos);
			
			for(MemoryObject object : orphans.objects.values()) {
				object.delete();
			}
			
			for(MemoryField field : orphans.fields.values()) {
				field.delete();
			}
			
		} finally {
			
			this.eventQueue.setBlockSending(oldBlock);
			this.eventQueue.sendEvents();
			
		}
		
		return results;
	}
}
