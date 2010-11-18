package org.xydra.core.model;

import org.xydra.annotations.ModificationOperation;
import org.xydra.core.value.XValue;


/**
 * Allowing simple changes.
 * 
 * @author voelkel
 */
public interface XWritableField extends XBaseField {
	
	/**
	 * Sets the {@link XValue} of this field to the given value.
	 * 
	 * Passing "null" as the 'value' arguments implies an remove operation (will
	 * remove the current {@link XValue})
	 * 
	 * @param value The new {@link XValue}
	 * 
	 * @return true, if this operation actually changed the current
	 *         {@link XValue} of this field, false otherwise
	 */
	@ModificationOperation
	boolean setValue(XValue value);
	
}
