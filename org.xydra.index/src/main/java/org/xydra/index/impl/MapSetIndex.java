package org.xydra.index.impl;

import java.io.Serializable;

import org.xydra.index.Factory;
import org.xydra.index.IEntrySet;
import org.xydra.index.IMapSetIndex;
import org.xydra.index.ISerializableMapSetIndex;

/**
 * Not {@link Serializable}. See {@link ISerializableMapSetIndex} / {@link SerializableMapSetIndex}.
 *
 * @author xamde
 * @param <K>
 * @param <E>
 */
public class MapSetIndex<K, E> extends AbstractMapSetIndex<K, E>implements IMapSetIndex<K, E> {

	public MapSetIndex(final Factory<IEntrySet<E>> entrySetFactory) {
		super(entrySetFactory);
	}

	public MapSetIndex(final Factory<IEntrySet<E>> entrySetFactory, final boolean concurrent) {
		super(entrySetFactory, concurrent);
	}

	/**
	 * @return an impl that uses more memory and is fast
	 */
	public static <K, E> MapSetIndex<K, E> createWithFastEntrySets() {
		return new MapSetIndex<K, E>(new FastEntrySetFactory<E>());
	}

	/**
	 * @return an impl that uses less memory and is slower
	 */
	public static <K, E> MapSetIndex<K, E> createWithSmallEntrySets() {
		return new MapSetIndex<K, E>(new SmallEntrySetFactory<E>());
	}

	/**
	 * @return an impl that uses less memory and is slower
	 */
	public static <K, E> MapSetIndex<K, E> createWithFastWeakEntrySets() {
		return new MapSetIndex<K, E>(new FastWeakEntrySetFactory<E>());
	}


}
