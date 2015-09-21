package org.xydra.index.impl;

import java.util.Iterator;

import org.xydra.index.IMapMapMapSetIndex;
import org.xydra.index.IMapMapSetIndex;
import org.xydra.index.ITripleSetIndex;
import org.xydra.index.query.Constraint;
import org.xydra.index.query.EqualsConstraint;
import org.xydra.index.query.KeyKeyEntryTuple;
import org.xydra.index.query.KeyKeyKeyEntryTuple;
import org.xydra.index.query.Wildcard;

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
public abstract class AbstractTripleSetIndexImpl<K, L, M, E> implements ITripleSetIndex<K, L, M, E> {

	/**
	 * o-URI -> s-URI -> set of IStatement
	 */
	protected transient IMapMapSetIndex<M, K, E> index_o_s_stmt;

	/**
	 * p-URI > o-URI > set of IStatement
	 */
	protected transient IMapMapSetIndex<L, M, E> index_p_o_stmt;

	/**
	 * s-URI > p-URI > o-URI > set of IStatement
	 */
	protected transient IMapMapMapSetIndex<K, L, M, E> index_s_p_o_stmt;

	@Override
	public void clear() {
		this.index_o_s_stmt.clear();
		this.index_p_o_stmt.clear();
		this.index_s_p_o_stmt.clear();
	}

	@Override
	public boolean contains(final Constraint<K> c1, final Constraint<L> c2, final Constraint<M> c3) {
		final Iterator<E> it = this.lookup(c1, c2, c3);
		final boolean result = it.hasNext();
		return result;
	}

	@Override
	public void deIndex(final K s, final L p, final M o, final E entry) {
		this.index_s_p_o_stmt.deIndex(s, p, o, entry);
		this.index_o_s_stmt.deIndex(o, s, entry);
		this.index_p_o_stmt.deIndex(p, o, entry);
	}

	@Override
	public void index(final K s, final L p, final M o, final E entry) {
		this.index_s_p_o_stmt.index(s, p, o, entry);
		this.index_o_s_stmt.index(o, s, entry);
		this.index_p_o_stmt.index(p, o, entry);
	}

	@Override
	public Iterator<E> iterator() {
		return this.index_o_s_stmt.iterator();
	}

	/**
	 * @param c1 constraint for component 1 of triple (subject)
	 * @param c2 constraint for component 2 of triple (property)
	 * @param c3 constraint for component 3 of triple (object)
	 * @return all matching entities for (c1,c2,c3)
	 */
	@Override
	public Iterator<E> lookup(final Constraint<K> c1, final Constraint<L> c2, final Constraint<M> c3) {
		/* deal with the eight patterns */
		if (
		// spo -> s_p_o
		!c1.isStar() && !c2.isStar() && !c2.isStar() || !c1.isStar() && !c2.isStar() && c3.isStar()
				|| !c1.isStar() && c2.isStar() && c3.isStar() || c1.isStar() && c2.isStar() && c3.isStar()

		) {
			return this.index_s_p_o_stmt.constraintIterator(c1, c2, c3);
		}

		if (
		// *po -> p_o
		c1.isStar() && !c2.isStar() && !c3.isStar() || c1.isStar() && !c2.isStar() && c3.isStar()

		) {
			return this.index_p_o_stmt.constraintIterator(c2, c3);
		}

		if (
		// s*o -> o_s
		!c1.isStar() && c2.isStar() && !c3.isStar() || c1.isStar() && c2.isStar() && !c3.isStar()

		) {
			return this.index_o_s_stmt.constraintIterator(c3, c1);
		}

		throw new AssertionError("one of the patterns should have matched");
	}

	/**
	 * @param c1 @CanBeNull to denote wild-card
	 * @param c2 @CanBeNull to denote wild-card
	 * @param c3 @CanBeNull to denote wild-card
	 * @return
	 */
	public Iterator<E> lookup(final K c1, final L c2, final M c3) {
		return lookup(toConstraint_K(c1), toConstraint_L(c2), toConstraint_M(c3));
	}

	private Constraint<K> toConstraint_K(final K k) {
		if (k == null) {
			return new Wildcard<K>();
		} else {
			return new EqualsConstraint<K>(k);
		}
	}

	private Constraint<L> toConstraint_L(final L l) {
		if (l == null) {
			return new Wildcard<L>();
		} else {
			return new EqualsConstraint<L>(l);
		}
	}

	private Constraint<M> toConstraint_M(final M m) {
		if (m == null) {
			return new Wildcard<M>();
		} else {
			return new EqualsConstraint<M>(m);
		}
	}

	@Override
	public String toString() {
		final StringBuilder b = new StringBuilder();
		b.append(this.index_s_p_o_stmt.toString());
		return b.toString();
	}

	public Iterator<KeyKeyEntryTuple<K, L, M>> tripleIterator(final Constraint<K> c1, final Constraint<L> c2,
			final Constraint<M> c3) {
		return this.index_s_p_o_stmt.keyKeyKeyIterator(c1, c2, c3);
	}

	public Iterator<KeyKeyEntryTuple<K, L, M>> triples() {
		return tripleIterator(new Wildcard<K>(), new Wildcard<L>(), new Wildcard<M>());
	}

	/**
	 * Always looks in s_p_o index, hence its very slow for returning a few triples with c1=wildcard.
	 *
	 * @param resolver
	 * @param c1
	 * @param c2
	 * @param c3
	 * @param entryConstraint
	 * @return
	 */
	public Iterator<KeyKeyKeyEntryTuple<K, L, M, E>> tupleIterator(final Constraint<K> c1, final Constraint<L> c2,
			final Constraint<M> c3, final Constraint<E> entryConstraint) {
		return this.index_s_p_o_stmt.tupleIterator(c1, c2, c3, entryConstraint);
	}

	public Iterator<KeyKeyKeyEntryTuple<K, L, M, E>> tuples() {
		return tupleIterator(new Wildcard<K>(), new Wildcard<L>(), new Wildcard<M>(), new Wildcard<E>());
	}
}
