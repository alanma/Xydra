package org.xydra.index.impl;

import java.io.Serializable;

import org.xydra.index.ITripleIndex;

/**
 * An implementation that uses no indexes. Slow, but small memory footprint.
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
public class SmallSerializableTripleIndex<K extends Serializable, L extends Serializable, M extends Serializable>
extends AbstractSmallTripleIndex<K, L, M, SerializableMapMapSetIndex<K,L,M>>
implements ITripleIndex<K, L, M>, Serializable {

	@Override
	protected SerializableMapMapSetIndex<K, L, M> createMMSI() {
		return new SerializableMapMapSetIndex<K, L, M>(new FastEntrySetFactory<M>());
	}

}
