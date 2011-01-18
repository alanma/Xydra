package org.xydra.store.base;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.XWritableModel;
import org.xydra.base.XWritableObject;
import org.xydra.base.XType;
import org.xydra.base.XHalfWritableModel;
import org.xydra.core.XX;
import org.xydra.core.change.XCommand;


/**
 * A simple data container for {@link XHalfWritableModel}.
 * 
 * Minimal memory footprint, can be used as data transfer object.
 * 
 * @author voelkel
 */
public class SimpleModel implements Serializable, XWritableModel {
	
	private static final long serialVersionUID = 5593443685935758227L;
	
	private final XAddress address;
	private long revisionNumber;
	private final Map<XID,XWritableObject> objects;
	
	public SimpleModel(XAddress address) {
		this(address, XCommand.NEW);
	}
	
	public SimpleModel(XAddress address, long revisionNumber) {
		assert address.getAddressedType() == XType.XMODEL;
		this.address = address;
		this.revisionNumber = revisionNumber;
		this.objects = new HashMap<XID,XWritableObject>();
	}
	
	public SimpleModel(XAddress address, long revisionNumber, Map<XID,XWritableObject> objects) {
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
	public XWritableObject createObject(XID objectId) {
		XWritableObject object = this.objects.get(objectId);
		if(object != null) {
			return object;
		}
		XWritableObject newObject = new SimpleObject(XX.resolveObject(this.address, objectId));
		this.objects.put(objectId, newObject);
		return newObject;
	}
	
	@Override
	public boolean removeObject(XID objectId) {
		XWritableObject oldObject = this.objects.remove(objectId);
		return oldObject != null;
	}
	
	@Override
	public XWritableObject getObject(XID objectId) {
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
	
	@Override
	public void setRevisionNumber(long rev) {
		this.revisionNumber = rev;
	}
	
	@Override
	public void addObject(XWritableObject object) {
		this.objects.put(object.getID(), object);
	}
	
}
