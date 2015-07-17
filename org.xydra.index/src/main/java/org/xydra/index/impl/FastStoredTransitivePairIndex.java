package org.xydra.index.impl;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

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
 * This implementation is slightly faster than {@link StoredTransitivePairIndex}
 * but uses up to O(#right) additional memory while indexing / deIndexing.
 *
 * IMPROVE this isn't really that much faster but significantly more complicated
 * that StoredTransitivePairIndex
 *
 * @author dscharrer
 * @param <K>
 *            key type
 */
public class FastStoredTransitivePairIndex<K> extends AbstractStoredTransitivePairIndex<K> implements Serializable {

	private static final long serialVersionUID = -1689427335276832816L;

	public FastStoredTransitivePairIndex(final IPairIndex<K, K> direct, final Factory<IPairIndex<K, K>> implied) {
		super(direct, implied);
	}

	@Override
	public void addImplied(final K k1, final K k2) {

		// collect all added pairs
		final Set<K> added = new HashSet<K>();
		collectAdded(k1, k2, added);

		// nothing to add
		if (added.isEmpty()) {
			return;
		}

		// add implied pairs
		final Iterator<K> add = added.iterator();
		while (add.hasNext()) {
			this.implied.index(k1, add.next());
		}

		// recursively add pairs
		final Iterator<Pair<K, K>> left = constraintIterator(new Wildcard<K>(), new EqualsConstraint<K>(
				k1));
		while (left.hasNext()) {
			final K k = left.next().getFirst();
			maybeAddedToGroups(k, added, !left.hasNext());
		}

	}

	/**
	 * Get all k where (k1,k) is not implied now but adding the pair (k1,k2)
	 * would imply (k1,k).
	 */
	private void collectAdded(final K k1, final K k2, final Set<K> added) {

		if (added.contains(k2)) {
			return;
		}

		if (XI.equals(k1, k2) || implies(new EqualsConstraint<K>(k1), new EqualsConstraint<K>(k2))) {
			return;
		}

		added.add(k2);

		final Iterator<Pair<K, K>> right = constraintIterator(new EqualsConstraint<K>(k2),
				new Wildcard<K>());
		while (right.hasNext()) {
			collectAdded(k1, right.next().getSecond(), added);
		}

	}

	/**
	 * Recursively add implied pairs for all defined pairs left of k1.
	 *
	 * @param k1
	 * @param added
	 * @param write
	 *            added set may be modified.
	 */
	public void maybeAddedToGroups(final K k1, final Set<K> added, final boolean write) {

		final Constraint<K> c1 = new EqualsConstraint<K>(k1);

		Set<K> myAdded = added;

		// Ignore right sides that have already been added.
		Iterator<K> it = myAdded.iterator();
		while (it.hasNext()) {

			final K k = it.next();

			if (!implies(c1, new EqualsConstraint<K>(k))) {
				continue;
			}

			if (!write && myAdded == added) {

				// create a local copy of the added list without this one
				myAdded = new HashSet<K>(added);
				myAdded.remove(k);

				// as HashSet does not guarantee a consistent order, we need
				// to start from the beginning.
				it = myAdded.iterator();

			} else {
				it.remove();
			}

		}

		// nothing to add
		if (myAdded.isEmpty()) {
			return;
		}

		// add implied pairs
		final Iterator<K> add = myAdded.iterator();
		while (add.hasNext()) {
			this.implied.index(k1, add.next());
		}

		final Iterator<Pair<K, K>> left = constraintIterator(new Wildcard<K>(), c1);
		while (left.hasNext()) {
			final K k = left.next().getFirst();
			final boolean rw = (write || myAdded != added) && !left.hasNext();
			maybeAddedToGroups(k, myAdded, rw);
		}

	}

	@Override
	public void removeImplied(final K k1, final K k2) {

		// collect all removed pairs.
		final Set<K> removed = new HashSet<K>();
		collectRemoved(k1, k2, removed);

		// nothing to remove
		if (removed.isEmpty()) {
			return;
		}

		// remove implied pairs
		final Iterator<K> remove = removed.iterator();
		while (remove.hasNext()) {
			this.implied.deIndex(k1, remove.next());
		}

		// recursively remove pairs
		final Iterator<Pair<K, K>> left = constraintIterator(new Wildcard<K>(), new EqualsConstraint<K>(
				k1));
		while (left.hasNext()) {
			final K k = left.next().getFirst();
			maybeRemovedFromGroups(k, k1, removed, !left.hasNext());
		}

	}

	/**
	 * Get all k where (k1,k) is implied now but removing the pair (k1,k2) would
	 * cause it to no longer be implied.
	 */
	private void collectRemoved(final K k1, final K k2, final Set<K> removed) {

		if (removed.contains(k2)) {
			return;
		}

		final Iterator<Pair<K, K>> direct = constraintIterator(new EqualsConstraint<K>(k1),
				new Wildcard<K>());
		while (direct.hasNext()) {
			final K dg = direct.next().getSecond();
			if (XI.equals(dg, k2)
					|| implies(new EqualsConstraint<K>(dg), new EqualsConstraint<K>(k2)))
			{
				return; // pair is still implied
			}
		}

		removed.add(k2);

		final Iterator<Pair<K, K>> right = constraintIterator(new EqualsConstraint<K>(k2),
				new Wildcard<K>());
		while (right.hasNext()) {
			collectRemoved(k1, right.next().getSecond(), removed);
		}

	}

	/**
	 * Recursively remove obsolete implied pairs for all defined pairs left of
	 * k1.
	 *
	 * @param k1
	 * @param prev
	 * @param removed
	 * @param write
	 *            removed set may be modified.
	 */
	public void maybeRemovedFromGroups(final K k1, final K prev, final Set<K> removed, final boolean write) {
		final Constraint<K> c1 = new EqualsConstraint<K>(k1);

		Set<K> myRemoved = removed;

		// ignore pairs that have already been removed
		Iterator<K> it = myRemoved.iterator();
		while (it.hasNext()) {

			final K k = it.next();

			if (implies(c1, new EqualsConstraint<K>(k))) {
				continue;
			}

			if (!write && myRemoved == removed) {

				// create a local copy of the removed list
				myRemoved = new HashSet<K>(removed);
				myRemoved.remove(k);

				// as HashSet does not guarantee a consistent order, we need
				// to start from the beginning.
				it = myRemoved.iterator();

			} else {
				it.remove();
			}

		}

		// Update list of groups to be removed.
		final Iterator<Pair<K, K>> direct = constraintIterator(c1, new Wildcard<K>());
		while (direct.hasNext() && !myRemoved.isEmpty()) {

			final K dg = direct.next().getSecond();

			if (dg == prev) {
				continue;
			}

			Iterator<K> groupIt = myRemoved.iterator();
			while (groupIt.hasNext()) {

				final K k = groupIt.next();

				if (!XI.equals(dg, k)
						&& !implies(new EqualsConstraint<K>(dg), new EqualsConstraint<K>(k))) {
					continue;
				}

				if (!write && myRemoved == removed) {

					// create a local copy of the removed list without the group
					myRemoved = new HashSet<K>(removed);
					myRemoved.remove(k);

					// as HashSet does not guarantee a consistent order, we need
					// to start from the beginning.
					groupIt = myRemoved.iterator();

				} else {
					groupIt.remove();
				}

			}

		}

		// nothing to remove
		if (myRemoved.isEmpty()) {
			return;
		}

		// remove flattened group memberships
		final Iterator<K> remove = myRemoved.iterator();
		while (remove.hasNext()) {
			this.implied.deIndex(k1, remove.next());
		}

		final Iterator<Pair<K, K>> left = constraintIterator(new Wildcard<K>(), c1);
		while (left.hasNext()) {
			final K k = left.next().getFirst();
			final boolean rw = (write || myRemoved != removed) && !left.hasNext();
			maybeRemovedFromGroups(k, k1, myRemoved, rw);
		}

	}

}
