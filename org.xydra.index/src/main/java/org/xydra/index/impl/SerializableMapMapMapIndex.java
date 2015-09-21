package org.xydra.index.impl;

import java.io.Serializable;

import org.xydra.index.IMapMapIndex;
import org.xydra.index.IMapMapMapIndex;

/**
 * An implementation of {@link IMapMapMapIndex} using a IMapIndex of an IMapMapIndex. The IMapIndex is automatically
 * switched between a SmallMapIndex and a MapIndex as needed.
 *
 * The remove() method of iterators always works if c1 != * or (c2,c3) == (*,*). Otherwise it might throw
 * UnsupportedOperationException.
 *
 * @author dscharrer
 *
 * @param <K> key1 type
 * @param <L> key2 type
 * @param <M> key3 type
 * @param <E> entity type
 */
public class SerializableMapMapMapIndex<K extends Serializable, L extends Serializable, M extends Serializable, E extends Serializable>
		extends AbstractMapMapMapIndex<K, L, M, E>implements IMapMapMapIndex<K, L, M, E> {

	@Override
	protected IMapMapIndex<L, M, E> createMapMapIndex() {
		return new MapMapIndex<L, M, E>();
	}

}
