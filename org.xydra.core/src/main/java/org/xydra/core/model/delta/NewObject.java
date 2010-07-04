package org.xydra.core.model.delta;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.xydra.core.XX;
import org.xydra.core.change.XCommand;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XID;


/**
 * A completely new {@link DeltaObject} that isn't based on any existing object.
 * 
 * @author dscharrer
 * 
 */
public class NewObject implements DeltaObject {
	
	private Map<XID,NewField> fields = new HashMap<XID,NewField>();
	private final XAddress address;
	
	public NewObject(XAddress address) {
		this.address = address;
	}
	
	public void createField(XID fieldId) {
		XAddress fieldAddr = XX.resolveField(getAddress(), fieldId);
		this.fields.put(fieldId, new NewField(fieldAddr));
	}
	
	public void removeField(XID fieldId) {
		this.fields.remove(fieldId);
	}
	
	public DeltaField getField(XID fieldId) {
		return this.fields.get(fieldId);
	}
	
	public long getRevisionNumber() {
		return XCommand.NEW;
	}
	
	public XID getID() {
		return this.address.getObject();
	}
	
	public boolean hasField(XID fieldId) {
		return this.fields.containsKey(fieldId);
	}
	
	public XAddress getAddress() {
		return this.address;
	}
	
	public Iterator<XID> iterator() {
		return this.fields.keySet().iterator();
	}
	
	public boolean isEmpty() {
		return this.fields.isEmpty();
	}
	
	/**
	 * Get the number of {@link XCommand XCommands} needed to create this
	 * object.
	 */
	public int countChanges(int max) {
		int n = 1; // one to create the object
		if(n < max) {
			for(NewField field : this.fields.values()) {
				n += field.countChanges();
				if(n >= max) {
					break;
				}
			}
		}
		return n;
	}
	
}
