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
public interface ITripleIndex<K, L, M> extends IIndex, IRemovableTripleSink<K, L, M>,
		ITripleSource<K, L, M> {

	/**
	 * @param other
	 * @return the diff between this index and the other index
	 */
	IMapMapSetDiff<K, L, M> computeDiff(ITripleIndex<K, L, M> other);

}
