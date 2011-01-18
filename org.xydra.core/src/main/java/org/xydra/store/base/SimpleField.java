package org.xydra.store.base;

import java.io.Serializable;

import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.XWritableField;
import org.xydra.base.XType;
import org.xydra.base.XHalfWritableField;
import org.xydra.base.value.XValue;
import org.xydra.core.change.XCommand;
import org.xydra.index.XI;


/**
 * A simple data container for {@link XHalfWritableField}.
 * 
 * Minimal memory footprint, can be used as data transfer object.
 * 
 * @author voelkel
 */
public class SimpleField implements Serializable, XWritableField {
	
	private static final long serialVersionUID = -4704907115751969328L;
	
	private final XAddress address;
	private XValue value;
	private long revisionNumber;
	
	/**
	 * @param address
	 * @param revisionNumber
	 * @param value can be null
	 */
	public SimpleField(XAddress address, long revisionNumber, XValue value) {
		assert address.getAddressedType() == XType.XFIELD;
		this.address = address;
		this.revisionNumber = revisionNumber;
		this.value = value;
	}
	
	public SimpleField(XAddress address, long rev) {
		this.address = address;
		this.revisionNumber = rev;
		this.value = null;
	}
	
	public SimpleField(XAddress address) {
		this(address, XCommand.NEW);
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
		boolean changed = !XI.equals(this.value, value);
		this.value = value;
		return changed;
	}
	
	public void setRevisionNumber(long rev) {
		this.revisionNumber = rev;
	}
	
}
