package org.xydra.index.impl;

import java.io.Serializable;

import org.xydra.index.Factory;
import org.xydra.index.IEntrySet;
import org.xydra.index.ISerializableMapSetIndex;

/**
 * @author xamde
 * @param <K>
 * @param <E>
 */
public class SerializableMapSetIndex<K extends Serializable, E extends Serializable> extends AbstractMapSetIndex<K, E>
		implements ISerializableMapSetIndex<K, E> {

	public SerializableMapSetIndex(final Factory<IEntrySet<E>> entrySetFactory) {
		super(entrySetFactory);
	}

	public SerializableMapSetIndex(final Factory<IEntrySet<E>> entrySetFactory, final boolean concurrent) {
		super(entrySetFactory, concurrent);
	}

	/**
	 * @return an impl that uses more memory and is fast
	 */
	public static <K extends Serializable, E extends Serializable> SerializableMapSetIndex<K, E> createWithFastEntrySets() {
		return new SerializableMapSetIndex<K, E>(new FastEntrySetFactory<E>());
	}

	/**
	 * @return an impl that uses less memory and is slower
	 */
	public static <K extends Serializable, E extends Serializable> SerializableMapSetIndex<K, E> createWithSmallEntrySets() {
		return new SerializableMapSetIndex<K, E>(new SmallEntrySetFactory<E>());
	}

	/**
	 * @return an impl that uses less memory and is slower
	 */
	public static <K extends Serializable, E extends Serializable> SerializableMapSetIndex<K, E> createWithFastWeakEntrySets() {
		return new SerializableMapSetIndex<K, E>(new FastWeakEntrySetFactory<E>());
	}

}
