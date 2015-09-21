package org.xydra.index.impl;

import java.io.Serializable;

import org.xydra.index.IMapMapMapIndex;

/**
 * An {@link IMapMapMapIndex} implementation that uses multiple internal indices to allow fast queries on all keys.
 *
 * The remove() method of iterators always works for all constraint combinations.
 *
 * @author dscharrer
 * @param <K> key1
 * @param <L> key2
 * @param <M> key3
 * @param <E> entry
 */
public class FastSerializableTripleMap<K extends Serializable, L extends Serializable, M extends Serializable, E extends Serializable>
		extends AbstractFastTripleMap<K, L, M, E>implements IMapMapMapIndex<K, L, M, E> {

	@Override
	protected IMapMapMapIndex<K, L, M, E> createMapMapMapIndex_KLME() {
		return new SerializableMapMapMapIndex<>();
	}

	@Override
	protected IMapMapMapIndex<L, M, K, E> createMapMapMapIndex_LMKE() {
		return new SerializableMapMapMapIndex<>();
	}

	@Override
	protected IMapMapMapIndex<M, K, L, E> createMapMapMapIndex_MKLE() {
		return new SerializableMapMapMapIndex<>();
	}

}
