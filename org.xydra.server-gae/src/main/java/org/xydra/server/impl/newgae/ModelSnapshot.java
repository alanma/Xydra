package org.xydra.server.impl.newgae;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.xydra.core.model.XAddress;
import org.xydra.core.model.XBaseModel;
import org.xydra.core.model.XID;
import org.xydra.core.model.XObject;
import org.xydra.core.model.XType;


/**
 * A snapshot of an {@link XObject}.
 * 
 * @author dscharrer
 * 
 */
public class ModelSnapshot implements XBaseModel {
	
	private final XAddress addr;
	protected long rev;
	protected Map<XID,ObjectSnapshot> objects = new HashMap<XID,ObjectSnapshot>();
	
	protected ModelSnapshot(XAddress addr, long rev) {
		assert addr.getAddressedType() == XType.XMODEL;
		this.addr = addr;
		this.rev = rev;
	}
	
	@Override
	public ObjectSnapshot getObject(XID objectId) {
		return this.objects.get(objectId);
	}
	
	@Override
	public long getRevisionNumber() {
		return this.rev;
	}
	
	@Override
	public boolean hasObject(XID objectId) {
		return this.objects.containsKey(objectId);
	}
	
	@Override
	public boolean isEmpty() {
		return this.objects.isEmpty();
	}
	
	@Override
	public Iterator<XID> iterator() {
		return this.objects.keySet().iterator();
	}
	
	@Override
	public XAddress getAddress() {
		return this.addr;
	}
	
	@Override
	public XID getID() {
		return this.addr.getModel();
	}
	
}
