package org.xydra.index.impl;

import java.util.Iterator;

import org.xydra.index.Factory;
import org.xydra.index.IPairIndex;
import org.xydra.index.ITransitivePairIndex;
import org.xydra.index.XI;
import org.xydra.index.query.Constraint;
import org.xydra.index.query.EqualsConstraint;
import org.xydra.index.query.Pair;
import org.xydra.index.query.Wildcard;

/**
 * Implementation of {@link ITransitivePairIndex} that calculates all implied
 * pairs and stores them internally.
 *
 * While this allows fast lookup of implied pairs, in the worst case the number
 * of (stored) implied pairs may grow up to O((#pairs)^2), causing slow indexing
 * / deIndexing of pairs and high memory usage.
 *
 * Worst case index(k1,k2) should be around O(#left * #right) and deIndex(k1,k2)
 * around O(#left * #right * #direct) where #left is the number of implied pairs
 * (*,k1), #right is the number of implied pairs (k2,*) and #direct = max{
 * number of defined pairs (k3,*) | (k3,k1) is implied }
 *
 * @author dscharrer
 * @param <K>
 */
public class StoredTransitivePairIndex<K> extends AbstractStoredTransitivePairIndex<K> {

	private static final long serialVersionUID = -4489815903874718863L;

	public StoredTransitivePairIndex(final IPairIndex<K, K> direct, final Factory<IPairIndex<K, K>> implied) {
		super(direct, implied);
	}

	@Override
	public void addImplied(final K k1, final K k2) {

		if (!addAll(k1, k2)) {
			return;
		}

		final Iterator<Pair<K, K>> left = constraintIterator(new Wildcard<K>(), new EqualsConstraint<K>(
				k1));
		while (left.hasNext()) {
			addImplied(left.next().getFirst(), k2);
		}

	}

	/**
	 * Remove all now obsolete implied pairs (k1,k) for all k = k2 or k is in
	 * implied pair (k2,k)
	 */
	private boolean removeAll(final K k1, final K k2) {

		final Constraint<K> c1 = new EqualsConstraint<K>(k1);
		final Constraint<K> c2 = new EqualsConstraint<K>(k2);
		final Constraint<K> w = new Wildcard<K>();

		if (!implies(c1, c2)) {
			return false;
		}

		final Iterator<Pair<K, K>> dr = constraintIterator(c1, w);
		while (dr.hasNext()) {
			final K dg = dr.next().getSecond();

			if (XI.equals(dg, k2) || implies(new EqualsConstraint<K>(dg), c2))
			 {
				return false; // pair is still implied
			}
		}

		this.implied.deIndex(k1, k2);

		final Iterator<Pair<K, K>> right = constraintIterator(c2, w);
		while (right.hasNext()) {
			removeAll(k1, right.next().getSecond());
		}

		return true;
	}

	@Override
	public void removeImplied(final K k1, final K k2) {

		if (!removeAll(k1, k2)) {
			return;
		}

		final Iterator<Pair<K, K>> left = constraintIterator(new Wildcard<K>(), new EqualsConstraint<K>(
				k1));
		while (left.hasNext()) {
			removeImplied(left.next().getFirst(), k2);
		}

	}

}
