package org.xydra.core.value;

import org.xydra.core.model.XID;


/**
 * For references
 * 
 * An XValue for storing an XID.
 * 
 * @author voelkel
 * 
 *         TODO consider merging XIDValue and XID into XID. (let XID extend
 *         XValue).
 * 
 */
public interface XIDValue extends XValue {
	
	XID contents();
	
}
