package org.xydra.core.change;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.xydra.annotations.NeverNull;
import org.xydra.base.X;
import org.xydra.base.XAddress;
import org.xydra.base.XId;
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
import org.xydra.sharedutils.XyAssert;


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
 * 
 * Contra argument: Might not be a good idea, since the order of the commands in
 * a transaction usually matters. If multiple threads manipulate the same
 * TransactionModel it the same time, no order can be guaranteed, because the
 * Java scheduler does not execute the threads in a specific order.
 */
public class TransactionModel extends AbstractEntity implements XWritableModel {
	
	public static final long serialVersionUID = -5636313889791653240L;
	
	private MemoryModel baseModel;
	private long revisionNumber;
	
	private Map<XId,InModelTransactionObject> changedObjects, transChangedObjects;
	private Map<XAddress,InModelTransactionField> changedFields, transChangedFields;
	private Map<XAddress,XValue> changedValues, transChangedValues;
	
	private Set<XId> removedObjects, transRemovedObjects;
	private Set<XAddress> removedFields, transRemovedFields;
	
	private LinkedList<XAtomicCommand> commands;
	
	/*
	 * true, if events of a transaction are currently being added
	 */
	private boolean inTransaction;
	
	/*
	 * counts the number of the transaction which is currently being
	 * constructed. It's incremented each time a new transaction starts or a
	 * transaction is canceled. It's used to signal the
	 * InModelTransactionObjects and InModelTransactionFields which were
	 * returned during the construction of the transaction which was
	 * canceled/committed that a new transaction is being constructed and they
	 * are no longer valid.
	 */
	private long transactionNumber;
	
	public TransactionModel(MemoryModel model) {
		this.baseModel = model;
		this.revisionNumber = model.getRevisionNumber();
		
		this.changedObjects = new HashMap<XId,InModelTransactionObject>();
		this.changedFields = new HashMap<XAddress,InModelTransactionField>();
		this.changedValues = new HashMap<XAddress,XValue>();
		this.removedObjects = new HashSet<XId>();
		this.removedFields = new HashSet<XAddress>();
		
		this.transChangedObjects = new HashMap<XId,InModelTransactionObject>();
		this.transChangedFields = new HashMap<XAddress,InModelTransactionField>();
		this.transChangedValues = new HashMap<XAddress,XValue>();
		this.transRemovedObjects = new HashSet<XId>();
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
		
		this.transactionNumber++;
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
	
	/**
	 * Returns the number of the transaction which is currently being built.
	 * 
	 * @return the number of the transaction which is currently being built.
	 */
	protected long getTransactionNumber() {
		return this.transactionNumber;
	}
	
	// XWritableModel-specific methods
	
	@Override
	public XAddress getAddress() {
		return this.baseModel.getAddress();
	}
	
	@Override
	public XId getId() {
		return this.baseModel.getId();
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
		if(!command.getTarget().getModel().equals(this.getId())
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
	
	/**
	 * Checks whether the specified object exists at the moment.
	 * 
	 * Might return true, even though the specified object is not yet added
	 * (used in transaction handling of {@link #executeCommand(XCommand)}). This
	 * only happens if this TransactionModel is currently executing a
	 * transaction which may add the specified object (i.e. contains a command
	 * for adding this field, which was already tentatively executed), so that
	 * the object appears as existing for the rest of the commands which still
	 * need to be evaluated.
	 * 
	 * @param objectId The {@link XId} of the object
	 * @return true, if the specified object exists
	 */
	private boolean objectExists(XId objectId) {
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
	
	/**
	 * Checks whether the specified field exists at the moment.
	 * 
	 * Might return true, even though the specified field is not yet added (used
	 * in transaction handling of {@link #executeCommand(XCommand)}). This only
	 * happens if this TransactionModel is currently executing a transaction
	 * which may add the specified field (i.e. contains a command for adding
	 * this field, which was already tentatively executed), so that the field
	 * appears as existing for the rest of the commands which still need to be
	 * evaluated.
	 * 
	 * @param fieldId The {@link XId} of the field
	 * @return true, if the specified field exists
	 */
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
	
	/**
	 * Executes the given {@link XModelCommand}.
	 * 
	 * @param modelCommand the command which is to be executed
	 * @param callback the used callback.
	 * @return the revision number of the TransactionModel after the execution
	 *         or {@link XCommand#FAILED} if the execution failed
	 */
	private long handleModelCommand(XModelCommand modelCommand, XLocalChangeCallback callback) {
		XyAssert.xyAssert(callback != null); assert callback != null;
		XId objectId = modelCommand.getChangedEntity().getObject();
		
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
			
			this.addObjectToTransactionModel(object, this.inTransaction);
			
			if(!this.inTransaction) {
				// command succeeded -> add it to the list
				this.commands.add(modelCommand);
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
			XyAssert.xyAssert(object != null); assert object != null;
			XyAssert.xyAssert(object instanceof InModelTransactionObject);
			
			// check revision number
			if(!modelCommand.isForced()
			        && modelCommand.getRevisionNumber() != object.getRevisionNumber()) {
				callback.onFailure();
				return XCommand.FAILED;
			}
			
			this.removeObjectFromTransactionModel(object, this.inTransaction);
			
			if(!this.inTransaction) {
				// command succeeded -> add it to the list
				this.commands.add(modelCommand);
			}
			
			callback.onSuccess(this.getRevisionNumber());
			return this.getRevisionNumber();
		}
		
		throw new IllegalArgumentException(
		        "Given Command was not a correct instance of XModelCommand!");
	}
	
	/**
	 * Executes the given {@link XObjectCommand}.
	 * 
	 * @param objectCommand the command which is to be executed
	 * @param callback the used callback.
	 * @return the revision number of the TransactionModel after the execution
	 *         or {@link XCommand#FAILED} if the execution failed
	 */
	private long handleObjectCommand(XObjectCommand objectCommand, XLocalChangeCallback callback) {
		XyAssert.xyAssert(callback != null); assert callback != null;
		
		XId objectId = objectCommand.getChangedEntity().getObject();
		boolean objectExists = this.objectExists(objectId);
		
		if(!objectExists) {
			callback.onFailure();
			return XCommand.FAILED;
		}
		
		XWritableObject object = (!this.inTransaction) ? this.getObject(objectId) : this
		        .getObjectInTransaction(objectId);
		// remember: this is actually an InModelTransactionObject
		XyAssert.xyAssert(object != null); assert object != null;
		XyAssert.xyAssert(object instanceof InModelTransactionObject);
		
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
			
			this.addFieldToTransactionModel(field, this.inTransaction);
			
			if(!this.inTransaction) {
				// command succeeded -> add it to the list
				this.commands.add(objectCommand);
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
			XyAssert.xyAssert(field != null); assert field != null;
			XyAssert.xyAssert(field instanceof InModelTransactionField);
			
			// check revision number
			if(!objectCommand.isForced()
			        && objectCommand.getRevisionNumber() != field.getRevisionNumber()) {
				callback.onFailure();
				return XCommand.FAILED;
			}
			
			this.removeFieldFromTransactionModel(fieldAddress, this.inTransaction);
			
			if(!this.inTransaction) {
				// command succeeded -> add it to the list
				this.commands.add(objectCommand);
			}
			
			callback.onSuccess(this.getRevisionNumber());
			return this.getRevisionNumber();
		}
		
		throw new IllegalArgumentException(
		        "Given Command was not a correct instance of XObjectCommand!");
	}
	
	/**
	 * Executes the given {@link XFieldCommand}.
	 * 
	 * @param fieldCommand the command which is to be executed
	 * @param callback the used callback.
	 * @return the revision number of the TransactionModel after the execution
	 *         or {@link XCommand#FAILED} if the execution failed
	 */
	private long handleFieldCommand(XFieldCommand fieldCommand, XLocalChangeCallback callback) {
		XyAssert.xyAssert(callback != null); assert callback != null;
		
		XId objectId = fieldCommand.getChangedEntity().getObject();
		
		if(!this.objectExists(objectId)) {
			callback.onFailure();
			return XCommand.FAILED;
		}
		
		XWritableObject object = (!this.inTransaction) ? this.getObject(objectId) : this
		        .getObjectInTransaction(objectId);
		// remember: this is actually an InModelTransactionObject
		XyAssert.xyAssert(object != null); assert object != null;
		XyAssert.xyAssert(object instanceof InModelTransactionObject);
		
		// all commands concern fields -> check if it exists
		XId fieldId = fieldCommand.getChangedEntity().getField();
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
		XyAssert.xyAssert(field != null); assert field != null;
		XyAssert.xyAssert(field instanceof InModelTransactionField);
		
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
		
		this.manipulateValueInTransactionModel(fieldAddress, fieldCommand.getValue(),
		        this.inTransaction);
		
		if(!this.inTransaction) {
			// command succeeded -> add it to the list
			this.commands.add(fieldCommand);
		}
		
		callback.onSuccess(field.getRevisionNumber());
		return field.getRevisionNumber();
		
	}
	
	/**
	 * Executes the given {@link XTransaction}.
	 * 
	 * @param transaction the transaction which is to be executed
	 * @param callback the used callback.
	 * @return the revision number of the TransactionModel after the execution
	 *         or {@link XCommand#FAILED} if the execution failed
	 */
	private long handleTransaction(XTransaction transaction, XLocalChangeCallback callback) {
		this.inTransaction = true;
		
		XyAssert.xyAssert(callback != null); assert callback != null;
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
				
				XId objectId = command.getChangedEntity().getObject();
				XAddress objectAddress = command.getChangedEntity();
				XyAssert.xyAssert(objectAddress.getAddressedType() == XType.XOBJECT);
				
				if(mdlCmd.getChangeType() == ChangeType.ADD) {
					if(!this.hasObject(objectId)) {
						/*
						 * cases in which "hasObject" would evaluate to "true"
						 * here are cases were the command was forced and the
						 * field already existed, so we do not have to do
						 * anything here
						 */
						InModelTransactionObject object = new InModelTransactionObject(
						        objectAddress, XCommand.NEW, this);
						
						this.addObjectToTransactionModel(object, false);
					}
				} else {
					XyAssert.xyAssert(mdlCmd.getChangeType() == ChangeType.REMOVE);
					
					/*
					 * forced commands to not have to be treated specially, if
					 * the command was forced and the target didn't exist before
					 * the execution of the command, the following lines will
					 * just do nothing
					 */
					XWritableObject object = this.getObject(objectId);
					this.removeObjectFromTransactionModel(object, false);
				}
			} else if(command instanceof XObjectCommand) {
				XObjectCommand objCmd = (XObjectCommand)command;
				
				XAddress fieldAddress = command.getChangedEntity();
				XyAssert.xyAssert(fieldAddress.getAddressedType() == XType.XFIELD);
				
				if(objCmd.getChangeType() == ChangeType.ADD) {
					if(!this.hasField(fieldAddress)) {
						/*
						 * cases in which "hasField" would evaluate to "true"
						 * here are cases were the command was forced and the
						 * field already existed, so we do not have to do
						 * anything here
						 */
						InModelTransactionField field = new InModelTransactionField(fieldAddress,
						        XCommand.NEW, this);
						
						this.addFieldToTransactionModel(field, false);
					}
				} else {
					XyAssert.xyAssert(objCmd.getChangeType() == ChangeType.REMOVE);
					
					/*
					 * forced commands to not have to be treated specially, if
					 * the command was forced and the target didn't exist before
					 * the execution of the command, the following lines will
					 * just do nothing
					 */

					this.removeFieldFromTransactionModel(fieldAddress, false);
				}
			} else {
				XyAssert.xyAssert(command instanceof XFieldCommand);
				XAddress fieldAddress = command.getChangedEntity();
				XyAssert.xyAssert(fieldAddress.getAddressedType() == XType.XFIELD);
				
				/*
				 * the action that has to be executed for field commands is
				 * always the same, no matter what the command type was
				 */
				this.manipulateValueInTransactionModel(fieldAddress,
				        ((XFieldCommand)command).getValue(), false);
			}
			
			// add command to the command list
			this.commands.add((XAtomicCommand)command);
		}
		
		this.inTransaction = false;
		
		// Transaction "succeeded"
		callback.onSuccess(this.getRevisionNumber());
		return this.getRevisionNumber();
	}
	
	/**
	 * Adds a new object to this TransactionModel.
	 * 
	 * @param object The {@link InModelTransactionObject} which is to be added
	 * @param inTransaction true, if a transaction is being executed at the
	 *            moment. The object will not yet be added, but appear as added
	 *            for the rest of the commands of the transaction, because it is
	 *            not yet clear if all commands of the transaction can be
	 *            executed (used in transaction handling of
	 *            {@link #executeCommand(XCommand)})
	 */
	private void addObjectToTransactionModel(InModelTransactionObject object, boolean inTransaction) {
		XId objectId = object.getId();
		
		if(!inTransaction) {
			// remove from list of removed object, if needed
			this.removedObjects.remove(objectId);
			
			this.changedObjects.put(objectId, object);
		} else {
			/*
			 * Do not execute the changes just yet. They will be executed after
			 * it is certain that all commands of the transaction can be safely
			 * executed.
			 */
			this.transRemovedObjects.remove(objectId);
			this.transChangedObjects.put(objectId, object);
		}
	}
	
	/**
	 * Removes the specified object from this TransactionModel.
	 * 
	 * @param object The {@link XWritableObject} which is to be removed
	 * @param inTransaction true, if a transaction is being executed at the
	 *            moment. The object will not yet be removed, but appear as
	 *            removed for the rest of the commands of the transaction,
	 *            because it is not yet clear if all commands of the transaction
	 *            can be executed (used in transaction handling of
	 *            {@link #executeCommand(XCommand)})
	 */
	private void removeObjectFromTransactionModel(XWritableObject object, boolean inTransaction) {
		XId objectId = object.getId();
		
		// remove all fields of the object
		for(XId fieldId : object) {
			XAddress temp = object.getAddress();
			XAddress fieldAddress = XX.toAddress(temp.getRepository(), temp.getModel(),
			        temp.getObject(), fieldId);
			
			this.removeFieldFromTransactionModel(fieldAddress, inTransaction);
		}
		
		if(!inTransaction) {
			// mark it as removed
			this.removedObjects.add(objectId);
			
			// remove info from all other maps
			InModelTransactionObject removedObject = this.changedObjects.remove(objectId);
			if(removedObject != null) {
				removedObject.setRemoved();
			}
		} else {
			/*
			 * Do not execute the changes just yet. They will be executed after
			 * it is certain that all commands of the transaction can be safely
			 * executed.
			 */
			this.transRemovedObjects.add(objectId);
			this.transChangedObjects.remove(objectId);
		}
		
	}
	
	/**
	 * Adds a new field to this TransactionModel.
	 * 
	 * @param field The {@link InModelTransactionField} which is to be added
	 * @param inTransaction true, if a transaction is being executed at the
	 *            moment. The field will not yet be added, but appear as added
	 *            for the rest of the commands of the transaction, because it is
	 *            not yet clear if all commands of the transaction can be
	 *            executed (used in transaction handling of
	 *            {@link #executeCommand(XCommand)})
	 */
	private void addFieldToTransactionModel(InModelTransactionField field, boolean inTransaction) {
		XAddress fieldAddress = field.getAddress();
		
		if(!inTransaction) {
			// remove from list of removed fields, if needed
			this.removedFields.remove(fieldAddress);
			
			this.changedFields.put(fieldAddress, field);
		} else {
			/*
			 * Do not execute the changes just yet. They will be executed after
			 * it is certain that all commands of the transaction can be safely
			 * executed.
			 */
			this.transRemovedFields.remove(fieldAddress);
			this.transChangedFields.put(fieldAddress, field);
		}
	}
	
	/**
	 * Removes the specified field from this TransactionModel.
	 * 
	 * @param fieldAddress The {@link XAddress} of the field which is to be
	 *            removed
	 * @param inTransaction true, if a transaction is being executed at the
	 *            moment. The field will not yet be removed, but appear as
	 *            removed for the rest of the commands of the transaction,
	 *            because it is not yet clear if all commands of the transaction
	 *            can be executed (used in transaction handling of
	 *            {@link #executeCommand(XCommand)})
	 */
	private void removeFieldFromTransactionModel(XAddress fieldAddress, boolean inTransaction) {
		if(!inTransaction) {
			// mark it as removed
			this.removedFields.add(fieldAddress);
			
			// remove info from all other maps
			InModelTransactionField removedField = this.changedFields.remove(fieldAddress);
			
			if(removedField != null) {
				removedField.setRemoved();
			}
			
			this.changedValues.remove(fieldAddress);
		} else {
			/*
			 * Do not execute the changes just yet. They will be executed after
			 * it is certain that all commands of the transaction can be safely
			 * executed.
			 */
			this.transRemovedFields.add(fieldAddress);
			this.transChangedFields.remove(fieldAddress);
			this.transChangedValues.remove(fieldAddress);
		}
		
	}
	
	/**
	 * Sets the {@link XValue} of the specified field to the given value.
	 * 
	 * @param fieldAddress The {@link XAddress} of the field which value is to
	 *            be manipulated
	 * @param value The new {@link XValue}
	 * @param inTransaction true, if a transaction is being executed at the
	 *            moment. The value will not yet be changed, but appear as
	 *            changed for the rest of the commands of the transaction,
	 *            because it is not yet clear if all commands of the transaction
	 *            can be executed (used in transaction handling of
	 *            {@link #executeCommand(XCommand)})
	 */
	private void manipulateValueInTransactionModel(XAddress fieldAddress, XValue value,
	        boolean inTransaction) {
		if(!inTransaction) {
			// "add"/"remove"/"change" the value
			this.changedValues.put(fieldAddress, value);
		} else {
			/*
			 * Do not execute the changes just yet. They will be executed after
			 * it is certain that all commands of the transaction can be safely
			 * executed.
			 */
			this.transChangedValues.put(fieldAddress, value);
		}
	}
	
	/**
	 * Gets the specified object.
	 * 
	 * Might return true, even though the specified object was not yet added
	 * (used in transaction handling of {@link #executeCommand(XCommand)}). This
	 * only happens if this TransactionModel is currently executing a
	 * transaction which may add the specified object (i.e. contains a command
	 * for adding this object, which was already tentatively executed), so that
	 * the object appears as existing for the rest of the commands which still
	 * need to be evaluated.
	 * 
	 * @param objectId The {@link XId} of the object
	 * @return The specified object or null if it doesn't exist.
	 */
	private XWritableObject getObjectInTransaction(XId objectId) {
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
			XyAssert.xyAssert(object == null);
			object = this.getObject(objectId);
		}
		
		return object;
	}
	
	/**
	 * Gets the specified field.
	 * 
	 * Might return true, even though the specified field was not yet added
	 * (used in transaction handling of {@link #executeCommand(XCommand)}). This
	 * only happens if this TransactionModel is currently executing a
	 * transaction which may add the specified field (i.e. contains a command
	 * for adding this object, which was already tentatively executed), so that
	 * the field appears as existing for the rest of the commands which still
	 * need to be evaluated.
	 * 
	 * @param fieldAddress The {@link XAddress} of the field
	 * @return The specified field or null if it doesn't exist.
	 */
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
			XyAssert.xyAssert(field == null);
			field = this.getField(fieldAddress);
		}
		
		return field;
	}
	
	@Override
	public long getRevisionNumber() {
		return this.revisionNumber;
	}
	
	@Override
	public boolean hasObject(@NeverNull XId objectId) {
		/*
		 * only return true if the object is new or if it wasn't removed and it
		 * part of the baseModel
		 */

		return this.changedObjects.containsKey(objectId)
		        || (this.baseModel.hasObject(objectId) && !this.removedObjects.contains(objectId));
	}
	
	@Override
	public XWritableObject createObject(@NeverNull XId id) {
		XCommand addCommand = X.getCommandFactory().createSafeAddObjectCommand(
		        this.baseModel.getAddress(), id);
		
		this.executeCommand(addCommand);
		
		return this.getObject(id);
	}
	
	@Override
	public boolean isEmpty() {
		if(!this.baseModel.isEmpty()) {
			for(XId id : this.baseModel) {
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
	
	@Override
	public XWritableObject getObject(@NeverNull XId objectId) {
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
	
	@Override
	public boolean removeObject(@NeverNull XId objectId) {
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
	public Iterator<XId> iterator() {
		/*
		 * IMPROVE this is probably not the most effective implementation of
		 * this method
		 */

		// get all ids of objects in the baseModel that weren't removed
		LinkedList<XId> currentIds = new LinkedList<XId>();
		
		for(XId id : this.baseModel) {
			if(!this.removedObjects.contains(id)) {
				currentIds.add(id);
			}
		}
		
		// get all ids of newly added fields
		for(XId id : this.changedObjects.keySet()) {
			currentIds.add(id);
		}
		
		return currentIds.iterator();
	}
	
	/*
	 * Special methods needed for the helperclasses InTransactionObject and
	 * InTransactionField
	 */

	/**
	 * Gets the specified field. Used by {@link InModelTransactionObject}.
	 * 
	 * @param address the {@link XAddress} of the field
	 * @return the specified field or null if it doesn't exist
	 */
	protected XWritableField getField(XAddress address) {
		XyAssert.xyAssert(address.getAddressedType() == XType.XFIELD);
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
	
	/**
	 * Checks whether the specified field exists in this TransactionModel. Used
	 * by {@link InModelTransactionObject}.
	 * 
	 * @param address the {@link XAddress} of the field
	 * @return true, if the field exists, false otherwise
	 */
	private boolean hasField(XAddress address) {
		XyAssert.xyAssert(address.getAddressedType() == XType.XFIELD);
		
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
	
	/**
	 * Gets the value of the specified field. Used by
	 * {@link InModelTransactionField}.
	 * 
	 * @param address the {@link XAddress} of the field
	 * @return the value of the field (null if the specified field doesn't
	 *         exist)
	 */
	protected XValue getValue(XAddress address) {
		XyAssert.xyAssert(address.getAddressedType() == XType.XFIELD);
		
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
	
	/**
	 * Checks whether the specified object is empty. Used by
	 * {@link InModelTransactionObject}.
	 * 
	 * @param objectId the {@link XId} of the object
	 * @return true, if it is empty, false otherwise
	 */
	protected boolean objectIsEmpty(XId objectId) {
		XyAssert.xyAssert(this.hasObject(objectId));
		XWritableObject object = this.baseModel.getObject(objectId);
		
		// check whether really existing fields were "removed"
		if(object != null) {
			if(!object.isEmpty()) {
				XAddress temp = object.getAddress();
				
				for(XId id : object) {
					
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
	
	/**
	 * Returns an iterator over the {@link XId XIds} of the fields in the
	 * specified object. Used by {@link InModelTransactionObject}.
	 * 
	 * @param object the {@link XId} of the object
	 * @return an iterator over the {@link XId XIds} of the fields in the
	 *         specified object
	 */
	protected Iterator<XId> objectIterator(XId objectId) {
		XyAssert.xyAssert(this.hasObject(objectId));
		
		XWritableObject object = this.baseModel.getObject(objectId);
		HashSet<XId> set = new HashSet<XId>();
		
		// add ids of the fields of the base object which were not removed
		if(object != null) {
			XAddress temp = object.getAddress();
			
			for(XId id : object) {
				
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
