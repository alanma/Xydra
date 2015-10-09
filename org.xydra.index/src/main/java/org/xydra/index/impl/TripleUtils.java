package org.xydra.index.impl;

import java.util.Iterator;

import org.xydra.index.IMapMapSetIndex;
import org.xydra.index.ITripleSource;
import org.xydra.index.iterator.ITransformer;
import org.xydra.index.iterator.TransformingIterator;
import org.xydra.index.query.Constraint;
import org.xydra.index.query.EqualsConstraint;
import org.xydra.index.query.ITriple;
import org.xydra.index.query.Wildcard;

public class TripleUtils {

	public static <K, L, M> ITransformer<ITriple<K, L, M>, K> transformer_s() {
		return new ITransformer<ITriple<K, L, M>, K>() {

			@Override
			public K transform(final ITriple<K, L, M> in) {
				return in.s();
			}
		};
	}

	public static <K, L, M> ITransformer<ITriple<K, L, M>, L> transformer_p() {
		return new ITransformer<ITriple<K, L, M>, L>() {

			@Override
			public L transform(final ITriple<K, L, M> in) {
				return in.p();
			}
		};
	}

	public static <K, L, M> ITransformer<ITriple<K, L, M>, M> transformer_o() {
		return new ITransformer<ITriple<K, L, M>, M>() {

			@Override
			public M transform(final ITriple<K, L, M> in) {
				return in.o();
			}
		};
	}

	/**
	 * @param c1 constraint for component 1 of triple (subject)
	 * @param c2 constraint for component 2 of triple (property)
	 * @param c3 constraint for component 3 of triple (object)
	 * @param projectedConstraint (1,2, or 3)
	 * @return an {@link Iterator} that contains all triples matching the given constraints, projected to a single
	 *         component (first, second, or third)
	 */
	public static <K, L, M> Iterator<K> getConstraintMatchingAndProject_S(final ITripleSource<K, L, M> tripleIndex,
			final Constraint<K> c1, final Constraint<L> c2, final Constraint<M> c3) {
		final Iterator<ITriple<K, L, M>> tupleIterator = tripleIndex.getTriples(c1, c2, c3);
		final ITransformer<ITriple<K, L, M>, K> transformer = transformer_s();
		return new TransformingIterator<ITriple<K, L, M>, K>(tupleIterator, transformer);
	}

	public static <K, L, M> Iterator<L> getConstraintMatchingAndProject_P(final ITripleSource<K, L, M> tripleIndex,
			final Constraint<K> c1, final Constraint<L> c2, final Constraint<M> c3) {
		final Iterator<ITriple<K, L, M>> tupleIterator = tripleIndex.getTriples(c1, c2, c3);
		final ITransformer<ITriple<K, L, M>, L> transformer = transformer_p();
		return new TransformingIterator<ITriple<K, L, M>, L>(tupleIterator, transformer);
	}

	public static <K, L, M> Iterator<M> getConstraintMatchingAndProject_O(final ITripleSource<K, L, M> tripleIndex,
			final Constraint<K> c1, final Constraint<L> c2, final Constraint<M> c3) {
		final Iterator<ITriple<K, L, M>> tupleIterator = tripleIndex.getTriples(c1, c2, c3);
		final ITransformer<ITriple<K, L, M>, M> transformer = transformer_o();
		return new TransformingIterator<ITriple<K, L, M>, M>(tupleIterator, transformer);
	}

	public static <K, L, M> Iterator<K> getMatchingAndProject_S(final ITripleSource<K, L, M> tripleIndex, final K s,
			final L p, final M o) {
		final Iterator<ITriple<K, L, M>> tupleIterator = tripleIndex.getTriples(s, p, o);
		final ITransformer<ITriple<K, L, M>, K> transformer = transformer_s();
		return new TransformingIterator<ITriple<K, L, M>, K>(tupleIterator, transformer);
	}

	public static <K, L, M> Iterator<L> getMatchingAndProject_P(final ITripleSource<K, L, M> tripleIndex, final K s,
			final L p, final M o) {
		final Iterator<ITriple<K, L, M>> tupleIterator = tripleIndex.getTriples(s, p, o);
		final ITransformer<ITriple<K, L, M>, L> transformer = transformer_p();
		return new TransformingIterator<ITriple<K, L, M>, L>(tupleIterator, transformer);
	}

	public static <K, L, M> Iterator<M> getMatchingAndProject_O(final ITripleSource<K, L, M> tripleIndex, final K s,
			final L p, final M o) {
		final Iterator<ITriple<K, L, M>> tupleIterator = tripleIndex.getTriples(s, p, o);
		final ITransformer<ITriple<K, L, M>, M> transformer = transformer_o();
		return new TransformingIterator<ITriple<K, L, M>, M>(tupleIterator, transformer);
	}

	public static <K, L, M> Iterator<ITriple<K, L, M>> getConstraintTriples(final IMapMapSetIndex<K, L, M> index_s_p_o, final Constraint<K> c1,
			final Constraint<L> c2, final Constraint<M> c3) {
		if (c1 == null) {
			throw new IllegalArgumentException("c1 was null");
		}
		if (c2 == null) {
			throw new IllegalArgumentException("c2 was null");
		}
		if (c3 == null) {
			throw new IllegalArgumentException("c3 was null");
		}
		final Iterator<ITriple<K, L, M>> tupleIterator = index_s_p_o.tupleIterator(c1, c2, c3);
		return tupleIterator;
	}

	public static <K, L, M> Iterator<ITriple<K, L, M>> getTriples(final IMapMapSetIndex<K, L, M> index_s_p_o, final K s, final L p,
			final M o) {
		final Constraint<K> c1 = s == null ? new Wildcard<K>() : new EqualsConstraint<K>(s);
		final Constraint<L> c2 = p == null ? new Wildcard<L>() : new EqualsConstraint<L>(p);
		final Constraint<M> c3 = o == null ? new Wildcard<M>() : new EqualsConstraint<M>(o);
		return getConstraintTriples(index_s_p_o, c1, c2, c3);
	}

	/**
	 * @param t
	 * @return the reverse triple (o,p,s)
	 */
	public static <K, L, M> ITriple<M, L, K> reverse(final ITriple<K, L, M> t) {
		return new KeyKeyEntryTuple<M, L, K>(t.o(), t.p(), t.s());
	}

	/**
	 * Return the other end of a triple, given one if it'S "ends" (subject or object)
	 *
	 * @param a must be triple.s() or triple.o()
	 * @param triple
	 * @return s, if a==triple.o(); o, if a==triple.s();
	 */
	public static <T> T getOtherEnd(final T a, final ITriple<T, T, T> triple) {
		assert triple.s().equals(a) || triple.o().equals(a);
		if (triple.s().equals(a)) {
			return triple.o();
		}
		assert triple.o().equals(a);
		return triple.s();
	}

}
