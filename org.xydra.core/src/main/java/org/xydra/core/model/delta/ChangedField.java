package org.xydra.core.model.delta;

import org.xydra.core.XX;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XBaseField;
import org.xydra.core.model.XID;
import org.xydra.core.value.XValue;


/**
 * An {@link XBaseField} that represents changes to a field.
 * 
 * @author dscharrer
 * 
 */
public class ChangedField implements DeltaField {
	
	private XValue value;
	private final XBaseField base;
	boolean changed = false;
	
	public ChangedField(XBaseField base) {
		this.value = base.getValue();
		this.base = base;
	}
	
	public void setValue(XValue value) {
		this.changed = !XX.equals(value, this.base.getValue());
		this.value = value;
	}
	
	public XID getID() {
		return this.base.getID();
	}
	
	/**
	 * The current revision number of the original field.
	 */
	public long getRevisionNumber() {
		return this.base.getRevisionNumber();
	}
	
	public XValue getValue() {
		return this.value;
	}
	
	/**
	 * @return The value of the original field.
	 */
	public XValue getOldValue() {
		return this.base.getValue();
	}
	
	/**
	 * This field is changed exactly when the value was set to one different
	 * from the original field.
	 * 
	 * @return true if the field represents any changes
	 */
	public boolean isChanged() {
		return this.changed;
	}
	
	public XAddress getAddress() {
		return this.base.getAddress();
	}
	
	public boolean isEmpty() {
		return this.value == null;
	}
	
	public int countChanges(int i) {
		return isChanged() ? 1 : 0;
	}
	
}
