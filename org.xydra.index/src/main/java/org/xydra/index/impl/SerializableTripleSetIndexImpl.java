package org.xydra.index.impl;

import java.io.Serializable;

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
public class SerializableTripleSetIndexImpl
<K extends Serializable,
L extends Serializable,
M extends Serializable,
E extends Serializable>
extends AbstractTripleSetIndexImpl<K, L, M, E>
implements Serializable {

	public SerializableTripleSetIndexImpl() {
		final Factory<IEntrySet<E>> entrySetFactory = new SmallEntrySetFactory<E>();
		this.index_o_s_stmt = new SerializableMapMapSetIndex<M, K, E>(entrySetFactory);
		this.index_p_o_stmt = new SerializableMapMapSetIndex<L, M, E>(entrySetFactory);
		this.index_s_p_o_stmt = new SerializableMapMapMapSetIndex<K, L, M, E>(entrySetFactory);
	}

}
