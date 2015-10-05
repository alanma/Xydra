package org.xydra.index;

import java.io.Serializable;

/**
 * Note: All implementations need to be {@link Serializable}.
 *
 * @param <K> key type
 * @param <E> entity type
 */
public interface ISerializableMapSetIndex<K extends Serializable, E extends Serializable> extends IMapSetIndex<K, E>, Serializable {

}
