package org.xydra.store.impl.gae.snapshot;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.xydra.core.model.XAddress;
import org.xydra.core.model.XBaseModel;
import org.xydra.core.model.XID;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XType;


/**
 * A snapshot (read-only data transfer object) of an {@link XModel}.
 * 
 * @author dscharrer
 * 
 */
@Deprecated
@SuppressWarnings("deprecation")
public class ModelSnapshot implements XBaseModel, Serializable {
	
	private static final long serialVersionUID = 2234329418396820686L;
	
	private final XAddress address;
	protected long revisionNumber;
	protected Map<XID,ObjectSnapshot> objects = new HashMap<XID,ObjectSnapshot>();
	
	protected ModelSnapshot(XAddress addr, long rev) {
		assert addr.getAddressedType() == XType.XMODEL;
		this.address = addr;
		this.revisionNumber = rev;
	}
	
	@Override
	public ObjectSnapshot getObject(XID objectId) {
		return this.objects.get(objectId);
	}
	
	@Override
	public long getRevisionNumber() {
		return this.revisionNumber;
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
		return this.address;
	}
	
	@Override
	public XID getID() {
		return this.address.getModel();
	}
	
}
