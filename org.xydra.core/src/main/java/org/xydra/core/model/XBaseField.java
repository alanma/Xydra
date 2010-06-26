package org.xydra.core.model;

import org.xydra.annotations.ReadOperation;
import org.xydra.core.value.XValue;


/**
 * A basic field that at least supports read operations.
 * 
 * @author dscharrer
 * 
 */
public interface XBaseField extends IHasXAddress, IHasXID {
	
	/**
	 * @return the current {@link XValue} of this field
	 */
	@ReadOperation
	XValue getValue();
	
	/**
	 * Gets the current revision number of this field
	 * 
	 * @return The current revision number of this field
	 */
	@ReadOperation
	long getRevisionNumber();
	
	/**
	 * Returns true, if the {@link XValue} of this field equals null
	 * 
	 * @return true, if the {@link XValue} of this field equals null
	 */
	@ReadOperation
	boolean isEmpty();
	
}
