package org.xydra.core.model.delta;

import java.util.Iterator;

import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.rmof.XReadableField;
import org.xydra.base.rmof.XReadableObject;
import org.xydra.index.iterator.SingleValueIterator;


/**
 * A fake {@link XReadableObject} that holds exactly one {@link XReadableField}.
 * 
 * @author dscharrer
 * 
 */
public class BaseObjectWithOneField implements XReadableObject {
	
	private final XAddress address;
	private final XReadableField field;
	
	public BaseObjectWithOneField(XAddress addr, XReadableField field) {
		this.address = addr;
		this.field = field;
	}
	
	public XAddress getAddress() {
		return this.address;
	}
	
	public XReadableField getField(XID fieldId) {
		if(!this.field.getID().equals(fieldId)) {
			return null;
		}
		return this.field;
	}
	
	public XID getID() {
		return this.address.getObject();
	}
	
	public long getRevisionNumber() {
		throw new IllegalArgumentException("object needed");
	}
	
	public boolean hasField(XID fieldId) {
		return this.field.getID().equals(fieldId);
	}
	
	public boolean isEmpty() {
		return false;
	}
	
	public Iterator<XID> iterator() {
		return new SingleValueIterator<XID>(this.field.getID());
	}
	
}
