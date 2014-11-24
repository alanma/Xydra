package org.xydra.index.impl;

import java.util.Iterator;

import org.xydra.index.ITripleIndex;
import org.xydra.index.iterator.ITransformer;
import org.xydra.index.iterator.TransformingIterator;
import org.xydra.index.query.Constraint;
import org.xydra.index.query.ITriple;

public class TripleUtils {

	/**
	 * @param c1 constraint for component 1 of triple (subject)
	 * @param c2 constraint for component 2 of triple (property)
	 * @param c3 constraint for component 3 of triple (object)
	 * @param projectedConstraint (1,2, or 3)
	 * @return an {@link Iterator} that contains all triples matching the given
	 *         constraints, projected to a single component (first, second, or
	 *         third)
	 */
	public static <K, L, M> Iterator<K> getMatchingAndProject_S(ITripleIndex<K, L, M> tripleIndex,
			Constraint<K> c1, Constraint<L> c2, Constraint<M> c3) {
		Iterator<ITriple<K, L, M>> tupleIterator = tripleIndex.getTriples(c1, c2, c3);
		ITransformer<ITriple<K, L, M>, K> transformer = new ITransformer<ITriple<K, L, M>, K>() {
			@Override
			public K transform(ITriple<K, L, M> in) {
				return in.getKey1();
			}
		};
		return new TransformingIterator<ITriple<K, L, M>, K>(tupleIterator, transformer);
	}

	public static <K, L, M> Iterator<L> getMatchingAndProject_P(ITripleIndex<K, L, M> tripleIndex,
			Constraint<K> c1, Constraint<L> c2, Constraint<M> c3) {
		Iterator<ITriple<K, L, M>> tupleIterator = tripleIndex.getTriples(c1, c2, c3);
		ITransformer<ITriple<K, L, M>, L> transformer = new ITransformer<ITriple<K, L, M>, L>() {
			@Override
			public L transform(ITriple<K, L, M> in) {
				return in.getKey2();
			}
		};
		return new TransformingIterator<ITriple<K, L, M>, L>(tupleIterator, transformer);
	}

	public static <K, L, M> Iterator<M> getMatchingAndProject_O(ITripleIndex<K, L, M> tripleIndex,
			Constraint<K> c1, Constraint<L> c2, Constraint<M> c3) {
		Iterator<ITriple<K, L, M>> tupleIterator = tripleIndex.getTriples(c1, c2, c3);
		ITransformer<ITriple<K, L, M>, M> transformer = new ITransformer<ITriple<K, L, M>, M>() {
			@Override
			public M transform(ITriple<K, L, M> in) {
				return in.getEntry();
			}
		};
		return new TransformingIterator<ITriple<K, L, M>, M>(tupleIterator, transformer);
	}

	public static <K, L, M> Iterator<K> getMatchingAndProject_S(ITripleIndex<K, L, M> tripleIndex,
			K s, L p, M o) {
		Iterator<ITriple<K, L, M>> tupleIterator = tripleIndex.getTriples(s, p, o);
		ITransformer<ITriple<K, L, M>, K> transformer = new ITransformer<ITriple<K, L, M>, K>() {
			@Override
			public K transform(ITriple<K, L, M> in) {
				return in.getKey1();
			}
		};
		return new TransformingIterator<ITriple<K, L, M>, K>(tupleIterator, transformer);
	}

	public static <K, L, M> Iterator<L> getMatchingAndProject_P(ITripleIndex<K, L, M> tripleIndex,
			K s, L p, M o) {
		Iterator<ITriple<K, L, M>> tupleIterator = tripleIndex.getTriples(s, p, o);
		ITransformer<ITriple<K, L, M>, L> transformer = new ITransformer<ITriple<K, L, M>, L>() {
			@Override
			public L transform(ITriple<K, L, M> in) {
				return in.getKey2();
			}
		};
		return new TransformingIterator<ITriple<K, L, M>, L>(tupleIterator, transformer);
	}

	public static <K, L, M> Iterator<M> getMatchingAndProject_O(ITripleIndex<K, L, M> tripleIndex,
			K s, L p, M o) {
		Iterator<ITriple<K, L, M>> tupleIterator = tripleIndex.getTriples(s, p, o);
		ITransformer<ITriple<K, L, M>, M> transformer = new ITransformer<ITriple<K, L, M>, M>() {
			@Override
			public M transform(ITriple<K, L, M> in) {
				return in.getEntry();
			}
		};
		return new TransformingIterator<ITriple<K, L, M>, M>(tupleIterator, transformer);
	}

}
