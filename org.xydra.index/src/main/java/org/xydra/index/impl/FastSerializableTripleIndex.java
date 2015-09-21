package org.xydra.index.impl;

import java.io.Serializable;

import org.xydra.index.IMapMapSetIndex;
import org.xydra.index.ITripleIndex;
import org.xydra.index.query.Constraint;

/**
 * An implementation that uses several indexes internally and chooses among them.
 *
 * Fast implementation for {@link #contains(Constraint, Constraint, Constraint)} and fast implementation for
 * {@link #getTriples(Constraint, Constraint, Constraint)}.
 *
 * State:
 *
 * <pre>
 * s > p > o (inherited)
 * o > s > p
 * p > o > s
 * </pre>
 *
 * @author voelkel
 *
 * @param <K> key type 1 (s)
 * @param <L> key type 2 (p)
 * @param <M> key type 3 (o)
 */
public class FastSerializableTripleIndex<K extends Serializable, L extends Serializable, M extends Serializable>
extends AbstractFastTripleIndex<K, L, M>
implements ITripleIndex<K, L, M>, Serializable {

	@SuppressWarnings("unchecked")
	public FastSerializableTripleIndex() {
		super(

				(AbstractSmallTripleIndex<K, L, M, ? extends IMapMapSetIndex<K, L, M>>) new SmallSerializableTripleIndex<K, L, M>(),

				(IMapMapSetIndex<L, M, K>) new SerializableMapMapSetIndex<M, K, L>(new FastEntrySetFactory<L>()),

				(IMapMapSetIndex<M, K, L>) new SerializableMapMapSetIndex<L, M, K>(new FastEntrySetFactory<K>())

				);
	}


}

