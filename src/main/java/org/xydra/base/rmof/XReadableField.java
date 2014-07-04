package org.xydra.base.rmof;

import org.xydra.annotations.ReadOperation;


public interface XReadableField extends XStateReadableField {
	
	/**
	 * Gets the current revision number of this XBaseField
	 * 
	 * @return The current revision number of this XBaseField
	 */
	@ReadOperation
	long getRevisionNumber();
	
}
