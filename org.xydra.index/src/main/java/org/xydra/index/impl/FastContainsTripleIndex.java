package org.xydra.index.impl;

import java.io.Serializable;
import java.util.Iterator;

import org.xydra.index.IMapSetIndex;
import org.xydra.index.ITripleIndex;
import org.xydra.index.query.Constraint;
import org.xydra.index.query.EqualsConstraint;
import org.xydra.index.query.ITriple;
import org.xydra.index.query.Wildcard;

/**
 * An implementation that uses several indexes internally and chooses among them.
 *
 * Fast implementation for {@link #contains(Constraint, Constraint, Constraint)} , slow for
 * {@link #getTriples(Constraint, Constraint, Constraint)}.
 *
 * @author voelkel
 *
 * @param <K> key type 1
 * @param <L> key type 2
 * @param <M> key type 3
 */
public class FastContainsTripleIndex<K extends Serializable, L extends Serializable, M extends Serializable>
extends SmallTripleIndex<K, L, M>implements ITripleIndex<K, L, M> {

	/**
	 * o-URI -> p-URI
	 */
	private transient IMapSetIndex<M, K> index_o_s;

	/**
	 * p-URI > o-URI
	 */
	private transient IMapSetIndex<L, M> index_p_o;

	public FastContainsTripleIndex() {
		super();
		this.index_o_s = new MapSetIndex<M, K>(new FastEntrySetFactory<K>());
		this.index_p_o = new MapSetIndex<L, M>(new FastEntrySetFactory<M>());
	}

	@Override
	public void clear() {
		super.clear();
		this.index_o_s.clear();
		this.index_p_o.clear();
	}

	@Override
	public boolean contains(final Constraint<K> c1, final Constraint<L> c2, final Constraint<M> c3) {
		// deal with the eight patterns
		if (
				// spo -> s_p_o
				!c1.isStar() && !c2.isStar() && !c3.isStar() ||
				// sp* -> s_p_o
				!c1.isStar() && !c2.isStar() && c3.isStar() ||
				// s** -> s_p_o
				!c1.isStar() && c2.isStar() && c3.isStar() ||
				// *** -> s_p_o
				c1.isStar() && c2.isStar() && c3.isStar()

				) {
			return this.index_s_p_o.contains(c1, c2, c3);
		} else if (
				// *po -> p_o
				c1.isStar() && !c2.isStar() && !c3.isStar() ||
				// *p* -> p_o
				c1.isStar() && !c2.isStar() && c3.isStar()

				) {
			return this.index_p_o.contains(c2, c3);
		} else if (
				// s*o -> o_s
				!c1.isStar() && c2.isStar() && !c3.isStar() ||
				// **o -> o_s
				c1.isStar() && c2.isStar() && !c3.isStar()

				) {
			return this.index_o_s.contains(c3, c1);
		}

		throw new AssertionError("one of the patterns should have matched");
	}

	@Override
	public boolean contains(final K key1, final L key2, final M key3) {
		final Constraint<K> c1 = new EqualsConstraint<K>(key1);
		final Constraint<L> c2 = new EqualsConstraint<L>(key2);
		final Constraint<M> c3 = new EqualsConstraint<M>(key3);
		return this.contains(c1, c2, c3);
	}

	@Override
	public void deIndex(final K s, final L p, final M o) {
		super.deIndex(s, p, o);
		this.index_o_s.deIndex(o, s);
		this.index_p_o.deIndex(p, o);
	}

	@Override
	public boolean index(final K s, final L p, final M o) {
		boolean changes = super.index(s, p, o);
		changes |= transientIndex(s, p, o);
		return changes;
	}

	private boolean transientIndex(final K s, final L p, final M o) {
		boolean changes = this.index_o_s.index(o, s);
		changes |= this.index_p_o.index(p, o);
		return changes;
	}

	/**
	 * This method should be called after deserialisation to rebuild the transient indexes. Deserialisation in GWT and
	 * plain Java is quite different and incompatible, so this is not called automatically. For plain Java see
	 * http://java.sun.com/developer/technicalArticles/Programming /serialization/
	 */
	public void rebuildAfterDeserialize() {
		final Iterator<ITriple<K, L, M>> it = this.index_s_p_o.tupleIterator(new Wildcard<K>(), new Wildcard<L>(),
				new Wildcard<M>());
		while (it.hasNext()) {
			final ITriple<K, L, M> t = it.next();
			transientIndex(t.getKey1(), t.getKey2(), t.getEntry());
		}
	}

}
