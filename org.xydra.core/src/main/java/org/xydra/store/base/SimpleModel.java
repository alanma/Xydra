package org.xydra.store.base;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.xydra.core.XX;
import org.xydra.core.change.XCommand;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XID;
import org.xydra.core.model.XType;
import org.xydra.core.model.XWritableModel;


/**
 * A simple data container for {@link XWritableModel}.
 * 
 * @author voelkel
 */
public class SimpleModel implements XWritableModel, Serializable {
	
	private static final long serialVersionUID = 5593443685935758227L;
	private final XAddress address;
	private long revisionNumber;
	private final Map<XID,SimpleObject> objects;
	
	public SimpleModel(XAddress address) {
		assert address.getAddressedType() == XType.XMODEL;
		this.address = address;
		this.revisionNumber = XCommand.NEW;
		this.objects = new HashMap<XID,SimpleObject>();
	}
	
	public SimpleModel(XAddress address, long revisionNumber, Map<XID,SimpleObject> objects) {
		super();
		this.address = address;
		this.revisionNumber = revisionNumber;
		this.objects = objects;
	}
	
	@Override
	public XAddress getAddress() {
		return this.address;
	}
	
	@Override
	public XID getID() {
		return this.address.getModel();
	}
	
	@Override
	public long getRevisionNumber() {
		return this.revisionNumber;
	}
	
	@Override
	public SimpleObject createObject(XID objectId) {
		SimpleObject object = this.objects.get(objectId);
		if(object != null) {
			return object;
		}
		SimpleObject newObject = new SimpleObject(XX.resolveObject(this.address, objectId));
		this.objects.put(objectId, newObject);
		return newObject;
	}
	
	@Override
	public boolean removeObject(XID objectId) {
		SimpleObject oldObject = this.objects.remove(objectId);
		return oldObject != null;
	}
	
	@Override
	public SimpleObject getObject(XID objectId) {
		return this.objects.get(objectId);
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
	
	/**
	 * @param rev the new revision number
	 */
	public void setRevisionNumber(long rev) {
		this.revisionNumber = rev;
	}
	
}
