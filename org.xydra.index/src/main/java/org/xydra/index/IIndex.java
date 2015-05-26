package org.xydra.index;

import java.io.Serializable;

/**
 * Generic super-interface for all kinds of indexes.
 * 
 * @author xamde
 */
public interface IIndex extends Serializable {

	/**
	 * Delete all indexed data. Afterwards, the index {@link #isEmpty()}.
	 */
	void clear();

	/**
	 * @return true iff index contains no data.
	 */
	boolean isEmpty();

}
