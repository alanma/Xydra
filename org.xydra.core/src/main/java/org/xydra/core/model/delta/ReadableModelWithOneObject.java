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
	
	public XAddress getAddress() {
		return this.address;
	}
	
	public XID getID() {
		return this.address.getModel();
	}
	
	public XReadableObject getObject(XID objectId) {
		if(!this.object.getID().equals(objectId)) {
			return null;
		}
		return this.object;
	}
	
	public long getRevisionNumber() {
		throw new IllegalArgumentException();
	}
	
	public boolean hasObject(XID objectId) {
		return this.object.getID().equals(objectId);
	}
	
	public boolean isEmpty() {
		return false;
	}
	
	public Iterator<XID> iterator() {
		return new SingleValueIterator<XID>(this.object.getID());
	}
	
	@Override
	public XType getType() {
		return XType.XOBJECT;
	}
	
}
