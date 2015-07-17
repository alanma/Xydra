package org.xydra.index;

import java.io.Serializable;

/**
 * All implementations must be {@link Serializable}.
 *
 * @author voelkel
 *
 *         Multiple entries can be indexed for a certain key-combination.
 *
 * @param <K> key type
 * @param <L> key type
 * @param <E> entity type
 */
public interface ISerializableMapMapSetIndex<K extends Serializable, L extends Serializable, M extends Serializable>
extends IMapMapSetIndex<K, L, M>, Serializable {

}
