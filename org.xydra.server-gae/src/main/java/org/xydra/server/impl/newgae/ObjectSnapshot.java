package org.xydra.server.impl.newgae;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.xydra.core.model.XAddress;
import org.xydra.core.model.XBaseObject;
import org.xydra.core.model.XID;
import org.xydra.core.model.XObject;
import org.xydra.core.model.XType;


/**
 * A snapshot of an {@link XObject}.
 * 
 * @author dscharrer
 * 
 */
public class ObjectSnapshot implements XBaseObject {
	
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
