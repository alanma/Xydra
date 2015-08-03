package org.xydra.index.iterator;

/**
 * A filter for iterators
 *
 * @author xamde
 * @param <E>
 */
public interface IFilter<E> {

	/**
	 * @param entry
	 * @CanBeNull
	 * @return true iff the entry matches the filter and should therefore be
	 *         included in the resulting iterator
	 */
	boolean matches(E entry);

}
