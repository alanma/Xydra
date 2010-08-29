package org.xydra.core.value;

/**
 * An {@link XValue} for storing a list of Java Number values.
 * 
 * @author Kaidel
 * 
 */
public interface XNumberListValue<T extends Number> extends XListValue<T> {
	
	/**
	 * Returns the Number values as an array in the order they were added to the
	 * list.
	 * 
	 * Note: Changes to the returned array will not affect the XNumberListValue.
	 * 
	 * @return an array containing the list of Number values in the order they
	 *         were added to the list
	 */
	Number[] toNumberArray();
	
	/**
	 * Create a new {@link XNumberListValue} contains all entries from this
	 * value as well as the specified entry. The order od existing entries is
	 * preserved and the new entry is added to the end of the list. This value
	 * is not modified.
	 */
	XNumberListValue<T> add(T entry);
	
	/**
	 * Create a new {@link XNumberListValue} contains all entries from this
	 * value as well as the specified entry. The order od existing entries is
	 * preserved and the new entry is added at the specified index This value is
	 * not modified.
	 * 
	 * @throws IndexOutOfBoundsException if index is negative or greater than
	 *             size()
	 */
	XNumberListValue<T> add(int index, T entry);
	
	/**
	 * Create a new {@link XNumberListValue} contains all entries from this
	 * value except the specified entry. If the entry is contained multiple
	 * times, only the first occurrence is removed. This value is not modified.
	 */
	XNumberListValue<T> remove(T entry);
	
	/**
	 * Create a new {@link XNumberListValue} contains all entries from this
	 * value except the entry at the specified index. This value is not
	 * modified.
	 * 
	 * @throws IndexOutOfBoundsException if index is negative or greater than or
	 *             equal to size()
	 */
	XNumberListValue<T> remove(int index);
	
}
