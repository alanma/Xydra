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
import org.xydra.base.change.XAtomicCommand;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XFieldCommand;
import org.xydra.base.change.XModelCommand;
import org.xydra.base.change.XObjectCommand;
import org.xydra.base.change.XTransaction;
import org.xydra.base.rmof.XWritableField;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.base.rmof.XWritableObject;
import org.xydra.base.value.XValue;
import org.xydra.core.model.XField;
import org.xydra.core.model.XLocalChangeCallback;
import org.xydra.core.model.XObject;
import org.xydra.core.model.impl.memory.AbstractEntity;
import org.xydra.core.model.impl.memory.MemoryModel;
import org.xydra.core.model.impl.memory.MemoryObject;


/**
 * A helper class to create {@link XTransaction XTransactions} for a specific
 * {@link MemoryModel}. This class wraps the given {@link MemoryModel} and then
 * acts as a simulator. It provides all methods for changing the state of an
 * {@link MemoryModel}, but the changes that are executed on this
 * TransactionObject are not actually executed on the {@link MemoryModel}, but
 * just simulated and saved. After all changes were applied, they can be execute
 * in a single {@link XTransaction} on the given {@link MemoryModel} by calling
 * the {@link #commit()} method.
 * 
 * In short, this class makes creating and executing an {@link XTransaction} as
 * simple as executing simple methods on an {@link MemoryModel}.
 * 
 * @author Kaidel
 * 
 */

/**
 * TODO consider making this implementation thread-safe
 */
public class TransactionModel extends AbstractEntity implements XWritableModel {
	
	private static final long serialVersionUID = -5636313889791653240L;
	
	private MemoryModel baseModel;
	private long revisionNumber;
	
	private Map<XID,InModelTransactionObject> changedObjects, transChangedObjects;
	private Map<XAddress,InModelTransactionField> changedFields, transChangedFields;
	private Map<XAddress,XValue> changedValues, transChangedValues;
	
	private Set<XID> removedObjects, transRemovedObjects;
	private Set<XAddress> removedFields, transRemovedFields;
	
	private LinkedList<XAtomicCommand> commands;
	
	private boolean inTransaction;
	
	public TransactionModel(MemoryModel model) {
		this.baseModel = model;
		this.revisionNumber = model.getRevisionNumber();
		
		this.changedObjects = new HashMap<XID,InModelTransactionObject>();
		this.changedFields = new HashMap<XAddress,InModelTransactionField>();
		this.changedValues = new HashMap<XAddress,XValue>();
		this.removedObjects = new HashSet<XID>();
		this.removedFields = new HashSet<XAddress>();
		
		this.transChangedObjects = new HashMap<XID,InModelTransactionObject>();
		this.transChangedFields = new HashMap<XAddress,InModelTransactionField>();
		this.transChangedValues = new HashMap<XAddress,XValue>();
		this.transRemovedObjects = new HashSet<XID>();
		this.transRemovedFields = new HashSet<XAddress>();
		
		this.commands = new LinkedList<XAtomicCommand>();
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
		
		this.transChangedObjects.clear();
		this.transChangedFields.clear();
		this.transChangedValues.clear();
		this.transRemovedObjects.clear();
		this.transRemovedFields.clear();
		
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
		        && this.changedFields.isEmpty() && this.removedFields.isEmpty()
		        && this.changedValues.isEmpty() && this.commands.isEmpty());
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
			return this.handleTransaction(transaction, callback);
		}
		
		// given command is no transaction
		
		// Simulate the action the given command would actually execute
		
		// check whether the given command actually refers to this model
		if(!command.getTarget().getModel().equals(this.getID())
		        || !command.getTarget().getRepository().equals(this.getAddress().getRepository())) {
			usedCallback.onFailure();
			return XCommand.FAILED;
		}
		
		// all commands somehow concern objects -> check if it exists
		
		// Model Commands
		if(command instanceof XModelCommand) {
			return this.handleModelCommand((XModelCommand)command, usedCallback);
		}

		// Object Commands
		else if(command instanceof XObjectCommand) {
			return this.handleObjectCommand((XObjectCommand)command, usedCallback);
		}

		// XFieldCommands
		else if(command instanceof XFieldCommand) {
			return this.handleFieldCommand((XFieldCommand)command, usedCallback);
		}
		
		throw new IllegalArgumentException(
		        "Given Command was neither a correct instance of XModelCommand, XObjectCommand, XFieldCommand or XTransaction!");
	}
	
	private boolean objectExists(XID objectId) {
		boolean objectExists = this.hasObject(objectId);
		
		if(this.inTransaction) {
			// set it to true, if the object was added during the
			// transaction
			objectExists |= this.transChangedObjects.containsKey(objectId);
			
			// set it to false, if the field was removed
			objectExists &= !this.transRemovedObjects.contains(objectId);
		}
		
		return objectExists;
	}
	
	private boolean fieldExists(XWritableObject object, XAddress fieldAddress) {
		boolean fieldExists = object.hasField(fieldAddress.getField());
		
		if(this.inTransaction) {
			// set it to true, if the field was added during the
			// transaction
			fieldExists |= this.transChangedFields.containsKey(fieldAddress);
			
			// set it to false, if the field was removed
			fieldExists &= !this.transRemovedFields.contains(fieldAddress);
		}
		
		return fieldExists;
	}
	
	private long handleModelCommand(XModelCommand modelCommand, XLocalChangeCallback callback) {
		assert callback != null;
		XID objectId = modelCommand.getChangedEntity().getObject();
		
		boolean objectExists = this.objectExists(objectId);
		
		if(modelCommand.getChangeType() == ChangeType.ADD) {
			if(objectExists) {
				if(!modelCommand.isForced()) {
					// not forced, tried to add something that already
					// existed
					callback.onFailure();
					return XCommand.FAILED;
				}
				
				// forced command -> already finished with the execution
				callback.onSuccess(this.getRevisionNumber());
				return this.getRevisionNumber();
			}
			
			XAddress temp = this.getAddress();
			XAddress objectAddress = XX.toAddress(temp.getRepository(), temp.getModel(), objectId,
			        null);
			
			InModelTransactionObject object = new InModelTransactionObject(objectAddress,
			        XCommand.NEW, this);
			
			if(!this.inTransaction) {
				// remove from list of removed object, if needed
				this.removedObjects.remove(objectId);
				this.changedObjects.put(objectId, object);
				
				// command succeeded -> add it to the list
				this.commands.add((XAtomicCommand)modelCommand);
				
			} else {
				/*
				 * Do not execute the changes just yet. They will be executed
				 * after it is certain that all commands of the transaction can
				 * be safely executed.
				 */
				this.transRemovedObjects.remove(objectId);
				this.transChangedObjects.put(objectId, object);
			}
			
			callback.onSuccess(this.getRevisionNumber());
			return this.getRevisionNumber();
		}

		else if(modelCommand.getChangeType() == ChangeType.REMOVE) {
			if(!objectExists) {
				if(!modelCommand.isForced()) {
					// not forced, tried to remove something that didn't
					// exist
					callback.onFailure();
					return XCommand.FAILED;
				}
				
				// forced command -> already finished with the execution
				callback.onSuccess(this.getRevisionNumber());
				return this.getRevisionNumber();
			}
			
			XWritableObject object = (!this.inTransaction) ? this.getObject(objectId) : this
			        .getObjectInTransaction(objectId);
			
			// remember: this actually is an InModelTransactionObject
			assert object != null;
			assert object instanceof InModelTransactionObject;
			
			// check revision number
			if(!modelCommand.isForced()
			        && modelCommand.getRevisionNumber() != object.getRevisionNumber()) {
				callback.onFailure();
				return XCommand.FAILED;
			}
			
			// remove all fields of the removed object
			for(XID fieldId : object) {
				XAddress temp = object.getAddress();
				XAddress fieldAddress = XX.toAddress(temp.getRepository(), temp.getModel(),
				        temp.getObject(), fieldId);
				
				if(!this.inTransaction) {
					this.removedFields.add(fieldAddress);
					this.changedFields.remove(fieldAddress);
				} else {
					this.transRemovedFields.add(fieldAddress);
					this.transChangedFields.remove(fieldAddress);
				}
			}
			
			if(!this.inTransaction) {
				// mark it as removed
				this.removedObjects.add(objectId);
				
				// remove info from all other maps
				this.changedObjects.remove(objectId);
				
				// command succeeded -> add it to the list
				this.commands.add((XAtomicCommand)modelCommand);
			} else {
				/*
				 * Do not execute the changes just yet. They will be executed
				 * after it is certain that all commands of the transaction can
				 * be safely executed.
				 */

				this.transRemovedObjects.add(objectId);
				this.transChangedObjects.remove(objectId);
			}
			
			callback.onSuccess(this.getRevisionNumber());
			return this.getRevisionNumber();
		}
		
		throw new IllegalArgumentException(
		        "Given Command was not a correct instance of XModelCommand!");
	}
	
	private long handleObjectCommand(XObjectCommand objectCommand, XLocalChangeCallback callback) {
		assert callback != null;
		
		XID objectId = objectCommand.getChangedEntity().getObject();
		boolean objectExists = this.objectExists(objectId);
		
		if(!objectExists) {
			callback.onFailure();
			return XCommand.FAILED;
		}
		
		XWritableObject object = (!this.inTransaction) ? this.getObject(objectId) : this
		        .getObjectInTransaction(objectId);
		// remember: this is actually an InModelTransactionObject
		assert object != null;
		assert object instanceof InModelTransactionObject;
		
		XAddress fieldAddress = objectCommand.getChangedEntity();
		boolean fieldExists = this.fieldExists(object, fieldAddress);
		
		if(objectCommand.getChangeType() == ChangeType.ADD) {
			if(fieldExists) {
				if(!objectCommand.isForced()) {
					// not forced, tried to add something that already
					// existed
					callback.onFailure();
					return XCommand.FAILED;
				}
				
				// forced command -> already finished with the execution
				callback.onSuccess(this.getRevisionNumber());
				return this.getRevisionNumber();
			}
			
			InModelTransactionField field = new InModelTransactionField(fieldAddress, XCommand.NEW,
			        this);
			
			if(!this.inTransaction) {
				// remove from list of removed fields, if needed
				this.removedFields.remove(fieldAddress);
				
				this.changedFields.put(fieldAddress, field);
				
				// command succeeded -> add it to the list
				this.commands.add((XAtomicCommand)objectCommand);
			} else {
				/*
				 * Do not execute the changes just yet. They will be executed
				 * after it is certain that all commands of the transaction can
				 * be safely executed.
				 */
				this.transRemovedFields.remove(fieldAddress);
				this.transChangedFields.put(fieldAddress, field);
			}
			
			callback.onSuccess(this.getRevisionNumber());
			return this.getRevisionNumber();
		}

		else if(objectCommand.getChangeType() == ChangeType.REMOVE) {
			if(!fieldExists) {
				if(!objectCommand.isForced()) {
					// not forced, tried to remove something that didn't
					// exist
					callback.onFailure();
					return XCommand.FAILED;
				}
				
				// forced command -> already finished with the execution
				callback.onSuccess(this.getRevisionNumber());
				return this.getRevisionNumber();
			}
			
			XWritableField field = (!this.inTransaction) ? this.getField(fieldAddress) : this
			        .getFieldInTransaction(fieldAddress);
			// remember: this actually is an InModelTransactionField
			assert field != null;
			assert field instanceof InModelTransactionField;
			
			// check revision number
			if(!objectCommand.isForced()
			        && objectCommand.getRevisionNumber() != field.getRevisionNumber()) {
				callback.onFailure();
				return XCommand.FAILED;
			}
			
			if(!this.inTransaction) {
				// mark it as removed
				this.removedFields.add(fieldAddress);
				
				// remove info from all other maps
				this.changedFields.remove(fieldAddress);
				this.changedValues.remove(fieldAddress);
				
				// command succeeded -> add it to the list
				this.commands.add((XAtomicCommand)objectCommand);
			} else {
				/*
				 * Do not execute the changes just yet. They will be executed
				 * after it is certain that all commands of the transaction can
				 * be safely executed.
				 */
				this.transRemovedFields.add(fieldAddress);
				this.transChangedFields.remove(fieldAddress);
				this.transChangedValues.remove(fieldAddress);
			}
			
			callback.onSuccess(this.getRevisionNumber());
			return this.getRevisionNumber();
		}
		
		throw new IllegalArgumentException(
		        "Given Command was not a correct instance of XObjectCommand!");
	}
	
	private long handleFieldCommand(XFieldCommand fieldCommand, XLocalChangeCallback callback) {
		assert callback != null;
		
		XID objectId = fieldCommand.getChangedEntity().getObject();
		
		if(!this.objectExists(objectId)) {
			callback.onFailure();
			return XCommand.FAILED;
		}
		
		XWritableObject object = (!this.inTransaction) ? this.getObject(objectId) : this
		        .getObjectInTransaction(objectId);
		// remember: this is actually an InModelTransactionObject
		assert object != null;
		assert object instanceof InModelTransactionObject;
		
		// all commands concern fields -> check if it exists
		XID fieldId = fieldCommand.getChangedEntity().getField();
		XAddress fieldAddress = fieldCommand.getChangedEntity();
		
		if(!this.fieldExists(object, fieldAddress)) {
			callback.onFailure();
			return XCommand.FAILED;
		}
		
		XWritableField field = object.getField(fieldId);
		
		if(this.inTransaction && this.transChangedFields.containsKey(fieldAddress)) {
			field = this.transChangedFields.get(fieldAddress);
		}
		
		// remember: this actually is an InObjectTransactionField
		assert field != null;
		assert field instanceof InObjectTransactionField;
		
		// check revision number
		if(fieldCommand.getRevisionNumber() != field.getRevisionNumber()
		        && !fieldCommand.isForced()) {
			callback.onFailure();
			return XCommand.FAILED;
		}
		
		/*
		 * check whether the command is forced or not and if it meets the
		 * criteria it has to fulfill for execution
		 */
		XValue value = field.getValue();
		if(this.inTransaction && this.transChangedValues.containsKey(fieldAddress)) {
			/*
			 * field was changed in the XTransaction which is currently being
			 * added to this TransactionObject
			 */
			value = this.transChangedValues.get(fieldAddress);
		}
		
		if(fieldCommand.getChangeType() == ChangeType.ADD) {
			if(value != null && !fieldCommand.isForced()) {
				callback.onFailure();
				return XCommand.FAILED;
			}
		} else if(fieldCommand.getChangeType() == ChangeType.REMOVE) {
			if(value == null && !fieldCommand.isForced()) {
				if(!fieldCommand.isForced()) {
					callback.onFailure();
					return XCommand.FAILED;
				}
			}
		} else if(fieldCommand.getChangeType() == ChangeType.CHANGE) {
			if(value == null && !fieldCommand.isForced()) {
				callback.onFailure();
				return XCommand.FAILED;
			}
		}
		
		if(!this.inTransaction) {
			// "add"/"remove"/"change" the value
			this.changedValues.put(fieldAddress, fieldCommand.getValue());
			
			// command succeeded -> add it to the list
			this.commands.add((XAtomicCommand)fieldCommand);
		} else {
			/*
			 * Do not execute the changes just yet. They will be executed after
			 * it is certain that all commands of the transaction can be safely
			 * executed.
			 */
			this.transChangedValues.put(fieldAddress, fieldCommand.getValue());
		}
		
		callback.onSuccess(field.getRevisionNumber());
		return field.getRevisionNumber();
		
	}
	
	private long handleTransaction(XTransaction transaction, XLocalChangeCallback callback) {
		this.inTransaction = true;
		
		assert callback != null;
		/*
		 * assert: transaction holds no further XTransactions (can not happen,
		 * since instances of XTransactions can never hold other XTransactions)
		 */
		this.transChangedObjects.clear();
		this.transChangedFields.clear();
		this.transChangedValues.clear();
		this.transRemovedObjects.clear();
		this.transRemovedFields.clear();
		
		for(XCommand command : transaction) {
			long result = executeCommand(command);
			
			if(result == XCommand.FAILED) {
				// rollback happens implicitly, since nothing was really changed
				callback.onFailure();
				return XCommand.FAILED;
			}
		}
		
		// Transaction "succeeded"
		for(XCommand command : transaction) {
			// execute the changes of the commands on the TransactionObject
			if(command instanceof XModelCommand) {
				XModelCommand mdlCmd = (XModelCommand)command;
				
				XID objectId = command.getChangedEntity().getObject();
				XAddress objectAddress = command.getChangedEntity();
				assert objectAddress.getAddressedType() == XType.XFIELD;
				
				if(mdlCmd.getChangeType() == ChangeType.ADD) {
					if(!this.hasObject(objectId)) {
						/*
						 * cases in which "hasObject" would evaluate to "true"
						 * here are cases were the command was forced and the
						 * field already existed, so we do not have to do
						 * anything here
						 */

						this.removedObjects.remove(objectId);
						
						InModelTransactionObject object = new InModelTransactionObject(
						        objectAddress, XCommand.NEW, this);
						
						this.changedObjects.put(objectAddress.getObject(), object);
					}
				} else {
					assert mdlCmd.getChangeType() == ChangeType.REMOVE;
					
					/*
					 * forced commands to not have to be treated specially, if
					 * the command was forced and the target didn't exist before
					 * the execution of the command, the following lines will
					 * just do nothing
					 */

					XWritableObject object = this.getObject(objectId);
					
					for(XID fieldId : object) {
						XAddress temp = object.getAddress();
						XAddress fieldAddress = XX.toAddress(temp.getRepository(), temp.getModel(),
						        temp.getObject(), fieldId);
						
						this.removedFields.add(fieldAddress);
						this.changedFields.remove(fieldAddress);
					}
					
					// mark it as removed
					this.removedObjects.add(objectId);
					
					// remove info from all other maps
					this.changedObjects.remove(objectId);
				}
			} else if(command instanceof XObjectCommand) {
				XObjectCommand objCmd = (XObjectCommand)command;
				
				XAddress fieldAddress = command.getChangedEntity();
				assert fieldAddress.getAddressedType() == XType.XFIELD;
				
				if(objCmd.getChangeType() == ChangeType.ADD) {
					if(!this.hasField(fieldAddress)) {
						/*
						 * cases in which "hasField" would evaluate to "true"
						 * here are cases were the command was forced and the
						 * field already existed, so we do not have to do
						 * anything here
						 */

						this.removedFields.remove(fieldAddress);
						
						InModelTransactionField field = new InModelTransactionField(fieldAddress,
						        XCommand.NEW, this);
						
						this.changedFields.put(fieldAddress, field);
					}
				} else {
					assert objCmd.getChangeType() == ChangeType.REMOVE;
					
					/*
					 * forced commands to not have to be treated specially, if
					 * the command was forced and the target didn't exist before
					 * the execution of the command, the following lines will
					 * just do nothing
					 */

					// mark it as removed
					this.removedFields.add(fieldAddress);
					
					// remove info from all other maps
					this.changedFields.remove(fieldAddress);
					this.changedValues.remove(fieldAddress);
				}
			} else {
				assert command instanceof XFieldCommand;
				XAddress fieldAddress = command.getChangedEntity();
				assert fieldAddress.getAddressedType() == XType.XFIELD;
				
				/*
				 * the action that has to be executed for field commands is
				 * always the same, no matter what the command type was
				 */

				this.changedValues.put(fieldAddress, ((XFieldCommand)command).getValue());
			}
			
			// add command to the command list
			this.commands.add((XAtomicCommand)command);
		}
		
		this.inTransaction = false;
		
		// Transaction "succeeded"
		callback.onSuccess(this.getRevisionNumber());
		return this.getRevisionNumber();
	}
	
	private XWritableObject getObjectInTransaction(XID objectId) {
		/*
		 * extra method for this is useful concerning concurrent access,
		 * rewriting getObject would make some problems here
		 * 
		 * Note: extra method is not necessary if we do not care about
		 * concurrent access
		 */
		XWritableObject object = null;
		
		if(this.inTransaction && this.transChangedObjects.containsKey(objectId)) {
			object = this.transChangedObjects.get(objectId);
		} else {
			assert object == null;
			object = this.getObject(objectId);
		}
		
		return object;
	}
	
	private XWritableField getFieldInTransaction(XAddress fieldAddress) {
		/*
		 * extra method for this is useful concerning concurrent access,
		 * rewriting getfield would make some problems here
		 * 
		 * Note: extra method is not necessary if we do not care about
		 * concurrent access
		 */
		XWritableField field = null;
		
		if(this.inTransaction && this.transChangedFields.containsKey(fieldAddress)) {
			field = this.transChangedFields.get(fieldAddress);
		} else {
			assert field == null;
			field = this.getField(fieldAddress);
		}
		
		return field;
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
		if(!this.baseModel.isEmpty()) {
			for(XID id : this.baseModel) {
				if(!this.removedObjects.contains(id)) {
					// field wasn't removed in the TransactionObject
					return false;
				}
			}
		}
		
		return this.changedObjects.isEmpty() && this.changedFields.isEmpty();
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
		XWritableObject object = this.getObject(objectId);
		
		if(object == null) {
			// field doesn't exist
			return false;
		}
		
		long revNr = object.getRevisionNumber();
		XAddress temp = this.getAddress();
		XAddress address = XX.toAddress(temp.getRepository(), temp.getModel(), objectId, null);
		
		XModelCommand command = X.getCommandFactory().createSafeRemoveObjectCommand(address, revNr);
		this.executeCommand(command);
		
		return this.getObject(objectId) == null;
	}
	
	@Override
	public XType getType() {
		return XType.XMODEL;
	}
	
	@Override
	protected AbstractEntity getFather() {
		return this.baseModel.getFather();
	}
	
	@Override
	public Iterator<XID> iterator() {
		/*
		 * IMPROVE this is probably not the most effective implementation of
		 * this method
		 */

		// get all ids of objects in the baseModel that weren't removed
		LinkedList<XID> currentIds = new LinkedList<XID>();
		
		for(XID id : this.baseModel) {
			if(!this.removedObjects.contains(id)) {
				currentIds.add(id);
			}
		}
		
		// get all ids of newly added fields
		for(XID id : this.changedObjects.keySet()) {
			currentIds.add(id);
		}
		
		return currentIds.iterator();
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
	
	private boolean hasField(XAddress address) {
		assert address.getAddressedType() == XType.XFIELD;
		
		if(this.changedFields.containsKey(address)) {
			return true;
		} else {
			/*
			 * only look into the base model if the field wasn't removed
			 */
			if(!this.removedFields.contains(address)) {
				XObject object = this.baseModel.getObject(address.getObject());
				
				if(object != null) {
					return object.hasField(address.getField());
				}
			}
		}
		
		return false;
	}
	
	protected XValue getValue(XAddress address) {
		assert address.getAddressedType() == XType.XFIELD;
		
		if(this.removedFields.contains(address)) {
			return null;
		} else if(this.changedValues.containsKey(address)) {
			return this.changedValues.get(address);
		} else {
			XObject object = this.baseModel.getObject(address.getObject());
			XField field = object.getField(address.getField());
			
			if(field != null) {
				return field.getValue();
			}

			else {
				// field doesn't exist
				return null;
			}
		}
	}
	
	protected boolean objectIsEmpty(XID objectId) {
		assert this.hasObject(objectId);
		XWritableObject object = this.baseModel.getObject(objectId);
		
		// check whether really existing fields were "removed"
		if(object != null) {
			if(!object.isEmpty()) {
				XAddress temp = object.getAddress();
				
				for(XID id : object) {
					
					XAddress address = XX
					        .toAddress(temp.getObject(), temp.getModel(), objectId, id);
					if(!this.removedFields.contains(address)) {
						// field wasn't removed in the TransactionObject
						return false;
					}
				}
			}
		}
		
		// check if no new fields were added
		for(XAddress address : this.changedFields.keySet()) {
			if(address.getObject().equals(objectId)) {
				return false;
			}
		}
		
		return true;
	}
	
	protected Iterator<XID> objectIterator(XID objectId) {
		assert this.hasObject(objectId);
		
		XWritableObject object = this.baseModel.getObject(objectId);
		HashSet<XID> set = new HashSet<XID>();
		
		// add ids of the fields of the base object which were not removed
		if(object != null) {
			XAddress temp = object.getAddress();
			
			for(XID id : object) {
				
				XAddress address = XX.toAddress(temp.getObject(), temp.getModel(), objectId, id);
				if(!this.removedFields.contains(address)) {
					// field wasn't removed in the TransactionObject
					set.add(id);
				}
			}
		}
		
		// add ids of newly added fields
		for(XAddress address : this.changedFields.keySet()) {
			if(address.getObject().equals(objectId)) {
				set.add(address.getField());
			}
		}
		
		return set.iterator();
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
