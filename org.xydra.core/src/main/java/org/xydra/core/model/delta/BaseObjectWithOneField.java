package org.xydra.core.model.delta;

import java.util.Iterator;

import org.xydra.base.XAddress;
import org.xydra.base.XReadableField;
import org.xydra.base.XReadableObject;
import org.xydra.base.XID;
import org.xydra.index.iterator.SingleValueIterator;


/**
 * A fake {@link XReadableObject} that holds exactly one {@link XReadableField}.
 * 
 * @author dscharrer
 * 
 */
public class BaseObjectWithOneField implements XReadableObject {
	
	private final XReadableField field;
	private final XAddress address;
	
	public BaseObjectWithOneField(XAddress addr, XReadableField field) {
		this.address = addr;
		this.field = field;
	}
	
	public XReadableField getField(XID fieldId) {
		if(!this.field.getID().equals(fieldId)) {
			return null;
		}
		return this.field;
	}
	
	public long getRevisionNumber() {
		throw new IllegalArgumentException("object needed");
	}
	
	public XID getID() {
		return this.address.getObject();
	}
	
	public boolean hasField(XID fieldId) {
		return this.field.getID().equals(fieldId);
	}
	
	public XAddress getAddress() {
		return this.address;
	}
	
	public Iterator<XID> iterator() {
		return new SingleValueIterator<XID>(this.field.getID());
	}
	
	public boolean isEmpty() {
		return false;
	}
	
}
