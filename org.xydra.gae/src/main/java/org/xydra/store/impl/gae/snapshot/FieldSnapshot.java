package org.xydra.store.impl.gae.snapshot;

import java.io.Serializable;

import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.XType;
import org.xydra.base.rmof.XReadableField;
import org.xydra.base.value.XValue;
import org.xydra.core.model.XField;


/**
 * A snapshot (read-only data transfer object) of an {@link XField}.
 * 
 * @author dscharrer
 * 
 */
@Deprecated
public class FieldSnapshot implements XReadableField, Serializable {
	
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
