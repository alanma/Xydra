package org.xydra.index.impl;

import java.io.Serializable;
import java.util.Iterator;

import org.xydra.index.Factory;
import org.xydra.index.IEntrySet;
import org.xydra.index.IMapMapMapSetIndex;
import org.xydra.index.IMapMapSetIndex;
import org.xydra.index.TripleSetIndex;
import org.xydra.index.query.Constraint;

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
public class TripleSetIndexImpl<K extends Serializable, L extends Serializable, M extends Serializable, E extends Serializable>
implements TripleSetIndex<K, L, M, E>, Serializable {

	/**
	 * o-URI -> s-URI -> set of IStatement
	 */
	private transient IMapMapSetIndex<M, K, E> index_o_s_stmt;

	/**
	 * p-URI > o-URI > set of IStatement
	 */
	private transient IMapMapSetIndex<L, M, E> index_p_o_stmt;

	/**
	 * s-URI > p-URI > o-URI > set of IStatement
	 */
	private transient IMapMapMapSetIndex<K, L, M, E> index_s_p_o_stmt;

	public TripleSetIndexImpl() {
		final Factory<IEntrySet<E>> entrySetFactory = new SmallEntrySetFactory<E>();
		this.index_o_s_stmt = new SerializableMapMapSetIndex<M, K, E>(entrySetFactory);
		this.index_p_o_stmt = new SerializableMapMapSetIndex<L, M, E>(entrySetFactory);
		this.index_s_p_o_stmt = new MapMapMapSetIndex<K, L, M, E>(entrySetFactory);
	}

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
		// deal with the eight patterns

		if (
				// spo -> s_p_o
				!c1.isStar() && !c2.isStar() && !c2.isStar() ||
				!c1.isStar() && !c2.isStar() && c3.isStar() ||
				!c1.isStar() && c2.isStar() && c3.isStar() ||
				c1.isStar() && c2.isStar() && c3.isStar()

				) {
			return this.index_s_p_o_stmt.constraintIterator(c1, c2, c3);
		}

		if (
				// *po -> p_o
				c1.isStar() && !c2.isStar() && !c3.isStar() ||
				c1.isStar() && !c2.isStar() && c3.isStar()

				) {
			return this.index_p_o_stmt.constraintIterator(c2, c3);
		}

		if (
				// s*o -> o_s
				!c1.isStar() && c2.isStar() && !c3.isStar() ||
				c1.isStar() && c2.isStar() && !c3.isStar()

				) {
			return this.index_o_s_stmt.constraintIterator(c3, c1);
		}

		throw new AssertionError("one of the patterns should have matched");
	}

}
