package org.xydra.core.change;

import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.XType;
import org.xydra.base.rmof.XWritableField;
import org.xydra.base.value.XValue;
import org.xydra.core.model.XField;


/**
 * TODO Document & Implement
 * 
 * @author Kaidel
 * 
 */

public class InModelTransactionField implements XWritableField {
	
	private XField field;
	private TransactionModel model;
	
	public InModelTransactionField(XField field, TransactionModel model) {
		this.model = model;
		this.field = field;
	}
	
	@Override
	public long getRevisionNumber() {
		return this.model.getFieldRevisionNumber(this.getAddress());
	}
	
	@Override
	public XValue getValue() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public XAddress getAddress() {
		return this.field.getAddress();
	}
	
	@Override
	public XID getID() {
		return this.field.getID();
	}
	
	@Override
	public boolean setValue(XValue value) {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public XType getType() {
		return XType.XFIELD;
	}
	
}
