package org.xydra.index.impl;

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
public class FastTripleIndex<K, L, M> extends AbstractFastTripleIndex<K, L, M>implements ITripleIndex<K, L, M> {

	@SuppressWarnings("unchecked")
	public FastTripleIndex() {
		super(

		(IMapMapSetIndex<K, L, M>) new MapMapSetIndex<K, L, M>(new FastEntrySetFactory<M>()),

		(IMapMapSetIndex<L, M, K>) new MapMapSetIndex<M, K, L>(new FastEntrySetFactory<L>()),

		(IMapMapSetIndex<M, K, L>) new MapMapSetIndex<L, M, K>(new FastEntrySetFactory<K>())

		);
	}

}
