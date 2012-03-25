package org.xydra.core.model.delta;

import java.util.Iterator;

import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.XType;
import org.xydra.base.rmof.XReadableModel;
import org.xydra.base.rmof.XReadableObject;
import org.xydra.index.iterator.SingleValueIterator;


/**
 * A fake {@link XReadableModel} that holds exactly one {@link XReadableObject}.
 * 
 * @author dscharrer
 * 
 */
public class ReadableModelWithOneObject implements XReadableModel {
	
	private final XAddress address;
	private final XReadableObject object;
	
	public ReadableModelWithOneObject(XReadableObject object) {
		this.address = object.getAddress().getParent();
		this.object = object;
	}
	
	@Override
    public XAddress getAddress() {
		return this.address;
	}
	
	@Override
    public XID getId() {
		return this.address.getModel();
	}
	
	@Override
    public XReadableObject getObject(XID objectId) {
		if(!this.object.getId().equals(objectId)) {
			return null;
		}
		return this.object;
	}
	
	@Override
    public long getRevisionNumber() {
		throw new IllegalArgumentException();
	}
	
	@Override
    public boolean hasObject(XID objectId) {
		return this.object.getId().equals(objectId);
	}
	
	@Override
    public boolean isEmpty() {
		return false;
	}
	
	@Override
    public Iterator<XID> iterator() {
		return new SingleValueIterator<XID>(this.object.getId());
	}
	
	@Override
	public XType getType() {
		return XType.XOBJECT;
	}
	
}
