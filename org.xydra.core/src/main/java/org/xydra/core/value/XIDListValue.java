package org.xydra.core.value;

import org.xydra.core.model.XID;


/**
 * An {@link XValue} for storing a list of {@link XID XIDs}.
 * 
 * @author Kaidel
 * 
 */
public interface XIDListValue extends XListValue<XID> {
	
	/**
	 * Returns the {@link XID} values as an array in the order they were added
	 * to the list.
	 * 
	 * Note: Changes to the returned array will not affect the XIDListValue.
	 * 
	 * @return an array containing the list of {@link XID} values in the order
	 *         they were added to the list
	 */
	public XID[] contents();
	
}
