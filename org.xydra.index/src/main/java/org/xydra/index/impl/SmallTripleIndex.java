package org.xydra.index.impl;

import java.io.Serializable;
import java.util.Iterator;

import org.xydra.index.IMapMapSetIndex.IMapMapSetDiff;
import org.xydra.index.ITripleIndex;
import org.xydra.index.query.Constraint;
import org.xydra.index.query.EqualsConstraint;
import org.xydra.index.query.ITriple;
import org.xydra.index.query.Wildcard;

/**
 * An implementation that uses no indexes. Slow, but small memory footprint.
 *
 * @author voelkel
 *
 * @param <K> key type 1
 * @param <L> key type 2
 * @param <M> key type 3
 */
public class SmallTripleIndex<K extends Serializable, L extends Serializable, M extends Serializable> implements ITripleIndex<K, L, M>, Serializable {

	/**
	 * s > p > o
	 */
	protected SerializableMapMapSetIndex<K, L, M> index_s_p_o;

	public SmallTripleIndex() {
		this.index_s_p_o = new SerializableMapMapSetIndex<K, L, M>(new FastEntrySetFactory<M>());
	}

	@Override
	public void clear() {
		this.index_s_p_o.clear();
	}

	@Override
	public boolean contains(final Constraint<K> c1, final Constraint<L> c2, final Constraint<M> c3) {
		return getTriples(c1, c2, c3).hasNext();
	}

	/**
	 * @param key1
	 * @NeverNull
	 * @param key2
	 * @NeverNull
	 * @param key3
	 * @NeverNull
	 * @return true if triple index contains the given triple
	 */
	@Override
	public boolean contains(final K key1, final L key2, final M key3) {
		final Constraint<K> c1 = new EqualsConstraint<K>(key1);
		final Constraint<L> c2 = new EqualsConstraint<L>(key2);
		final Constraint<M> c3 = new EqualsConstraint<M>(key3);
		return this.contains(c1, c2, c3);
	}

	@Override
	public void deIndex(final K s, final L p, final M o) {
		this.index_s_p_o.deIndex(s, p, o);
	}

	@Override
	public void dump() {
		System.out.println("Dumping s-p-o-index (there are others)");
		final Iterator<ITriple<K, L, M>> it = this.index_s_p_o.tupleIterator(new Wildcard<K>(),
				new Wildcard<L>(), new Wildcard<M>());
		while (it.hasNext()) {
			final ITriple<K, L, M> t = it.next();
			System.out.println(t.getKey1() + " - " + t.getKey2() + " - " + t.getEntry());
		}
	}

	@Override
	public Iterator<ITriple<K, L, M>> getTriples(final Constraint<K> c1, final Constraint<L> c2,
			final Constraint<M> c3) {
		if (c1 == null) {
			throw new IllegalArgumentException("c1 was null");
		}
		if (c2 == null) {
			throw new IllegalArgumentException("c2 was null");
		}
		if (c3 == null) {
			throw new IllegalArgumentException("c3 was null");
		}
		final Iterator<ITriple<K, L, M>> tupleIterator = this.index_s_p_o.tupleIterator(c1, c2, c3);
		return tupleIterator;
	}

	@Override
	public Iterator<ITriple<K, L, M>> getTriples(final K s, final L p, final M o) {
		final Constraint<K> c1 = s == null ? new Wildcard<K>() : new EqualsConstraint<K>(s);
		final Constraint<L> c2 = p == null ? new Wildcard<L>() : new EqualsConstraint<L>(p);
		final Constraint<M> c3 = o == null ? new Wildcard<M>() : new EqualsConstraint<M>(o);
		return getTriples(c1, c2, c3);
	}

	@Override
	public boolean index(final K s, final L p, final M o) {
		return this.index_s_p_o.index(s, p, o);
	}

	@Override
	public IMapMapSetDiff<K, L, M> computeDiff(final ITripleIndex<K, L, M> other) {
		final SmallTripleIndex<K, L, M> otherIndex = (SmallTripleIndex<K, L, M>) other;
		final IMapMapSetDiff<K, L, M> spoDiff = this.index_s_p_o.computeDiff(otherIndex.index_s_p_o);
		return spoDiff;
	}

	@Override
	public boolean isEmpty() {
		return this.index_s_p_o.isEmpty();
	}

	public Iterator<K> keyIterator() {
		return this.index_s_p_o.keyIterator();
	}

}
