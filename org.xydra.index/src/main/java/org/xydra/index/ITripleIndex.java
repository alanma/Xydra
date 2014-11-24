package org.xydra.index;

import org.xydra.index.IMapMapSetIndex.IMapMapSetDiff;

/**
 * An index for triples of keys that supports queries on all three keys. Indexes
 * three keys (to boolean, either a key combination is there or not).
 * 
 * The same concept for two-tuples instead of three-tuples is called
 * {@link IPairIndex}.
 * 
 * @param <K> key type
 * @param <L> key type
 * @param <M> key type
 */
public interface ITripleIndex<K, L, M> extends IIndex, ITripleSink<K, L, M>, ITripleSource<K, L, M> {

	/**
	 * Remove the given triple from the index, if it was present
	 * 
	 * @param s
	 * @param p
	 * @param o
	 */
	void deIndex(K s, L p, M o);

	/**
	 * Add the given triple to the index
	 * 
	 * @param s
	 * @param p
	 * @param o
	 */
	@Override
	void index(K s, L p, M o);

	/**
	 * @param other
	 * @return the diff between this index and the other index
	 */
	IMapMapSetDiff<K, L, M> computeDiff(ITripleIndex<K, L, M> other);

}
