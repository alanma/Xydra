package org.xydra.index.impl;

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
public class FastTripleMap<K, L, M, E> extends AbstractFastTripleMap<K, L, M, E>implements IMapMapMapIndex<K, L, M, E> {

	@Override
	protected IMapMapMapIndex<K, L, M, E> createMapMapMapIndex_KLME() {
		return new MapMapMapIndex<K,L,M,E>();
	}

	@Override
	protected IMapMapMapIndex<L, M, K, E> createMapMapMapIndex_LMKE() {
		return new MapMapMapIndex<L,M,K,E>();
	}

	@Override
	protected IMapMapMapIndex<M, K, L, E> createMapMapMapIndex_MKLE() {
		return new MapMapMapIndex<M,K,L,E>();
	}

}
