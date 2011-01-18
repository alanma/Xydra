package org.xydra.core.model.delta;

import java.util.Iterator;

import org.xydra.base.XAddress;
import org.xydra.base.XReadableModel;
import org.xydra.base.XReadableObject;
import org.xydra.base.XID;
import org.xydra.index.iterator.SingleValueIterator;


/**
 * A fake {@link XReadableModel} that holds exactly one {@link XReadableObject}.
 * 
 * @author dscharrer
 * 
 */
public class BaseModelWithOneObject implements XReadableModel {
	
	private final XReadableObject object;
	private final XAddress address;
	
	public BaseModelWithOneObject(XReadableObject object) {
		this.address = object.getAddress().getParent();
		this.object = object;
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
	
	public XID getID() {
		return this.address.getModel();
	}
	
	public boolean hasObject(XID objectId) {
		return this.object.getID().equals(objectId);
	}
	
	public XAddress getAddress() {
		return this.address;
	}
	
	public Iterator<XID> iterator() {
		return new SingleValueIterator<XID>(this.object.getID());
	}
	
	public boolean isEmpty() {
		return false;
	}
	
}
