package org.xydra.index.query;

import java.io.Serializable;

/**
 * Inherits {@link #hashCode()}, {@link #equals(Object)} and {@link #toString()}
 *
 * @author xamde
 *
 * @param <K>
 * @param <E>
 */
public class KeyEntryTuple<K, E> extends Pair<K, E> implements HasEntry<E>, Serializable {

	public KeyEntryTuple(final K key, final E entry) {
		super(key, entry);
	}

	@Override
	public E getEntry() {
		return getSecond();
	}

	public K getKey() {
		return getFirst();
	}

}
