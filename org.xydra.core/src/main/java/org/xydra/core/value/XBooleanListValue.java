package org.xydra.core.value;

/**
 * An {@link XValue} for storing a list of Java Boolean values.
 * 
 * @author Kaidel
 * 
 */
public interface XBooleanListValue extends XListValue<Boolean> {
	
	/**
	 * Returns the boolean values as an array in the order they were added to
	 * the list.
	 * 
	 * Note: Changes to the returned array will not affect the
	 * XBooleanListValue.
	 * 
	 * @return an array containing the list of boolean values in the order they
	 *         were added to the list
	 */
	boolean[] contents();
	
	/**
	 * Create a new {@link XBooleanListValue} contains all entries from this
	 * value as well as the specified entry. The order od existing entries is
	 * preserved and the new entry is added to the end of the list. This value
	 * is not modified.
	 */
	XBooleanListValue add(Boolean entry);
	
	/**
	 * Create a new {@link XBooleanListValue} contains all entries from this
	 * value as well as the specified entry. The order od existing entries is
	 * preserved and the new entry is added at the specified index This value is
	 * not modified.
	 * 
	 * @throws IndexOutOfBoundsException if index is negative or greater than
	 *             size()
	 */
	XBooleanListValue add(int index, Boolean entry);
	
	/**
	 * Create a new {@link XBooleanListValue} contains all entries from this
	 * value except the specified entry. If the entry is contained multiple
	 * times, only the first occurrence is removed. This value is not modified.
	 */
	XBooleanListValue remove(Boolean entry);
	
	/**
	 * Create a new {@link XCollectionValue} contains all entries from this
	 * value except the entry at the specified index. This value is not
	 * modified.
	 * 
	 * @throws IndexOutOfBoundsException if index is negative or greater than or
	 *             equal to size()
	 */
	XBooleanListValue remove(int index);
	
}
