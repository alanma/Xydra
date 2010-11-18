package org.xydra.store.base;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Set;

import org.xydra.core.XX;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XID;
import org.xydra.core.model.XWritableObject;


/**
 * A simple data container.
 * 
 * @author voelkel
 */
public class SimpleObject implements XWritableObject, Serializable {
	
	private static final long serialVersionUID = 5593443685935758227L;
	protected XAddress address;
	protected long revisionNumber;
	protected Set<XID> fieldIds;
	
	public SimpleObject(XAddress address, long revisionNumber, Set<XID> fieldIds) {
		super();
		this.address = address;
		this.revisionNumber = revisionNumber;
		this.fieldIds = fieldIds;
	}
	
	@Override
	public XAddress getAddress() {
		return this.address;
	}
	
	@Override
	public XID getID() {
		return this.address.getField();
	}
	
	@Override
	public long getRevisionNumber() {
		return this.revisionNumber;
	}
	
	@Override
	public SimpleField createField(XID fieldId) {
		this.fieldIds.add(fieldId);
		return getField(fieldId);
	}
	
	@Override
	public boolean removeField(XID fieldId) {
		return this.fieldIds.remove(fieldId);
	}
	
	@Override
	public SimpleField getField(XID fieldId) {
		if(hasField(fieldId)) {
			return new SimpleField(XX.resolveField(this.getAddress(), fieldId),
			        SimpleConstants.REVISION_NUMBER_UNDEFINED, null);
		} else {
			return null;
		}
	}
	
	@Override
	public boolean hasField(XID fieldId) {
		return this.fieldIds.contains(fieldId);
	}
	
	@Override
	public boolean isEmpty() {
		return this.fieldIds.isEmpty();
	}
	
	@Override
	public Iterator<XID> iterator() {
		return this.fieldIds.iterator();
	}
	
}
