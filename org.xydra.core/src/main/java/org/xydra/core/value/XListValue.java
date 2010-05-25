package org.xydra.core.value;

/**
 * An XValue for storing a list of values.
 * 
 * @author Kaidel
 * 
 * @param <E> The type of value which is to be stored.
 */

public interface XListValue<E> extends XValue, Iterable<E> {
	
	/**
	 * Checks whether this XListValue contains the given element or not
	 * 
	 * @param elem
	 * @return true, if this XListValue contains the given element, false
	 *         otherwise.
	 */
	boolean contains(Object elem);
	
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
	
	boolean isEmpty();
	
	int size();
	
	E get(int index);
	
}
