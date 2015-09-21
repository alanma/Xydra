package org.xydra.index.impl;

import java.io.Serializable;

import org.xydra.index.Factory;
import org.xydra.index.IEntrySet;
import org.xydra.index.IMapMapMapSetIndex;
import org.xydra.index.IMapMapSetIndex;

/**
 * <pre>
 * K -> ( L -> ( M -> Set(E) ) )
 * </pre>
 *
 * @author xamde
 * @param <K>
 * @param <L>
 * @param <M>
 * @param <E>
 */
public class SerializableMapMapMapSetIndex<K extends Serializable, L extends Serializable, M extends Serializable, E extends Serializable>
		extends AbstractMapMapMapSetIndex<K, L, M, E, IMapMapSetIndex<K, L, M>, IMapMapSetIndex<L, M, E>>
		implements IMapMapMapSetIndex<K, L, M, E>, Serializable {

	public SerializableMapMapMapSetIndex(final Factory<IEntrySet<E>> entrySetFactory) {
		super(entrySetFactory);
	}

	@Override
	protected IMapMapSetIndex<L, M, E> createMMSI_LME(final Factory<IEntrySet<E>> entrySetFactory) {
		return new SerializableMapMapSetIndex<L, M, E>(entrySetFactory);
	}

}
