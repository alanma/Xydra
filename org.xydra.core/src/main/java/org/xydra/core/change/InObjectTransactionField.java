package org.xydra.core.change;

import org.xydra.base.X;
import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.XType;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XFieldCommand;
import org.xydra.base.rmof.XWritableField;
import org.xydra.base.value.XValue;
import org.xydra.core.model.XObject;
import org.xydra.core.model.impl.memory.AbstractEntity;


/**
 * A class that simulates the behavior of an {@link XWritableField}, used by
 * {@link TransactionObject} to simulate the behavior of a wrapped
 * {@link XObject} without making any changes to the state of the wrapped
 * object.
 * 
 * @author Kaidel
 * 
 */

public class InObjectTransactionField extends AbstractEntity implements XWritableField {
	
	private XAddress address;
	private TransactionObject object;
	private long revisionNumber;
	private long transactionNumber;
	private boolean isRemoved;
	
	public InObjectTransactionField(XAddress fieldAddress, long revisionNumber,
	        TransactionObject object) {
		this.address = fieldAddress;
		this.object = object;
		this.revisionNumber = revisionNumber;
		this.transactionNumber = object.getTransactionNumber();
	}
	
	@Override
	public long getRevisionNumber() {
		isValid();
		
		return this.revisionNumber;
	}
	
	@Override
	public XValue getValue() {
		isValid();
		
		return this.object.getValue(this.getID());
	}
	
	@Override
	public boolean isEmpty() {
		isValid();
		
		return getValue() == null;
	}
	
	@Override
	public XAddress getAddress() {
		isValid();
		
		return this.address;
	}
	
	@Override
	public XID getID() {
		isValid();
		
		return this.address.getField();
	}
	
	@Override
	public boolean setValue(XValue value) {
		isValid();
		
		XFieldCommand command;
		
		if(value == null) {
			// remove type
			command = X.getCommandFactory().createSafeRemoveValueCommand(this.getAddress(),
			        this.getRevisionNumber());
		} else {
			if(this.getValue() == null) {
				// add type
				command = X.getCommandFactory().createSafeAddValueCommand(this.getAddress(),
				        this.getRevisionNumber(), value);
			} else {
				// change type
				command = X.getCommandFactory().createSafeChangeValueCommand(this.getAddress(),
				        this.getRevisionNumber(), value);
			}
		}
		
		return this.object.executeCommand(command) != XCommand.FAILED;
	}
	
	@Override
	public boolean equals(Object object) {
		isValid();
		
		return super.equals(object);
	}
	
	/**
	 * Compares the states of two {@link InObjectTransactionField
	 * InObjectTransactionFields}. Two InObjectTransactionFields are equal if
	 * they have the same XID, the same value and the same
	 * parent-TransactionObject.
	 * 
	 * @param field the field to which this field is to be compared
	 * @return true, if the given field has the same XID, the same value and the
	 *         same parent-TransactionObject as this field
	 */
	public boolean equalInObjectTransactionFieldState(InObjectTransactionField field) {
		isValid();
		
		boolean result = this.address.equals(field.address)
		        && this.object.equalTransactionObjectState(field.object);
		
		if(this.getValue() == null) {
			result &= field.getValue() == null;
		} else {
			result &= this.getValue().equals(field.getValue());
		}
		
		return result;
	}
	
	@Override
	public int hashCode() {
		isValid();
		
		return super.hashCode();
	}
	
	@Override
	public AbstractEntity getFather() {
		isValid();
		
		return this.object;
	}
	
	@Override
	public XType getType() {
		isValid();
		
		return XType.XFIELD;
	}
	
	private void isValid() {
		if(this.isRemoved) {
			throw new IllegalArgumentException(
			        "InObjecTransactionField was already removed and can no longer be used.");
		} else if(this.transactionNumber != this.object.getTransactionNumber()) {
			throw new IllegalArgumentException(
			        "InObjectTransactionField refers to an already committed or canceled transaction and "
			                + "can no longer be used.");
		}
		
	}
	
	protected void setRemoved() {
		this.isRemoved = true;
	}
}
