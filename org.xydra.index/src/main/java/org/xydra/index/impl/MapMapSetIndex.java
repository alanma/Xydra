package org.xydra.index.impl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.xydra.index.Factory;
import org.xydra.index.IEntrySet;
import org.xydra.index.IMapMapSetIndex;
import org.xydra.index.IMapSetIndex;
import org.xydra.index.IMapSetIndex.IMapSetDiff;
import org.xydra.index.iterator.AbstractCascadedIterator;
import org.xydra.index.iterator.NoneIterator;
import org.xydra.index.query.Constraint;
import org.xydra.index.query.EqualsConstraint;
import org.xydra.index.query.GenericKeyEntryTupleConstraintFilteringIterator;
import org.xydra.index.query.ITriple;
import org.xydra.index.query.KeyEntryTuple;
import org.xydra.index.query.KeyKeyEntryTuple;
import org.xydra.index.query.Wildcard;

/**
 * <pre>
 * K -> ( L -> Set(E) )
 * </pre>
 *
 * @author xamde
 * @param <K>
 * @param <L>
 * @param <E>
 */
public class MapMapSetIndex<K, L, E> implements IMapMapSetIndex<K, L, E> {

	// reduce object creation at runtime, recycling these
	private final Wildcard<K> STAR_K = new Wildcard<K>();
	private final Wildcard<L> STAR_L = new Wildcard<L>();
	private final Wildcard<E> STAR_E = new Wildcard<E>();

	/* needed for tupleIterator()
	 *
	 * This iterator knows key1. Together with the values from the tuple (tuple-key, tuple-entry) it forms the
	 * result-tuples (key, tuple-key, tuple-entry) */
	private static class AdaptMapEntryToTupleIterator<K, L, E> implements Iterator<ITriple<K, L, E>> {

		private final K key1;
		private final Iterator<KeyEntryTuple<L, E>> tupleIterator;

		public AdaptMapEntryToTupleIterator(final K key1, final Iterator<KeyEntryTuple<L, E>> tupleIterator) {
			this.key1 = key1;
			this.tupleIterator = tupleIterator;
		}

		@Override
		public boolean hasNext() {
			return this.tupleIterator.hasNext();
		}

		@Override
		public ITriple<K, L, E> next() {
			if (this.tupleIterator.hasNext()) {
				final KeyEntryTuple<L, E> x = this.tupleIterator.next();
				return new KeyKeyEntryTuple<K, L, E>(this.key1, x.getKey(), x.getEntry());
			} else {
				return null;
			}
		}

		@Override
		public void remove() {
			this.tupleIterator.remove();
		}

	}

	/* needed for tupleIterator() */
	private static class CascadingMapEntry_K_MapSet_Iterator<K, L, E>
	extends AbstractCascadedIterator<Map.Entry<K, IMapSetIndex<L, E>>, ITriple<K, L, E>> {

		private final Constraint<L> c1;

		private final Constraint<E> entryConstraint;

		public CascadingMapEntry_K_MapSet_Iterator(final Iterator<Map.Entry<K, IMapSetIndex<L, E>>> base,
				final Constraint<L> c1, final Constraint<E> entryConstraint) {
			super(base);
			assert c1 != null;
			assert entryConstraint != null;
			this.c1 = c1;
			this.entryConstraint = entryConstraint;
		}

		@Override
		protected Iterator<ITriple<K, L, E>> toIterator(final Entry<K, IMapSetIndex<L, E>> baseEntry) {
			final Iterator<KeyEntryTuple<L, E>> baseTuples = baseEntry.getValue().tupleIterator(this.c1,
					this.entryConstraint);

			// performance gain?
			if (!baseTuples.hasNext()) {
				return NoneIterator.create();
			}

			return new AdaptMapEntryToTupleIterator<K, L, E>(baseEntry.getKey(), baseTuples);
		}

	}

	/* needed for constraintIterator() */
	private class CascadingMapSetIndexIterator extends AbstractCascadedIterator<IMapSetIndex<L, E>, E> {
		private final Constraint<L> c1;

		public CascadingMapSetIndexIterator(final Iterator<IMapSetIndex<L, E>> base, final Constraint<L> c1) {
			super(base);
			this.c1 = c1;
		}

		@Override
		protected Iterator<E> toIterator(final IMapSetIndex<L, E> baseEntry) {
			return baseEntry.constraintIterator(this.c1);
		}
	}

	/* needed for tupleIterator() */
	private class RememberKeyIterator implements Iterator<ITriple<K, L, E>> {

		private final K key;
		private final Iterator<KeyEntryTuple<L, E>> tupleIterator;

		public RememberKeyIterator(final K key, final Iterator<KeyEntryTuple<L, E>> tupleIterator) {
			this.key = key;
			this.tupleIterator = tupleIterator;
		}

		@Override
		public boolean hasNext() {
			return this.tupleIterator.hasNext();
		}

		@Override
		public ITriple<K, L, E> next() {
			final KeyEntryTuple<L, E> e = this.tupleIterator.next();
			return new KeyKeyEntryTuple<K, L, E>(this.key, e.getKey(), e.getEntry());
		}

		@Override
		public void remove() {
			this.tupleIterator.remove();
		}
	}

	private final Map<K, IMapSetIndex<L, E>> map = new HashMap<K, IMapSetIndex<L, E>>(2);

	// experimental extension
	public Iterator<K> key1Iterator() {
		return this.map.keySet().iterator();
	}

	// experimental extension
	public IMapSetIndex<L, E> getMapEntry(final K k) {
		return this.map.get(k);
	}

	private final Factory<IEntrySet<E>> entrySetFactory;

	/**
	 * @param entrySetFactory Theis factory configures the trade-off between time and space. Existing factories you can
	 *        use are {@link FastEntrySetFactory} and {@link SmallEntrySetFactory}.
	 */
	public MapMapSetIndex(final Factory<IEntrySet<E>> entrySetFactory) {
		this.entrySetFactory = entrySetFactory;
	}

	@Override
	public void clear() {
		this.map.clear();
	}

	@Override
	public Iterator<E> constraintIterator(final Constraint<K> c1, final Constraint<L> c2) {
		if (c1 instanceof Wildcard<?>) {
			return new CascadingMapSetIndexIterator(this.map.values().iterator(), c2);
		} else if (c1 instanceof EqualsConstraint<?>) {
			final EqualsConstraint<K> keyConstraint = (EqualsConstraint<K>) c1;
			final K key = keyConstraint.getKey();
			final IMapSetIndex<L, E> index1 = this.map.get(key);
			return index1 == null ? NoneIterator.<E> create() : index1.constraintIterator(c2);
		} else {
			throw new AssertionError("unknown constraint type " + c1.getClass());
		}
	}

	@Override
	public boolean contains(final Constraint<K> c1, final Constraint<L> c2, final Constraint<E> entryConstraint) {
		final Iterator<E> c1it = this.constraintIterator(c1, c2);
		while (c1it.hasNext()) {
			final E entry = c1it.next();
			if (entryConstraint.matches(entry)) {
				// we found any element, that's good enough
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean contains(final K c1, final L c2, final E entryConstraint) {
		// IMPROVE could be implemented faster if all indexes supported
		// Constraint-less querying
		return contains(

				c1 == null ? this.STAR_K : new EqualsConstraint<K>(c1),

						c2 == null ? this.STAR_L : new EqualsConstraint<L>(c2),

								entryConstraint == null ? this.STAR_E : new EqualsConstraint<E>(entryConstraint)

				);
	}

	/**
	 * @param key1
	 * @NeverNull
	 * @param key2
	 * @NeverNull
	 * @return @CanBeNull
	 */
	public IEntrySet<E> lookup(final K key1, final L key2) {
		final IMapSetIndex<L, E> e = this.map.get(key1);
		if (e == null) {
			return null;
		}

		return e.lookup(key2);
	}

	public static class DiffImpl<K, L, E> implements IMapMapSetDiff<K, L, E> {

		protected MapMapSetIndex<K, L, E> added;
		protected MapMapSetIndex<K, L, E> removed;

		@Override
		public IMapMapSetIndex<K, L, E> getAdded() {
			return this.added;
		}

		@Override
		public IMapMapSetIndex<K, L, E> getRemoved() {
			return this.removed;
		}

		public DiffImpl(final Factory<IEntrySet<E>> entrySetFactory) {
			this.added = new MapMapSetIndex<K, L, E>(entrySetFactory);
			this.removed = new MapMapSetIndex<K, L, E>(entrySetFactory);
		}

	}

	@Override
	public IMapMapSetDiff<K, L, E> computeDiff(final IMapMapSetIndex<K, L, E> otherFuture) {
		final DiffImpl<K, L, E> diff = new DiffImpl<K, L, E>(this.entrySetFactory);

		final MapMapSetIndex<K, L, E> otherIndex = (MapMapSetIndex<K, L, E>) otherFuture;

		for (final Entry<K, IMapSetIndex<L, E>> thisEntry : this.map.entrySet()) {
			final K key = thisEntry.getKey();
			final IMapSetIndex<L, E> otherValue = otherIndex.map.get(key);
			if (otherValue != null) {
				// same (key,*,*) entry, compare sub-maps
				final IMapSetDiff<L, E> mapSetDiff = thisEntry.getValue().computeDiff(otherValue);
				if (!mapSetDiff.getAdded().isEmpty()) {
					diff.added.map.put(key, mapSetDiff.getAdded());
				}
				if (!mapSetDiff.getRemoved().isEmpty()) {
					diff.removed.map.put(key, mapSetDiff.getRemoved());
				}
			} else {
				// missing in other => removed a complete sub-tree (key,*,*)
				diff.removed.map.put(thisEntry.getKey(), thisEntry.getValue());
			}
		}

		// compare other to this
		for (final Entry<K, IMapSetIndex<L, E>> otherEntry : otherIndex.map.entrySet()) {
			final K key = otherEntry.getKey();
			if (!this.containsKey(key)) {
				// whole set (key,*,*) missing in this => added
				diff.added.map.put(key, otherEntry.getValue());
			}
		}

		return diff;
	}

	private boolean containsKey(final K key) {
		return this.map.containsKey(key);
	}

	@Override
	public boolean deIndex(final K key1, final L key2, final E entry) {
		final IMapSetIndex<L, E> index1 = this.map.get(key1);
		if (index1 != null) {
			if (index1 instanceof SingleEntryMapSetIndex<?, ?>) {
				// special remove of single entry map
				return this.map.remove(key1) != null;
			} else {
				// normal remove
				final boolean contains = index1.deIndex(key2, entry);
				if (index1.isEmpty()) {
					this.map.remove(key1);
				}
				return contains;
			}
		} else {
			return false;
		}
	}

	@Override
	public boolean index(final K key1, final L key2, final E entry) {
		IMapSetIndex<L, E> index1 = this.map.get(key1);
		if (index1 == null) {
			index1 = new SingleEntryMapSetIndex<L, E>(key2, entry);
			this.map.put(key1, index1);
			return true;
		} else {
			// we need to index more than one (*,K,E)
			if (index1 instanceof SingleEntryMapSetIndex<?, ?>) {
				// put a flexible map instead
				final L k = ((SingleEntryMapSetIndex<L, E>) index1).getKey();
				final E e = ((SingleEntryMapSetIndex<L, E>) index1).getEntry();
				index1 = new MapSetIndex<L, E>(this.entrySetFactory);
				this.map.put(key1, index1);
				// add existing single entry.
				index1.index(k, e);
			}
			// always index the new stuff
			return index1.index(key2, entry);
		}
	}

	@Override
	public boolean isEmpty() {
		return this.map.isEmpty();
	}

	@Override
	public Iterator<E> iterator() {
		return this.constraintIterator(new Wildcard<K>(), new Wildcard<L>());
	}

	@Override
	public Iterator<ITriple<K, L, E>> tupleIterator(final Constraint<K> c1, final Constraint<L> c2,
			final Constraint<E> entryConstraint) {
		assert c1 != null;
		assert c2 != null;
		assert entryConstraint != null;
		if (c1 instanceof Wildcard<?>) {
			final Iterator<Map.Entry<K, IMapSetIndex<L, E>>> entryIt = this.map.entrySet().iterator();

			// performance gain
			if (!entryIt.hasNext()) {
				return NoneIterator.create();
			}

			// cascade to tuples
			final Iterator<ITriple<K, L, E>> cascaded = new CascadingMapEntry_K_MapSet_Iterator<K, L, E>(entryIt, c2,
					entryConstraint);
			// filter entries
			final Iterator<ITriple<K, L, E>> filtered = new GenericKeyEntryTupleConstraintFilteringIterator<ITriple<K, L, E>, E>(
					cascaded, entryConstraint);
			return filtered;
		} else if (c1 instanceof EqualsConstraint<?>) {
			final EqualsConstraint<K> keyConstraint = (EqualsConstraint<K>) c1;
			final K key = keyConstraint.getKey();
			final IMapSetIndex<L, E> index1 = this.map.get(key);
			if (index1 == null) {
				return NoneIterator.<ITriple<K, L, E>> create();
			} else {
				return new RememberKeyIterator(key, index1.tupleIterator(c2, entryConstraint));
			}
		} else {
			throw new AssertionError("unknown constraint type " + c1.getClass());
		}
	}

	@Override
	public String toString() {
		return this.map.toString();
	}

	@Override
	public Iterator<K> keyIterator() {
		return this.map.keySet().iterator();
	}

	@Override
	public Iterator<ITriple<K, L, E>> tupleIterator(final K c1, final L c2, final E entryConstraint) {
		// IMPROVE could be implemented faster if all indexes supported
		// Constraint-less querying
		return tupleIterator(

				c1 == null ? this.STAR_K : new EqualsConstraint<K>(c1),

						c2 == null ? this.STAR_L : new EqualsConstraint<L>(c2),

								entryConstraint == null ? this.STAR_E : new EqualsConstraint<E>(entryConstraint)

				);
	}

	public void dump() {
		final Iterator<ITriple<K, L, E>> it = tupleIterator(this.STAR_K, this.STAR_L, this.STAR_E);
		while (it.hasNext()) {
			final ITriple<K, L, E> e = it.next();
			System.out.println("(" + e.getKey1() + ", " + e.getKey2() + ", " + e.getEntry() + ")");
		}
	}

	public static <K, L, E> MapMapSetIndex<K, L, E> createWithSmallSets() {
		return new MapMapSetIndex<K, L, E>(new SmallEntrySetFactory<E>());
	}

	public static <K, L, E> MapMapSetIndex<K, L, E> createWithFastSets() {
		return new MapMapSetIndex<K, L, E>(new FastEntrySetFactory<E>());
	}

}
