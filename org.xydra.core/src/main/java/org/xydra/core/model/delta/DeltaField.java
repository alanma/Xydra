package org.xydra.core.model.delta;

import org.xydra.core.model.XBaseField;
import org.xydra.core.value.XValue;


/**
 * A {@link XBaseField} that also allows direct changes.
 * 
 * @author dscharrer
 * 
 */
public interface DeltaField extends XBaseField {
	
	/**
	 * Set the value of this field to the one given.
	 */
	public void setValue(XValue value);
	
}
