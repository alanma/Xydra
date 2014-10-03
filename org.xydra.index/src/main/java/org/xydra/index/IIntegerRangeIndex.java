package org.xydra.index;

import java.util.Iterator;
import java.util.Map.Entry;

/**
 * A set of special integer ranges: Ranges cannot overlap each other; adjacent
 * ranges are automatically merged.
 * 
 * Can be used for representing, e.g. character classes with Unicode code points
 * in regular expressions
 * 
 * @author xamde
 */
public interface IIntegerRangeIndex extends IIndex {

	/**
	 * @param i
	 * @return true iff i is inside one of the indexed ranges
	 */
	boolean isInInterval(int i);

	/**
	 * Index a new range. Start == end is legal.
	 * 
	 * Invariant: Ranges don't overlap each other. Each integer belongs to only
	 * one range.
	 * 
	 * @param start
	 *            inclusive, must be <= end
	 * @param end
	 *            inclusive, must be >= start
	 */
	void index(int start, int end);

	/**
	 * Dump content to logger at info-level
	 */
	void dump();

	/**
	 * @return the indexed ranges sorted by their first component; each entry is
	 *         [start,end], both inclusive
	 */
	Iterator<Entry<Integer, Integer>> rangesIterator();

	/**
	 * Removes a given range of integers. This does not delete the range-object
	 * (they don't exist) as such, but adapts existing ranges (and deletes maybe
	 * some), so that afterwards for all integers in range [start, end]
	 * {@link #rangesIterator()} returns false.
	 * 
	 * @param start
	 *            inclusive
	 * @param end
	 *            inclusive
	 */
	void deIndex(int start, int end);

}
