package org.xydra.index;

/**
 * Generic super-interface for all kinds of indexes.
 *
 * @author xamde
 */
public interface IIndex  {

	/**
	 * Delete all indexed data. Afterwards, the index {@link #isEmpty()}.
	 */
	void clear();

	/**
	 * @return true iff index contains no data.
	 */
	boolean isEmpty();

}
