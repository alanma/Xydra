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
	 * Creates a new {@link XNumberListValue} containing all entries from this
	 * value as well as the specified entry. The order of the already existing
	 * entries is preserved and the new entry is added to the end of the list.
	 * This value is not modified.
	 * 
	 * @param entry The new entry.
	 * @return a new {@link XNumberListValue} containing all entries from this
	 *         value as well as the specified entry.
	 */
	XNumberListValue<T> add(Number entry);
	
	/**
	 * Creates a new {@link XNumberListValue} containing all entries from this
	 * value as well as the specified entry. The order of the already existing
	 * entries is preserved and the new entry is added at the specified index
	 * This value is not modified.
	 * 
	 * @param index The index at which the new entry is to be added
	 * @param entry The new entry
	 * @return a new {@link XNumberListValue} containing all entries from this
	 *         value as well as the specified entry at the given index
	 * @throws IndexOutOfBoundsException if index is negative or greater than
	 *             size()
	 */
	XNumberListValue<T> add(int index, Number entry);
	
	/**
	 * Creates a new {@link XNumberListValue} containing all entries from this
	 * value except the specified entry. Only the first occurrence of the entry
	 * is removed, if this list contains it multiple times. This value is not
	 * modified.
	 * 
	 * @param entry The entry which is to be removed
	 * @return a new {@link XNumberListValue} containing all entries from this
	 *         value except the given entry
	 */
	XNumberListValue<T> remove(Number entry);
	
	/**
	 * Creates a new {@link XNumberListValue} containing all entries from this
	 * value except the entry at the specified index. This value is not
	 * modified.
	 * 
	 * @param index The index of the entry which is to be removed
	 * @return a new {@link XNumberListValue} containing all entries from this
	 *         value except the entry at the given index.
	 * 
	 * @throws IndexOutOfBoundsException if index is negative or greater than or
	 *             equal to size()
	 */
	XNumberListValue<T> remove(int index);
}
