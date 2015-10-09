package org.xydra.index.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.xydra.index.Factory;
import org.xydra.index.IEntrySet;
import org.xydra.index.IMapMapSetIndex;
import org.xydra.index.IMapSetIndex;
import org.xydra.index.IMapSetIndex.IMapSetDiff;
import org.xydra.index.IPair;
import org.xydra.index.iterator.AbstractCascadedIterator;
import org.xydra.index.iterator.ITransformer;
import org.xydra.index.iterator.Iterators;
import org.xydra.index.iterator.NoneIterator;
import org.xydra.index.query.Constraint;
import org.xydra.index.query.EqualsConstraint;
import org.xydra.index.query.GenericKeyEntryTupleConstraintFilteringIterator;
import org.xydra.index.query.ITriple;
import org.xydra.index.query.KeyEntryTuple;
import org.xydra.index.query.KeyKeyEntryTuple;
import org.xydra.index.query.Pair;
import org.xydra.index.query.Wildcard;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;

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

	public static class DiffImpl<K, L, E> implements IMapMapSetDiff<K, L, E> {

		protected MapMapSetIndex<K, L, E> added;
		protected MapMapSetIndex<K, L, E> removed;

		public DiffImpl(final Factory<IEntrySet<E>> entrySetFactory) {
			this.added = new MapMapSetIndex<K, L, E>(entrySetFactory);
			this.removed = new MapMapSetIndex<K, L, E>(entrySetFactory);
		}

		@Override
		public IMapMapSetIndex<K, L, E> getAdded() {
			return this.added;
		}

		@Override
		public IMapMapSetIndex<K, L, E> getRemoved() {
			return this.removed;
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

	/* needed for sp_tupleIterator() */
	private class SP_RememberKeyIterator<A, B> implements Iterator<KeyEntryTuple<A, B>> {

		private final A key;
		private final Iterator<B> subIterator;

		public SP_RememberKeyIterator(final A key, final Iterator<B> subIterator) {
			this.key = key;
			this.subIterator = subIterator;
		}

		@Override
		public boolean hasNext() {
			return this.subIterator.hasNext();
		}

		@Override
		public KeyEntryTuple<A, B> next() {
			final B b = this.subIterator.next();
			return new KeyEntryTuple<A, B>(this.key, b);
		}

		@Override
		public void remove() {
			this.subIterator.remove();
		}
	}

	private static final Logger log = LoggerFactory.getLogger(MapMapSetIndex.class);

	public static <K, L, E> MapMapSetIndex<K, L, E> createWithFastSets() {
		return new MapMapSetIndex<K, L, E>(new FastEntrySetFactory<E>());
	}

	public static <K, L, E> MapMapSetIndex<K, L, E> createWithSmallSets() {
		return new MapMapSetIndex<K, L, E>(new SmallEntrySetFactory<E>());
	}

	private final Factory<IEntrySet<E>> entrySetFactory;

	private final Map<K, IMapSetIndex<L, E>> map = new HashMap<K, IMapSetIndex<L, E>>(2);

	private final Wildcard<E> STAR_E = new Wildcard<E>();

	// reduce object creation at runtime, recycling these
	private final Wildcard<K> STAR_K = new Wildcard<K>();

	private final Wildcard<L> STAR_L = new Wildcard<L>();

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

	public static <K, L, E> IMapMapSetDiff<K, L, E> computeDiff(final MapMapSetIndex<K, L, E> mmsi,
			final MapMapSetIndex<K, L, E> otherIndex) {
		final DiffImpl<K, L, E> diff = new DiffImpl<K, L, E>(mmsi.entrySetFactory);

		for (final Entry<K, IMapSetIndex<L, E>> thisEntry : mmsi.map.entrySet()) {
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
			if (!mmsi.containsKey(key)) {
				// whole set (key,*,*) missing in this => added
				diff.added.map.put(key, otherEntry.getValue());
			}
		}

		return diff;
	}

	@Override
	public IMapMapSetDiff<K, L, E> computeDiff(final IMapMapSetIndex<K, L, E> otherFuture) {
		return computeDiff(this,  (MapMapSetIndex<K, L, E>) otherFuture);
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
		return contains(toConstraint_K(c1), toConstraint_L(c2), toConstraint(entryConstraint));
	}

	private boolean containsKey(final K key) {
		return this.map.containsKey(key);
	}

	public boolean deIndex(final K key1, final L key2) {
		final IMapSetIndex<L, E> index1 = this.map.get(key1);
		if (index1 != null) {
			if (index1 instanceof SingleEntryMapSetIndex<?, ?>) {
				// special remove of single entry map
				return this.map.remove(key1) != null;
			} else {
				// normal remove
				final boolean contains = index1.deIndex(key2);
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
	public void dump() {
		final StringBuilder b = new StringBuilder();

		// sort keys
		final List<K> keys = Iterators.toList(this.keyIterator());
		Collections.sort(keys, new Comparator<K>() {

			@Override
			public int compare(final K a, final K b) {
				return a.toString().compareTo(b.toString());
			}
		});
		for (final K key : keys) {
			final Iterator<ITriple<K, L, E>> it = this.tupleIterator(new EqualsConstraint<K>(key), this.STAR_L,
					this.STAR_E);
			while (it.hasNext()) {
				final ITriple<K, L, E> t = it.next();
				b.append("(" + t.getKey1() + ", " + t.getKey2() + ", " + t.getEntry() + ")\n");
			}
		}
		log.info(b.toString());
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final MapMapSetIndex other = (MapMapSetIndex) obj;
		if (this.map == null) {
			if (other.map != null) {
				return false;
			}
		} else if (!this.map.equals(other.map)) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (this.map == null ? 0 : this.map.hashCode());
		return result;
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
	public Iterator<K> keyIterator() {
		return this.map.keySet().iterator();
	}

	@Override
	public Iterator<KeyEntryTuple<K, L>> keyKeyIterator(final Constraint<K> c1, final Constraint<L> c2) {
		assert c1 != null;
		assert c2 != null;
		if (c1 instanceof Wildcard<?>) {
			final Iterator<Map.Entry<K, IMapSetIndex<L, E>>> entryIt = this.map.entrySet().iterator();

			// performance gain
			if (!entryIt.hasNext()) {
				return NoneIterator.create();
			}

			return Iterators.cascade(entryIt,
					new ITransformer<Map.Entry<K, IMapSetIndex<L, E>>, Iterator<KeyEntryTuple<K, L>>>() {

						@Override
						public Iterator<KeyEntryTuple<K, L>> transform(final Entry<K, IMapSetIndex<L, E>> in) {
							final K key = in.getKey();
							return Iterators.transform(in.getValue().keyIterator(),
									new ITransformer<L, KeyEntryTuple<K, L>>() {

								@Override
								public KeyEntryTuple<K, L> transform(final L in) {
									return new KeyEntryTuple<K, L>(key, in);
								}
							});
						}
					});
		} else if (c1 instanceof EqualsConstraint<?>) {
			final EqualsConstraint<K> keyConstraint = (EqualsConstraint<K>) c1;
			final K key = keyConstraint.getKey();
			final IMapSetIndex<L, E> index1 = this.map.get(key);
			if (index1 == null) {
				return NoneIterator.<KeyEntryTuple<K, L>> create();
			} else {
				return new SP_RememberKeyIterator<K, L>(key,

				Iterators.filterWithConstraint(index1.keyIterator(), c2)

				);
			}
		} else {
			throw new AssertionError("unknown constraint type " + c1.getClass());
		}
	}

	@Override
	public IMapSetIndex<L, E> lookup(final K k) {
		return this.map.get(k);
	}

	/**
	 * @param key1 @NeverNull
	 * @param key2 @NeverNull
	 * @return @CanBeNull
	 */
	public IEntrySet<E> lookup(final K key1, final L key2) {
		final IMapSetIndex<L, E> e = this.map.get(key1);
		if (e == null) {
			return null;
		}

		return e.lookup(key2);
	}

	private Constraint<E> toConstraint(final E entryConstraint) {
		return entryConstraint == null ? this.STAR_E : new EqualsConstraint<E>(entryConstraint);
	}

	private Constraint<K> toConstraint_K(final K c1) {
		return c1 == null ? this.STAR_K : new EqualsConstraint<K>(c1);
	}

	private Constraint<L> toConstraint_L(final L c2) {
		return c2 == null ? this.STAR_L : new EqualsConstraint<L>(c2);
	}

	@Override
	public String toString() {
		return toString("");
	}

	@Override
	public String toString(final String indent) {
		final StringBuilder b = new StringBuilder();

		final List<IPair<String, K>> keyMap = new ArrayList<>(this.map.size());
		for (final K key : this.map.keySet()) {
			keyMap.add(new Pair<>(key.toString(), key));
		}
		Collections.sort(keyMap, new Comparator<IPair<String, K>>() {

			@Override
			public int compare(final IPair<String, K> a, final IPair<String, K> b) {
				return a.getFirst().compareTo(b.getFirst());
			}

		});

		for (final IPair<String, K> pair : keyMap) {
			b.append(indent);
			b.append(pair.getFirst());
			b.append(" -> \n");
			final IMapSetIndex<L, E> subMap = this.map.get(pair.getSecond());
			b.append(subMap.toString(indent + "  "));
		}

		return b.toString();
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
	public Iterator<ITriple<K, L, E>> tupleIterator(final K c1, final L c2, final E entryConstraint) {
		// IMPROVE could be implemented faster if all indexes supported
		// Constraint-less querying
		return tupleIterator(toConstraint_K(c1), toConstraint_L(c2), toConstraint(entryConstraint));
	}

}
