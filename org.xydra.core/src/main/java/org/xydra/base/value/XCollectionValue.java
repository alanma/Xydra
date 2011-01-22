package org.xydra.base.value;

/**
 * An {@link XValue} for storing a collections of values.
 * 
 * @author dscharrer
 * 
 * @param <E> The type of value which is to be stored.
 */
public interface XCollectionValue<E> extends XValue, Iterable<E> {
	
	/**
	 * Create a new {@link XCollectionValue} containing all entries from this
	 * value as well as the specified entry. This value is not modified.
	 * 
	 * @param entry The new entry.
	 * @return a new {@link XCollectionValue} containing all entries from this
	 *         value as well as the specified entry.
	 */
	XCollectionValue<E> add(E entry);
	
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
	 * Create a new {@link XCollectionValue} containing all entries from this
	 * value except the specified entry. This value is not modified.
	 * 
	 * @param entry The entry which is to be removed
	 * @return a new {@link XCollectionValue} containing all entries from this
	 *         value except the given entry
	 */
	XCollectionValue<E> remove(E entry);
	
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
	
}
