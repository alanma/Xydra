package org.xydra.core.change;

import org.xydra.base.X;
import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.XType;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XFieldCommand;
import org.xydra.base.rmof.XWritableField;
import org.xydra.base.value.XValue;
import org.xydra.core.model.impl.memory.AbstractEntity;


/**
 * TODO Document & Implement
 * 
 * @author Kaidel
 * 
 */

public class InModelTransactionField extends AbstractEntity implements XWritableField {
	private XAddress address;
	private TransactionModel model;
	private long revisionNumber;
	
	public InModelTransactionField(XAddress address, long revisionNumber, TransactionModel model) {
		this.address = address;
		this.model = model;
		this.revisionNumber = revisionNumber;
	}
	
	@Override
	public long getRevisionNumber() {
		return this.revisionNumber;
	}
	
	@Override
	public XValue getValue() {
		return this.model.getValue(this.getAddress());
	}
	
	@Override
	public boolean isEmpty() {
		return getValue() == null;
	}
	
	@Override
	public XAddress getAddress() {
		return this.address;
	}
	
	@Override
	public XID getID() {
		return this.address.getField();
	}
	
	@Override
	public boolean setValue(XValue value) {
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
		return super.equals(object);
	}
	
	@Override
	public int hashCode() {
		return super.hashCode();
	}
	
	@Override
	public AbstractEntity getFather() {
		return this.model;
	}
	
	@Override
	public XType getType() {
		return XType.XFIELD;
	}
	
}
