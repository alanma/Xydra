package org.xydra.core.value;

/**
 * An {@link XValue} for storing a list of Java Long values.
 * 
 * @author Kaidel
 * 
 */
public interface XLongListValue extends XNumberListValue<Long> {
	
	/**
	 * Returns the Long values as an array in the order they were added to the
	 * list.
	 * 
	 * Note: Changes to the returned array will not affect the XLongListValue.
	 * 
	 * @return an array containing the list of Long values in the order they
	 *         were added to the list
	 */
	long[] contents();
	
	/**
	 * Create a new {@link XLongListValue} contains all entries from this value
	 * as well as the specified entry. The order od existing entries is
	 * preserved and the new entry is added to the end of the list. This value
	 * is not modified.
	 */
	XLongListValue add(Long entry);
	
	/**
	 * Create a new {@link XLongListValue} contains all entries from this value
	 * as well as the specified entry. The order od existing entries is
	 * preserved and the new entry is added at the specified index This value is
	 * not modified.
	 * 
	 * @throws IndexOutOfBoundsException if index is negative or greater than
	 *             size()
	 */
	XLongListValue add(int index, Long entry);
	
	/**
	 * Create a new {@link XLongListValue} contains all entries from this value
	 * except the specified entry. If the entry is contained multiple times,
	 * only the first occurrence is removed. This value is not modified.
	 */
	XLongListValue remove(Long entry);
	
	/**
	 * Create a new {@link XLongListValue} contains all entries from this value
	 * except the entry at the specified index. This value is not modified.
	 * 
	 * @throws IndexOutOfBoundsException if index is negative or greater than or
	 *             equal to size()
	 */
	XLongListValue remove(int index);
	
}
