package org.xydra.index.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.xydra.index.Factory;
import org.xydra.index.IEntrySet;
import org.xydra.index.IEntrySet.IEntrySetDiff;
import org.xydra.index.IMapSetIndex;
import org.xydra.index.IPair;
import org.xydra.index.iterator.AbstractCascadedIterator;
import org.xydra.index.iterator.ClosableIterator;
import org.xydra.index.iterator.ClosableIteratorAdapter;
import org.xydra.index.iterator.ITransformer;
import org.xydra.index.iterator.Iterators;
import org.xydra.index.iterator.NoneIterator;
import org.xydra.index.query.Constraint;
import org.xydra.index.query.EqualsConstraint;
import org.xydra.index.query.GenericKeyEntryTupleConstraintFilteringIterator;
import org.xydra.index.query.KeyEntryTuple;
import org.xydra.index.query.Pair;
import org.xydra.index.query.Wildcard;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;

/**
 *
 * Note: This class is not {@link Serializable}, due to many non-static (due to genrics) anonymous inner classes. With
 * some work, using Object#readObject this could be made {@link Serializable}.
 *
 * @author xamde
 * @param <K>
 * @param <E>
 */
public abstract class AbstractMapSetIndex<K, E> implements IMapSetIndex<K, E> {

	private static Logger log;

	private static void ensureLogger() {
		if (log == null) {
			log = LoggerFactory.getLogger(AbstractMapSetIndex.class);
		}
	}

	/* needed for tupleIterator() */
	private class AdaptMapEntryToTupleIterator implements Iterator<KeyEntryTuple<K, E>> {

		private final Entry<K, IEntrySet<E>> base;
		private final Iterator<E> it;

		public AdaptMapEntryToTupleIterator(final Entry<K, IEntrySet<E>> base) {
			this.base = base;
			this.it = base.getValue().iterator();
		}

		@Override
		public boolean hasNext() {
			return this.it.hasNext();
		}

		@Override
		public KeyEntryTuple<K, E> next() {
			return new KeyEntryTuple<K, E>(this.base.getKey(), this.it.next());
		}

		@Override
		public void remove() {
			this.it.remove();
		}

	}

	/* needed for constraintIterator() */
	private class CascadingEntrySetIterator extends AbstractCascadedIterator<IEntrySet<E>, E> {
		public CascadingEntrySetIterator(final Iterator<IEntrySet<E>> base) {
			super(base);
		}

		@Override
		protected Iterator<E> toIterator(final IEntrySet<E> baseEntry) {
			return baseEntry.iterator();
		}
	}

	/* needed for tupleIterator() */
	private class CascadingMapEntry_K_EntrySet_Iterator
			extends AbstractCascadedIterator<Map.Entry<K, IEntrySet<E>>, KeyEntryTuple<K, E>> {

		public CascadingMapEntry_K_EntrySet_Iterator(final Iterator<Map.Entry<K, IEntrySet<E>>> base) {
			super(base);
		}

		@Override
		protected Iterator<KeyEntryTuple<K, E>> toIterator(final Map.Entry<K, IEntrySet<E>> baseEntry) {
			return new AdaptMapEntryToTupleIterator(baseEntry);
		}

	}

	public static class DiffImpl<K, E> implements IMapSetDiff<K, E> {

		protected IMapSetIndex<K, E> added;
		protected IMapSetIndex<K, E> removed;

		@Override
		public IMapSetIndex<K, E> getAdded() {
			return this.added;
		}

		@Override
		public IMapSetIndex<K, E> getRemoved() {
			return this.removed;
		}

	}

	public static class LocalDiffImpl<K, E> implements IMapSetDiff<K, E> {

		protected AbstractMapSetIndex<K, E> added;
		protected AbstractMapSetIndex<K, E> removed;

		public LocalDiffImpl(final Factory<IEntrySet<E>> entrySetFactory) {
			this.added = new MapSetIndex<K, E>(entrySetFactory);
			this.removed = new MapSetIndex<K, E>(entrySetFactory);
		}

		@Override
		public IMapSetIndex<K, E> getAdded() {
			return this.added;
		}

		@Override
		public IMapSetIndex<K, E> getRemoved() {
			return this.removed;
		}

	}

	/* needed for tupleIterator() */
	private class AddKeyIterator implements Iterator<KeyEntryTuple<K, E>> {

		private final Iterator<E> base;
		private final K key;

		public AddKeyIterator(final K key, final Iterator<E> base) {
			this.base = base;
			this.key = key;
		}

		@Override
		public boolean hasNext() {
			return this.base.hasNext();
		}

		@Override
		public KeyEntryTuple<K, E> next() {
			final E entry = this.base.next();
			return new KeyEntryTuple<K, E>(this.key, entry);
		}

		@Override
		public void remove() {
			this.base.remove();
		}

	}

	private Factory<IEntrySet<E>> entrySetFactory;

	private Map<K, IEntrySet<E>> map;

	public AbstractMapSetIndex(final Factory<IEntrySet<E>> entrySetFactory) {
		this(entrySetFactory, false);
	}

	/**
	 * @param entrySetFactory
	 * @param concurrent
	 */
	public AbstractMapSetIndex(final Factory<IEntrySet<E>> entrySetFactory, final boolean concurrent) {
		if (concurrent) {
			this.map = new ConcurrentHashMap<K, IEntrySet<E>>(4);
		} else {
			this.map = new HashMap<K, IEntrySet<E>>(4);
		}
		this.entrySetFactory = entrySetFactory;
	}

	@Override
	public void clear() {
		this.map.clear();
	}

	@Override
	public IMapSetDiff<K, E> computeDiff(final IMapSetIndex<K, E> otherFuture) {
		if (otherFuture instanceof AbstractMapSetIndex<?, ?>) {
			return computeDiff_MapSetIndex((AbstractMapSetIndex<K, E>) otherFuture);
		} // else:
		final IMapSetDiff<K, E> twistedDiff = otherFuture.computeDiff(this);
		final DiffImpl<K, E> diff = new DiffImpl<K, E>();
		diff.added = twistedDiff.getRemoved();
		diff.removed = twistedDiff.getAdded();
		return diff;
	}

	private IMapSetDiff<K, E> computeDiff_MapSetIndex(final AbstractMapSetIndex<K, E> otherIndex) {
		final LocalDiffImpl<K, E> diff = new LocalDiffImpl<K, E>(this.entrySetFactory);

		for (final Entry<K, IEntrySet<E>> thisEntry : this.map.entrySet()) {
			final K key = thisEntry.getKey();
			final IEntrySet<E> otherValue = otherIndex.map.get(key);
			if (otherValue != null) {
				// same (key,*) entry, compare sets
				final IEntrySetDiff<E> setDiff = thisEntry.getValue().computeDiff(otherValue);
				if (!setDiff.getAdded().isEmpty()) {
					diff.added.map.put(key, setDiff.getAdded());
				}
				if (!setDiff.getRemoved().isEmpty()) {
					diff.removed.map.put(key, setDiff.getRemoved());
				}
			} else {
				// whole set (key,*) missing in other => removed
				diff.removed.map.put(key, thisEntry.getValue());
			}
		}

		// compare other to this
		for (final Entry<K, IEntrySet<E>> otherEntry : otherIndex.map.entrySet()) {
			final K key = otherEntry.getKey();
			if (!this.map.containsKey(key)) {
				// other has it, this does not => added
				diff.added.map.put(key, otherEntry.getValue());
			}
			// we treated the case of same key in the loop above
		}

		return diff;
	}

	@Override
	public ClosableIterator<E> constraintIterator(final Constraint<K> c1) {
		if (c1.isStar()) {
			return new CascadingEntrySetIterator(this.map.values().iterator());
		} else if (c1 instanceof EqualsConstraint<?>) {
			final EqualsConstraint<K> keyConstraint = (EqualsConstraint<K>) c1;
			final K key = keyConstraint.getKey();
			return valueIterator(key);
		} else {
			throw new AssertionError("unknown constraint type " + c1.getClass());
		}
	}

	/**
	 * @param key
	 * @return roughly the equivalent of {@link #lookup(Object)}.iterator
	 */
	public ClosableIterator<E> valueIterator(final K key) {
		final IEntrySet<E> index0 = this.map.get(key);
		return index0 == null ? NoneIterator.<E> create() : new ClosableIteratorAdapter<E>(index0.iterator());
	}

	@Override
	public boolean contains(final Constraint<K> c1, final Constraint<E> entryConstraint) {
		// IMPROVE can this be sped up?

		if (c1.isStar()) {
			if (entryConstraint.isStar()) {
				return !this.map.isEmpty();
			} else {
				assert entryConstraint instanceof EqualsConstraint<?>;
				final E entry = ((EqualsConstraint<E>) entryConstraint).getKey();
				for (final IEntrySet<E> e : this.map.values()) {
					if (e.contains(entry)) {
						return true;
					}
				}
				return false;
			}
		} else {
			assert c1 instanceof EqualsConstraint<?>;
			final K key = ((EqualsConstraint<K>) c1).getKey();
			if (entryConstraint.isStar()) {
				return this.map.containsKey(key);
			} else {
				assert entryConstraint instanceof EqualsConstraint<?>;
				final E entry = ((EqualsConstraint<E>) entryConstraint).getKey();
				return contains(key, entry);
			}
		}
	}

	@Override
	public boolean contains(final K key, final E entry) {
		final IEntrySet<E> index0 = this.map.get(key);
		return index0 != null && index0.contains(entry);
	}

	@Override
	public boolean containsKey(final K key) {
		return this.map.containsKey(key);
	}

	@Override
	public boolean deIndex(final K key) {
		return this.map.remove(key) != null;
	}

	public void deIndexAll(final Iterable<K> keys) {
		for (final K key : keys) {
			deIndex(key);
		}
	}

	@Override
	public boolean deIndex(final K key1, final E entry) {
		final IEntrySet<E> index0 = this.map.get(key1);
		if (index0 == null) {
			return false;
		} else {
			final boolean removed = index0.deIndex(entry);
			if (index0.isEmpty()) {
				this.map.remove(key1);
			}
			return removed;
		}
	}

	/**
	 * Dump the contents to Xydra Logging as log.info(...)
	 */
	public void dump() {
		ensureLogger();
		final Iterator<KeyEntryTuple<K, E>> it = tupleIterator(new Wildcard<K>(), new Wildcard<E>());

		final List<KeyEntryTuple<K, E>> list = Iterators.firstNtoList(it, 1000);
		if (it.hasNext()) {
			// iterator has over 1000 elements, ignore sort order
			for (final KeyEntryTuple<K, E> t : list) {
				dumpTuple(t);
			}
			while (it.hasNext()) {
				final KeyEntryTuple<K, E> t = it.next();
				dumpTuple(t);
			}
		} else {
			Collections.sort(list, new Comparator<KeyEntryTuple<K, E>>() {

				@SuppressWarnings({ "unchecked", "rawtypes" })
				@Override
				public int compare(final KeyEntryTuple<K, E> o1, final KeyEntryTuple<K, E> o2) {

					final K k1 = o1.getKey();
					final K k2 = o2.getKey();

					if (k1 instanceof Comparable) {
						return ((Comparable) k1).compareTo(k2);
					} else {
						return k1.toString().compareTo(k2.toString());
					}
				}
			});
			for (final KeyEntryTuple<K, E> t : list) {
				dumpTuple(t);
			}
		}

	}

	private void dumpTuple(final KeyEntryTuple<K, E> t) {
		log.info("(" + t.getFirst() + ", " + t.getSecond() + ")");
	}

	/**
	 * @return the internal map.entrySet
	 */
	public Set<Entry<K, IEntrySet<E>>> getEntries() {
		return this.map.entrySet();
	}

	@Override
	public boolean index(final K key1, final E entry) {
		IEntrySet<E> index0 = this.map.get(key1);
		if (index0 == null) {
			index0 = this.entrySetFactory.createInstance();
			this.map.put(key1, index0);
		}
		return index0.index(entry);
	}

	@Override
	public boolean isEmpty() {
		return this.map.isEmpty();
	}

	@Override
	public Iterator<K> keyIterator() {
		return this.map.keySet().iterator();
	}

	public Set<K> keySet() {
		return this.map.keySet();
	}

	public Iterator<E> values() {
		final Iterator<Entry<K, IEntrySet<E>>> it = this.map.entrySet().iterator();
		return Iterators.cascade(it, new ITransformer<Entry<K, IEntrySet<E>>, Iterator<E>>() {

			@Override
			public Iterator<E> transform(final Entry<K, IEntrySet<E>> in) {
				return in.getValue().iterator();
			}

		});
	}

	@Override
	public IEntrySet<E> lookup(final K key) {
		return this.map.get(key);
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
			b.append(" -> ");

			final IEntrySet<E> subMap = this.map.get(pair.getSecond());
			if(subMap.size() > 1) {
				b.append("\n");
				b.append(indent);
				b.append("  ");
			}
			b.append(subMap.toString());
			b.append("\n");
		}

		return b.toString();
	}

	@Override
	public Iterator<KeyEntryTuple<K, E>> tupleIterator(final Constraint<K> c1, final Constraint<E> entryConstraint) {
		assert c1 != null;
		assert entryConstraint != null;

		if (c1.isStar()) {
			final Iterator<Map.Entry<K, IEntrySet<E>>> entryIt = this.map.entrySet().iterator();
			if (!entryIt.hasNext()) {
				return NoneIterator.create();
			}
			// cascade to tuples
			final Iterator<KeyEntryTuple<K, E>> cascaded = new CascadingMapEntry_K_EntrySet_Iterator(entryIt);
			if (entryConstraint.isStar()) {
				return cascaded;
			} else {
				// filter entries
				final Iterator<KeyEntryTuple<K, E>> filtered = new GenericKeyEntryTupleConstraintFilteringIterator<KeyEntryTuple<K, E>, E>(
						cascaded, entryConstraint);
				return filtered;
			}
		} else if (c1 instanceof EqualsConstraint<?>) {
			final EqualsConstraint<K> keyConstraint = (EqualsConstraint<K>) c1;
			final K key = keyConstraint.getKey();
			final IEntrySet<E> index0 = this.map.get(key);
			if (index0 == null) {
				return NoneIterator.<KeyEntryTuple<K, E>> create();
			} else {
				return new AddKeyIterator(key, index0.constraintIterator(entryConstraint));
			}
		} else {
			throw new AssertionError("unknown constraint type " + c1.getClass());
		}
	}

	/**
	 * @return number of keys
	 */
	public int size() {
		return this.map.size();
	}

}
