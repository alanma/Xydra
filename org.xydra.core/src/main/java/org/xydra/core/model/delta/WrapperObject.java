package org.xydra.core.model.delta;

import java.util.Iterator;

import org.xydra.core.model.XAddress;
import org.xydra.core.model.XBaseField;
import org.xydra.core.model.XBaseObject;
import org.xydra.core.model.XID;
import org.xydra.index.iterator.SingleValueIterator;


/**
 * A fake {@link XBaseObject} that holds exactly one {@link XBaseField}.
 * 
 * @author dscharrer
 * 
 */
public class WrapperObject implements XBaseObject {
	
	private final XBaseField field;
	private final XAddress address;
	
	public WrapperObject(XAddress addr, XBaseField field) {
		this.address = addr;
		this.field = field;
	}
	
	public XBaseField getField(XID fieldId) {
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
