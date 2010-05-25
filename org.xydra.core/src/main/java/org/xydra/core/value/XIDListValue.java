package org.xydra.core.value;

import org.xydra.core.model.XID;


/**
 * An XValue for storing a list of XIDs.
 * 
 * @author Kaidel
 * 
 */

public interface XIDListValue extends XListValue<XID> {
	
	/**
	 * @return the list of XID values in order (changes to the returned array
	 *         won't affect the value)
	 */
	public XID[] contents();
	
}
