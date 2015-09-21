package org.xydra.index.impl;

import java.io.Serializable;

/**
 * An implementation that uses no indexes. Slow, but small memory footprint.
 *
 * See {@link SmallSerializableTripleIndex} for a {@link Serializable} version.
 *
 * State:
 * <pre>
 * s > p > o
 * </pre>
 *
 * @author voelkel
 *
 * @param <K> key type 1
 * @param <L> key type 2
 * @param <M> key type 3
 */
public class SmallTripleIndex<K, L, M> extends AbstractSmallTripleIndex<K, L, M, MapMapSetIndex<K, L, M>> {

	private final boolean fastEntrySets;

	/**
	 * @param fastEntrySets true = use fast entry sets; false = use small entry sets.
	 */
	public SmallTripleIndex(final boolean fastEntrySets) {
		this.fastEntrySets = fastEntrySets;
	}

	@Override
	protected MapMapSetIndex<K, L, M> createMMSI() {
		if (this.fastEntrySets) {
			return MapMapSetIndex.createWithFastSets();
		} else {
			return MapMapSetIndex.createWithSmallSets();
		}
	}

}
