package org.xydra.index;

import java.io.Serializable;

/**
 * All implementations must be {@link Serializable}.
 *
 * @param <K> key type
 * @param <L> key type
 * @param <E> entity type
 */
public interface ISerializableTripleIndex<K extends Serializable, L extends Serializable, M extends Serializable>
		extends ITripleIndex<K, L, M>, Serializable {

}
