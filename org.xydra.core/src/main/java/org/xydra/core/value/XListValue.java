package org.xydra.core.value;

/**
 * An XValue for storing a list of values.
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
	 * @param elem
	 * @return The index of the first occurrence of the given element. Returns
	 *         -1 if this XListValue doesn't contain the given element.
	 */
	int indexOf(Object elem);
	
	/**
	 * Returns the index of the last occurrence of the given element in this
	 * XListValue
	 * 
	 * @param elem
	 * @return The index of the last occurrence of the given element. Returns -1
	 *         if this XListValue doesn't contain the given element.
	 */
	int lastIndexOf(Object elem);
	
	/**
	 * @return the element at the given index.
	 * @throws IndexOutOfBoundsException if the given index is less than zero or
	 *             greater than or equal to size()
	 */
	E get(int index);
	
}
