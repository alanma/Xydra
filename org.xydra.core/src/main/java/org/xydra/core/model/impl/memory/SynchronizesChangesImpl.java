package org.xydra.core.model.impl.memory;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.xydra.core.change.ChangeType;
import org.xydra.core.change.XAtomicEvent;
import org.xydra.core.change.XChanges;
import org.xydra.core.change.XCommand;
import org.xydra.core.change.XEvent;
import org.xydra.core.change.XFieldCommand;
import org.xydra.core.change.XFieldEvent;
import org.xydra.core.change.XFieldEventListener;
import org.xydra.core.change.XModelCommand;
import org.xydra.core.change.XModelEvent;
import org.xydra.core.change.XObjectCommand;
import org.xydra.core.change.XObjectEvent;
import org.xydra.core.change.XObjectEventListener;
import org.xydra.core.change.XSendsFieldEvents;
import org.xydra.core.change.XSendsObjectEvents;
import org.xydra.core.change.XSendsTransactionEvents;
import org.xydra.core.change.XTransaction;
import org.xydra.core.change.XTransactionEvent;
import org.xydra.core.change.XTransactionEventListener;
import org.xydra.core.change.impl.memory.MemoryFieldCommand;
import org.xydra.core.change.impl.memory.MemoryModelCommand;
import org.xydra.core.change.impl.memory.MemoryObjectCommand;
import org.xydra.core.model.IHasXAddress;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XBaseField;
import org.xydra.core.model.XBaseModel;
import org.xydra.core.model.XBaseObject;
import org.xydra.core.model.XChangeLog;
import org.xydra.core.model.XExecutesCommands;
import org.xydra.core.model.XField;
import org.xydra.core.model.XID;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;
import org.xydra.core.model.XSynchronizationCallback;
import org.xydra.core.model.XSynchronizesChanges;
import org.xydra.core.model.delta.ChangedField;
import org.xydra.core.model.delta.ChangedModel;
import org.xydra.core.model.delta.ChangedObject;
import org.xydra.index.XI;


/**
 * Abstract base class for entities that can execute {@link XTransaction
 * XTransactions} ({@link XObject} and {@link XModel}) implementing most of the
 * logic behind transactions and synchronization.
 * 
 * @author dscharrer
 */
public abstract class SynchronizesChangesImpl implements IHasXAddress, XSynchronizesChanges,
        XExecutesCommands, XSendsObjectEvents, XSendsFieldEvents, XSendsTransactionEvents,
        Serializable {
	
	private static final long serialVersionUID = -5649382238597273583L;
	
	protected static class Orphans implements Serializable {
		
		private static final long serialVersionUID = -146971665894476381L;
		
		Map<XID,MemoryObject> objects = new HashMap<XID,MemoryObject>();
		Map<XAddress,MemoryField> fields = new HashMap<XAddress,MemoryField>();
		
	}
	
	protected final MemoryEventManager eventQueue;
	
	private Set<XObjectEventListener> objectChangeListenerCollection;
	private Set<XFieldEventListener> fieldChangeListenerCollection;
	private Set<XTransactionEventListener> transactionListenerCollection;
	
	public SynchronizesChangesImpl(MemoryEventManager queue) {
		this.eventQueue = queue;
		this.objectChangeListenerCollection = new HashSet<XObjectEventListener>();
		this.fieldChangeListenerCollection = new HashSet<XFieldEventListener>();
		this.transactionListenerCollection = new HashSet<XTransactionEventListener>();
	}
	
	protected long executeTransaction(XTransaction transaction, XSynchronizationCallback callback) {
		synchronized(this.eventQueue) {
			checkRemoved();
			
			assert !this.eventQueue.transactionInProgess : "double transaction detected";
			
			// make sure that the transaction actually refers to this model
			if(!transaction.getTarget().equals(getAddress())) {
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
				return XCommand.FAILED;
			}
			
			// all commands are OK, count the number of changes
			
			long nChanges = model.countCommandsNeeded(2);
			
			if(nChanges == 0) {
				// nothing to change
				return XCommand.NOCHANGE;
			}
			
			this.eventQueue.newLocalChange(transaction, callback);
			
			long oldRev = getOldRevisionNumber();
			
			// only execute as transaction if there actually are multiple
			// changes
			boolean hasOrphans = (this.eventQueue.orphans != null);
			if(nChanges > 1) {
				
				if(!hasOrphans) {
					beginStateTransaction();
				}
				
				// set "transactionInProgress" to true to stop involuntarily
				// increasing the affected revision numbers
				this.eventQueue.transactionInProgess = true;
			}
			
			int since = this.eventQueue.getNextPosition();
			
			// apply changes
			
			for(XID objectId : model.getRemovedObjects()) {
				removeObjectInternal(objectId);
			}
			
			for(XBaseObject object : model.getNewObjects()) {
				MemoryObject newObject = createObjectInternal(object.getID());
				for(XID fieldId : object) {
					XBaseField field = object.getField(fieldId);
					MemoryField newField = newObject.createFieldInternal(fieldId);
					if(!field.isEmpty()) {
						newField.setValueInternal(field.getValue());
					}
				}
			}
			
			for(ChangedObject object : model.getChangedObjects()) {
				MemoryObject oldObject = getObject(object.getID());
				
				for(XID fieldId : object.getRemovedFields()) {
					oldObject.removeFieldInternal(fieldId);
				}
				
				for(XBaseField field : object.getNewFields()) {
					MemoryField newField = oldObject.createFieldInternal(field.getID());
					if(!field.isEmpty()) {
						newField.setValueInternal(field.getValue());
					}
				}
				
				for(ChangedField field : object.getChangedFields()) {
					if(field.isChanged()) {
						MemoryField oldField = oldObject.getField(field.getID());
						oldField.setValueInternal(field.getValue());
					}
				}
				
			}
			
			// update revision numbers and save state
			
			if(nChanges > 1) {
				
				long newRevision = oldRev + 1;
				
				/*
				 * FIXME this may fail: changes to objects and fields will use
				 * their own actor when creating the events, which may be
				 * different from the one of this model (object) - but
				 * transaction(event)s should only contain events from the same
				 * actor.
				 */
				this.eventQueue.createTransactionEvent(getSessionActor(), getModel(), getObject(),
				        since);
				
				// new objects
				for(XBaseObject object : model.getNewObjects()) {
					MemoryObject newObject = getObject(object.getID());
					assert newObject != null : "should have been created above";
					for(XID fieldId : object) {
						MemoryField newField = newObject.getField(fieldId);
						assert newField != null : "should have been created above";
						newField.setRevisionNumber(newRevision);
						newField.save();
					}
					newObject.setRevisionNumber(newRevision);
					newObject.save();
				}
				
				// changed objects
				for(ChangedObject object : model.getChangedObjects()) {
					MemoryObject oldObject = getObject(object.getID());
					assert oldObject != null : "should have existed already and not been removed";
					
					boolean changed = object.getRemovedFields().iterator().hasNext();
					
					// new fields in old objects
					for(XBaseField field : object.getNewFields()) {
						MemoryField newField = oldObject.getField(field.getID());
						assert newField != null : "should have been created above";
						newField.setRevisionNumber(newRevision);
						newField.save();
						changed = true;
					}
					
					// changed fields
					for(ChangedField field : object.getChangedFields()) {
						if(field.isChanged()) {
							MemoryField oldField = oldObject.getField(field.getID());
							assert oldField != null : "should have existed already and not been removed";
							oldField.setRevisionNumber(newRevision);
							oldField.save();
							changed = true;
						}
					}
					
					if(changed) {
						oldObject.setRevisionNumber(newRevision);
						oldObject.save();
					}
					
				}
				
				this.eventQueue.transactionInProgess = false;
				
				// really increment the model's revision number
				incrementRevisionAndSave();
				
				if(!hasOrphans) {
					endStateTransaction();
				}
				
				// dispatch events
				this.eventQueue.sendEvents();
				
			} else {
				assert getOldRevisionNumber() == oldRev + 1 : "there should have been exactly one change";
			}
			
			return oldRev + 1;
			
		}
		
	}
	
	public XChangeLog getChangeLog() {
		return this.eventQueue.getChangeLog();
	}
	
	/**
	 * Returns a collection containing the {@link XField XFields} and
	 * {@link XObject XObjects} that are (temporarily) removed while
	 * synchronizing.
	 */
	protected Orphans getOrphans() {
		return this.eventQueue.orphans;
	}
	
	public void rollback(long revision) {
		
		checkSync();
		
		if(revision < 0) {
			throw new IllegalArgumentException("invalid revision number: " + revision);
		}
		
		XChangeLog log = getChangeLog();
		long currentRev = log.getCurrentRevisionNumber();
		if(revision == currentRev) {
			return;
		}
		
		boolean oldBlock = this.eventQueue.setBlockSending(true);
		int pos = this.eventQueue.getNextPosition();
		
		// stop the change log to prevent the rollback events from being logged
		boolean oldLogging = this.eventQueue.setLogging(false);
		
		boolean hasOrphans = (this.eventQueue.orphans != null);
		if(!hasOrphans) {
			this.eventQueue.orphans = new Orphans();
			beginStateTransaction();
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
					assert event instanceof XTransactionEvent;
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
				
				this.eventQueue.saveLog();
			}
			
		} finally {
			
			if(!hasOrphans) {
				endStateTransaction();
				
				this.eventQueue.setBlockSending(oldBlock);
				this.eventQueue.sendEvents();
			}
			
		}
		
		this.eventQueue.setLogging(oldLogging);
		
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
		
		assert getModel() == null
		        || event.getRevisionNumber() == getModel().getRevisionNumber()
		        || (event.inTransaction() && event.getOldModelRevision() == getModel()
		                .getRevisionNumber());
		
		// TODO support rolling back repository events.
		if(event instanceof XModelEvent) {
			assert getModel() == this;
			assert event.getTarget().equals(getAddress());
			XID objectId = ((XModelEvent)event).getObjectID();
			if(event.getChangeType() == ChangeType.REMOVE) {
				assert !getModel().hasObject(objectId);
				MemoryObject object = createObjectInternal(objectId);
				assert event.getOldObjectRevision() >= 0;
				object.setRevisionNumber(event.getOldObjectRevision());
				object.save();
			} else {
				assert event.getChangeType() == ChangeType.ADD;
				assert getModel().hasObject(objectId);
				assert event.getRevisionNumber() == getModel().getObject(objectId)
				        .getRevisionNumber()
				        || (event.inTransaction() && getModel().getObject(objectId)
				                .getRevisionNumber() == XCommand.NEW);
				removeObjectInternal(objectId);
			}
			
		} else {
			assert event instanceof XObjectEvent || event instanceof XFieldEvent;
			MemoryObject object = getObject(event.getTarget().getObject());
			assert object != null;
			assert event.getRevisionNumber() == object.getRevisionNumber()
			        || (event.inTransaction() && event.getOldObjectRevision() == object
			                .getRevisionNumber());
			
			if(event instanceof XObjectEvent) {
				assert event.getTarget().equals(object.getAddress());
				XID fieldId = ((XObjectEvent)event).getFieldID();
				if(event.getChangeType() == ChangeType.REMOVE) {
					assert !object.hasField(fieldId);
					MemoryField field = object.createFieldInternal(fieldId);
					assert event.getOldFieldRevision() >= 0;
					field.setRevisionNumber(event.getOldFieldRevision());
					field.save();
				} else {
					assert event.getChangeType() == ChangeType.ADD;
					assert object.hasField(fieldId);
					assert event.getRevisionNumber() == object.getField(fieldId)
					        .getRevisionNumber()
					        || (event.inTransaction() && object.getField(fieldId)
					                .getRevisionNumber() == XCommand.NEW);
					object.removeFieldInternal(fieldId);
				}
				
			} else {
				assert event instanceof XFieldEvent;
				MemoryField field = object.getField(((XFieldEvent)event).getFieldID());
				assert field != null;
				assert event.getRevisionNumber() == field.getRevisionNumber()
				        || (event.inTransaction() && event.getOldFieldRevision() == field
				                .getRevisionNumber());
				assert XI.equals(field.getValue(), ((XFieldEvent)event).getNewValue());
				field.setValueInternal(((XFieldEvent)event).getOldValue());
				assert event.getOldFieldRevision() >= 0;
				field.setRevisionNumber(event.getOldFieldRevision());
				field.save();
			}
			
			assert event.getOldObjectRevision() >= 0;
			object.setRevisionNumber(event.getOldObjectRevision());
			object.save();
		}
		
		assert event.getOldModelRevision() >= 0;
		setRevisionNumberIfModel(event.getOldModelRevision());
		saveIfModel();
	}
	
	public long[] synchronize(XEvent[] remoteChanges) {
		
		List<LocalChange> localChanges = this.eventQueue.getLocalChanges();
		
		checkSync();
		
		long[] results = new long[localChanges.size()];
		
		boolean oldBlock = this.eventQueue.setBlockSending(true);
		
		try {
			
			assert this.eventQueue.orphans == null;
			beginStateTransaction();
			this.eventQueue.orphans = new Orphans();
			
			int pos = this.eventQueue.getNextPosition();
			
			long syncRev = getSynchronizedRevision();
			
			// Roll back to the old revision and save removed entities.
			rollback(syncRev);
			
			// Apply the remote changes.
			for(XEvent remoteChange : remoteChanges) {
				if(remoteChange == null) {
					this.eventQueue.logNullEvent();
					continue;
				}
				assert !(this instanceof XModel)
				        || getModel().getRevisionNumber() == remoteChange.getOldModelRevision();
				/*
				 * FIXME the remove changes should be applied as the actor
				 * specified in the event
				 */
				XCommand replayCommand = XChanges.createReplayCommand(remoteChange);
				long result = executeCommand(replayCommand);
				if(result < 0) {
					// FIXME local commands should still be re-applied to main
					// consistency
					throw new IllegalStateException("could not apply remote change: "
					        + remoteChange);
				}
				assert getModel() == null ? getObject().getRevisionNumber() == remoteChange
				        .getRevisionNumber() : getModel().getRevisionNumber() == remoteChange
				        .getRevisionNumber();
				this.eventQueue.setSyncRevision(remoteChange.getRevisionNumber());
			}
			
			// Re-apply the local changes.
			long nRemote = remoteChanges.length;
			for(int i = 0; i < localChanges.size(); i++) {
				// FIXME use the actorId from the local change
				LocalChange lc = localChanges.get(i);
				XCommand command = lc.command;
				
				// Adapt the command if needed.
				if(command instanceof XModelCommand) {
					XModelCommand mc = (XModelCommand)command;
					if(mc.getChangeType() == ChangeType.REMOVE && mc.getRevisionNumber() > syncRev) {
						command = MemoryModelCommand.createRemoveCommand(mc.getTarget(),
						        mc.getRevisionNumber() + nRemote, mc.getObjectID());
						lc.command = command;
					}
				} else if(command instanceof XObjectCommand) {
					XObjectCommand oc = (XObjectCommand)command;
					if(oc.getChangeType() == ChangeType.REMOVE && oc.getRevisionNumber() > syncRev) {
						command = MemoryObjectCommand.createRemoveCommand(oc.getTarget(),
						        oc.getRevisionNumber() + nRemote, oc.getFieldID());
						lc.command = command;
					}
				} else if(command instanceof XFieldCommand) {
					XFieldCommand fc = (XFieldCommand)command;
					if(fc.getRevisionNumber() > syncRev) {
						switch(command.getChangeType()) {
						case ADD:
							command = MemoryFieldCommand.createAddCommand(fc.getTarget(),
							        fc.getRevisionNumber() + nRemote, fc.getValue());
							break;
						case REMOVE:
							command = MemoryFieldCommand.createRemoveCommand(fc.getTarget(),
							        fc.getRevisionNumber() + nRemote);
							break;
						case CHANGE:
							command = MemoryFieldCommand.createChangeCommand(fc.getTarget(),
							        fc.getRevisionNumber() + nRemote, fc.getValue());
							break;
						default:
							assert false : "Invalid command: " + fc;
						}
						lc.command = command;
					}
				}
				
				/*
				 * Applying all local commands as the current actor, ignoring
				 * what actor was used when they were originally executed.
				 */
				results[i] = executeCommand(command);
				
				// TODO maybe allow the callback to "fix" the change here to
				// prevent an avalanche of failed commands (wrong revisions) and
				// to prevent sending events that will be undone again anyway.
				
			}
			
			// TODO call LocalCommand callbacks
			
			// Clean unneeded events.
			this.eventQueue.cleanEvents(pos);
			
		} finally {
			
			cleanupOrphans();
			
			this.eventQueue.saveLog();
			endStateTransaction();
			
			this.eventQueue.setBlockSending(oldBlock);
			this.eventQueue.sendEvents();
			
		}
		
		return results;
	}
	
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
	
	/**
	 * Increment this entity's revision number.
	 */
	protected abstract void incrementRevisionAndSave();
	
	/**
	 * Get the {@link MemoryObject} with the given {@link XID}.
	 * 
	 * If the entity this method is called on already is an {@link MemoryObject}
	 * the method returns this entity exactly when the given {@link XID} matches
	 * its {@link XID} and null otherwise.
	 * 
	 * @param objectId The {@link XID} of the {@link MemoryObject} which is to
	 *            be returned
	 * 
	 * @return true if there is an {@link XObject} with the given {@link XID}
	 */
	protected abstract MemoryObject getObject(XID objectId);
	
	/**
	 * Create a new object, increase revision (if not in a transaction) and
	 * enqueue the corresponding event.
	 * 
	 * The caller is responsible for handling synchronization, for checking that
	 * this model has not been removed and for checking that the object doesn't
	 * already exist.
	 */
	protected abstract MemoryObject createObjectInternal(XID objectId);
	
	/**
	 * Remove an existing object, increase revision (if not in a transaction)
	 * and enqueue the corresponding event(s).
	 * 
	 * The caller is responsible for handling synchronization, for checking that
	 * this model has not been removed and for checking that the object actually
	 * exists.
	 */
	protected abstract void removeObjectInternal(XID objectId);
	
	/**
	 * @return Return the proxy for reading the current state.
	 */
	protected abstract XBaseModel getTransactionTarget();
	
	/**
	 * @return the revision number to return when executing {@link XCommand
	 *         XCommands}.
	 */
	protected abstract long getOldRevisionNumber();
	
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
	 * @throws IllegalStateException if this entity has already been removed
	 */
	protected abstract void checkRemoved() throws IllegalStateException;
	
	/**
	 * Save using the persistence layer, if this is a subtype of {@link XModel}.
	 */
	protected abstract void saveIfModel();
	
	/**
	 * Set the new revision number, if this is a subtype of {@link XModel}.
	 */
	protected abstract void setRevisionNumberIfModel(long modelRevisionNumber);
	
	/**
	 * Check if this entity may be synchronized.
	 */
	protected abstract void checkSync();
	
	/**
	 * Start a new state transaction.
	 * 
	 * @return true if a transaction was started and should be ended later.
	 */
	protected abstract void beginStateTransaction();
	
	/**
	 * End the current state transaction.
	 */
	protected abstract void endStateTransaction();
	
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
	
	@Override
	public XID getSessionActor() {
		return this.eventQueue.getActor();
	}
	
	@Override
	public void setSessionActor(XID actorId, String passwordHash) {
		this.eventQueue.setSessionActor(actorId, passwordHash);
	}
	
	@Override
	public List<LocalChange> getLocalChanges() {
		return this.eventQueue.getLocalChanges();
	}
	
	@Override
	public long getSynchronizedRevision() {
		return this.eventQueue.getSyncRevision();
	}
	
}
