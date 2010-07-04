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
import org.xydra.core.model.XBaseObject;
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
import org.xydra.core.model.delta.DeltaField;
import org.xydra.core.model.delta.DeltaModel;
import org.xydra.core.model.delta.DeltaObject;
import org.xydra.core.model.delta.NewField;
import org.xydra.core.model.delta.NewObject;


/**
 * Abstract base class for entities that can execute transactions (
 * {@link XObject} and {@link XModel}) implementing most of the logic behind
 * transactions.
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
	/** if this variable equals true, a transaction is currently running */
	private boolean transactionInProgress = false;
	
	public SynchronizesChangesImpl(MemoryEventQueue queue) {
		this.eventQueue = queue;
	}
	
	protected boolean transactionInProgress() {
		return this.transactionInProgress;
	}
	
	public long executeTransaction(XID actor, XTransaction transaction) {
		synchronized(this.eventQueue) {
			checkRemoved();
			
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

			for(int i = 0; i < transaction.size(); i++) {
				XAtomicCommand command = transaction.getCommand(i);
				
				if(command instanceof XModelCommand) {
					if(!transCheckModelCommand(model, (XModelCommand)command)) {
						return XCommand.FAILED;
					}
				} else if(command instanceof XObjectCommand) {
					if(!transCheckObjectCommand(model, (XObjectCommand)command)) {
						return XCommand.FAILED;
					}
				} else if(command instanceof XFieldCommand) {
					if(!transCheckFieldCommand(model, (XFieldCommand)command)) {
						return XCommand.FAILED;
					}
				} else {
					assert false : "transactions can only contain model, object and field commands";
				}
				
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
			if(nChanges > 1) {
				// set "transactionInProgress" to true to stop involuntarily
				// increasing the affected revision numbers
				this.transactionInProgress = true;
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
				
				this.transactionInProgress = false;
				
				// really increment the model's revision number
				incrementRevisionAndSave();
				
				// dispatch events
				this.eventQueue.sendEvents();
				
			} else {
				assert getOldRevisionNumber() == oldRev + 1 : "there should have been exactly one change";
			}
			
			return oldRev;
			
		}
		
	}
	
	/**
	 * Check if the given {@link XModelCommand} is valid in a model that is
	 * given by the current model but with the objects in removedObjects and
	 * fields in removedFields removed and the the objects in addedObjects and
	 * fields in addedFields created and values changed to those in
	 * changedValues and update the delta according to the command.
	 * 
	 * @return true if the command is valid.
	 */
	private boolean transCheckModelCommand(DeltaModel model, XModelCommand command) {
		
		XID objectId = command.getObjectID();
		
		switch(command.getChangeType()) {
		
		case ADD:
			if(model.hasObject(objectId)) {
				// command is invalid or doesn't change anything
				return command.isForced();
			}
			// command is OK and adds a new object
			model.createObject(objectId);
			return true;
			
		case REMOVE:
			XBaseObject object = model.getObject(objectId);
			
			if(object == null) {
				// command is invalid or doesn't change anything
				return command.isForced();
			}
			if(object.getRevisionNumber() != command.getRevisionNumber()) {
				if(!command.isForced()) {
					// command is invalid
					return false;
				}
			}
			// command is OK and removes an existing object
			model.removeObject(objectId);
			return true;
			
		default:
			throw new AssertionError("impossible type for model commands");
		}
		
	}
	
	/**
	 * Check if the given {@link XObjectCommand} is valid in a model that is
	 * given by the current model but with the objects in removedObjects and
	 * fields in removedFields removed and the the objects in addedObjects and
	 * fields in addedFields created and values changed to those in
	 * changedValues and update the delta according to the command.
	 * 
	 * @return true if the command is valid.
	 */
	private boolean transCheckObjectCommand(DeltaModel model, XObjectCommand command) {
		
		DeltaObject object = model.getObject(command.getObjectID());
		if(object == null) {
			// command is invalid
			return false;
		}
		
		XID fieldId = command.getFieldID();
		
		switch(command.getChangeType()) {
		
		case ADD:
			if(object.hasField(fieldId)) {
				// command is invalid or doesn't change anything
				return command.isForced();
			}
			// command is OK and adds a new field
			object.createField(fieldId);
			return true;
			
		case REMOVE:
			XBaseField field = object.getField(fieldId);
			
			if(field == null) {
				// command is invalid or doesn't change anything
				return command.isForced();
			}
			if(field.getRevisionNumber() != command.getRevisionNumber()) {
				if(!command.isForced()) {
					// command is invalid
					return false;
				}
			}
			// command is OK and removes an existing field
			object.removeField(fieldId);
			return true;
			
		default:
			throw new AssertionError("impossible type for object commands");
		}
		
	}
	
	/**
	 * Check if the given {@link XFieldCommand} is valid in a model that is
	 * given by the current model but with the objects in removedObjects and
	 * fields in removedFields removed and the the objects in addedObjects and
	 * fields in addedFields created and values changed to those in
	 * changedValues and update the delta according to the command.
	 * 
	 * @return true if the command is valid.
	 */
	private boolean transCheckFieldCommand(DeltaModel model, XFieldCommand command) {
		
		DeltaObject object = model.getObject(command.getObjectID());
		if(object == null) {
			// command is invalid
			return false;
		}
		
		DeltaField field = object.getField(command.getFieldID());
		if(field == null) {
			// command is invalid
			return false;
		}
		
		if(!command.isForced()) {
			if(field.getRevisionNumber() != command.getRevisionNumber()) {
				// command is invalid (wrong revision)
				return false;
			}
			if((command.getChangeType() == ChangeType.ADD) != field.isEmpty()) {
				// command is invalid (wrong type)
				return false;
			}
		}
		
		// command is OK
		field.setValue(command.getValue());
		
		return true;
	}
	
	public MemoryChangeLog getChangeLog() {
		return this.eventQueue.getChangeLog();
	}
	
	protected Orphans getOrphans() {
		return this.eventQueue.orphans;
	}
	
	public void rollback(long revision) {
		
		checkSync();
		
		if(revision < 0) {
			throw new RuntimeException("invalid revision number: " + revision);
		}
		
		MemoryChangeLog log = getChangeLog();
		long currentRev = log.getCurrentRevisionNumber();
		if(revision == currentRev) {
			return;
		}
		
		// stop the change log to prevent the rollback events from being logged
		boolean oldLogging = this.eventQueue.setLogging(false);
		
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
		log.truncateToRevision(revision);
		this.eventQueue.setLogging(oldLogging);
		
		saveIfModel();
		
	}
	
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
			this.eventQueue.orphans = new Orphans();
			
			int pos = this.eventQueue.getNextPosition();
			
			// Roll back to the old revision and save removed entities.
			rollback(lastRevision);
			
			// Apply the remote changes.
			for(XEvent remoteChange : remoteChanges) {
				if(remoteChange == null) {
					this.eventQueue.getChangeLog().appendEvent(null);
					continue;
				}
				// TODO ignore model events?
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
			
			Orphans orphans = this.eventQueue.orphans;
			this.eventQueue.orphans = null;
			
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
	
	/**
	 * Increment this entity's revision number.
	 */
	protected abstract void incrementRevisionAndSave();
	
	/**
	 * Get an object with a given ID.
	 * 
	 * If this is already and object the method returns this exactly when the
	 * given ID matches this object's ID, null otherwise.
	 * 
	 * @return true if there is an object with the given ID
	 */
	protected abstract MemoryObject getObject(XID objectId);
	
	/**
	 * Create a new object with the given ID.
	 * 
	 * If this is already an object this method should never be called.
	 * 
	 */
	protected abstract MemoryObject createObject(XID actor, XID objectId);
	
	/**
	 * Remove the existing object with the given ID.
	 * 
	 * If this is already an object this method should never be called.
	 * 
	 */
	protected abstract boolean removeObject(XID actor, XID objectId);
	
	/**
	 * @return Return the proxy for reading the current state.
	 */
	protected abstract XBaseModel getTransactionTarget();
	
	/**
	 * @return the revision number to return when executing commands
	 */
	protected abstract long getOldRevisionNumber();
	
	/**
	 * @return the model to use for sending transaction events
	 */
	protected abstract MemoryModel getModel();
	
	/**
	 * @return the object to use for sending transaction events
	 */
	protected abstract MemoryObject getObject();
	
	/**
	 * @throws IllegalStateException if this entity has already been removed
	 */
	protected abstract void checkRemoved() throws IllegalStateException;
	
	/**
	 * Save, if this is a model.
	 */
	protected abstract void saveIfModel();
	
	/**
	 * Set the new revision number, if this is a model.
	 */
	protected abstract void setRevisionNumberIfModel(long modelRevisionNumber);
	
	/**
	 * Check if this entity may be synchronized.
	 */
	protected abstract void checkSync();
	
}
