package org.xydra.core.value;

import java.util.Set;


/**
 * An XValue for storing a set of values. The stored values do not have a
 * specific order, but access to the contains method is fast.
 * 
 * @author dscharrer
 * 
 * @param <E> The type of value which is to be stored.
 */
public interface XSetValue<E> extends XCollectionValue<E> {
	
	/**
	 * @return a {@link Set} containing values in this {@link XSetValue} -
	 *         changes to the {@link Set} are NOT reflected in this value
	 */
	public Set<E> toSet();
	
}
