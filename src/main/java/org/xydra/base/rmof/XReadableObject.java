package org.xydra.base.rmof;

import org.xydra.annotations.ReadOperation;
import org.xydra.base.XId;


public interface XReadableObject extends XStateReadableObject {
	
	/* More specific return type */
	@ReadOperation
	XReadableField getField(XId fieldId);
	
	/**
	 * Returns the current revision number of this object.
	 * 
	 * @return The current revision number of this object.
	 * @throws IllegalStateException if this object has already been removed
	 */
	@ReadOperation
	long getRevisionNumber();
}
