package org.xydra.core.model.impl.memory;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.xydra.annotations.NeverNull;
import org.xydra.base.IHasXAddress;
import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.XType;
import org.xydra.base.change.ChangeType;
import org.xydra.base.change.XAtomicEvent;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XEvent;
import org.xydra.base.change.XFieldEvent;
import org.xydra.base.change.XModelEvent;
import org.xydra.base.change.XObjectEvent;
import org.xydra.base.change.XRepositoryCommand;
import org.xydra.base.change.XRepositoryEvent;
import org.xydra.base.change.XReversibleFieldEvent;
import org.xydra.base.change.XTransaction;
import org.xydra.base.change.XTransactionEvent;
import org.xydra.base.change.impl.memory.MemoryRepositoryEvent;
import org.xydra.base.rmof.XReadableField;
import org.xydra.base.rmof.XReadableModel;
import org.xydra.base.rmof.XReadableObject;
import org.xydra.core.change.XChanges;
import org.xydra.core.change.XFieldEventListener;
import org.xydra.core.change.XObjectEventListener;
import org.xydra.core.change.XSendsFieldEvents;
import org.xydra.core.change.XSendsObjectEvents;
import org.xydra.core.change.XSendsTransactionEvents;
import org.xydra.core.change.XTransactionEventListener;
import org.xydra.core.model.IHasChangeLog;
import org.xydra.core.model.XChangeLog;
import org.xydra.core.model.XExecutesCommands;
import org.xydra.core.model.XLocalChange;
import org.xydra.core.model.XLocalChangeCallback;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;
import org.xydra.core.model.XSynchronizesChanges;
import org.xydra.core.model.delta.ChangedField;
import org.xydra.core.model.delta.ChangedModel;
import org.xydra.core.model.delta.ChangedObject;
import org.xydra.index.XI;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.sharedutils.XyAssert;


/**
 * Abstract base class for entities that can execute {@link XTransaction
 * XTransactions} ({@link XObject} and {@link XModel}) implementing most of the
 * logic behind transactions and synchronization.
 * 
 * @author dscharrer
 */
public abstract class SynchronizesChangesImpl extends AbstractEntity implements IHasXAddress,
        IHasChangeLog, XSynchronizesChanges, XExecutesCommands, XSendsObjectEvents,
        XSendsFieldEvents, XSendsTransactionEvents, Serializable {
	
	protected static class Orphans implements Serializable {
		
		private static final long serialVersionUID = -146971665894476381L;
		
		Map<XAddress,MemoryField> fields = new HashMap<XAddress,MemoryField>();
		Map<XId,MemoryObject> objects = new HashMap<XId,MemoryObject>();
		
	}
	
	private static final Logger log = LoggerFactory.getLogger(SynchronizesChangesImpl.class);
	
	private static final long serialVersionUID = -5649382238597273583L;
	
	protected final MemoryEventManager eventQueue;
	
	private final Set<XFieldEventListener> fieldChangeListenerCollection;
	
	private final Set<XObjectEventListener> objectChangeListenerCollection;
	/** Has this entity been removed? */
	protected boolean removed = false;
	private final Set<XTransactionEventListener> transactionListenerCollection;
	
	public SynchronizesChangesImpl(MemoryEventManager queue) {
		this.eventQueue = queue;
		this.objectChangeListenerCollection = new HashSet<XObjectEventListener>();
		this.fieldChangeListenerCollection = new HashSet<XFieldEventListener>();
		this.transactionListenerCollection = new HashSet<XTransactionEventListener>();
	}
	
	@Override
	public boolean addListenerForFieldEvents(XFieldEventListener changeListener) {
		synchronized(this.eventQueue) {
			return this.fieldChangeListenerCollection.add(changeListener);
		}
	}
	
	@Override
	public boolean addListenerForObjectEvents(XObjectEventListener changeListener) {
		synchronized(this.eventQueue) {
			return this.objectChangeListenerCollection.add(changeListener);
		}
	}
	
	@Override
	public boolean addListenerForTransactionEvents(XTransactionEventListener changeListener) {
		synchronized(this.eventQueue) {
			return this.transactionListenerCollection.add(changeListener);
		}
	}
	
	/**
	 * @throws IllegalStateException if this entity has already been removed
	 */
	protected abstract void checkRemoved() throws IllegalStateException;
	
	/**
	 * Check if this entity may be synchronized.
	 */
	protected abstract void checkSync();
	
	private void cleanupOrphans() {
		Orphans orphans = this.eventQueue.orphans;
		this.eventQueue.orphans = null;
		
		for(MemoryObject object : orphans.objects.values()) {
			object.delete();
		}
		
		for(MemoryField field : orphans.fields.values()) {
			field.delete();
		}
	}
	
	@Override
	public int countUnappliedLocalChanges() {
		synchronized(this.eventQueue) {
			int count = 0;
			for(XLocalChange lc : this.eventQueue.getLocalChanges()) {
				if(!lc.isApplied()) {
					count++;
				}
			}
			return count;
		}
	}
	
	/**
	 * Create a new object, increase revision (if not in a transaction) and
	 * enqueue the corresponding event.
	 * 
	 * The caller is responsible for handling synchronization, for checking that
	 * this model has not been removed and for checking that the object doesn't
	 * already exist.
	 */
	protected abstract MemoryObject createObjectInternal(XId objectId);
	
	protected long executeTransaction(XTransaction transaction, XLocalChangeCallback callback) {
		synchronized(this.eventQueue) {
			checkRemoved();
			
			assert !this.eventQueue.transactionInProgess : "double transaction detected";
			
			// make sure that the transaction actually refers to this model
			if(!transaction.getTarget().equals(getAddress())) {
				if(callback != null) {
					callback.onFailure();
				}
				return XCommand.FAILED;
			}
			
			ChangedModel model = new ChangedModel(getTransactionTarget());
			
			/*
			 * Check if the transaction is valid by recording the changes made
			 * by the commands in a delta to the current state (removedObjects,
			 * addedObjects, removedFields, addedFields, changedValue) and
			 * checking the commands against that.
			 */
			if(!model.executeCommand(transaction)) {
				if(callback != null) {
					callback.onFailure();
				}
				return XCommand.FAILED;
			}
			
			// all commands are OK, count the number of changes
			
			long nChanges = model.countCommandsNeeded(2);
			
			if(nChanges == 0) {
				// nothing to change
				if(callback != null) {
					callback.onSuccess(XCommand.NOCHANGE);
				}
				return XCommand.NOCHANGE;
			}
			
			this.eventQueue.newLocalChange(transaction, callback);
			
			long oldRev = getCurrentRevisionNumber();
			
			// only execute as transaction if there actually are multiple
			// changes
			if(nChanges > 1) {
				// set "transactionInProgress" to true to stop involuntarily
				// increasing the affected revision numbers
				this.eventQueue.transactionInProgess = true;
			}
			
			int since = this.eventQueue.getNextPosition();
			
			// apply changes
			
			for(XId objectId : model.getRemovedObjects()) {
				removeObjectInternal(objectId);
			}
			
			for(XReadableObject object : model.getNewObjects()) {
				MemoryObject newObject = createObjectInternal(object.getId());
				for(XId fieldId : object) {
					XReadableField field = object.getField(fieldId);
					MemoryField newField = newObject.createFieldInternal(fieldId);
					if(!field.isEmpty()) {
						newField.setValueInternal(field.getValue());
					}
				}
			}
			
			for(ChangedObject object : model.getChangedObjects()) {
				MemoryObject oldObject = getObject(object.getId());
				
				for(XId fieldId : object.getRemovedFields()) {
					oldObject.removeFieldInternal(fieldId);
				}
				
				for(XReadableField field : object.getNewFields()) {
					MemoryField newField = oldObject.createFieldInternal(field.getId());
					if(!field.isEmpty()) {
						newField.setValueInternal(field.getValue());
					}
				}
				
				for(ChangedField field : object.getChangedFields()) {
					if(field.isChanged()) {
						MemoryField oldField = oldObject.getField(field.getId());
						oldField.setValueInternal(field.getValue());
					}
				}
				
			}
			
			// update revision numbers and save state
			
			if(nChanges > 1) {
				
				long newRevision = oldRev + 1;
				
				this.eventQueue.createTransactionEvent(getSessionActor(), getModel(), getObject(),
				        since);
				
				// new objects
				for(XReadableObject object : model.getNewObjects()) {
					MemoryObject newObject = getObject(object.getId());
					assert newObject != null : "should have been created above";
					for(XId fieldId : object) {
						MemoryField newField = newObject.getField(fieldId);
						assert newField != null : "should have been created above";
						newField.setRevisionNumber(newRevision);
					}
					newObject.setRevisionNumber(newRevision);
				}
				
				// changed objects
				for(ChangedObject object : model.getChangedObjects()) {
					MemoryObject oldObject = getObject(object.getId());
					assert oldObject != null : "should have existed already and not been removed";
					
					boolean changed = object.getRemovedFields().iterator().hasNext();
					
					// new fields in old objects
					for(XReadableField field : object.getNewFields()) {
						MemoryField newField = oldObject.getField(field.getId());
						assert newField != null : "should have been created above";
						newField.setRevisionNumber(newRevision);
						changed = true;
					}
					
					// changed fields
					for(ChangedField field : object.getChangedFields()) {
						if(field.isChanged()) {
							MemoryField oldField = oldObject.getField(field.getId());
							assert oldField != null : "should have existed already and not been removed";
							oldField.setRevisionNumber(newRevision);
							changed = true;
						}
					}
					
					if(changed) {
						oldObject.setRevisionNumber(newRevision);
					}
					
				}
				
				this.eventQueue.transactionInProgess = false;
				
				// really increment the model's revision number
				incrementRevision();
				
				// dispatch events
				this.eventQueue.sendEvents();
				
			} else {
				assert getCurrentRevisionNumber() == oldRev + 1 : "there should have been exactly one change";
			}
			
			return oldRev + 1;
			
		}
		
	}
	
	/**
	 * Notifies all listeners that have registered interest for notification on
	 * {@link XFieldEvent XFieldEvents} happening on child-{@link MemoryField
	 * MemoryFields} of this entity.
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
	 * {@link XObjectEvent XObjectEvents} happening on child-
	 * {@link MemoryObject MemoryObjects} of this entity.
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
	 * {@link XTransactionEvent XTransactionEvents} happening on this entity.
	 * 
	 * @param event The {@link XTransactonEvent} which will be propagated to the
	 *            registered listeners.
	 */
	protected void fireTransactionEvent(XTransactionEvent event) {
		for(XTransactionEventListener listener : this.transactionListenerCollection) {
			listener.onChangeEvent(event);
		}
	}
	
	@Override
	public XChangeLog getChangeLog() {
		return this.eventQueue.getChangeLog();
	}
	
	/**
	 * @return the revision number to return when executing {@link XCommand
	 *         XCommands}.
	 */
	protected abstract long getCurrentRevisionNumber();
	
	@Override
	public XLocalChange[] getLocalChanges() {
		synchronized(this.eventQueue) {
			List<MemoryLocalChange> mlc = this.eventQueue.getLocalChanges();
			return mlc.toArray(new XLocalChange[mlc.size()]);
		}
	}
	
	/**
	 * @return the {@link MemoryModel} to use for sending
	 *         {@link XTransactionEvent XTransactionEvents}
	 */
	protected abstract MemoryModel getModel();
	
	/**
	 * @return the {@link MemoryObject} to use for sending
	 *         {@link XTransactionEvent XTransactionEvents}
	 */
	protected abstract MemoryObject getObject();
	
	/**
	 * Get the {@link MemoryObject} with the given {@link XId}.
	 * 
	 * If the entity this method is called on already is an {@link MemoryObject}
	 * the method returns this entity exactly when the given {@link XId} matches
	 * its {@link XId} and null otherwise.
	 * 
	 * @param objectId The {@link XId} of the {@link MemoryObject} which is to
	 *            be returned
	 * 
	 * @return true if there is an {@link XObject} with the given {@link XId}
	 */
	protected abstract MemoryObject getObject(@NeverNull XId objectId);
	
	@Override
	public XId getSessionActor() {
		synchronized(this.eventQueue) {
			return this.eventQueue.getActor();
		}
	}
	
	@Override
	public String getSessionPassword() {
		synchronized(this.eventQueue) {
			return this.eventQueue.getPasswordHash();
		}
	}
	
	@Override
	public long getSynchronizedRevision() {
		synchronized(this.eventQueue) {
			return this.eventQueue.getSyncRevision();
		}
	}
	
	/**
	 * @return Return the proxy for reading the current state.
	 */
	protected abstract XReadableModel getTransactionTarget();
	
	/**
	 * Increment this entity's revision number.
	 */
	protected abstract void incrementRevision();
	
	@Override
	public boolean removeListenerForFieldEvents(XFieldEventListener changeListener) {
		synchronized(this.eventQueue) {
			return this.fieldChangeListenerCollection.remove(changeListener);
		}
	}
	
	@Override
	public boolean removeListenerForObjectEvents(XObjectEventListener changeListener) {
		synchronized(this.eventQueue) {
			return this.objectChangeListenerCollection.remove(changeListener);
		}
	}
	
	@Override
	public boolean removeListenerForTransactionEvents(XTransactionEventListener changeListener) {
		synchronized(this.eventQueue) {
			return this.transactionListenerCollection.remove(changeListener);
		}
	}
	
	/**
	 * Remove an existing object, increase revision (if not in a transaction)
	 * and enqueue the corresponding event(s).
	 * 
	 * The caller is responsible for handling synchronization, for checking that
	 * this model has not been removed and for checking that the object actually
	 * exists.
	 */
	protected abstract void removeObjectInternal(XId objectId);
	
	private long replayCommand(XCommand command) {
		
		XyAssert.xyAssert(!this.eventQueue.transactionInProgess);
		
		if(command instanceof XRepositoryCommand) {
			if(getAddress().getAddressedType() != XType.XMODEL) {
				return XCommand.FAILED;
			}
			
			XRepositoryCommand rc = (XRepositoryCommand)command;
			
			if(!rc.getRepositoryId().equals(getAddress().getRepository())) {
				// given given repository-id are not consistent
				return XCommand.FAILED;
			}
			
			if(!rc.getModelId().equals(getModel().getId())) {
				return XCommand.FAILED;
			}
			
			if(command.getChangeType() == ChangeType.ADD) {
				if(!this.removed) {
					// ID already taken
					if(rc.isForced()) {
						/*
						 * the forced event only cares about the postcondition -
						 * that there is a model with the given ID, not about
						 * that there was no such model before
						 */
						return XCommand.NOCHANGE;
					}
					return XCommand.FAILED;
				}
				
				this.removed = false;
				
				XRepositoryEvent event = MemoryRepositoryEvent.createAddEvent(getSessionActor(),
				        getAddress().getParent(), getModel().getId(), getCurrentRevisionNumber(),
				        false);
				this.eventQueue.enqueueRepositoryEvent(getModel().getFather(), event);
				
				incrementRevision();
			}
			
			else if(command.getChangeType() == ChangeType.REMOVE) {
				if(this.removed) {
					// ID not taken
					if(rc.isForced()) {
						/*
						 * the forced event only cares about the postcondition -
						 * that there is no model with the given ID, not about
						 * that there was such a model before
						 */
						return XCommand.NOCHANGE;
					}
					return XCommand.FAILED;
				}
				if(!rc.isForced() && getCurrentRevisionNumber() != rc.getRevisionNumber()) {
					return XCommand.FAILED;
				}
				
				int since = this.eventQueue.getNextPosition();
				boolean inTrans = getModel().enqueueModelRemoveEvents(getSessionActor());
				if(inTrans) {
					this.eventQueue.createTransactionEvent(getSessionActor(), getModel(), null,
					        since);
				}
				
				getModel().removeInternal();
				
			} else {
				throw new AssertionError("unknown command type: " + rc);
			}
			
			XyAssert.xyAssert(getModel().getRevisionNumber() == getCurrentRevisionNumber());
			return getCurrentRevisionNumber();
		}
		
		// TODO allow replaying XModelCommands on a model-less object
		
		return executeCommand(command);
	}
	
	private boolean replayEvent(XEvent event) {
		
		XyAssert.xyAssert(!event.inTransaction(), "event %s should not be in a txn", event);
		
		while(getChangeLog().getCurrentRevisionNumber() < event.getOldModelRevision()) {
			this.eventQueue.logNullEvent();
		}
		
		long oldModelRev = getModel() == null ? -1 : getModel().getRevisionNumber();
		XyAssert.xyAssert(oldModelRev <= event.getOldModelRevision());
		setRevisionNumberIfModel(event.getOldModelRevision());
		// TODO adjust object and field revisions? this is needed for
		// synchronizing parent-less
		// objects and/or if there are missing events (access rights?)
		
		XCommand replayCommand = XChanges.createReplayCommand(event);
		/*
		 * The remote changes should be applied as the actor specified in the
		 * event
		 */
		XId oldActor = getSessionActor();
		/* switch actor to the one specified in this event */
		setSessionActor(event.getActor(), "NOTSET");
		long result = replayCommand(replayCommand);
		/* Switch back actor */
		setSessionActor(oldActor, "NOTSET");
		
		if(result < 0) {
			setRevisionNumberIfModel(oldModelRev);
			return false;
		}
		XEvent newEvent = getChangeLog().getEventAt(result);
		XyAssert.xyAssert(event.equals(newEvent), "should be equal", event, newEvent);
		assert getModel() == null ? getObject().getRevisionNumber() == event.getRevisionNumber()
		        : getModel().getRevisionNumber() == event.getRevisionNumber();
		XyAssert.xyAssert(getCurrentRevisionNumber() == event.getRevisionNumber());
		
		XyAssert.xyAssert(event.getChangedEntity().getObject() == null
		        || getObject(event.getChangedEntity().getObject()) == null
		        || getObject(event.getChangedEntity().getObject()).getRevisionNumber() == event
		                .getRevisionNumber());
		
		return true;
	}
	
	@Override
	public void rollback(long revision) {
		
		checkSync();
		
		if(revision < -1) {
			throw new IllegalArgumentException("invalid revision number: " + revision);
		}
		
		synchronized(this.eventQueue) {
			
			XChangeLog log = getChangeLog();
			long currentRev = log.getCurrentRevisionNumber();
			if(revision == currentRev) {
				return;
			}
			
			boolean oldBlock = this.eventQueue.setBlockSending(true);
			int pos = this.eventQueue.getNextPosition();
			
			// stop the change log to prevent the rollback events from being
			// logged
			boolean oldLogging = this.eventQueue.setLogging(false);
			
			boolean hasOrphans = (this.eventQueue.orphans != null);
			if(!hasOrphans) {
				this.eventQueue.orphans = new Orphans();
			}
			
			try {
				
				// rollback each event individually
				for(long i = currentRev; i > revision; i--) {
					XEvent event = log.getEventAt(i);
					if(event == null) {
						continue;
					}
					if(event instanceof XAtomicEvent) {
						rollbackEvent((XAtomicEvent)event);
					} else {
						XyAssert.xyAssert(event instanceof XTransactionEvent);
						XTransactionEvent trans = (XTransactionEvent)event;
						for(int j = trans.size() - 1; j >= 0; j--) {
							XAtomicEvent atomicEvent = trans.getEvent(j);
							rollbackEvent(atomicEvent);
						}
					}
					
				}
				
				// reset the change log
				this.eventQueue.truncateLog(revision);
				
				if(!hasOrphans) {
					// Clean unneeded events.
					this.eventQueue.cleanEvents(pos);
					
					cleanupOrphans();
				}
				
			} finally {
				
				if(!hasOrphans) {
					this.eventQueue.setBlockSending(oldBlock);
					this.eventQueue.sendEvents();
				}
				
			}
			
			this.eventQueue.setLogging(oldLogging);
			
		}
		
	}
	
	/**
	 * Rolls back the changes represented by the given {@link XAtomicEvent} and
	 * will restore the states of the affected entity and its parents to the
	 * timepoint before the {@link XCommand} which is responsible for this
	 * {@link XAtomicEvent} was executed.
	 * 
	 * @param event The {@link XAtomicEvent} which represented changes will be
	 *            rolled back
	 */
	private void rollbackEvent(XAtomicEvent event) {
		
		XyAssert.xyAssert(getModel() == null
		        || event.getRevisionNumber() == getModel().getRevisionNumber()
		        || (event.inTransaction() && event.getOldModelRevision() == getModel()
		                .getRevisionNumber()));
		
		if(event instanceof XRepositoryEvent) {
			XyAssert.xyAssert(getModel() == this);
			XyAssert.xyAssert(event.getTarget().equals(getAddress().getParent()));
			XyAssert.xyAssert(event.getChangedEntity().getModel().equals(getAddress().getModel()));
			if(event.getChangeType() == ChangeType.REMOVE) {
				XyAssert.xyAssert(this.removed == true);
				this.removed = false;
			} else {
				XyAssert.xyAssert(event.getChangeType() == ChangeType.ADD);
				XyAssert.xyAssert(this.removed == false);
				XyAssert.xyAssert(getModel().isEmpty());
				this.removed = true;
			}
			
		} else if(event instanceof XModelEvent) {
			// TODO allow applying XModelEvents on a model-less object
			XyAssert.xyAssert(getModel() == this);
			XyAssert.xyAssert(event.getTarget().equals(getAddress()));
			XId objectId = ((XModelEvent)event).getObjectId();
			if(event.getChangeType() == ChangeType.REMOVE) {
				XyAssert.xyAssert(!getModel().hasObject(objectId));
				MemoryObject object = createObjectInternal(objectId);
				XyAssert.xyAssert(event.getOldObjectRevision() >= 0);
				object.setRevisionNumber(event.getOldObjectRevision());
			} else {
				XyAssert.xyAssert(event.getChangeType() == ChangeType.ADD);
				XyAssert.xyAssert(getModel().hasObject(objectId));
				XyAssert.xyAssert(event.getRevisionNumber() == getModel().getObject(objectId)
				        .getRevisionNumber()
				        || (event.inTransaction() && getModel().getObject(objectId)
				                .getRevisionNumber() == XCommand.NEW));
				removeObjectInternal(objectId);
			}
			
		} else {
			XyAssert.xyAssert(event instanceof XObjectEvent || event instanceof XFieldEvent);
			MemoryObject object = getObject(event.getTarget().getObject());
			XyAssert.xyAssert(object != null);
			assert object != null;
			XyAssert.xyAssert(event.getRevisionNumber() == object.getRevisionNumber()
			        || (event.inTransaction() && event.getOldObjectRevision() == object
			                .getRevisionNumber()));
			
			if(event instanceof XObjectEvent) {
				XyAssert.xyAssert(event.getTarget().equals(object.getAddress()));
				XId fieldId = ((XObjectEvent)event).getFieldId();
				if(event.getChangeType() == ChangeType.REMOVE) {
					XyAssert.xyAssert(!object.hasField(fieldId));
					MemoryField field = object.createFieldInternal(fieldId);
					XyAssert.xyAssert(event.getOldFieldRevision() >= 0);
					field.setRevisionNumber(event.getOldFieldRevision());
				} else {
					XyAssert.xyAssert(event.getChangeType() == ChangeType.ADD);
					XyAssert.xyAssert(object.hasField(fieldId));
					XyAssert.xyAssert(event.getRevisionNumber() == object.getField(fieldId)
					        .getRevisionNumber()
					        || (event.inTransaction() && object.getField(fieldId)
					                .getRevisionNumber() == XCommand.NEW));
					object.removeFieldInternal(fieldId);
				}
				
			} else {
				XyAssert.xyAssert(event instanceof XReversibleFieldEvent);
				MemoryField field = object.getField(((XReversibleFieldEvent)event).getFieldId());
				XyAssert.xyAssert(field != null);
				assert field != null;
				XyAssert.xyAssert(event.getRevisionNumber() == field.getRevisionNumber()
				        || (event.inTransaction() && event.getOldFieldRevision() == field
				                .getRevisionNumber()));
				XyAssert.xyAssert(XI.equals(field.getValue(),
				        ((XReversibleFieldEvent)event).getNewValue()));
				field.setValueInternal(((XReversibleFieldEvent)event).getOldValue());
				XyAssert.xyAssert(event.getOldFieldRevision() >= 0);
				field.setRevisionNumber(event.getOldFieldRevision());
			}
			
			XyAssert.xyAssert(event.getOldObjectRevision() >= 0);
			object.setRevisionNumber(event.getOldObjectRevision());
		}
		
		setRevisionNumberIfModel(event.getOldModelRevision());
	}
	
	/**
	 * Set the new revision number, if this is a subtype of {@link XModel}.
	 */
	protected abstract void setRevisionNumberIfModel(long modelRevisionNumber);
	
	@Override
	public void setSessionActor(XId actorId, String passwordHash) {
		synchronized(this.eventQueue) {
			this.eventQueue.setSessionActor(actorId, passwordHash);
		}
	}
	
	@Override
	public boolean synchronize(XEvent[] remoteChanges) {
		
		boolean success = true;
		boolean removedChanged;
		
		synchronized(this.eventQueue) {
			
			XyAssert.xyAssert(!this.eventQueue.transactionInProgess);
			
			boolean oldRemoved = this.removed;
			
			List<MemoryLocalChange> localChanges = this.eventQueue.getLocalChanges();
			
			log.info("sync: merging " + remoteChanges.length + " remote and " + localChanges.size()
			        + " local changes, local rev is " + getCurrentRevisionNumber() + " (synced to "
			        + getSynchronizedRevision() + ")");
			
			checkSync();
			
			long[] results = null;
			
			boolean oldBlock = this.eventQueue.setBlockSending(true);
			
			XyAssert.xyAssert(this.eventQueue.orphans == null);
			this.eventQueue.orphans = new Orphans();
			
			int pos = this.eventQueue.getNextPosition();
			
			long syncRev = getSynchronizedRevision();
			
			// Roll back to the old revision and save removed entities.
			rollback(syncRev);
			
			// Apply the remote changes.
			for(XEvent remoteChange : remoteChanges) {
				log.info("sync: merging remote event " + remoteChange);
				if(remoteChange == null) {
					this.eventQueue.logNullEvent();
					continue;
				}
				if(!replayEvent(remoteChange)) {
					success = false;
					break;
				}
			}
			
			long remoteRev = getCurrentRevisionNumber();
			this.eventQueue.setSyncRevision(remoteRev);
			
			// Remove local changes that have been applied remotely.
			for(int i = localChanges.size() - 1; i >= 0; i--) {
				MemoryLocalChange lc = localChanges.get(i);
				if(lc.isApplied() && lc.getRemoteRevision() <= remoteRev) {
					XyAssert.xyAssert(lc.getRemoteRevision() >= 0);
					localChanges.remove(i);
				}
			}
			
			results = new long[localChanges.size()];
			
			// Re-apply the local changes.
			for(int i = 0; i < localChanges.size(); i++) {
				
				// FIXME use the actorId from the local change
				MemoryLocalChange lc = localChanges.get(i);
				
				lc.updateCommand(syncRev, remoteRev);
				
				/*
				 * Applying all local commands as the current actor, ignoring
				 * what actor was used when they were originally executed.
				 */
				results[i] = replayCommand(lc.getCommand());
				
			}
			
			// Clean unneeded events.
			this.eventQueue.cleanEvents(pos);
			
			cleanupOrphans();
			
			this.eventQueue.setBlockSending(oldBlock);
			
			// invoke callbacks for failed / nochange commands
			for(int i = 0; i < results.length; i++) {
				MemoryLocalChange change = localChanges.get(i);
				if(results[i] == XCommand.FAILED) {
					log.info("sync: client command conflicted: " + change.getCommand());
					if(change.getCallback() != null) {
						change.getCallback().onFailure();
					}
				} else if(results[i] == XCommand.NOCHANGE) {
					log.info("sync: client command redundant: " + change.getCommand());
					if(change.getCallback() != null) {
						change.getCallback().onSuccess(results[i]);
					}
				}
			}
			
			// remove failed / nochange commands
			// IMPROVE this is O(nLocalChanges^2) worst case
			for(int i = results.length - 1; i >= 0; i--) {
				if(results[i] < 0) {
					localChanges.remove(i);
				}
			}
			
			MemoryModel model = getModel();
			removedChanged = (oldRemoved != this.removed && model != null && model.getFather() != null);
			if(!removedChanged) {
				this.eventQueue.sendEvents();
			}
			
			log.info("sync: merged changes, new local rev is " + getCurrentRevisionNumber()
			        + " (synced to " + getSynchronizedRevision() + ")");
		}
		
		// This needs to be outside of the synchronized block to prevent
		// deadlocks between repository and model locking.
		if(removedChanged) {
			getModel().getFather().updateRemoved(getModel());
		}
		
		return success;
		
	}
	
}
