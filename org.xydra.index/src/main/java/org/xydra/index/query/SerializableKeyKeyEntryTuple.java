package org.xydra.index.query;

import java.io.Serializable;

/**
 * A generic triple
 *
 * @author xamde
 * @param <K>
 * @param <L>
 * @param <E>
 */
public class SerializableKeyKeyEntryTuple<K extends Serializable, L extends Serializable, M extends Serializable, E extends Serializable>
extends KeyKeyKeyEntryTuple<K, L, M, E>implements Serializable {

	public SerializableKeyKeyEntryTuple(final K key1, final L key2, final M key3, final E entry) {
		super(key1, key2, key3, entry);
	}

}
