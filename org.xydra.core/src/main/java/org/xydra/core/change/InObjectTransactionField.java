package org.xydra.core.change;

import org.xydra.base.X;
import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.XX;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XFieldCommand;
import org.xydra.base.rmof.XWritableField;
import org.xydra.base.value.XValue;


/**
 * TODO Document & Implement
 * 
 * @author Kaidel
 * 
 */

public class InObjectTransactionField implements XWritableField {
	private XID fieldId;
	private TransactionObject object;
	
	public InObjectTransactionField(XID fieldId, TransactionObject object) {
		this.fieldId = fieldId;
		this.object = object;
	}
	
	@Override
	public long getRevisionNumber() {
		return this.object.getFieldRevisionNumber(this.getID());
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
		if(object instanceof InObjectTransactionField) {
			InObjectTransactionField field = (InObjectTransactionField)object;
			
			/*
			 * Two InObjectTransactionFields are equal if they have the same
			 * XID, the same value and the same parent-TransactionObject
			 */
			boolean result = this.fieldId.equals(field.fieldId) && this.object.equals(field.object);
			
			if(this.getValue() == null) {
				result &= field.getValue() == null;
			} else {
				result &= this.getValue().equals(field.getValue());
			}
			
			return result;
		} else if(object instanceof XWritableField) {
			
			/*
			 * this can only happen when someone gets a field from transObject
			 * that already existed in the underlying XObject of the
			 * transObject, so we'll need to simulate the equals relation of
			 * XWritableField here
			 */

			XWritableField field = (XWritableField)object;
			return field.getAddress().equals(this.getAddress())
			        && field.getRevisionNumber() == this.getRevisionNumber();
			
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		return super.hashCode();
	}
}
