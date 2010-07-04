package org.xydra.core.model.delta;

import org.xydra.core.model.XBaseField;
import org.xydra.core.value.XValue;


/**
 * A subtype of {@link XBaseField} that also supports change operations.
 * 
 * @author dscharrer
 * 
 */
public interface DeltaField extends XBaseField {
	
	/**
	 * Sets the {@link XValue} of this field to the given value.
	 * 
	 * Passing "null" as the 'value' arguments implies an remove operation (will
	 * remove the current {@link XValue})
	 * 
	 * @param value The new {@link XValue}
	 */
	public void setValue(XValue value);
	
}
