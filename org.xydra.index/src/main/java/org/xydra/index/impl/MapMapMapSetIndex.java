package org.xydra.index.impl;

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
public class MapMapMapSetIndex<K, L, M, E>
		extends AbstractMapMapMapSetIndex<K, L, M, E, IMapMapSetIndex<K, L, M>, IMapMapSetIndex<L, M, E>>
		implements IMapMapMapSetIndex<K, L, M, E> {

	public MapMapMapSetIndex(final Factory<IEntrySet<E>> entrySetFactory) {
		super(entrySetFactory);
	}

	@Override
	protected IMapMapSetIndex<L, M, E> createMMSI_LME(final Factory<IEntrySet<E>> entrySetFactory) {
		return new MapMapSetIndex<L, M, E>(entrySetFactory);
	}

}
