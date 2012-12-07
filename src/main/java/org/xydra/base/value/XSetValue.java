package org.xydra.base.value;

import java.util.Set;


/**
 * An {@link XValue} for storing a set of values. The stored values do not have
 * a specific order, but access to the contains() method is fast.
 * 
 * @author dscharrer
 * 
 * @param <E> The type of value which is to be stored.
 */
public interface XSetValue<E> extends XCollectionValue<E> {
	
	/**
	 * Create a new {@link XSetValue} contains all entries from this value as
	 * well as the specified entry. This value is not modified.
	 */
	@Override
    XSetValue<E> add(E entry);
	
	/**
	 * Create a new {@link XSetValue} contains all entries from this value
	 * except the specified entry. This value is not modified.
	 */
	@Override
    XSetValue<E> remove(E entry);
	
	/**
	 * Returns a {@link Set} containing the values in this XSetValue.
	 * 
	 * Note: Changes to the returned {@link Set} will not affect the XSetValue.
	 * 
	 * @return a {@link Set} containing values in this {@link XSetValue} -
	 *         changes to the {@link Set} are NOT reflected in this value
	 */
	public Set<E> toSet();
	
}
