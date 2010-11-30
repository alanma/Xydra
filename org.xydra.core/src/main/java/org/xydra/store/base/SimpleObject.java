package org.xydra.store.base;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.xydra.core.change.XCommand;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XID;
import org.xydra.core.model.XWritableObject;


/**
 * A simple data container for {@link XWritableObject}.
 * 
 * @author voelkel
 */
public class SimpleObject implements XWritableObject, Serializable {
	
	private static final long serialVersionUID = 5593443685935758227L;
	
	private final XAddress address;
	private long revisionNumber;
	private final Map<XID,SimpleField> fields;
	
	public SimpleObject(XAddress address) {
		super();
		this.address = address;
		this.revisionNumber = XCommand.NEW;
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
		SimpleField newField = new SimpleField(getAddress());
		this.fields.put(fieldId, newField);
		return newField;
	}
	
	@Override
	public boolean removeField(XID fieldId) {
		SimpleField oldField = this.fields.remove(fieldId);
		return oldField != null;
	}
	
	@Override
	public SimpleField getField(XID fieldId) {
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
	
	/**
	 * @param rev the new revision number
	 */
	public void setRevisionNumber(long rev) {
		this.revisionNumber = rev;
	}
	
}
