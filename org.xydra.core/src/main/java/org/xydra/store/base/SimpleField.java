package org.xydra.store.base;

import java.io.Serializable;

import org.xydra.core.change.XCommand;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XID;
import org.xydra.core.model.XWritableField;
import org.xydra.core.value.XValue;
import org.xydra.index.XI;


/**
 * A simple data container for {@link XWritableField}
 * 
 * @author voelkel
 */
public class SimpleField implements XWritableField, Serializable {
	
	private static final long serialVersionUID = -4704907115751969328L;
	private final XAddress address;
	private long revisionNumber;
	private XValue value;
	
	/**
	 * @param address
	 * @param revisionNumber
	 * @param value can be null
	 */
	public SimpleField(XAddress address, long revisionNumber, XValue value) {
		this.address = address;
		this.revisionNumber = revisionNumber;
		this.value = value;
	}
	
	public SimpleField(XAddress address) {
		this.address = address;
		this.revisionNumber = XCommand.NEW;
		this.value = null;
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
	public long getRevisionNumber() {
		return this.revisionNumber;
	}
	
	@Override
	public XValue getValue() {
		return this.value;
	}
	
	@Override
	public boolean isEmpty() {
		return this.value == null;
	}
	
	@Override
	public boolean setValue(XValue value) {
		boolean changed = XI.equals(this.value, value);
		this.value = value;
		return changed;
	}
	
	/**
	 * @param rev the new revision number
	 */
	public void setRevisionNumber(long rev) {
		this.revisionNumber = rev;
	}
	
}
