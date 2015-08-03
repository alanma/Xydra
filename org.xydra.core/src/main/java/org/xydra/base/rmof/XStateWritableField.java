package org.xydra.base.rmof;

import org.xydra.annotations.ModificationOperation;
import org.xydra.base.value.XValue;


public interface XStateWritableField extends XStateReadableField {

	/**
	 * Sets the {@link XValue} of this field to the given value.
	 *
	 * Passing "null" as the 'value' arguments implies an remove operation (will
	 * remove the current {@link XValue})
	 *
	 * @param value The new {@link XValue} @CanBeNull
	 *
	 * @return true, if this operation actually changed the current
	 *         {@link XValue} of this field, false otherwise
	 */
	@ModificationOperation
	boolean setValue(XValue value);

}
