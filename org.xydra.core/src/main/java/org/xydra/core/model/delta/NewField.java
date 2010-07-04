package org.xydra.core.model.delta;

import org.xydra.core.change.XCommand;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XID;
import org.xydra.core.value.XValue;


/**
 * A completely new {@link DeltaField} that isn't based on any existing field.
 * 
 * @author dscharrer
 * 
 */
public class NewField implements DeltaField {
	
	private XValue value;
	private final XAddress address;
	
	public NewField(XAddress address) {
		this.address = address;
	}
	
	public long getRevisionNumber() {
		return XCommand.NEW;
	}
	
	public XValue getValue() {
		return this.value;
	}
	
	public void setValue(XValue value) {
		this.value = value;
	}
	
	public XID getID() {
		return this.address.getField();
	}
	
	public XAddress getAddress() {
		return this.address;
	}
	
	public boolean isEmpty() {
		return this.value == null;
	}
	
	/**
	 * Get the number of {@link XCommand XCommands} needed to create this field.
	 */
	public int countChanges() {
		return isEmpty() ? 1 : 2;
	}
	
}
