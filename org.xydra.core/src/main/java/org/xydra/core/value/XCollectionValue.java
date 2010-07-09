package org.xydra.core.value;

/**
 * An XValue for storing a collections of values.
 * 
 * @author dscharrer
 * 
 * @param <E> The type of value which is to be stored.
 */
public interface XCollectionValue<E> extends XValue, Iterable<E> {
	
	/**
	 * Checks whether this XCollectionValue contains the given element or not
	 * 
	 * @return true, if this XCollectionValue contains the given element, false
	 *         otherwise.
	 */
	boolean contains(E elem);
	
	/**
	 * @return true, if this {@link XCollectionValue} doesn't have any entries.
	 */
	boolean isEmpty();
	
	/**
	 * @return the number of entries in this {@link XCollectionValue}
	 */
	int size();
	
	/**
	 * @return the contents of this {@link XListValue} as an array.
	 */
	E[] toArray();
	
}
