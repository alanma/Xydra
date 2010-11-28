package org.xydra.store.base;

import java.io.Serializable;

import org.xydra.core.model.XAddress;
import org.xydra.core.model.XID;
import org.xydra.core.model.XWritableField;
import org.xydra.core.value.XValue;


/**
 * A simple data container for {@link XWritableField}
 * 
 * @author voelkel
 */
public class SimpleField implements XWritableField, Serializable {
	
	/**
     * 
     */
	private static final long serialVersionUID = -4704907115751969328L;
	protected XAddress address;
	private long revisionNumber;
	private XValue value;
	
	/**
	 * @param address
	 * @param revisionNumber
	 * @param value can be null
	 */
	public SimpleField(XAddress address, long revisionNumber, XValue value) {
		super();
		this.address = address;
		this.revisionNumber = revisionNumber;
		this.value = value;
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
		if(this.value == null) {
			this.value = value;
			return value == null;
		} else {
			boolean change = this.value.equals(value);
			this.value = value;
			return change;
		}
	}
	
}
