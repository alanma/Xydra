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
import org.xydra.core.model.impl.memory.MemoryField;


/**
 * TODO Document
 * 
 * TODO check if this implementation is thread-safe "enough" TODO implement
 * better handling of Transaction-commands
 * 
 * @author Kaidel
 * 
 */
// suppressing warning while this is in flux ~~max
@SuppressWarnings("unused")
public class TransactionObject implements XWritableObject {
	
	private XObject baseObject;
	private long revisionNumber;
	
	private Map<XID,XField> changedFields;
	private Map<XID,XValue> changedValues;
	
	private Set<XID> removedFields;
	
	private Map<XID,Long> fieldRevisionNumbers;
	
	private ArrayList<XAtomicCommand> commands;
	
	public TransactionObject(XObject object) {
		this.baseObject = object;
		this.revisionNumber = object.getRevisionNumber();
		
		this.changedFields = new HashMap<XID,XField>();
		this.changedValues = new HashMap<XID,XValue>();
		this.removedFields = new HashSet<XID>();
		this.fieldRevisionNumbers = new HashMap<XID,Long>();
		
		this.commands = new ArrayList<XAtomicCommand>();
	}
	
	// Transaction methods
	
	public LinkedList<XCommand> getCurrentCommandList() {
		// return a copy so that it is not possible to directly change the
		// internal list
		LinkedList<XCommand> copy = new LinkedList<XCommand>();
		
		Collections.copy(copy, this.commands);
		return copy;
	}
	
	public long commit() {
		// TODO maybe use the builder directly instead of the LinkedList
		XTransactionBuilder builder = new XTransactionBuilder(this.getAddress());
		
		for(int i = 0; i < this.commands.size(); i++) {
			builder.addCommand(this.commands.get(i));
		}
		
		XTransaction transaction = builder.build();
		
		long revNr = this.baseObject.executeCommand(transaction);
		
		if(revNr != XCommand.FAILED) {
			this.clear(); // restarts the TransactionObject
		}
		return revNr;
	}
	
	public void clear() {
		/*
		 * TODO what happens with currently used InObjectTransactionFields when
		 * this method is called?
		 */

		this.changedFields = new HashMap<XID,XField>();
		this.changedValues = new HashMap<XID,XValue>();
		this.removedFields = new HashSet<XID>();
		this.fieldRevisionNumbers = new HashMap<XID,Long>();
		
		this.commands = new ArrayList<XAtomicCommand>();
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
		
		/*
		 * Make sure that revision numbers are increased correctly
		 */
		if(command.getChangeType() == ChangeType.TRANSACTION) {
			XTransaction transaction = (XTransaction)command;
			
			for(int i = 0; i < transaction.size(); i++) {
				long result = executeCommand(transaction.getCommand(i));
				
				if(result == XCommand.FAILED) {
					usedCallback.onFailure();
					return XCommand.FAILED;
				}
			}
			
			// Transaction "succeeded"
			usedCallback.onSuccess(this.revisionNumber);
			return this.revisionNumber;
		}
		
		// Simulate the action the given command would actually execute
		
		// no transaction
		
		// check wether the given command actually refers to the this object
		if(!command.getTarget().getObject().equals(this.getID())
		        || !command.getTarget().getModel().equals(this.getAddress().getModel())) {
			usedCallback.onFailure();
			return XCommand.FAILED;
		}
		
		// Object Commands
		if(command instanceof XObjectCommand) {
			XObjectCommand objectCommand = (XObjectCommand)command;
			
			// check revision number
			if(objectCommand.getRevisionNumber() != this.revisionNumber) {
				usedCallback.onFailure();
				return XCommand.FAILED;
			}
			
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
				
				// increase revision numbers
				this.revisionNumber++;
				this.fieldRevisionNumbers.put(fieldId, this.revisionNumber);
				
				XField field = new MemoryField(null, fieldId);
				this.changedFields.put(fieldId, field);
				
				// command succeeded -> add it to the list
				this.commands.add((XAtomicCommand)command);
				
				usedCallback.onSuccess(this.revisionNumber);
				return this.revisionNumber;
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
				
				// increase revision numbers
				this.revisionNumber++;
				
				// mark it as removed
				this.removedFields.add(fieldId);
				
				// remove info from all other maps
				this.changedFields.remove(fieldId);
				this.changedValues.remove(fieldId);
				this.fieldRevisionNumbers.remove(fieldId);
				
				// command succeeded -> add it to the list
				this.commands.add((XAtomicCommand)command);
				
				usedCallback.onSuccess(this.revisionNumber);
				return this.revisionNumber;
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
			assert field instanceof InModelTransactionField;
			
			// check revision number
			if(fieldCommand.getRevisionNumber() != field.getRevisionNumber()) {
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
				
				// increase revision numbers
				this.revisionNumber++;
				this.fieldRevisionNumbers.put(fieldId, this.revisionNumber);
				
				// "change" the value
				this.changedValues.put(fieldId, fieldCommand.getValue());
				
				// command succeeded -> add it to the list
				this.commands.add((XAtomicCommand)command);
				
				usedCallback.onSuccess(this.revisionNumber);
				return this.revisionNumber;
			}

			else if(fieldCommand.getChangeType() == ChangeType.REMOVE) {
				if(field.getValue() == null) {
					if(!fieldCommand.isForced()) {
						usedCallback.onFailure();
						return XCommand.FAILED;
					}
				}
				
				// increase revision numbers
				this.revisionNumber++;
				this.fieldRevisionNumbers.put(fieldId, this.revisionNumber);
				
				// "change" the value
				this.changedValues.put(fieldId, null);
				
				// command succeeded -> add it to the list
				this.commands.add((XAtomicCommand)command);
				
				usedCallback.onSuccess(this.revisionNumber);
				return this.revisionNumber;
			}

			else if(fieldCommand.getChangeType() == ChangeType.CHANGE) {
				/*
				 * if(field.getValue() != fieldCommand.g) {
				 * if(!fieldCommand.isForced()) { callback.onFailure(); return
				 * XCommand.FAILED; } }
				 * 
				 * 
				 * TODO Check how forced Change Commands work!
				 */

				// increase revision numbers
				this.revisionNumber++;
				this.fieldRevisionNumbers.put(fieldId, this.revisionNumber);
				
				// "change" the value
				this.changedValues.put(fieldId, fieldCommand.getValue());
				
				// command succeeded -> add it to the list
				this.commands.add((XAtomicCommand)command);
				
				usedCallback.onSuccess(this.revisionNumber);
				return this.revisionNumber;
			}
		}
		
		throw new IllegalArgumentException(
		        "Given Command was neither a correct instance of XObjectCommand, XTransactio or XFieldCommand!");
	}
	
	public long getRevisionNumber() {
		return this.revisionNumber;
	}
	
	public boolean isEmpty() {
		return this.baseObject.isEmpty() && this.changedFields.isEmpty();
	}
	
	@Override
	public boolean hasField(XID fieldId) {
		XField field = null;
		
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
		this.executeCommand(command);
		
		return this.getField(fieldId);
	}
	
	@Override
	public boolean removeField(XID fieldId) {
		long revNr = this.getField(fieldId).getRevisionNumber();
		XAddress temp = this.getAddress();
		XAddress address = XX.toAddress(temp.getRepository(), temp.getModel(), temp.getObject(),
		        temp.getField());
		
		XObjectCommand command = X.getCommandFactory().createSafeRemoveFieldCommand(
		        this.getAddress(), revNr);
		this.executeCommand(command);
		
		return this.getField(fieldId) == null;
	}
	
	/*
	 * Special methods needed for the helperclass InObjectTransactionField
	 */

	public XWritableField getField(XID fieldId) {
		/*
		 * TODO maybe already add the value and the field itself to
		 * changedFields as soon as someone asks for it to speed up look-up
		 * times?
		 */

		XField field = null;
		
		if(this.changedFields.containsKey(fieldId)) {
			field = this.changedFields.get(fieldId);
		} else {
			/*
			 * only look into the base model if the field wasn't removed
			 */
			if(!this.removedFields.contains(fieldId)) {
				field = this.baseObject.getField(fieldId);
			}
		}
		
		if(field == null) {
			return null;
		} else {
			return new InObjectTransactionField(fieldId, this);
		}
	}
	
	protected long getFieldRevisionNumber(XID fieldId) {
		// assert: there exists and XField with the given address either in
		// changedFields or baseObject
		
		long revNr = 0;
		
		if(this.fieldRevisionNumbers.containsKey(fieldId)) {
			revNr = this.fieldRevisionNumbers.get(fieldId);
		} else {
			XField field = this.baseObject.getField(fieldId);
			
			assert field != null;
			
			revNr = field.getRevisionNumber();
		}
		
		return revNr;
	}
	
	public XValue getValue(XID id) {
		// TODO maybe improve handling of removed fields
		
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
