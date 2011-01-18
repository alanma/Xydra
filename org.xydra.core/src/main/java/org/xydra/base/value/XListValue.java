package org.xydra.base.value;

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
	 * Creates a new {@link XListValue} containing all entries from this value
	 * as well as the specified entry. The order of the already existing entries
	 * is preserved and the new entry is added to the end of the list. This
	 * value is not modified.
	 * 
	 * @param entry The new entry.
	 * @return a new {@link XListValue} containing all entries from this value
	 *         as well as the specified entry.
	 */
	XListValue<E> add(E entry);
	
	/**
	 * Creates a new {@link XListValue} containing all entries from this value
	 * as well as the specified entry. The order of the already existing entries
	 * is preserved and the new entry is added at the specified index This value
	 * is not modified.
	 * 
	 * @param index The index at which the new entry is to be added
	 * @param entry The new entry
	 * @return a new {@link XListValue} containing all entries from this value
	 *         as well as the specified entry at the given index
	 * @throws IndexOutOfBoundsException if index is negative or greater than
	 *             size()
	 */
	XListValue<E> add(int index, E entry);
	
	/**
	 * Creates a new {@link XListValue} containing all entries from this value
	 * except the specified entry. Only the first occurrence of the entry is
	 * removed, if this list contains it multiple times. This value is not
	 * modified.
	 * 
	 * @param entry The entry which is to be removed
	 * @return a new {@link XListValue} containing all entries from this value
	 *         except the given entry
	 */
	XListValue<E> remove(E entry);
	
	/**
	 * Creates a new {@link XListValue} containing all entries from this value
	 * except the entry at the specified index. This value is not modified.
	 * 
	 * @param index The index of the entry which is to be removed
	 * @return a new {@link XListValue} containing all entries from this value
	 *         except the entry at the given index.
	 * 
	 * @throws IndexOutOfBoundsException if index is negative or greater than or
	 *             equal to size()
	 */
	XListValue<E> remove(int index);
	
}
