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
import org.xydra.core.model.impl.memory.AbstractEntity;
import org.xydra.core.model.impl.memory.MemoryObject;


/**
 * A helper class to create {@link XTransaction XTransactions} for a specific
 * {@link MemoryObject}. This class wraps the given {@link XObject} and then
 * acts as a simulator. It provides all methods for changing the state of an
 * {@link MemoryObject}, but the changes that are executed on this
 * TransactionObject are not actually executed on the {@link MemoryObject}, but
 * just simulated and safed. After you've made all changes, you can then execute
 * them in a single {@link XTransaction} on the given {@link MemoryObject} by
 * calling the {@link #commit()} method.
 * 
 * In short, this class makes creating and executing an {@link XTransaction} as
 * simple as executing some methods on an {@link MemoryObject}
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
	
	// help objects for executing transactions
	private HashMap<XID,InObjectTransactionField> transChangedFields;
	private Map<XID,XValue> transChangedValues;
	
	private Set<XID> transRemovedFields;
	
	private ArrayList<XAtomicCommand> commands;
	private ArrayList<XAtomicCommand> transCommands;
	
	private boolean inTransaction = false;
	
	public TransactionObject(MemoryObject object) {
		this.baseObject = object;
		this.revisionNumber = object.getRevisionNumber();
		
		this.changedFields = new HashMap<XID,InObjectTransactionField>();
		this.changedValues = new HashMap<XID,XValue>();
		this.removedFields = new HashSet<XID>();
		
		this.transChangedFields = new HashMap<XID,InObjectTransactionField>();
		this.transChangedValues = new HashMap<XID,XValue>();
		this.transRemovedFields = new HashSet<XID>();
		
		this.commands = new ArrayList<XAtomicCommand>();
		this.transCommands = new ArrayList<XAtomicCommand>();
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
		
		this.transChangedFields.clear();
		this.transChangedValues.clear();
		this.transRemovedFields.clear();
		
		this.commands.clear();
		this.transCommands.clear();
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
			long result = this.handleTransaction(transaction);
			
			if(result == XCommand.FAILED) {
				usedCallback.onFailure();
			} else {
				usedCallback.onSuccess(this.getRevisionNumber());
			}
			return this.getRevisionNumber();
		}
		
		// given command is no transaction
		
		// Simulate the action the given command would actually execute
		
		// check whether the given command actually refers to the this object
		if(!command.getTarget().getObject().equals(this.getID())
		        || !command.getTarget().getModel().equals(this.getAddress().getModel())) {
			if(!this.inTransaction) {
				usedCallback.onFailure();
			}
			return XCommand.FAILED;
		}
		
		// TODO comment inTransaction cases!
		
		// Object Commands
		if(command instanceof XObjectCommand) {
			XObjectCommand objectCommand = (XObjectCommand)command;
			
			XID fieldId = command.getChangedEntity().getField();
			
			if(command.getChangeType() == ChangeType.ADD) {
				if(hasField(fieldId)) {
					if(!objectCommand.isForced()) {
						// not forced, tried to add something that already
						// existed
						
						if(!this.inTransaction) {
							usedCallback.onFailure();
						}
						return XCommand.FAILED;
					}
					
					// forced command -> already finished with the execution
					if(!this.inTransaction) {
						usedCallback.onSuccess(this.getRevisionNumber());
					}
					return this.getRevisionNumber();
				}
				
				XAddress temp = this.getAddress();
				XAddress address = XX.toAddress(temp.getRepository(), temp.getModel(),
				        temp.getObject(), fieldId);
				
				InObjectTransactionField field = new InObjectTransactionField(address,
				        XCommand.NEW, this);
				
				if(this.inTransaction) {
					this.transChangedFields.put(fieldId, field);
					
					this.transCommands.add((XAtomicCommand)command);
				} else {
					// remove from list of removed fields, if needed
					this.removedFields.remove(fieldId);
					
					this.changedFields.put(fieldId, field);
					
					// command succeeded -> add it to the list
					this.commands.add((XAtomicCommand)command);
					
					usedCallback.onSuccess(this.getRevisionNumber());
				}
				
				return this.getRevisionNumber();
			}

			else if(command.getChangeType() == ChangeType.REMOVE) {
				if(!hasField(fieldId)) {
					if(!objectCommand.isForced()) {
						// not forced, tried to remove something that didn't
						// exist
						if(!this.inTransaction) {
							usedCallback.onFailure();
						}
						return XCommand.FAILED;
					}
					
					// forced command -> already finished with the execution
					if(!this.inTransaction) {
						usedCallback.onSuccess(this.getRevisionNumber());
					}
					return this.getRevisionNumber();
				}
				
				XWritableField field = this.getField(fieldId);
				// remember: this actually is an InModelTransactionField
				assert field != null;
				assert field instanceof InModelTransactionField;
				
				// check revision number
				if(!objectCommand.isForced()
				        && objectCommand.getRevisionNumber() != field.getRevisionNumber()) {
					if(!this.inTransaction) {
						usedCallback.onFailure();
					}
					return XCommand.FAILED;
				}
				
				if(this.inTransaction) {
					this.transRemovedFields.add(fieldId);
					
					this.transCommands.add((XAtomicCommand)command);
				} else {
					// mark it as removed
					this.removedFields.add(fieldId);
					
					// remove info from all other maps
					this.changedFields.remove(fieldId);
					this.changedValues.remove(fieldId);
					
					// command succeeded -> add it to the list
					this.commands.add((XAtomicCommand)command);
					
					usedCallback.onSuccess(this.getRevisionNumber());
				}
				
				return this.getRevisionNumber();
			}
		}
		
		// XFieldCommands
		if(command instanceof XFieldCommand) {
			XFieldCommand fieldCommand = (XFieldCommand)command;
			XID fieldId = command.getChangedEntity().getField();
			
			if(!hasField(fieldId)) {
				if(!this.inTransaction) {
					usedCallback.onFailure();
				}
				return XCommand.FAILED;
			}
			
			XWritableField field = this.getField(fieldId);
			// remember: this actually is an InObjectTransactionField
			assert field != null;
			assert field instanceof InObjectTransactionField;
			
			// check revision number
			if(fieldCommand.getRevisionNumber() != field.getRevisionNumber()
			        && !fieldCommand.isForced()) {
				if(!this.inTransaction) {
					usedCallback.onFailure();
				}
				return XCommand.FAILED;
			}
			
			if(fieldCommand.getChangeType() == ChangeType.ADD) {
				if(field.getValue() != null) {
					if(!fieldCommand.isForced()) {
						if(!this.inTransaction) {
							usedCallback.onFailure();
						}
						return XCommand.FAILED;
					}
				}
				
				if(this.inTransaction) {
					this.transChangedValues.put(fieldId, fieldCommand.getValue());
					
					this.transCommands.add((XAtomicCommand)command);
				} else {
					// "add" the value
					this.changedValues.put(fieldId, fieldCommand.getValue());
					
					// command succeeded -> add it to the list
					this.commands.add((XAtomicCommand)command);
					
					usedCallback.onSuccess(field.getRevisionNumber());
				}
				return field.getRevisionNumber();
			}

			else if(fieldCommand.getChangeType() == ChangeType.REMOVE) {
				if(field.getValue() == null) {
					if(!fieldCommand.isForced()) {
						if(!this.inTransaction) {
							usedCallback.onFailure();
						}
						return XCommand.FAILED;
					}
				}
				
				if(this.inTransaction) {
					this.transChangedValues.put(fieldId, null);
					
					this.transCommands.add((XAtomicCommand)command);
				} else {
					// "remove" the value
					this.changedValues.put(fieldId, null);
					
					// command succeeded -> add it to the list
					this.commands.add((XAtomicCommand)command);
					
					usedCallback.onSuccess(field.getRevisionNumber());
				}
				return field.getRevisionNumber();
			}

			else if(fieldCommand.getChangeType() == ChangeType.CHANGE) {
				if(field.getValue() == null) {
					if(!fieldCommand.isForced()) {
						if(!this.inTransaction) {
							usedCallback.onFailure();
						}
						return XCommand.FAILED;
					}
				}
				
				if(this.inTransaction) {
					this.transChangedValues.put(fieldId, fieldCommand.getValue());
					
					this.transCommands.add((XAtomicCommand)command);
				} else {
					// "change" the value
					this.changedValues.put(fieldId, fieldCommand.getValue());
					
					// command succeeded -> add it to the list
					this.commands.add((XAtomicCommand)command);
					
					usedCallback.onSuccess(field.getRevisionNumber());
				}
				
				return field.getRevisionNumber();
			}
		}
		
		throw new IllegalArgumentException(
		        "Given Command was neither a correct instance of XObjectCommand, XFieldCommand or XTransaction!");
	}
	
	private long handleTransaction(XTransaction transaction) {
		this.inTransaction = true;
		
		for(int i = 0; i < transaction.size(); i++) {
			XCommand command = transaction.getCommand(i);
			long result = executeCommand(command);
			
			if(result == XCommand.FAILED) {
				// rollback happens implicitly, since nothing was really changed
				return XCommand.FAILED;
			}
		}
		
		this.inTransaction = false;
		
		// Transaction "succeeded"
		return this.revisionNumber;
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
					this.changedFields.put(fieldId, new InObjectTransactionField(
					        field.getAddress(), field.getRevisionNumber(), this));
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
		LinkedList<XID> currentIds = new LinkedList<XID>();
		
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
