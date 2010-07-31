package org.xydra.core.model.impl.memory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xydra.core.XX;
import org.xydra.core.change.ChangeType;
import org.xydra.core.change.XAtomicCommand;
import org.xydra.core.change.XAtomicEvent;
import org.xydra.core.change.XCommand;
import org.xydra.core.change.XEvent;
import org.xydra.core.change.XFieldCommand;
import org.xydra.core.change.XModelCommand;
import org.xydra.core.change.XModelEvent;
import org.xydra.core.change.XObjectCommand;
import org.xydra.core.change.XObjectEvent;
import org.xydra.core.change.XTransaction;
import org.xydra.core.change.XTransactionEvent;
import org.xydra.core.change.impl.memory.MemoryFieldCommand;
import org.xydra.core.change.impl.memory.MemoryModelCommand;
import org.xydra.core.change.impl.memory.MemoryObjectCommand;
import org.xydra.core.model.IHasXAddress;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XBaseField;
import org.xydra.core.model.XBaseModel;
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
import org.xydra.core.model.delta.NewField;
import org.xydra.core.model.delta.NewObject;


/**
 * Abstract base class for entities that can execute {@link XTransaction
 * XTransactions} ( {@link XObject} and {@link XModel}) implementing most of the
 * logic behind transactions.
 * 
 * @author dscharrer
 */
public abstract class SynchronizesChangesImpl implements IHasXAddress, XSynchronizesChanges,
        XExecutesCommands {
	
	protected static class Orphans {
		Map<XID,MemoryObject> objects = new HashMap<XID,MemoryObject>();
		Map<XAddress,MemoryField> fields = new HashMap<XAddress,MemoryField>();
	}
	
	protected final MemoryEventQueue eventQueue;
	
	public SynchronizesChangesImpl(MemoryEventQueue queue) {
		this.eventQueue = queue;
	}
	
	public long executeTransaction(XID actor, XTransaction transaction) {
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
			
			long nChanges = model.countChanges(2);
			
			if(nChanges == 0) {
				// nothing to change
				return XCommand.NOCHANGE;
			}
			
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
				removeObject(actor, objectId);
			}
			
			for(NewObject object : model.getNewObjects()) {
				MemoryObject newObject = createObject(actor, object.getID());
				for(XID fieldId : object) {
					XBaseField field = object.getField(fieldId);
					XField newField = newObject.createField(actor, fieldId);
					if(!field.isEmpty()) {
						newField.setValue(actor, field.getValue());
					}
				}
			}
			
			for(ChangedObject object : model.getChangedObjects()) {
				MemoryObject oldObject = getObject(object.getID());
				
				for(XID fieldId : object.getRemovedFields()) {
					oldObject.removeField(actor, fieldId);
				}
				
				for(NewField field : object.getNewFields()) {
					XField newField = oldObject.createField(actor, field.getID());
					if(!field.isEmpty()) {
						newField.setValue(actor, field.getValue());
					}
				}
				
				for(ChangedField field : object.getChangedFields()) {
					if(field.isChanged()) {
						XField oldField = oldObject.getField(field.getID());
						oldField.setValue(actor, field.getValue());
					}
				}
				
			}
			
			// update revision numbers and save state
			
			if(nChanges > 1) {
				
				long newRevision = oldRev + 1;
				
				this.eventQueue.createTransactionEvent(actor, getModel(), getObject(), since);
				
				// new objects
				for(NewObject object : model.getNewObjects()) {
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
					for(NewField field : object.getNewFields()) {
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
			
			return oldRev;
			
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
			throw new RuntimeException("invalid revision number: " + revision);
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
			for(long i = currentRev - 1; i >= revision; i--) {
				// TODO: ignore model events
				XEvent event = log.getEventAt(i);
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
		XAtomicCommand command = XX.createForcedUndoCommand(event);
		long result = executeCommand(null, command);
		assert result > 0 : "rollback command " + command + " for event " + event + " failed";
		XAddress target = event.getTarget();
		
		// fix revision numbers
		setRevisionNumberIfModel(event.getModelRevisionNumber());
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
		setRevisionNumberIfModel(event.getModelRevisionNumber());
		saveIfModel();
	}
	
	public long[] synchronize(List<XEvent> remoteChanges, long lastRevision, XID actor,
	        List<XCommand> localChanges, List<? extends XSynchronizationCallback> callbacks) {
		
		if(callbacks != null && localChanges.size() != callbacks.size()) {
			throw new IllegalArgumentException("number of callbacks must equal number of commands");
		}
		
		checkSync();
		
		long[] results = new long[localChanges.size()];
		
		boolean oldBlock = this.eventQueue.setBlockSending(true);
		
		try {
			
			assert this.eventQueue.orphans == null;
			beginStateTransaction();
			this.eventQueue.orphans = new Orphans();
			
			int pos = this.eventQueue.getNextPosition();
			
			// Roll back to the old revision and save removed entities.
			rollback(lastRevision);
			
			// Apply the remote changes.
			for(XEvent remoteChange : remoteChanges) {
				if(remoteChange == null) {
					this.eventQueue.logNullEvent();
					continue;
				}
				XCommand replayCommand = XX.createReplayCommand(remoteChange);
				long result = executeCommand(remoteChange.getActor(), replayCommand);
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
				
				results[i] = executeCommand(actor, command);
				
				if(callbacks != null && results[i] == XCommand.FAILED) {
					callbacks.get(i).failed();
				}
				
			}
			
			// Clean unneeded events.
			this.eventQueue.cleanEvents(pos);
			
			cleanupOrphans();
			
		} finally {
			
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
	 * Creates a new {@link MemoryObject} with the given {@link XID}.
	 * 
	 * This method should never be called on entities that are {@link XObject
	 * XObjects}.
	 * 
	 * @param actor The {@link XID} of the actor
	 * @param objectId The {@link XID} for the {@link MemoryObject} which is to
	 *            be created
	 * @return the newly created {@link MemoryObject} or the already existing
	 *         {@link MemoryObject}, if the given {@link XID} was already taken.
	 * @throws AssertionError if the entity on which this method is called is an
	 *             {@link XObject}
	 */
	protected abstract MemoryObject createObject(XID actor, XID objectId);
	
	/**
	 * Removes the {@link MemoryObject} with the given {@link XID}.
	 * 
	 * This method should never be called on entities that are {@link XObject
	 * XObjects}.
	 * 
	 * @param actor The {@link XID} of the actor
	 * @param objectId The {@link XID} of the {@link MemoryObject} which is to
	 *            be removed
	 * @return true, if removal is successful.
	 * @throws AssertionError if the entity on which this method is called is an
	 *             {@link XObject}
	 */
	protected abstract boolean removeObject(XID actor, XID objectId);
	
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
	
}
