package org.xydra.base.rmof.impl.memory;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.XType;
import org.xydra.base.XX;
import org.xydra.base.change.XCommand;
import org.xydra.base.rmof.XWritableObject;
import org.xydra.base.rmof.XRevWritableField;
import org.xydra.base.rmof.XRevWritableObject;


/**
 * A simple data container for {@link XWritableObject}.
 * 
 * Minimal memory footprint, can be used as data transfer object.
 * 
 * @author voelkel
 */
public class SimpleObject implements Serializable, XRevWritableObject {
	
	private static final long serialVersionUID = 5593443685935758227L;
	
	private final XAddress address;
	private final Map<XID,SimpleField> fields;
	private long revisionNumber;
	
	public SimpleObject(XAddress address) {
		this(address, XCommand.NEW);
	}
	
	public SimpleObject(XAddress address, long revisionNumber) {
		assert address.getAddressedType() == XType.XOBJECT;
		this.address = address;
		this.revisionNumber = revisionNumber;
		this.fields = new HashMap<XID,SimpleField>(2);
	}
	
	@Override
	public void addField(SimpleField field) {
		this.fields.put(field.getID(), field);
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
	public XAddress getAddress() {
		return this.address;
	}
	
	@Override
	public XRevWritableField getField(XID fieldId) {
		return this.fields.get(fieldId);
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
	public boolean removeField(XID fieldId) {
		XRevWritableField oldField = this.fields.remove(fieldId);
		return oldField != null;
	}
	
	@Override
	public void setRevisionNumber(long rev) {
		this.revisionNumber = rev;
	}
	
}
