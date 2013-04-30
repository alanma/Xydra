package org.xydra.core.model.impl.memory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.xydra.annotations.NeverNull;
import org.xydra.annotations.ReadOperation;
import org.xydra.base.XAddress;
import org.xydra.base.XId;
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
import org.xydra.base.rmof.XRevWritableObject;
import org.xydra.base.rmof.impl.memory.SimpleModel;
import org.xydra.core.XCopyUtils;
import org.xydra.core.change.XModelEventListener;
import org.xydra.core.change.XModelSyncEventListener;
import org.xydra.core.change.XSendsModelSyncEvents;
import org.xydra.core.model.XChangeLog;
import org.xydra.core.model.XChangeLogState;
import org.xydra.core.model.XField;
import org.xydra.core.model.XLocalChangeCallback;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;
import org.xydra.core.model.XRepository;
import org.xydra.core.model.XUndoFailedLocalChangeCallback;
import org.xydra.sharedutils.XyAssert;


/**
 * An implementation of {@link XModel}.
 * 
 * @author voelkel
 * @author Kaidel
 * 
 */
public class MemoryModel extends SynchronizesChangesImpl implements XModel, XSendsModelSyncEvents {
	
	private static final long serialVersionUID = -2969189978307340483L;
	
	/** The father-repository of this MemoryModel */
	private final MemoryRepository father;
	
	private final Map<XId,MemoryObject> loadedObjects = new HashMap<XId,MemoryObject>();
	
	private final Set<XModelEventListener> modelChangeListenerCollection;
	
	private final XRevWritableModel state;
	
	private final Set<XModelSyncEventListener> modelSyncChangeListenerCollection;
	
	/**
	 * Creates a new MemoryModel with the given {@link MemoryRepository} as its
	 * father.
	 * 
	 * @param actorId TODO
	 * @param father The father-{@link MemoryRepository} for this MemoryModel
	 * @param modelState The initial {@link XModelState} of this MemoryModel.
	 */
	protected MemoryModel(XId actorId, String passwordHash, MemoryRepository father,
	        XRevWritableModel modelState, XChangeLogState log) {
		this(actorId, passwordHash, father, modelState, log, modelState.getRevisionNumber(), false);
	}
	
	/**
	 * Creates a new MemoryModel with the given {@link MemoryRepository} as its
	 * father, the sync revision can be set and optionally localChanges be
	 * rebuild in order to fully restore the state of a serialized MemoryModel.
	 * 
	 * @param actorId TODO
	 * @param passwordHash
	 * @param father The father-{@link MemoryRepository} for this MemoryModel
	 * @param modelState The initial {@link XChangeLogState} of this
	 *            MemoryModel.
	 * @param log
	 * @param syncRev the synchronized revision of this MemoryModel
	 * @param loadLocalChanges if true, the
	 *            {@link MemoryEventManager#localChanges} will be
	 *            initialized/rebuild from the provided {@link XChangeLog} log.
	 */
	public MemoryModel(XId actorId, String passwordHash, MemoryRepository father,
	        XRevWritableModel modelState, XChangeLogState log, long syncRev,
	        boolean loadLocalChanges) {
		this(actorId, passwordHash, father, modelState, log, syncRev, loadLocalChanges, null);
	}
	
	public MemoryModel(XId actorId, String passwordHash, MemoryRepository father,
	        XRevWritableModel modelState, XChangeLogState log, long syncRev,
	        boolean loadLocalChanges, List<XCommand> localChangesAsCommands) {
		super(new MemoryEventManager(actorId, passwordHash, new MemoryChangeLog(log), syncRev));
		XyAssert.xyAssert(log != null);
		assert log != null;
		
		this.state = modelState;
		this.father = father;
		
		this.modelChangeListenerCollection = new HashSet<XModelEventListener>();
		this.modelSyncChangeListenerCollection = new HashSet<XModelSyncEventListener>();
		
		if(father == null && getAddress().getRepository() != null
		        && this.eventQueue.getChangeLog() != null
		        && this.eventQueue.getChangeLog().getCurrentRevisionNumber() == -1) {
			XAddress repoAddr = getAddress().getParent();
			XCommand createCommand = MemoryRepositoryCommand.createAddCommand(repoAddr, true,
			        getId());
			// FIXME Thomas
			// this.eventQueue.newLocalChange(createCommand, null);
			XRepositoryEvent createEvent = MemoryRepositoryEvent.createAddEvent(actorId, repoAddr,
			        getId());
			this.eventQueue.enqueueRepositoryEvent(null, createEvent);
			this.eventQueue.sendEvents();
		}
		
		if(loadLocalChanges && localChangesAsCommands != null) {
			
			Iterator<XCommand> localCommands = localChangesAsCommands.iterator();
			
			while(localCommands.hasNext()) {
				XCommand command = localCommands.next();
				
				this.eventQueue.newLocalChange(command, new XUndoFailedLocalChangeCallback(command,
				        this, null));
			}
			
		}
		XyAssert.xyAssert(
		        this.eventQueue.getChangeLog() == null
		                || this.eventQueue.getChangeLog().getCurrentRevisionNumber() == getRevisionNumber(),
		        "eventQueue.changeLog==null?" + (this.eventQueue.getChangeLog() == null)
		                + "; getRevisionNumber()=" + getRevisionNumber());
		
	}
	
	/**
	 * Creates a new MemoryModel without father-{@link XRepository} but with a
	 * parent repository XId so it can be synchronized.
	 * 
	 * @param actorId TODO
	 * @param passwordHash
	 * @param modelAddr The {@link XAddress} for this MemoryModel.
	 */
	public MemoryModel(XId actorId, String passwordHash, XAddress modelAddr) {
		this(actorId, passwordHash, new SimpleModel(modelAddr));
	}
	
	/**
	 * Creates a new MemoryModel without father-{@link XRepository}.
	 * 
	 * @param actorId TODO
	 * @param passwordHash
	 * @param modelId The {@link XId} for this MemoryModel.
	 */
	public MemoryModel(XId actorId, String passwordHash, XId modelId) {
		this(actorId, passwordHash, XX.toAddress(null, modelId, null, null));
	}
	
	/**
	 * Creates a new MemoryModel without father-{@link XRepository}.
	 * 
	 * @param actorId TODO
	 * @param passwordHash
	 * @param modelState The initial {@link XRevWritableModel} state of this
	 *            MemoryModel.
	 */
	public MemoryModel(XId actorId, String passwordHash, XRevWritableModel modelState) {
		this(actorId, passwordHash, modelState, createChangeLog(modelState));
	}
	
	/**
	 * Creates a new MemoryModel without father-{@link XRepository}.
	 * 
	 * @param actorId TODO
	 * @param passwordHash
	 * @param modelState The initial {@link XRevWritableModel} state of this
	 *            MemoryModel.
	 * @param log
	 */
	public MemoryModel(XId actorId, String passwordHash, XRevWritableModel modelState,
	        XChangeLogState log) {
		this(actorId, passwordHash, null, modelState, log);
	}
	
	private static XChangeLogState createChangeLog(XRevWritableModel modelState) {
		XChangeLogState log = new MemoryChangeLogState(modelState.getAddress());
		if(modelState.getRevisionNumber() != 0 || modelState.getAddress().getRepository() == null) {
			// Bump the log revision.
			log.setFirstRevisionNumber(modelState.getRevisionNumber() + 1);
		} else {
			XyAssert.xyAssert(log.getCurrentRevisionNumber() == -1);
		}
		return log;
	}
	
	@Override
	public boolean addListenerForModelEvents(XModelEventListener changeListener) {
		synchronized(this.eventQueue) {
			return this.modelChangeListenerCollection.add(changeListener);
		}
	}
	
	/**
	 * @throws IllegalStateException if this method is called after this
	 *             MemoryModel was already removed
	 */
	@Override
	protected void checkRemoved() throws IllegalStateException {
		if(this.removed) {
			throw new IllegalStateException("this model has been removed: " + getAddress());
		}
	}
	
	@Override
	protected void checkSync() {
		// models can always sync
	}
	
	@Override
	public MemoryObject createObject(@NeverNull XId objectId) {
		
		XModelCommand command = MemoryModelCommand.createAddCommand(getAddress(), true, objectId);
		
		// synchronize so that return is never null if command succeeded
		synchronized(this.eventQueue) {
			long result = executeModelCommand(command);
			MemoryObject object = getObject(objectId);
			XyAssert.xyAssert(result == XCommand.FAILED || object != null);
			return object;
		}
	}
	
	@Override
	protected MemoryObject createObjectInternal(XId objectId) {
		XyAssert.xyAssert(getRevisionNumber() >= 0);
		
		XyAssert.xyAssert(!hasObject(objectId));
		
		boolean inTrans = this.eventQueue.transactionInProgess;
		
		MemoryObject object = null;
		Orphans orphans = this.eventQueue.orphans;
		if(orphans != null) {
			object = orphans.objects.remove(objectId);
		}
		if(object == null) {
			XRevWritableObject objectState = this.state.createObject(objectId);
			XyAssert.xyAssert(getAddress().contains(objectState.getAddress()));
			object = new MemoryObject(this, this.eventQueue, objectState);
		} else {
			this.state.addObject(object.getState());
		}
		XyAssert.xyAssert(object.getModel() == this);
		
		this.loadedObjects.put(object.getId(), object);
		
		XModelEvent event = MemoryModelEvent.createAddEvent(this.eventQueue.getActor(),
		        getAddress(), objectId, getRevisionNumber(), inTrans);
		
		this.eventQueue.enqueueModelEvent(this, event);
		
		// event propagation and revision number increasing happens after
		// all events were successful
		if(!inTrans) {
			
			object.incrementRevision();
			
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
		for(XId objectId : this) {
			MemoryObject object = getObject(objectId);
			object.delete();
		}
		for(XId objectId : this.loadedObjects.keySet()) {
			this.state.removeObject(objectId);
		}
		this.state.setRevisionNumber(this.state.getRevisionNumber() + 1);
		this.loadedObjects.clear();
		this.removed = true;
	}
	
	protected boolean enqueueModelRemoveEvents(XId actorId) {
		
		boolean inTrans = false;
		
		for(XId objectId : this) {
			MemoryObject object = getObject(objectId);
			enqueueObjectRemoveEvents(actorId, object, true, true);
			inTrans = true;
		}
		
		XAddress repoAdrr = hasFather() ? getFather().getAddress() : getAddress().getParent();
		XRepositoryEvent event = MemoryRepositoryEvent.createRemoveEvent(actorId, repoAdrr,
		        getId(), getCurrentRevisionNumber(), inTrans);
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
	 * @param actor The {@link XId} of the actor
	 * @param object The {@link MemoryObject} which is to be removed (must not
	 *            be null)
	 * @param inTrans true, if the removal of this {@link MemoryObject} occurs
	 *            during an {@link XTransaction}.
	 * @param implied true if this model is also removed in the same transaction
	 * @throws IllegalArgumentException if the given {@link MemoryOjbect} equals
	 *             null
	 */
	protected void enqueueObjectRemoveEvents(XId actor, MemoryObject object, boolean inTrans,
	        boolean implied) {
		
		if(object == null) {
			throw new IllegalArgumentException("object must not be null");
		}
		
		for(XId fieldId : object) {
			XyAssert.xyAssert(inTrans);
			MemoryField field = object.getField(fieldId);
			object.enqueueFieldRemoveEvents(actor, field, inTrans, true);
		}
		
		// add event to remove the object
		XModelEvent event = MemoryModelEvent.createRemoveEvent(actor, getAddress(), object.getId(),
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
		
		synchronized(this.eventQueue) {
			// compare revision number, repository ID & modelId
			if(this.father != null) {
				if(model.father == null) {
					return false;
				}
				
				return (getRevisionNumber() == model.getRevisionNumber())
				        && (this.father.getId().equals(model.father.getId()))
				        && (getId().equals(model.getId()));
			} else {
				if(model.father != null) {
					return false;
				}
				
				return (getRevisionNumber() == model.getRevisionNumber())
				        && (getId().equals(model.getId()));
			}
		}
	}
	
	@Override
	public long executeCommand(XCommand command) {
		return executeCommand(command, null);
	}
	
	@Override
	public long executeCommand(XCommand command, XLocalChangeCallback callback) {
		XUndoFailedLocalChangeCallback wrappingCallback = new XUndoFailedLocalChangeCallback(
		        command, this, callback);
		if(command instanceof XTransaction) {
			return executeTransaction((XTransaction)command, wrappingCallback);
		} else if(command instanceof XModelCommand) {
			return executeModelCommand((XModelCommand)command, wrappingCallback);
		}
		MemoryObject object = getObject(command.getTarget().getObject());
		if(object == null) {
			return XCommand.FAILED;
		}
		return object.executeCommand(command, wrappingCallback);
	}
	
	@Override
	public long executeModelCommand(XModelCommand command) {
		return executeModelCommand(command, null);
	}
	
	protected long executeModelCommand(XModelCommand command, XLocalChangeCallback callback) {
		synchronized(this.eventQueue) {
			checkRemoved();
			
			XyAssert.xyAssert(!this.eventQueue.transactionInProgess);
			
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
	
	@Override
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
	@Override
	@ReadOperation
	public MemoryRepository getFather() {
		return this.father;
	}
	
	@Override
	public XId getId() {
		synchronized(this.eventQueue) {
			return this.state.getId();
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
	public MemoryObject getObject(@NeverNull XId objectId) {
		synchronized(this.eventQueue) {
			checkRemoved();
			
			MemoryObject object = this.loadedObjects.get(objectId);
			if(object != null) {
				return object;
			}
			
			XRevWritableObject objectState = this.state.getObject(objectId);
			if(objectState == null) {
				return null;
			}
			
			object = new MemoryObject(this, this.eventQueue, objectState);
			this.loadedObjects.put(objectId, object);
			
			return object;
		}
	}
	
	/**
	 * @return the {@link XId} of the father-{@link XRepository} of this
	 *         MemoryModel or null, if this object has no father.
	 */
	protected XId getRepositoryId() {
		return this.father == null ? null : this.father.getId();
	}
	
	@Override
	public long getRevisionNumber() {
		synchronized(this.eventQueue) {
			return this.state.getRevisionNumber();
		}
	}
	
	protected XRevWritableModel getState() {
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
		synchronized(this.eventQueue) {
			int hashCode = getId().hashCode() + (int)getRevisionNumber();
			
			if(this.father != null) {
				hashCode += this.father.getId().hashCode();
			}
			
			return hashCode;
		}
	}
	
	@Override
	public boolean hasObject(@NeverNull XId id) {
		synchronized(this.eventQueue) {
			checkRemoved();
			return this.loadedObjects.containsKey(id) || this.state.hasObject(id);
		}
	}
	
	@Override
	protected void incrementRevision() {
		XyAssert.xyAssert(!this.eventQueue.transactionInProgess);
		long newRevision = getRevisionNumber() + 1;
		this.state.setRevisionNumber(newRevision);
	}
	
	@Override
	public boolean isEmpty() {
		synchronized(this.eventQueue) {
			checkRemoved();
			return this.state.isEmpty();
		}
	}
	
	@Override
	public Iterator<XId> iterator() {
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
			this.state.removeObject(object.getId());
		}
		
		this.state.setRevisionNumber(this.state.getRevisionNumber() + 1);
		
		this.loadedObjects.clear();
		
		this.removed = true;
	}
	
	@Override
	public boolean removeListenerForModelEvents(XModelEventListener changeListener) {
		synchronized(this.eventQueue) {
			return this.modelChangeListenerCollection.remove(changeListener);
		}
	}
	
	@Override
	public boolean removeObject(@NeverNull XId objectId) {
		
		// no synchronization necessary here (except that in
		// executeModelCommand())
		
		XModelCommand command = MemoryModelCommand.createRemoveCommand(getAddress(),
		        XCommand.FORCED, objectId);
		
		long result = executeModelCommand(command);
		XyAssert.xyAssert(result >= 0 || result == XCommand.NOCHANGE);
		return result != XCommand.NOCHANGE;
	}
	
	@Override
	protected void removeObjectInternal(XId objectId) {
		
		MemoryObject object = getObject(objectId);
		XyAssert.xyAssert(object != null);
		assert object != null;
		
		boolean inTrans = this.eventQueue.transactionInProgess;
		boolean makeTrans = !object.isEmpty();
		int since = this.eventQueue.getNextPosition();
		enqueueObjectRemoveEvents(this.eventQueue.getActor(), object, makeTrans || inTrans, false);
		
		// remove the object
		this.loadedObjects.remove(object.getId());
		this.state.removeObject(object.getId());
		
		Orphans orphans = this.eventQueue.orphans;
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
			
			incrementRevision();
			
			// propagate events
			this.eventQueue.sendEvents();
			
		}
		
	}
	
	@Override
	protected void setRevisionNumberIfModel(long modelRevisionNumber) {
		this.state.setRevisionNumber(modelRevisionNumber);
	}
	
	@Override
	public String toString() {
		return this.getId() + " rev[" + this.getRevisionNumber() + "]";
	}
	
	@Override
	public XRevWritableModel createSnapshot() {
		synchronized(this.eventQueue) {
			if(this.removed) {
				return null;
			}
			return XCopyUtils.createSnapshot(this);
		}
	}
	
	@Override
	public XType getType() {
		return XType.XMODEL;
	}
	
	@Override
	public boolean addListenerForModelSyncEvents(XModelSyncEventListener syncListener) {
		synchronized(this.eventQueue) {
			return this.modelSyncChangeListenerCollection.add(syncListener);
		}
	}
	
	@Override
	public boolean removeListenerForModelSyncEvents(XModelSyncEventListener syncListener) {
		synchronized(this.eventQueue) {
			return this.modelSyncChangeListenerCollection.remove(syncListener);
		}
	}
	
	public void fireModelSyncEvent(XModelEvent event) {
		for(XModelSyncEventListener listener : this.modelSyncChangeListenerCollection) {
			listener.onSynced(event);
		}
	}
}
