package org.xydra.store.base;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.XWritableField;
import org.xydra.base.XWritableObject;
import org.xydra.base.XType;
import org.xydra.base.XHalfWritableObject;
import org.xydra.core.XX;
import org.xydra.core.change.XCommand;


/**
 * A simple data container for {@link XHalfWritableObject}.
 * 
 * Minimal memory footprint, can be used as data transfer object.
 * 
 * @author voelkel
 */
public class SimpleObject implements Serializable, XWritableObject {
	
	private static final long serialVersionUID = 5593443685935758227L;
	
	private final XAddress address;
	private long revisionNumber;
	private final Map<XID,SimpleField> fields;
	
	@Override
	public void addField(SimpleField field) {
		this.fields.put(field.getID(), field);
	}
	
	public SimpleObject(XAddress address) {
		this(address, XCommand.NEW);
	}
	
	public SimpleObject(XAddress address, long revisionNumber) {
		assert address.getAddressedType() == XType.XOBJECT;
		this.address = address;
		this.revisionNumber = revisionNumber;
		this.fields = new HashMap<XID,SimpleField>();
	}
	
	@Override
	public XAddress getAddress() {
		return this.address;
	}
	
	@Override
	public XID getID() {
		return this.address.getObject();
	}
	
	@Override
	public long getRevisionNumber() {
		return this.revisionNumber;
	}
	
	@Override
	public SimpleField createField(XID fieldId) {
		SimpleField field = this.fields.get(fieldId);
		if(field != null) {
			return field;
		}
		SimpleField newField = new SimpleField(XX.resolveField(this.address, fieldId));
		this.fields.put(fieldId, newField);
		return newField;
	}
	
	@Override
	public boolean removeField(XID fieldId) {
		XWritableField oldField = this.fields.remove(fieldId);
		return oldField != null;
	}
	
	@Override
	public XWritableField getField(XID fieldId) {
		return this.fields.get(fieldId);
	}
	
	@Override
	public boolean hasField(XID fieldId) {
		return this.fields.containsKey(fieldId);
	}
	
	@Override
	public boolean isEmpty() {
		return this.fields.isEmpty();
	}
	
	@Override
	public Iterator<XID> iterator() {
		return this.fields.keySet().iterator();
	}
	
	@Override
	public void setRevisionNumber(long rev) {
		this.revisionNumber = rev;
	}
	
}
