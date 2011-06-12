package org.xydra.core.change;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.xydra.base.X;
import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.XType;
import org.xydra.base.XX;
import org.xydra.base.change.ChangeType;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XFieldCommand;
import org.xydra.base.change.XModelCommand;
import org.xydra.base.change.XObjectCommand;
import org.xydra.base.change.XTransaction;
import org.xydra.base.rmof.XWritableField;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.base.rmof.XWritableObject;
import org.xydra.base.value.XValue;
import org.xydra.core.model.XLocalChangeCallback;
import org.xydra.core.model.XObject;
import org.xydra.core.model.impl.memory.AbstractEntity;
import org.xydra.core.model.impl.memory.MemoryModel;
import org.xydra.core.model.impl.memory.MemoryObject;


/**
 * TODO Document
 * 
 * TODO create special XObject- and XField-types that pass all changes that are
 * done on them to the TransactionModel and return these when a user tries to
 * get them by using the TransactionModel.
 * 
 * TODO check if this implementation is thread-safe "enough"
 * 
 * @author Kaidel
 * 
 */

public class TransactionModel extends AbstractEntity implements XWritableModel {
	
	private static final long serialVersionUID = -5636313889791653240L;
	
	private MemoryModel baseModel;
	private long revisionNumber;
	
	private Map<XID,InModelTransactionObject> changedObjects;
	private Map<XAddress,InModelTransactionField> changedFields;
	private Map<XAddress,XValue> changedValues;
	
	private Set<XID> removedObjects;
	private Set<XAddress> removedFields;
	
	private LinkedList<XCommand> commands;
	
	public TransactionModel(MemoryModel model) {
		this.baseModel = model;
		this.revisionNumber = model.getRevisionNumber();
		
		this.changedObjects = new HashMap<XID,InModelTransactionObject>();
		this.changedFields = new HashMap<XAddress,InModelTransactionField>();
		this.changedValues = new HashMap<XAddress,XValue>();
		this.removedObjects = new HashSet<XID>();
		this.removedFields = new HashSet<XAddress>();
		
		this.commands = new LinkedList<XCommand>();
	}
	
	// Transaction methods
	
	/**
	 * Returns the current list of commands that were stored after the last call
	 * of {@link #commit()}.
	 * 
	 * @return the current list of commands that were stored after the last call
	 *         of {@link #commit()}.
	 */
	public LinkedList<XCommand> getCurrentCommandList() {
		// return a copy so that it is not possible to directly change the
		// internal list
		LinkedList<XCommand> copy = new LinkedList<XCommand>();
		
		Collections.copy(copy, this.commands);
		return copy;
	}
	
	/**
	 * Executes the stored commands as an {@link XTransaction} on the wrapped
	 * {@link MemoryModel}. If it succeeds, this TransactionModel will be reset
	 * and can then be used to create a new XTransaction, if the transaction
	 * fails, nothing happens.
	 * 
	 * @return the revision number of the wrapped {@link MemoryModel} after the
	 *         execution of the {@link XTransaction} or XCommand.FAILED if the
	 *         execution fails
	 */
	public long commit() {
		XTransactionBuilder builder = new XTransactionBuilder(this.getAddress());
		
		for(int i = 0; i < this.commands.size(); i++) {
			builder.addCommand(this.commands.get(i));
		}
		
		XTransaction transaction = builder.build();
		
		long revNr = this.baseModel.executeCommand(transaction);
		
		if(revNr != XCommand.FAILED) {
			this.clear(); // restarts the TransactionModel
		}
		this.revisionNumber = revNr;
		return revNr;
	}
	
	/**
	 * Resets the TransactionModel, i.e. gets rid of all changes made to the
	 * TransactionModel, but not to the wrapped {@link MemoryModel}. After the
	 * execution, the TransactionModel will then represent the state the wrapped
	 * {@link MemoryModel} currently is in.
	 */
	public void clear() {
		/*
		 * TODO what happens with currently used InModelTransactionObjects &
		 * -Fields when this method is called?
		 */

		this.changedObjects.clear();
		this.changedFields.clear();
		this.changedValues.clear();
		this.removedObjects.clear();
		this.removedFields.clear();
		
		this.commands.clear();
	}
	
	/**
	 * Returns the current number of stored commands, which would be executed by
	 * calling {@link #commit()}.
	 * 
	 * @return the current number of stored commands, which would be executed by
	 *         calling {@link #commit()}.
	 */
	public int size() {
		return this.commands.size();
	}
	
	/**
	 * Returns true, if the state of the TransactionObject is different than the
	 * state of the wrapped {@link MemoryObject}, false otherwise.
	 * 
	 * @return true, if the state of the TransactionObject is different than the
	 *         state of the wrapped {@link MemoryObject}, false otherwise.
	 */
	public boolean isChanged() {
		return !(this.changedObjects.isEmpty() && this.removedObjects.isEmpty()
		        && this.changedFields.isEmpty() && this.removedFields.isEmpty() && this.changedValues
		        .isEmpty());
	}
	
	// XWritableModel-specific methods
	
	public XAddress getAddress() {
		return this.baseModel.getAddress();
	}
	
	public XID getID() {
		return this.baseModel.getID();
	}
	
	public long executeCommand(XCommand command) {
		return this.executeCommand(command, null);
	}
	
	public long executeCommand(XCommand command, XLocalChangeCallback callback) {
		XLocalChangeCallback usedCallback = callback;
		if(usedCallback == null) {
			usedCallback = new DummyCallback();
		}
		
		// check if it is a transaction
		if(command.getChangeType() == ChangeType.TRANSACTION) {
			XTransaction transaction = (XTransaction)command;
			
			for(int i = 0; i < transaction.size(); i++) {
				long result = executeCommand(transaction.getCommand(i));
				
				if(result == XCommand.FAILED) {
					usedCallback.onFailure();
					return XCommand.FAILED;
				}
			}
			
			/*
			 * TODO Improve transaction handling (rollback etc.)
			 */

			// Transaction "succeeded"
			usedCallback.onSuccess(this.revisionNumber);
			return this.revisionNumber;
		}
		
		// given command is no transaction
		
		// Simulate the action the given command would actually execute
		
		// check whether the given command actually refers to this model
		if(!command.getTarget().getModel().equals(this.getID())
		        || !command.getTarget().getRepository().equals(this.getAddress().getRepository())) {
			usedCallback.onFailure();
			return XCommand.FAILED;
		}
		
		// Model Commands
		if(command instanceof XModelCommand) {
			XModelCommand modelCommand = (XModelCommand)command;
			
			XID objectId = command.getChangedEntity().getObject();
			
			if(command.getChangeType() == ChangeType.ADD) {
				if(hasObject(objectId)) {
					if(!modelCommand.isForced()) {
						// not forced, tried to add something that already
						// existed
						usedCallback.onFailure();
						return XCommand.FAILED;
					}
					
					// forced command -> already finished with the execution
					usedCallback.onSuccess(this.getRevisionNumber());
					return this.getRevisionNumber();
				}
				
				// remove from list of removed object, if needed
				this.removedObjects.remove(objectId);
				
				XAddress temp = this.getAddress();
				XAddress objectAddress = XX.toAddress(temp.getRepository(), temp.getModel(),
				        objectId, null);
				
				InModelTransactionObject object = new InModelTransactionObject(objectAddress,
				        XCommand.NEW, this);
				this.changedObjects.put(objectId, object);
				
				// command succeeded -> add it to the list
				this.commands.add(command);
				
				usedCallback.onSuccess(this.getRevisionNumber());
				return this.getRevisionNumber();
			}

			else if(command.getChangeType() == ChangeType.REMOVE) {
				if(!hasObject(objectId)) {
					if(!modelCommand.isForced()) {
						// not forced, tried to remove something that didn't
						// exist
						usedCallback.onFailure();
						return XCommand.FAILED;
					}
					
					// forced command -> already finished with the execution
					usedCallback.onSuccess(this.getRevisionNumber());
					return this.getRevisionNumber();
				}
				
				XWritableObject object = this.getObject(objectId);
				// remember: this actually is an InModelTransactionObject
				assert object != null;
				assert object instanceof InModelTransactionObject;
				
				// check revision number
				if(!modelCommand.isForced()
				        && modelCommand.getRevisionNumber() != object.getRevisionNumber()) {
					usedCallback.onFailure();
					return XCommand.FAILED;
				}
				
				// mark it as removed
				this.removedObjects.add(objectId);
				
				// remove info from all other maps
				this.changedObjects.remove(objectId);
				
				// command succeeded -> add it to the list
				this.commands.add(command);
				
				usedCallback.onSuccess(this.getRevisionNumber());
				return this.getRevisionNumber();
			}
		}

		// Object Commands
		else if(command instanceof XObjectCommand) {
			XObjectCommand objectCommand = (XObjectCommand)command;
			
			XID fieldId = command.getChangedEntity().getField();
			
			if(!hasObject(command.getTarget().getObject())) {
				usedCallback.onFailure();
				return XCommand.FAILED;
			}
			
			XWritableObject object = this.getObject(command.getTarget().getObject());
			// remember: this is actually an InModelTransactionObject
			assert object != null;
			assert object instanceof InModelTransactionObject;
			
			if(command.getChangeType() == ChangeType.ADD) {
				if(object.hasField(fieldId)) {
					if(!objectCommand.isForced()) {
						// not forced, tried to add something that already
						// existed
						usedCallback.onFailure();
						return XCommand.FAILED;
					}
					
					// forced command -> already finished with the execution
					usedCallback.onSuccess(this.getRevisionNumber());
					return this.getRevisionNumber();
				}
				
				// remove from list of removed fields, if needed
				this.removedFields.remove(fieldId);
				
				XAddress temp = object.getAddress();
				XAddress address = XX.toAddress(temp.getRepository(), temp.getModel(), temp
				        .getObject(), fieldId);
				
				InModelTransactionField field = new InModelTransactionField(address, XCommand.NEW,
				        this);
				this.changedFields.put(address, field);
				
				// command succeeded -> add it to the list
				this.commands.add(command);
				
				usedCallback.onSuccess(this.getRevisionNumber());
				return this.getRevisionNumber();
			}

			else if(command.getChangeType() == ChangeType.REMOVE) {
				if(!object.hasField(fieldId)) {
					if(!objectCommand.isForced()) {
						// not forced, tried to remove something that didn't
						// exist
						usedCallback.onFailure();
						return XCommand.FAILED;
					}
					
					// forced command -> already finished with the execution
					usedCallback.onSuccess(this.getRevisionNumber());
					return this.getRevisionNumber();
				}
				
				XAddress fieldAddress = command.getChangedEntity();
				
				XWritableField field = this.getField(fieldAddress);
				// remember: this actually is an InModelTransactionField
				assert field != null;
				assert field instanceof InModelTransactionField;
				
				// check revision number
				if(!objectCommand.isForced()
				        && objectCommand.getRevisionNumber() != field.getRevisionNumber()) {
					usedCallback.onFailure();
					return XCommand.FAILED;
				}
				
				// mark it as removed
				this.removedFields.add(fieldAddress);
				
				// remove info from all other maps
				this.changedFields.remove(fieldAddress);
				this.changedValues.remove(fieldAddress);
				
				// command succeeded -> add it to the list
				this.commands.add(command);
				
				usedCallback.onSuccess(this.getRevisionNumber());
				return this.getRevisionNumber();
			}
		}
		
		// XFieldCommands
		if(command instanceof XFieldCommand) {
			XFieldCommand fieldCommand = (XFieldCommand)command;
			XAddress fieldAddress = command.getChangedEntity();
			XID fieldId = fieldAddress.getField();
			XID objectId = fieldAddress.getObject();
			
			if(!hasObject(objectId)) {
				usedCallback.onFailure();
				return XCommand.FAILED;
			}
			
			XWritableObject object = this.getObject(objectId);
			// remember this is actually an InModelTransactionObject
			assert object != null;
			assert object instanceof InModelTransactionObject;
			
			if(!object.hasField(fieldId)) {
				usedCallback.onFailure();
				return XCommand.FAILED;
			}
			
			XWritableField field = object.getField(fieldId);
			// remember: this actually is an InModelTransactionField
			assert field != null;
			assert field instanceof InObjectTransactionField;
			
			// check revision number
			if(fieldCommand.getRevisionNumber() != field.getRevisionNumber()
			        && !fieldCommand.isForced()) {
				usedCallback.onFailure();
				return XCommand.FAILED;
			}
			
			if(fieldCommand.getChangeType() == ChangeType.ADD) {
				if(field.getValue() != null) {
					if(!fieldCommand.isForced()) {
						usedCallback.onFailure();
						return XCommand.FAILED;
					}
				}
				
				// "add" the value
				this.changedValues.put(fieldAddress, fieldCommand.getValue());
				
				// command succeeded -> add it to the list
				this.commands.add(command);
				
				usedCallback.onSuccess(field.getRevisionNumber());
				return field.getRevisionNumber();
			}

			else if(fieldCommand.getChangeType() == ChangeType.REMOVE) {
				if(field.getValue() == null) {
					if(!fieldCommand.isForced()) {
						usedCallback.onFailure();
						return XCommand.FAILED;
					}
				}
				
				// "remove" the value
				this.changedValues.put(fieldAddress, null);
				
				// command succeeded -> add it to the list
				this.commands.add(command);
				
				usedCallback.onSuccess(field.getRevisionNumber());
				return field.getRevisionNumber();
			}

			else if(fieldCommand.getChangeType() == ChangeType.CHANGE) {
				if(field.getValue() == null) {
					if(!fieldCommand.isForced()) {
						usedCallback.onFailure();
						return XCommand.FAILED;
					}
				}
				
				// "change" the value
				this.changedValues.put(fieldAddress, fieldCommand.getValue());
				
				// command succeeded -> add it to the list
				this.commands.add(command);
				
				usedCallback.onSuccess(field.getRevisionNumber());
				return field.getRevisionNumber();
			}
		}
		
		throw new IllegalArgumentException(
		        "Given Command was neither a correct instance of XModelCommand, XObjectCommand, XFieldCommand or XTransaction!");
	}
	
	@Override
	public long getRevisionNumber() {
		return this.revisionNumber;
	}
	
	public boolean hasObject(XID objectId) {
		/*
		 * only return true if the object is new or if it wasn't removed and it
		 * part of the baseModel
		 */

		return this.changedObjects.containsKey(objectId)
		        || (this.baseModel.hasObject(objectId) && !this.removedObjects.contains(objectId));
	}
	
	public XWritableObject createObject(XID id) {
		XCommand addCommand = X.getCommandFactory().createSafeAddObjectCommand(
		        this.baseModel.getAddress(), id);
		
		this.executeCommand(addCommand);
		
		return this.getObject(id);
	}
	
	public boolean isEmpty() {
		// TODO Implement correctly!
		
		return this.baseModel.isEmpty() && this.changedObjects.isEmpty()
		        && this.changedFields.isEmpty();
	}
	
	public long executeModelCommand(XModelCommand command) {
		return this.executeCommand(command, null);
	}
	
	public XWritableObject getObject(XID objectId) {
		XWritableObject object = null;
		
		if(this.changedObjects.containsKey(objectId)) {
			object = this.changedObjects.get(objectId);
		} else {
			/*
			 * only look into the base model if the object wasn't removed
			 */
			if(!this.removedObjects.contains(objectId)) {
				object = this.baseModel.getObject(objectId);
			}
		}
		
		if(object == null) {
			return null;
		} else {
			return new InModelTransactionObject(object.getAddress(), object.getRevisionNumber(),
			        this);
		}
	}
	
	public boolean removeObject(XID objectId) {
		return this.baseModel.removeObject(objectId);
	}
	
	@Override
	public XType getType() {
		return XType.XMODEL;
	}
	
	@Override
	protected AbstractEntity getFather() {
		return this.baseModel.getFather();
	}
	
	/*
	 * Special methods needed for the helperclasses InTransactionObject and
	 * InTransactionField
	 */

	protected XWritableField getField(XAddress address) {
		assert address.getAddressedType() == XType.XFIELD;
		XWritableField field = null;
		
		if(this.changedFields.containsKey(address)) {
			field = this.changedFields.get(address);
		} else {
			/*
			 * only look into the base model if the field wasn't removed
			 */
			if(!this.removedFields.contains(address)) {
				XObject object = this.baseModel.getObject(address.getObject());
				
				if(object != null) {
					field = object.getField(address.getField());
				}
			}
		}
		
		if(field == null) {
			return null;
		} else {
			return new InModelTransactionField(field.getAddress(), field.getRevisionNumber(), this);
		}
	}
	
	protected XValue getValue(XAddress address) {
		assert address.getAddressedType() == XType.XFIELD;
		
		// TODO implement
		return null;
	}
	
	// Unsupported Methods
	@Override
	public Iterator<XID> iterator() {
		throw new UnsupportedOperationException();
	}
	
	private class DummyCallback implements XLocalChangeCallback {
		
		@Override
		public void onFailure() {
			// do nothing
			
		}
		
		@Override
		public void onSuccess(long revision) {
			// do nothing
			
		}
		
	}
	
}
