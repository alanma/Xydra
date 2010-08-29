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
	
	/**
	 * Create a new {@link XIDListValue} contains all entries from this value as
	 * well as the specified entry. The order od existing entries is preserved
	 * and the new entry is added to the end of the list. This value is not
	 * modified.
	 */
	XIDListValue add(XID entry);
	
	/**
	 * Create a new {@link XIDListValue} contains all entries from this value as
	 * well as the specified entry. The order od existing entries is preserved
	 * and the new entry is added at the specified index This value is not
	 * modified.
	 * 
	 * @throws IndexOutOfBoundsException if index is negative or greater than
	 *             size()
	 */
	XIDListValue add(int index, XID entry);
	
	/**
	 * Create a new {@link XIDListValue} contains all entries from this value
	 * except the specified entry. If the entry is contained multiple times,
	 * only the first occurrence is removed. This value is not modified.
	 */
	XIDListValue remove(XID entry);
	
	/**
	 * Create a new {@link XIDListValue} contains all entries from this value
	 * except the entry at the specified index. This value is not modified.
	 * 
	 * @throws IndexOutOfBoundsException if index is negative or greater than or
	 *             equal to size()
	 */
	XIDListValue remove(int index);
	
}
