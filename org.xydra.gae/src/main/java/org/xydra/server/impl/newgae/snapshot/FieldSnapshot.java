package org.xydra.server.impl.newgae.snapshot;

import java.io.Serializable;

import org.xydra.core.model.XAddress;
import org.xydra.core.model.XBaseField;
import org.xydra.core.model.XField;
import org.xydra.core.model.XID;
import org.xydra.core.model.XType;
import org.xydra.core.value.XValue;


/**
 * A snapshot (read-only data transfer object) of an {@link XField}.
 * 
 * @author dscharrer
 * 
 */
public class FieldSnapshot implements XBaseField, Serializable {
	
	private static final long serialVersionUID = -6014779550527350829L;
	
	private final XAddress addr;
	/** Set by {@link GaeSnapshotService} */
	protected XValue value;
	protected long rev;
	
	protected FieldSnapshot(XAddress addr, long rev) {
		assert addr.getAddressedType() == XType.XFIELD;
		this.addr = addr;
		this.rev = rev;
	}
	
	@Override
	public long getRevisionNumber() {
		return this.rev;
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
	public XAddress getAddress() {
		return this.addr;
	}
	
	@Override
	public XID getID() {
		return this.addr.getField();
	}
	
}
