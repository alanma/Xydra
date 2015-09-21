package org.xydra.index.impl;

import org.xydra.index.Factory;
import org.xydra.index.IEntrySet;

/**
 * Indexes sets of entries by three keys.
 *
 * Note how this is different from a 'QuadrupleIndex' (which would support queries such as (*,*,*,x)).
 *
 * @author voelkel
 *
 * @param <K> first part of triple
 * @param <L> second part of triple
 * @param <M> third part of triples
 * @param <E> type of entries in the indexed set
 */
public class TripleSetIndexImpl<K, L, M, E> extends AbstractTripleSetIndexImpl<K, L, M, E> {

	public TripleSetIndexImpl() {
		final Factory<IEntrySet<E>> entrySetFactory = new SmallEntrySetFactory<E>();
		this.index_o_s_stmt = new MapMapSetIndex<M, K, E>(entrySetFactory);
		this.index_p_o_stmt = new MapMapSetIndex<L, M, E>(entrySetFactory);
		this.index_s_p_o_stmt = new MapMapMapSetIndex<K, L, M, E>(entrySetFactory);
	}

}
