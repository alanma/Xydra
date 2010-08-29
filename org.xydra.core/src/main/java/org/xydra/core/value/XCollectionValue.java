package org.xydra.core.value;

/**
 * An {@link XValue} for storing a collections of values.
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
	 * Checks whether this XCollectionValue has entries or not.
	 * 
	 * @return true, if this {@link XCollectionValue} doesn't have any entries.
	 */
	boolean isEmpty();
	
	/**
	 * Returns the number of entries in this XCollectionValue
	 * 
	 * @return the number of entries in this XCollectionValue
	 */
	int size();
	
	/**
	 * Returns the contents of this XCollectionValue as an array.
	 * 
	 * Note: Changes to the returned array will not affect the XCollectionValue
	 * itself.
	 * 
	 * @return the contents of this {@link XListValue} as an array.
	 */
	E[] toArray();
	
	/**
	 * Create a new {@link XCollectionValue} contains all entries from this
	 * value as well as the specified entry. This value is not modified.
	 */
	XCollectionValue<E> add(E entry);
	
	/**
	 * Create a new {@link XCollectionValue} contains all entries from this
	 * value except the specified entry. This value is not modified.
	 */
	XCollectionValue<E> remove(E entry);
	
}
