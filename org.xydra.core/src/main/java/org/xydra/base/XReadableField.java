package org.xydra.base;

import org.xydra.annotations.ReadOperation;
import org.xydra.base.value.XValue;


/**
 * A basic field that at least supports read operations.
 * 
 * @author dscharrer
 * 
 */
public interface XReadableField extends IHasXAddress, IHasXID {
	
	/**
	 * Returns the current {@link XValue} of this XBaseField or null if its
	 * {@link XValue} is not set
	 * 
	 * @return the current {@link XValue} of this XBaseField or null if its
	 *         {@link XValue} is not set
	 */
	@ReadOperation
	XValue getValue();
	
	/**
	 * Gets the current revision number of this XBaseField
	 * 
	 * @return The current revision number of this XBaseField
	 */
	@ReadOperation
	long getRevisionNumber();
	
	/**
	 * Returns true, if the {@link XValue} of this XBaseField is not set (it
	 * equals null)
	 * 
	 * @return true, if the {@link XValue} of this XBaseFieldfield is not set
	 *         (it equals null)
	 */
	@ReadOperation
	boolean isEmpty();
	
}
