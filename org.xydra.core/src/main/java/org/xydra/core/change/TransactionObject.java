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
import org.xydra.base.XID;
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
import org.xydra.core.model.XObject;
import org.xydra.core.model.impl.memory.AbstractEntity;
import org.xydra.core.model.impl.memory.MemoryObject;


/**
 * A helper class to create {@link XTransaction XTransactions} for a specific
 * {@link XObject}. This class wraps the given {@link XObject} and then acts as
 * a simulator. It provides all methods for changing the state of an
 * {@link XObject}, but the changes that are executed on this TransactionObject
 * are not actually executed on the {@link XObject}, but just simulated and
 * safed. After you've made all changes, you can then execute them in a single
 * {@link XTransaction} on the given {@link XObject} by calling the
 * {@link #commit()} method.
 * 
 * In short, this class makes creating and executing an {@link XTransaction} as
 * simple as executing some methods on an {@link XObject}
 * 
 * @author Kaidel
 * 
 */

/**
 * TODO check if this implementation is thread-safe "enough" *
 */
public class TransactionObject extends AbstractEntity implements XWritableObject {
	
	private MemoryObject baseObject;
	private long revisionNumber;
	
	private HashMap<XID,InObjectTransactionField> changedFields;
	private Map<XID,XValue> changedValues;
	
	private Set<XID> removedFields;
	
	private ArrayList<XAtomicCommand> commands;
	
	public TransactionObject(MemoryObject object) {
		this.baseObject = object;
		this.revisionNumber = object.getRevisionNumber();
		
		this.changedFields = new HashMap<XID,InObjectTransactionField>();
		this.changedValues = new HashMap<XID,XValue>();
		this.removedFields = new HashSet<XID>();
		
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
	 * {@link XObject}. If it succeeds, this TransactionObject will be reset and
	 * can then be used to create a new XTransaction, if the transaction fails,
	 * nothing happens.
	 * 
	 * @return the revision number of the wrapped {@link XObject} after the
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
	 * TransactionObject, but not to the wrapped {@link XObject}. After the
	 * execution, the TransactionObject will then represent the state the
	 * wrapped {@link XObject} currently is in.
	 */
	public void clear() {
		/*
		 * TODO what happens with currently used InObjectTransactionFields when
		 * this method is called?
		 * 
		 * Maybe implement a "stateCounter" which is incremented every time this
		 * method is called, initialize the InObjectTransactionFields with this
		 * counter and the InObjectTransactionFields need to check whether their
		 * counter is the same as the counter of their TransactionObject. If
		 * not, throw an exception. Problem: Renders already returned
		 * InObjectTransactionFields useless.
		 */

		this.changedFields.clear();
		this.changedValues.clear();
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
	 * state of the wrapped {@link XObject}, false otherwise.
	 * 
	 * @return true, if the state of the TransactionObject is different than the
	 *         state of the wrapped {@link XObject}, false otherwise.
	 */
	public boolean isChanged() {
		return !(this.changedFields.isEmpty() && this.removedFields.isEmpty() && this.changedValues
		        .isEmpty());
	}
	
	// XWritableObject-specific methods
	
	public XAddress getAddress() {
		return this.baseObject.getAddress();
	}
	
	public XID getID() {
		return this.baseObject.getID();
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
		
		// check wether the given command actually refers to the this object
		if(!command.getTarget().getObject().equals(this.getID())
		        || !command.getTarget().getModel().equals(this.getAddress().getModel())) {
			usedCallback.onFailure();
			return XCommand.FAILED;
		}
		
		// Object Commands
		if(command instanceof XObjectCommand) {
			XObjectCommand objectCommand = (XObjectCommand)command;
			
			XID fieldId = command.getChangedEntity().getField();
			
			if(command.getChangeType() == ChangeType.ADD) {
				if(hasField(fieldId)) {
					if(!objectCommand.isForced()) {
						// not forced, tried to add something that already
						// existed
						usedCallback.onFailure();
						return XCommand.FAILED;
					}
				}
				
				// remove from list of removed fields, if needed
				this.removedFields.remove(fieldId);
				
				InObjectTransactionField field = new InObjectTransactionField(fieldId,
				        XCommand.NEW, this);
				this.changedFields.put(fieldId, field);
				
				// command succeeded -> add it to the list
				this.commands.add((XAtomicCommand)command);
				
				usedCallback.onSuccess(this.getRevisionNumber());
				return this.getRevisionNumber();
			}

			else if(command.getChangeType() == ChangeType.REMOVE) {
				if(!hasField(fieldId)) {
					if(!objectCommand.isForced()) {
						// not forced, tried to remove something that didn't
						// exist
						usedCallback.onFailure();
						return XCommand.FAILED;
					}
				}
				
				XWritableField field = this.getField(fieldId);
				if(field == null && !objectCommand.isForced()) {
					usedCallback.onFailure();
					return XCommand.FAILED;
				}
				
				// check revision number
				if(!objectCommand.isForced()) {
					assert field != null;
					if(objectCommand.getRevisionNumber() != field.getRevisionNumber()) {
						usedCallback.onFailure();
						return XCommand.FAILED;
					}
				}
				
				// mark it as removed
				this.removedFields.add(fieldId);
				
				// remove info from all other maps
				this.changedFields.remove(fieldId);
				this.changedValues.remove(fieldId);
				
				// command succeeded -> add it to the list
				this.commands.add((XAtomicCommand)command);
				
				usedCallback.onSuccess(this.getRevisionNumber());
				return this.getRevisionNumber();
			}
		}
		
		// XFieldCommands
		if(command instanceof XFieldCommand) {
			XFieldCommand fieldCommand = (XFieldCommand)command;
			XID fieldId = command.getChangedEntity().getField();
			
			if(!hasField(fieldId)) {
				usedCallback.onFailure();
				return XCommand.FAILED;
			}
			
			XWritableField field = this.getField(fieldId);
			// remember: this actually is an InTransactionField
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
				this.changedValues.put(fieldId, fieldCommand.getValue());
				
				// command succeeded -> add it to the list
				this.commands.add((XAtomicCommand)command);
				
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
				this.changedValues.put(fieldId, null);
				
				// command succeeded -> add it to the list
				this.commands.add((XAtomicCommand)command);
				
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
				this.changedValues.put(fieldId, fieldCommand.getValue());
				
				// command succeeded -> add it to the list
				this.commands.add((XAtomicCommand)command);
				
				usedCallback.onSuccess(field.getRevisionNumber());
				return field.getRevisionNumber();
			}
		}
		
		throw new IllegalArgumentException(
		        "Given Command was neither a correct instance of XObjectCommand, XTransaction or XFieldCommand!");
	}
	
	@Override
	public long getRevisionNumber() {
		return this.revisionNumber;
	}
	
	public boolean isEmpty() {
		if(!this.baseObject.isEmpty()) {
			for(XID id : this.baseObject) {
				if(!this.removedFields.contains(id)) {
					// field wasn't removed in the TransactionObject
					return false;
				}
			}
		}
		
		return this.changedFields.isEmpty();
	}
	
	@Override
	public boolean hasField(XID fieldId) {
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
	public XWritableField createField(XID fieldId) {
		XObjectCommand command = X.getCommandFactory().createSafeAddFieldCommand(this.getAddress(),
		        fieldId);
		if(!hasField(fieldId)) {
			this.executeCommand(command);
		}
		
		return this.getField(fieldId);
	}
	
	@Override
	public boolean removeField(XID fieldId) {
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
	
	public boolean equalTransactionObject(TransactionObject object) {
		return this.baseObject.equals(object.baseObject)
		        && this.changedFields.equals(object.changedFields)
		        && this.changedValues.equals(object.changedValues)
		        && this.removedFields.equals(object.removedFields)
		        && this.revisionNumber == object.revisionNumber;
	}
	
	public XWritableField getField(XID fieldId) {
		boolean exists = false;
		
		if(this.changedFields.containsKey(fieldId)) {
			exists = true;
		} else {
			/*
			 * only look into the base model if the field wasn't removed
			 */
			if(!this.removedFields.contains(fieldId)) {
				XField field = this.baseObject.getField(fieldId);
				if(field != null) {
					this.changedFields.put(fieldId,
					        new InObjectTransactionField(fieldId, field.getRevisionNumber(), this));
					exists = true;
				}
			}
		}
		
		if(exists == false) {
			return null;
		} else {
			assert this.changedFields.containsKey(fieldId);
			
			return this.changedFields.get(fieldId);
		}
	}
	
	@Override
	public int hashCode() {
		return super.hashCode();
	}
	
	@Override
	public Iterator<XID> iterator() {
		/*
		 * TODO this is probably not the most effective implementation of this
		 * method
		 */

		// get all ids of objects in the baseObject that weren't removed
		HashSet<XID> currentIds = new HashSet<XID>();
		
		for(XID id : this.baseObject) {
			if(!this.removedFields.contains(id)) {
				currentIds.add(id);
			}
		}
		
		// get all ids of newly added fields
		for(XID id : this.changedFields.keySet()) {
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

	protected XValue getValue(XID id) {
		// TODO maybe improve handling of removed fields (throw exception or
		// something)
		
		if(this.removedFields.contains(id)) {
			return null;
		} else if(this.changedValues.containsKey(id)) {
			return this.changedValues.get(id);
		} else {
			XField field = this.baseObject.getField(id);
			
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
