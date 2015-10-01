package org.xydra.index.impl;

import java.io.Serializable;
import java.util.Iterator;

import org.xydra.index.IMapMapSetIndex;
import org.xydra.index.ITripleIndex;
import org.xydra.index.iterator.Iterators;
import org.xydra.index.query.Constraint;
import org.xydra.index.query.EqualsConstraint;
import org.xydra.index.query.ITriple;
import org.xydra.index.query.Wildcard;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;

/**
 * An implementation that uses no additional indexes. Slow, but small memory footprint. Sub-classes can override methods
 * and use more indexes.
 *
 * State:
 *
 * <pre>
 * s > p > o
 * </pre>
 *
 * @author voelkel
 *
 * @param <K> key type 1
 * @param <L> key type 2
 * @param <M> key type 3
 */
public abstract class AbstractSmallTripleIndex<K, L, M, MMSI extends IMapMapSetIndex<K, L, M>>
		implements ITripleIndex<K, L, M>, Serializable {

	private static final Logger log = LoggerFactory.getLogger(AbstractSmallTripleIndex.class);

	/**
	 * s > p > o
	 */
	protected MMSI index_s_p_o;

	public AbstractSmallTripleIndex() {
		this.index_s_p_o = createMMSI();
	}

	@Override
	public void clear() {
		this.index_s_p_o.clear();
	}

	// @Override
	// public IMapMapSetDiff<K, L, M> computeDiff(final ITripleIndex<K, L, M> other) {
	// if(other instanceof AbstractSmallTripleIndex) {
	// @SuppressWarnings("rawtypes")
	// final AbstractSmallTripleIndex otherIndex = (AbstractSmallTripleIndex) other;
	// @SuppressWarnings("unchecked")
	// final IMapMapSetIndex<K, L, M> otherRawIndex = otherIndex.getMapMapSetIndex();
	// final IMapMapSetDiff<K, L, M> spoDiff = this.index_s_p_o.computeDiff(otherRawIndex);
	// return spoDiff;
	// } else {
	// throw new UnsupportedOperationException();
	// }
	// }

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

	protected abstract MMSI createMMSI();

	@Override
	public void deIndex(final K s, final L p, final M o) {
		this.index_s_p_o.deIndex(s, p, o);
	}

	@Override
	public String dump() {
		log.info("Dumping s-p-o-index (there are other indexes)");
		this.index_s_p_o.dump();
		return "";
	}

	public MMSI getMapMapSetIndex() {
		return this.index_s_p_o;
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
	public boolean isEmpty() {
		return this.index_s_p_o.isEmpty();
	}

	public Iterator<K> keyIterator() {
		return this.index_s_p_o.keyIterator();
	}

	// sub-classes can provide optimized implementations which do not need to create KeyKeyEntry tuple intermediate
	// objects
	@Override
	public Iterator<M> getObjects() {
		return Iterators.distinct(TripleUtils.getMatchingAndProject_O(this, (K) null, null, null));
	}

	// sub-classes can provide optimized implementations which do not need to create KeyKeyEntry tuple intermediate
	// objects
	@Override
	public Iterator<M> getObjects_SPX(final K s, final L p) {
		return Iterators.distinct(TripleUtils.getMatchingAndProject_O(this, s, p, null));
	}

	// sub-classes can provide optimized implementations which do not need to create KeyKeyEntry tuple intermediate
	// objects
	@Override
	public Iterator<L> getPredicates() {
		return Iterators.distinct(TripleUtils.getMatchingAndProject_P(this, (K) null, null, null));
	}

	// sub-classes can provide optimized implementations which do not need to create KeyKeyEntry tuple intermediate
	// objects
	@Override
	public Iterator<L> getPredicates_SX(final K s) {
		return Iterators.distinct(TripleUtils.getMatchingAndProject_P(this, s, null, null));
	}

	// sub-classes can provide optimized implementations which do not need to create KeyKeyEntry tuple intermediate
	// objects
	@Override
	public Iterator<L> getPredicates_SXO(final K s, final M o) {
		return Iterators.distinct(TripleUtils.getMatchingAndProject_P(this, s, null, o));
	}

	// sub-classes can provide optimized implementations which do not need to create KeyKeyEntry tuple intermediate
	// objects
	@Override
	public Iterator<K> getSubjects() {
		return Iterators.distinct(TripleUtils.getMatchingAndProject_S(this, (K) null, null, null));
	}

	// sub-classes can provide optimized implementations which do not need to create KeyKeyEntry tuple intermediate
	// objects
	@Override
	public Iterator<K> getSubjects_XPO(final L p, final M o) {
		return Iterators.distinct(TripleUtils.getMatchingAndProject_S(this, null, p, o));
	}

}
