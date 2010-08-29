package org.xydra.core.value;

/**
 * An {@link XValue} for storing a list of values.
 * 
 * @author Kaidel
 * 
 * @param <E> The type of value which is to be stored.
 */
public interface XListValue<E> extends XCollectionValue<E> {
	
	/**
	 * Returns the index of the first occurrence of the given element in this
	 * XListValue
	 * 
	 * @param elem The element which index of its first occurrence in this list
	 *            is to be returned
	 * @return The index of the first occurrence of the given element. Returns
	 *         -1 if this XListValue doesn't contain the given element.
	 */
	int indexOf(E elem);
	
	/**
	 * Returns the index of the last occurrence of the given element in this
	 * XListValue
	 * 
	 * @param elem The element which index of its last occurrence in this list
	 *            is to be returned
	 * @return The index of the last occurrence of the given element. Returns -1
	 *         if this XListValue doesn't contain the given element.
	 */
	int lastIndexOf(E elem);
	
	/**
	 * Returns the element at the given index.
	 * 
	 * @param index The index of the element which is to be returned
	 * @return the element at the given index.
	 * @throws IndexOutOfBoundsException if the given index is less than zero or
	 *             greater than or equal to size()
	 */
	E get(int index);
	
	/**
	 * Create a new {@link XCollectionValue} contains all entries from this
	 * value as well as the specified entry. The order od existing entries is
	 * preserved and the new entry is added to the end of the list. This value
	 * is not modified.
	 */
	XListValue<E> add(E entry);
	
	/**
	 * Create a new {@link XCollectionValue} contains all entries from this
	 * value as well as the specified entry. The order od existing entries is
	 * preserved and the new entry is added at the specified index This value is
	 * not modified.
	 * 
	 * @throws IndexOutOfBoundsException if index is negative or greater than
	 *             size()
	 */
	XListValue<E> add(int index, E entry);
	
	/**
	 * Create a new {@link XCollectionValue} contains all entries from this
	 * value except the specified entry. If the entry is contained multiple
	 * times, only the first occurrence is removed. This value is not modified.
	 */
	XListValue<E> remove(E entry);
	
	/**
	 * Create a new {@link XCollectionValue} contains all entries from this
	 * value except the entry at the specified index. This value is not
	 * modified.
	 * 
	 * @throws IndexOutOfBoundsException if index is negative or greater than or
	 *             equal to size()
	 */
	XListValue<E> remove(int index);
	
}
