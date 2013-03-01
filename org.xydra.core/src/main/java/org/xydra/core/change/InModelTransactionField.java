package org.xydra.core.change;

import org.xydra.base.X;
import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.XType;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XFieldCommand;
import org.xydra.base.rmof.XWritableField;
import org.xydra.base.value.XValue;
import org.xydra.core.model.impl.memory.AbstractEntity;


/**
 * TODO Document
 * 
 * @author Kaidel
 * 
 */

public class InModelTransactionField extends AbstractEntity implements XWritableField {
	private XAddress address;
	private TransactionModel model;
	private long revisionNumber;
	private long transactionNumber;
	private boolean isRemoved = false;
	
	public InModelTransactionField(XAddress address, long revisionNumber, TransactionModel model) {
		this.address = address;
		this.model = model;
		this.revisionNumber = revisionNumber;
		this.transactionNumber = model.getTransactionNumber();
	}
	
	@Override
	public long getRevisionNumber() {
		isValid();
		
		return this.revisionNumber;
	}
	
	@Override
	public XValue getValue() {
		isValid();
		
		return this.model.getValue(this.getAddress());
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
	public XId getId() {
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
		
		return this.model.executeCommand(command) != XCommand.FAILED;
	}
	
	@Override
	public boolean equals(Object object) {
		isValid();
		
		return super.equals(object);
	}
	
	@Override
	public int hashCode() {
		isValid();
		
		return super.hashCode();
	}
	
	@Override
	public AbstractEntity getFather() {
		isValid();
		
		return this.model;
	}
	
	@Override
	public XType getType() {
		isValid();
		
		return XType.XFIELD;
	}
	
	private void isValid() {
		if(this.isRemoved) {
			throw new IllegalArgumentException(
			        "InModelTransactionField was already removed and can no longer be used.");
		} else if(this.transactionNumber != this.model.getTransactionNumber()) {
			throw new IllegalArgumentException(
			        "InModelTransactionField refers to an already committed or canceled transaction and "
			                + "can no longer be used.");
		}
		
	}
	
	protected void setRemoved() {
		this.isRemoved = true;
	}
}
