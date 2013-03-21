package org.xydra.core.change;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.xydra.base.X;
import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.XType;
import org.xydra.base.XX;
import org.xydra.base.change.ChangeType;
import org.xydra.base.change.XAtomicCommand;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XFieldCommand;
import org.xydra.base.change.XObjectCommand;
import org.xydra.base.change.XTransaction;
import org.xydra.base.rmof.XWritableField;
import org.xydra.base.rmof.XWritableObject;
import org.xydra.base.value.XValue;
import org.xydra.core.model.XField;
import org.xydra.core.model.XLocalChangeCallback;
import org.xydra.core.model.impl.memory.AbstractEntity;
import org.xydra.core.model.impl.memory.MemoryObject;
import org.xydra.sharedutils.XyAssert;


/**
 * A helper class to create {@link XTransaction XTransactions} for a specific
 * {@link MemoryObject}. This class wraps the given {@link MemoryObject} and
 * then acts as a simulator. It provides all methods for changing the state of
 * an {@link MemoryObject}, but the changes that are executed on this
 * TransactionObject are not actually executed on the {@link MemoryObject}, but
 * just simulated and saved. After all changes were applied, they can be
 * executed in a single {@link XTransaction} on the given {@link MemoryObject}
 * by calling the {@link #commit()} method.
 * 
 * In short, this class makes creating and executing an {@link XTransaction} as
 * simple as executing simple methods on an {@link MemoryObject}
 * 
 * @author Kaidel
 * 
 */

/*
 * TODO consider making this implementation thread-safe
 * 
 * Contra argument: Might not be a good idea, since the order of the commands in
 * a transaction usually matters. If multiple threads manipulate the same
 * TransactionObject it the same time, no order can be guaranteed, because the
 * Java scheduler does not execute the threads in a specific order.
 */
public class TransactionObject extends AbstractEntity implements XWritableObject {
	
	private MemoryObject baseObject;
	private long revisionNumber;
	
	private HashMap<XId,InObjectTransactionField> changedFields, transChangedFields;
	private Map<XId,XValue> changedValues, transChangedValues;
	
	private Set<XId> removedFields, transRemovedFields;
	
	private ArrayList<XAtomicCommand> commands;
	
	/*
	 * true, if events of a transaction are currently being added
	 */
	private boolean inTransaction = false;
	
	/*
	 * counts the number of the transaction which is currently being
	 * constructed. It's incremented each time a new transaction starts or a
	 * transaction is canceled. It's used to signal the
	 * InObjectTransactionFields which were returned during the construction of
	 * the transaction which was canceled/committed that a new transaction is
	 * being constructed and they are no longer valid.
	 */
	private long transactionNumber;
	
	public TransactionObject(MemoryObject object) {
		this.baseObject = object;
		this.revisionNumber = object.getRevisionNumber();
		
		this.changedFields = new HashMap<XId,InObjectTransactionField>();
		this.changedValues = new HashMap<XId,XValue>();
		this.removedFields = new HashSet<XId>();
		
		this.transChangedFields = new HashMap<XId,InObjectTransactionField>();
		this.transChangedValues = new HashMap<XId,XValue>();
		this.transRemovedFields = new HashSet<XId>();
		
		this.commands = new ArrayList<XAtomicCommand>();
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
	 * {@link MemoryObject}. If it succeeds, this TransactionObject will be
	 * reset and can then be used to create a new XTransaction, if the
	 * transaction fails, nothing happens.
	 * 
	 * @return the revision number of the wrapped {@link MemoryObject} after the
	 *         execution of the {@link XTransaction} or XCommand.FAILED if the
	 *         execution fails
	 */
	public long commit() {
		XTransactionBuilder builder = new XTransactionBuilder(this.getAddress());
		
		for(int i = 0; i < this.commands.size(); i++) {
			builder.addCommand(this.commands.get(i));
		}
		
		XTransaction transaction = builder.build();
		
		long revNr = this.baseObject.executeCommand(transaction);
		
		if(revNr != XCommand.FAILED) {
			this.clear(); // restarts the TransactionObject
		}
		
		this.revisionNumber = revNr;
		return revNr;
	}
	
	/**
	 * Resets the TransactionObject, i.e. gets rid of all changes made to the
	 * TransactionObject, but not to the wrapped {@link MemoryObject}. After the
	 * execution, the TransactionObject will then represent the state the
	 * wrapped {@link MemoryObject} currently is in.
	 */
	public void clear() {
		this.changedFields.clear();
		this.changedValues.clear();
		this.removedFields.clear();
		
		this.transChangedFields.clear();
		this.transChangedValues.clear();
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
		return !(this.changedFields.isEmpty() && this.removedFields.isEmpty()
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
	
	// XWritableObject-specific methods
	
	@Override
	public XAddress getAddress() {
		return this.baseObject.getAddress();
	}
	
	@Override
	public XId getId() {
		return this.baseObject.getId();
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
		
		// check whether the given command actually refers to the this object
		if(!command.getTarget().getObject().equals(this.getId())
		        || !command.getTarget().getModel().equals(this.getAddress().getModel())) {
			usedCallback.onFailure();
			
			return XCommand.FAILED;
		}
		
		// Object Commands
		if(command instanceof XObjectCommand) {
			return this.handleObjectCommand((XObjectCommand)command, usedCallback);
		}
		
		// XFieldCommands
		if(command instanceof XFieldCommand) {
			return this.handleFieldCommand((XFieldCommand)command, usedCallback);
		}
		
		throw new IllegalArgumentException(
		        "Given Command was neither a correct instance of XObjectCommand, XFieldCommand or XTransaction!");
	}
	
	/**
	 * Checks whether the specified field exists at the moment.
	 * 
	 * Might return true, even though the specified field is not yet added (used
	 * in transaction handling of {@link #executeCommand(XCommand)}). This only
	 * happens if this TransactionObject is currently executing a transaction
	 * which may add the specified field (i.e. contains a command for adding
	 * this field, which was already tentatively executed), so that the field
	 * appears as existing for the rest of the commands which still need to be
	 * evaluated.
	 * 
	 * @param fieldId The {@link XId} of the field
	 * @return true, if the specified field exists
	 */
	private boolean fieldExists(XId fieldId) {
		boolean fieldExists = this.hasField(fieldId);
		
		if(this.inTransaction) {
			// set it to true, if the field was added during the
			// transaction
			fieldExists |= this.transChangedFields.containsKey(fieldId);
			
			// set it to false, if the field was removed
			fieldExists &= !this.transRemovedFields.contains(fieldId);
		}
		
		return fieldExists;
	}
	
	/**
	 * Executes the given {@link XObjectCommand}.
	 * 
	 * @param objectCommand the command which is to be executed
	 * @param callback the used callback.
	 * @return the revision number of the TransactionObject after the execution
	 *         or {@link XCommand#FAILED} if the execution failed
	 */
	private long handleObjectCommand(XObjectCommand objectCommand, XLocalChangeCallback callback) {
		XyAssert.xyAssert(callback != null); assert callback != null;
		
		XId fieldId = objectCommand.getChangedEntity().getField();
		
		boolean fieldExists = this.fieldExists(fieldId);
		
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
			
			XAddress temp = this.getAddress();
			XAddress address = XX.toAddress(temp.getRepository(), temp.getModel(),
			        temp.getObject(), fieldId);
			
			InObjectTransactionField field = new InObjectTransactionField(address, XCommand.NEW,
			        this);
			
			this.addFieldToTransactionObject(field, this.inTransaction);
			
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
					if(!this.inTransaction) {
						callback.onFailure();
					}
					return XCommand.FAILED;
				}
				
				// forced command -> already finished with the execution
				callback.onSuccess(this.getRevisionNumber());
				return this.getRevisionNumber();
			}
			
			XWritableField field = null;
			
			if(this.inTransaction && this.transChangedFields.containsKey(fieldId)) {
				field = this.transChangedFields.get(fieldId);
			} else {
				XyAssert.xyAssert(field == null);
				field = this.getField(fieldId);
			}
			
			// remember: this actually is an InModelTransactionField
			XyAssert.xyAssert(field != null); assert field != null; // because "fieldExists" was true
			assert field instanceof InObjectTransactionField : "field is instanceof "
			        + field.getClass().getName();
			
			// check revision number
			if(!objectCommand.isForced()
			        && objectCommand.getRevisionNumber() != field.getRevisionNumber()) {
				callback.onFailure();
				return XCommand.FAILED;
			}
			
			this.removeFieldFromTransactionObject(fieldId, this.inTransaction);
			
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
	 * @return the revision number of the TransactionObject after the execution
	 *         or {@link XCommand#FAILED} if the execution failed
	 */
	private long handleFieldCommand(XFieldCommand fieldCommand, XLocalChangeCallback callback) {
		XyAssert.xyAssert(callback != null); assert callback != null;
		XId fieldId = fieldCommand.getChangedEntity().getField();
		
		if(!this.fieldExists(fieldId)) {
			callback.onFailure();
			return XCommand.FAILED;
		}
		
		XWritableField field = this.getField(fieldId);
		
		if(this.inTransaction && this.transChangedFields.containsKey(fieldId)) {
			field = this.transChangedFields.get(fieldId);
		}
		
		// remember: this actually is an InObjectTransactionField
		XyAssert.xyAssert(field != null); assert field != null;
		XyAssert.xyAssert(field instanceof InObjectTransactionField);
		
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
		if(this.inTransaction && this.transChangedValues.containsKey(fieldId)) {
			/*
			 * field was changed in the XTransaction which is currently being
			 * added to this TransactionObject
			 */
			value = this.transChangedValues.get(fieldId);
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
		
		this.manipulateValueInTransactionObject(fieldId, fieldCommand.getValue(),
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
	 * @return the revision number of the TransactionObject after the execution
	 *         or {@link XCommand#FAILED} if the execution failed
	 */
	private long handleTransaction(XTransaction transaction, XLocalChangeCallback callback) {
		this.inTransaction = true;
		
		XyAssert.xyAssert(callback != null); assert callback != null;
		/*
		 * assert: transaction holds no further XTransactions (can not happen,
		 * since instances of XTransactions can never hold other XTransactions)
		 */
		this.transChangedFields.clear();
		this.transChangedValues.clear();
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
			if(command instanceof XObjectCommand) {
				XObjectCommand objCmd = (XObjectCommand)command;
				
				XId fieldId = command.getChangedEntity().getField();
				
				if(objCmd.getChangeType() == ChangeType.ADD) {
					if(!this.hasField(fieldId)) {
						/*
						 * cases in which "hasField" would evaluate to "true"
						 * here are cases were the command was forced and the
						 * field already existed, so we do not have to do
						 * anything here
						 */

						XAddress temp = this.getAddress();
						XAddress address = XX.toAddress(temp.getRepository(), temp.getModel(),
						        temp.getObject(), fieldId);
						
						InObjectTransactionField field = new InObjectTransactionField(address,
						        XCommand.NEW, this);
						
						this.addFieldToTransactionObject(field, false);
					}
				} else {
					XyAssert.xyAssert(objCmd.getChangeType() == ChangeType.REMOVE);
					
					/*
					 * forced commands to not have to be treated specially, if
					 * the command was forced and the target didn't exist before
					 * the execution of the command, the following lines will
					 * just do nothing
					 */

					this.removeFieldFromTransactionObject(fieldId, false);
				}
			} else {
				XyAssert.xyAssert(command instanceof XFieldCommand);
				XId fieldId = command.getChangedEntity().getField();
				
				/*
				 * the action that has to be executed for field commands is
				 * always the same, no matter what the command type was
				 */

				this.manipulateValueInTransactionObject(fieldId,
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
	 * Adds a new field to this TransactionObject.
	 * 
	 * @param field The {@link InObjectTransactionField} which is to be added
	 * @param inTransaction true, if a transaction is being executed at the
	 *            moment. The field will not yet be added, but appear as added
	 *            for the rest of the commands of the transaction, because it is
	 *            not yet clear if all commands of the transaction can be
	 *            executed (used in transaction handling of
	 *            {@link #executeCommand(XCommand)})
	 */
	private void addFieldToTransactionObject(InObjectTransactionField field, boolean inTransaction) {
		XId fieldId = field.getId();
		
		if(!inTransaction) {
			// remove from list of removed fields, if needed
			this.removedFields.remove(fieldId);
			
			this.changedFields.put(fieldId, field);
			
		} else {
			/*
			 * Do not execute the changes just yet. They will be executed after
			 * it is certain that all commands of the transaction can be safely
			 * executed.
			 */
			this.transRemovedFields.remove(fieldId);
			this.transChangedFields.put(fieldId, field);
		}
	}
	
	/**
	 * Removes the specified field from this TransactionObject.
	 * 
	 * @param fieldId The {@link XId} of the field which is to be removed
	 * @param inTransaction true, if a transaction is being executed at the
	 *            moment. The field will not yet be removed, but appear as
	 *            removed for the rest of the commands of the transaction,
	 *            because it is not yet clear if all commands of the transaction
	 *            can be executed (used in transaction handling of
	 *            {@link #executeCommand(XCommand)})
	 */
	private void removeFieldFromTransactionObject(XId fieldId, boolean inTransaction) {
		if(!inTransaction) {
			// mark it as removed
			this.removedFields.add(fieldId);
			
			// remove info from all other maps
			InObjectTransactionField removedField = this.changedFields.remove(fieldId);
			if(removedField != null) {
				removedField.setRemoved();
			}
			
			this.changedValues.remove(fieldId);
			
		} else {
			/*
			 * Do not execute the changes just yet. They will be executed after
			 * it is certain that all commands of the transaction can be safely
			 * executed.
			 */
			this.transRemovedFields.add(fieldId);
			this.transChangedFields.remove(fieldId);
			this.transChangedValues.remove(fieldId);
		}
	}
	
	/**
	 * Sets the {@link XValue} of the specified field to the given value.
	 * 
	 * @param fieldId The {@link XId} of the field which value is to be
	 *            manipulated
	 * @param value The new {@link XValue}
	 * @param inTransaction true, if a transaction is being executed at the
	 *            moment. The value will not yet be changed, but appear as
	 *            changed for the rest of the commands of the transaction,
	 *            because it is not yet clear if all commands of the transaction
	 *            can be executed (used in transaction handling of
	 *            {@link #executeCommand(XCommand)})
	 */
	private void manipulateValueInTransactionObject(XId fieldId, XValue value, boolean inTransaction) {
		if(!inTransaction) {
			// "add"/"remove"/"change" the value
			this.changedValues.put(fieldId, value);
			
		} else {
			/*
			 * Do not execute the changes just yet. They will be executed after
			 * it is certain that all commands of the transaction can be safely
			 * executed.
			 */
			this.transChangedValues.put(fieldId, value);
		}
		
	}
	
	@Override
	public long getRevisionNumber() {
		return this.revisionNumber;
	}
	
	@Override
	public boolean isEmpty() {
		if(!this.baseObject.isEmpty()) {
			for(XId id : this.baseObject) {
				if(!this.removedFields.contains(id)) {
					// field wasn't removed in the TransactionObject
					return false;
				}
			}
		}
		
		return this.changedFields.isEmpty();
	}
	
	@Override
	public boolean hasField(XId fieldId) {
		if(this.changedFields.containsKey(fieldId)) {
			return true;
		} else {
			/*
			 * only look into the base model if the field wasn't removed
			 */
			if(!this.removedFields.contains(fieldId)) {
				return this.baseObject.hasField(fieldId);
			}
		}
		
		// field doesn't exist (or was removed)
		return false;
	}
	
	@Override
	public XWritableField createField(XId fieldId) {
		XObjectCommand command = X.getCommandFactory().createSafeAddFieldCommand(this.getAddress(),
		        fieldId);
		if(!hasField(fieldId)) {
			this.executeCommand(command);
		}
		
		return this.getField(fieldId);
	}
	
	@Override
	public boolean removeField(XId fieldId) {
		XWritableField field = this.getField(fieldId);
		
		if(field == null) {
			// field doesn't exist
			return false;
		}
		
		long revNr = field.getRevisionNumber();
		XAddress temp = this.getAddress();
		XAddress address = XX.toAddress(temp.getRepository(), temp.getModel(), temp.getObject(),
		        fieldId);
		
		XObjectCommand command = X.getCommandFactory().createSafeRemoveFieldCommand(address, revNr);
		this.executeCommand(command);
		
		return this.getField(fieldId) == null;
	}
	
	@Override
	public boolean equals(Object object) {
		if(object instanceof XWritableObject) {
			XWritableObject writObject = (XWritableObject)object;
			
			return this.getAddress().equals(writObject.getAddress())
			        && this.revisionNumber == writObject.getRevisionNumber();
		}
		
		return false;
	}
	
	/**
	 * Tests whether two given TransactionObjects have the same state. Two
	 * TransactionObjects have the same state, if the same actions were executed
	 * on them and not yet propagated to their wrapped {@link MemoryObject} (the
	 * order in which these changes were executed doesn't matter) and they have
	 * the same wrapped {@link MemoryObject}
	 * 
	 * @param object object, to which this object should be compared to.
	 * @return true, if both objects have the same state, false otherwise
	 */
	public boolean equalTransactionObjectState(TransactionObject object) {
		return this.baseObject.equals(object.baseObject)
		        && this.changedFields.equals(object.changedFields)
		        && this.changedValues.equals(object.changedValues)
		        && this.removedFields.equals(object.removedFields);
	}
	
	@Override
	public XWritableField getField(XId fieldId) {
		if(this.changedFields.containsKey(fieldId)) {
			return this.changedFields.get(fieldId);
		} else {
			/*
			 * only look into the base model if the field wasn't removed
			 */
			if(!this.removedFields.contains(fieldId)) {
				XField field = this.baseObject.getField(fieldId);
				if(field != null) {
					InObjectTransactionField transField = new InObjectTransactionField(
					        field.getAddress(), field.getRevisionNumber(), this);
					
					this.changedFields.put(fieldId, transField);
					
					return transField;
				}
			}
		}
		
		return null;
	}
	
	@Override
	public int hashCode() {
		return super.hashCode();
	}
	
	@Override
	public Iterator<XId> iterator() {
		/*
		 * Note: this is probably not the most effective implementation of this
		 * method
		 */

		// get all ids of objects in the baseObject that weren't removed
		LinkedList<XId> currentIds = new LinkedList<XId>();
		
		for(XId id : this.baseObject) {
			if(!this.removedFields.contains(id)) {
				currentIds.add(id);
			}
		}
		
		// get all ids of newly added fields
		for(XId id : this.changedFields.keySet()) {
			currentIds.add(id);
		}
		
		return currentIds.iterator();
	}
	
	@Override
	protected AbstractEntity getFather() {
		return this.baseObject.getFather();
	}
	
	@Override
	public XType getType() {
		return XType.XOBJECT;
	}
	
	/*
	 * Special methods needed for the helperclass InObjectTransactionField
	 */

	/**
	 * Returns the value of the specified field (if it exists)
	 * 
	 * @param fieldId the {@link XId} which {@link XValue} is to be returned
	 * @return the {@link XValue} of the specified field (null if the field
	 *         doesn't exist)
	 */
	protected XValue getValue(XId fieldId) {
		if(this.removedFields.contains(fieldId)) {
			return null;
		} else if(this.changedValues.containsKey(fieldId)) {
			return this.changedValues.get(fieldId);
		} else {
			XField field = this.baseObject.getField(fieldId);
			
			if(field != null) {
				return field.getValue();
			}

			else {
				// field doesn't exist
				return null;
			}
		}
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
