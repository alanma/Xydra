package org.xydra.core.model.delta;

import org.xydra.core.model.XAddress;
import org.xydra.core.model.XBaseField;
import org.xydra.core.model.XID;
import org.xydra.core.model.XWritableField;
import org.xydra.core.value.XValue;
import org.xydra.index.XI;


/**
 * An {@link XBaseField}/{@link DeltaField} that represents changes to an
 * {@link XBaseField}.
 * 
 * An {@link XBaseField} is passed as an argument of the constructor. This
 * ChangedField will than basically represent the given {@link XBaseField} and
 * allow changes on its {@link XValue}. The changes do not happen directly on
 * the passed {@link XBaseField} but rather on a sort of copy that emulates the
 * passed {@link XBaseField}. A ChangedField provides methods to compare the
 * current state to the state the passed {@link XBaseField} was in at creation
 * time.
 * 
 * @author dscharrer
 * 
 */
public class ChangedField implements XWritableField {
	
	private XValue value;
	private final XBaseField base;
	boolean changed = false;
	
	/**
	 * Wrap an {@link XBaseField} to record a set of changes made. Multiple
	 * changes will be combined as much as possible such that a minimal set of
	 * changes remains.
	 * 
	 * Note that this is a very lightweight wrapper intended for a short
	 * lifetime. As a consequence, the wrapped {@link XBaseField} is not copied
	 * and changes to it (as opposed to this {@link ChangedField}) may result in
	 * undefined behavior of the {@link ChangedField}.
	 * 
	 * @param base The {@link XBaseField} this ChangedField will encapsulate and
	 *            represent
	 */
	public ChangedField(XBaseField base) {
		this.value = base.getValue();
		this.base = base;
	}
	
	public boolean setValue(XValue value) {
		// reset changed flag if the value is reset to the base field's value
		boolean changes = !XI.equals(getValue(), value);
		this.changed = !XI.equals(value, this.base.getValue());
		this.value = value;
		return changes;
	}
	
	public XID getID() {
		return this.base.getID();
	}
	
	/**
	 * Return the revision number of the wrapped {@link XBaseField}. The
	 * revision number does not increase with changes to this
	 * {@link ChangedField}.
	 * 
	 * @return the revision number of the original {@link XBaseField}
	 */
	public long getRevisionNumber() {
		return this.base.getRevisionNumber();
	}
	
	public XValue getValue() {
		return this.value;
	}
	
	/**
	 * @return The {@link XValue} the encapsulated {@link XBaseField} had at the
	 *         creation time of this ChangedField.
	 */
	public XValue getOldValue() {
		return this.base.getValue();
	}
	
	/**
	 * @return true, if the current {@link XValue} of this ChangedField is
	 *         different from the value of the underlying {@link XBaseField}
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
	
}
