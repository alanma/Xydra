package org.xydra.core.change;

import org.xydra.base.X;
import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.XType;
import org.xydra.base.XX;
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

public class InObjectTransactionField extends AbstractEntity implements XWritableField {
	
	private XID fieldId;
	private TransactionObject object;
	private long revisionNumber;
	
	public InObjectTransactionField(XID fieldId, long revisionNumber, TransactionObject object) {
		this.fieldId = fieldId;
		this.object = object;
		this.revisionNumber = revisionNumber;
	}
	
	@Override
	public long getRevisionNumber() {
		return this.revisionNumber;
	}
	
	@Override
	public XValue getValue() {
		return this.object.getValue(this.getID());
	}
	
	@Override
	public boolean isEmpty() {
		// TODO implement
		throw new UnsupportedOperationException();
	}
	
	@Override
	public XAddress getAddress() {
		XAddress temp = this.object.getAddress();
		return XX.toAddress(temp.getRepository(), temp.getModel(), temp.getObject(), this.fieldId);
	}
	
	@Override
	public XID getID() {
		return this.fieldId;
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
		
		return this.object.executeCommand(command) != XCommand.FAILED;
	}
	
	@Override
	public boolean equals(Object object) {
		return super.equals(object);
	}
	
	public boolean equalInObjectTransactionField(InObjectTransactionField field) {
		/*
		 * Two InObjectTransactionFields are equal if they have the same XID,
		 * the same value and the same parent-TransactionObject
		 */
		boolean result = this.fieldId.equals(field.fieldId) && this.object.equals(field.object);
		
		if(this.getValue() == null) {
			result &= field.getValue() == null;
		} else {
			result &= this.getValue().equals(field.getValue());
		}
		
		return result;
	}
	
	@Override
	public int hashCode() {
		return super.hashCode();
	}
	
	@Override
	public AbstractEntity getFather() {
		return this.object;
	}
	
	@Override
	public XType getType() {
		return XType.XFIELD;
	}
	
}
