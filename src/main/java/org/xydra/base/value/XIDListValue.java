package org.xydra.base.value;

import org.xydra.base.XID;


/**
 * An {@link XValue} for storing a list of {@link XID XIDs}.
 * 
 * @author Kaidel
 * 
 */
public interface XIDListValue extends XListValue<XID> {
	
	/**
	 * Creates a new {@link XIDListValue} containing all entries from this value
	 * as well as the specified entry. The order of the already existing entries
	 * is preserved and the new entry is added at the specified index This value
	 * is not modified.
	 * 
	 * @param index The index at which the new entry is to be added
	 * @param entry The new entry
	 * @return a new {@link XIDListValue} containing all entries from this value
	 *         as well as the specified entry at the given index
	 * @throws IndexOutOfBoundsException if index is negative or greater than
	 *             size()
	 */
	@Override
    XIDListValue add(int index, XID entry);
	
	/**
	 * Creates a new {@link XIDListValue} containing all entries from this value
	 * as well as the specified entry. The order of the already existing entries
	 * is preserved and the new entry is added to the end of the list. This
	 * value is not modified.
	 * 
	 * @param entry The new entry.
	 * @return a new {@link XIDListValue} containing all entries from this value
	 *         as well as the specified entry.
	 */
	@Override
    XIDListValue add(XID entry);
	
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
	 * Creates a new {@link XIDListValue} containing all entries from this value
	 * except the entry at the specified index. This value is not modified.
	 * 
	 * @param index The index of the entry which is to be removed
	 * @return a new {@link XIDListValue} containing all entries from this value
	 *         except the entry at the given index.
	 * 
	 * @throws IndexOutOfBoundsException if index is negative or greater than or
	 *             equal to size()
	 */
	@Override
    XIDListValue remove(int index);
	
	/**
	 * Creates a new {@link XIDListValue} containing all entries from this value
	 * except the specified entry. Only the first occurrence of the entry is
	 * removed, if this list contains it multiple times. This value is not
	 * modified.
	 * 
	 * @param entry The entry which is to be removed
	 * @return a new {@link XIDListValue} containing all entries from this value
	 *         except the given entry
	 */
	@Override
    XIDListValue remove(XID entry);
	
}