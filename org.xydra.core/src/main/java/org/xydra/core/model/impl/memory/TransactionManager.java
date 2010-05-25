package org.xydra.core.model.impl.memory;

import org.xydra.annotations.ModificationOperation;
import org.xydra.core.change.ChangeType;
import org.xydra.core.change.XAtomicCommand;
import org.xydra.core.change.XCommand;
import org.xydra.core.change.XFieldCommand;
import org.xydra.core.change.XModelCommand;
import org.xydra.core.change.XObjectCommand;
import org.xydra.core.change.XTransaction;
import org.xydra.core.model.IHasXAddress;
import org.xydra.core.model.XBaseField;
import org.xydra.core.model.XBaseModel;
import org.xydra.core.model.XBaseObject;
import org.xydra.core.model.XExecutesTransactions;
import org.xydra.core.model.XField;
import org.xydra.core.model.XID;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;
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
public abstract class TransactionManager implements IHasXAddress, XExecutesTransactions {
	
	protected final MemoryEventQueue eventQueue;
	/** if this variable equals true, a transaction is currently running */
	private boolean transactionInProgress = false;
	
	public TransactionManager(MemoryEventQueue queue) {
		this.eventQueue = queue;
	}
	
	protected boolean transactionInProgress() {
		return this.transactionInProgress;
	}
	
	@ModificationOperation
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
				XObject newObject = createObject(actor, object.getID());
				for(XID fieldId : object) {
					XBaseField field = object.getField(fieldId);
					XField newField = newObject.createField(actor, fieldId);
					if(!field.isEmpty()) {
						newField.setValue(actor, field.getValue());
					}
				}
			}
			
			for(ChangedObject object : model.getChangedObjects()) {
				XObject oldObject = getObject(object.getID());
				
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
	protected abstract XObject createObject(XID actor, XID objectId);
	
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
	
}
