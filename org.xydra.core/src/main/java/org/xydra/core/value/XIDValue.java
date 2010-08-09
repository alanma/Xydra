package org.xydra.core.value;

import org.xydra.core.model.XID;


/**
 * An {@link XValue} for storing an {@link XID}.
 * 
 * @author voelkel
 * 
 *         TODO consider merging XIDValue and XID into XID. (let XID extend
 *         XValue).
 * 
 */
public interface XIDValue extends XValue {
	
	/**
	 * Returns the stored {@link XID} value.
	 * 
	 * @return The stored {@link XID} value.
	 */
	XID contents();
	
}
