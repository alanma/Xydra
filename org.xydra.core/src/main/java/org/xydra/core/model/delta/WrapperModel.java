package org.xydra.core.model.delta;

import java.util.Iterator;

import org.xydra.core.model.XAddress;
import org.xydra.core.model.XBaseModel;
import org.xydra.core.model.XBaseObject;
import org.xydra.core.model.XID;
import org.xydra.index.iterator.SingleValueIterator;


/**
 * A fake {@link XBaseModel} that holds exactly one object.
 * 
 * @author dscharrer
 * 
 */
public class WrapperModel implements XBaseModel {
	
	private final XBaseObject object;
	private final XAddress address;
	
	public WrapperModel(XBaseObject object) {
		this.address = object.getAddress().getParent();
		this.object = object;
	}
	
	public XBaseObject getObject(XID objectId) {
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
