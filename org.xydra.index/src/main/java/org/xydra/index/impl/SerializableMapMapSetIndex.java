package org.xydra.index.impl;

import java.io.Serializable;

import org.xydra.index.Factory;
import org.xydra.index.IEntrySet;
import org.xydra.index.ISerializableMapMapSetIndex;

/**
 * <pre>
 * K -> ( L -> Set(E) )
 * </pre>
 *
 * @author xamde
 * @param <K>
 * @param <L>
 * @param <E>
 */
public class SerializableMapMapSetIndex<K extends Serializable, L extends Serializable, E extends Serializable>
extends MapMapSetIndex<K, L, E>implements ISerializableMapMapSetIndex<K, L, E>, Serializable {

	public SerializableMapMapSetIndex(final Factory<IEntrySet<E>> entrySetFactory) {
		super(entrySetFactory);

	}

	public static <K extends Serializable, L extends Serializable, E extends Serializable> SerializableMapMapSetIndex<K, L, E> createSerializableWithSmallSets() {
		return new SerializableMapMapSetIndex<K, L, E>(new SmallEntrySetFactory<E>());
	}

	public static <K extends Serializable, L extends Serializable, E extends Serializable> SerializableMapMapSetIndex<K, L, E> createSerializableWithFastSets() {
		return new SerializableMapMapSetIndex<K, L, E>(new FastEntrySetFactory<E>());
	}

}
