package org.xydra.store.impl.gae.snapshot;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.XType;
import org.xydra.base.rmof.XReadableObject;
import org.xydra.core.model.XObject;


/**
 * A snapshot (read-only data transfer object) of an {@link XObject}.
 * 
 * @author dscharrer
 * 
 */
@Deprecated
@SuppressWarnings("deprecation")
public class ObjectSnapshot implements XReadableObject, Serializable {
	
	private static final long serialVersionUID = 3435719513297598376L;
	
	private final XAddress addr;
	protected long rev;
	protected Map<XID,FieldSnapshot> fields = new HashMap<XID,FieldSnapshot>();
	
	protected ObjectSnapshot(XAddress addr, long rev) {
		assert addr.getAddressedType() == XType.XOBJECT;
		this.addr = addr;
		this.rev = rev;
	}
	
	@Override
	public FieldSnapshot getField(XID fieldId) {
		return this.fields.get(fieldId);
	}
	
	@Override
	public long getRevisionNumber() {
		return this.rev;
	}
	
	@Override
	public boolean hasField(XID fieldId) {
		return this.fields.containsKey(fieldId);
	}
	
	@Override
	public boolean isEmpty() {
		return this.fields.isEmpty();
	}
	
	@Override
	public Iterator<XID> iterator() {
		return this.fields.keySet().iterator();
	}
	
	@Override
	public XAddress getAddress() {
		return this.addr;
	}
	
	@Override
	public XID getID() {
		return this.addr.getObject();
	}
	
}
