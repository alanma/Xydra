package org.xydra.core.model;

import org.xydra.annotations.ReadOperation;
import org.xydra.core.value.XValue;



/**
 * A basic {@link XField} that at least supports read operations.
 * 
 * @author dscharrer
 * 
 */
public interface XBaseField extends IHasXAddress, IHasXID {
	
	/**
	 * @return the current value
	 */
	@ReadOperation
	XValue getValue();
	
	/**
	 * Gets the current revision number of this {@link XBaseField}.
	 * 
	 * @return The current revision number of this {@link XBaseField}.
	 */
	@ReadOperation
	long getRevisionNumber();
	
	/**
	 * Returns true, if the value of this field equals null
	 * 
	 * @return true, if the value of this field equals null
	 */
	@ReadOperation
	boolean isEmpty();
	
}
