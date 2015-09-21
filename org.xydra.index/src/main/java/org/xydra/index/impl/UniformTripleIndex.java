package org.xydra.index.impl;

import java.io.Serializable;
import java.util.Iterator;

import org.xydra.index.IUniformTripleIndex;
import org.xydra.index.iterator.ITransformer;
import org.xydra.index.iterator.TransformingIterator;
import org.xydra.index.query.Constraint;
import org.xydra.index.query.ITriple;

public class UniformTripleIndex<K extends Serializable> extends FastContainsSerializableTripleIndex<K, K, K> implements
IUniformTripleIndex<K> {

	/**
	 * @param c1 constraint for component 1 of triple (subject)
	 * @param c2 constraint for component 2 of triple (property)
	 * @param c3 constraint for component 3 of triple (object)
	 * @param projectedConstraint (1,2, or 3)
	 * @return an {@link Iterator} that contains all triples matching the given
	 *         constraints, projected to a single component (first, second, or
	 *         third)
	 */
	@Override
	public Iterator<K> getMatchingAndProject(final Constraint<K> c1, final Constraint<K> c2, final Constraint<K> c3,
			final int projectedConstraint) {
		final Iterator<ITriple<K, K, K>> tupleIterator = this.index_s_p_o.tupleIterator(c1, c2, c3);
		ITransformer<ITriple<K, K, K>, K> transformer;
		switch (projectedConstraint) {
		case 1:
			transformer = new ITransformer<ITriple<K, K, K>, K>() {
				@Override
				public K transform(final ITriple<K, K, K> in) {
					return in.getKey1();
				}
			};
			break;
		case 2:
			transformer = new ITransformer<ITriple<K, K, K>, K>() {
				@Override
				public K transform(final ITriple<K, K, K> in) {
					return in.getKey2();
				}
			};
			break;
		case 3:
			transformer = new ITransformer<ITriple<K, K, K>, K>() {
				@Override
				public K transform(final ITriple<K, K, K> in) {
					return in.getEntry();
				}
			};
			break;
		default:
			throw new AssertionError("projectedConstraint must be 1=s, 2=p, 3=o");
		}

		return new TransformingIterator<ITriple<K, K, K>, K>(tupleIterator, transformer);
	}

}
