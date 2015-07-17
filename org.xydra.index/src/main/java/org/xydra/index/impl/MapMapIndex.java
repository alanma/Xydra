package org.xydra.index.impl;

import java.io.Serializable;
import java.util.Iterator;

import org.xydra.index.IMapIndex;
import org.xydra.index.IMapMapIndex;
import org.xydra.index.iterator.AbstractCascadedIterator;
import org.xydra.index.iterator.ITransformer;
import org.xydra.index.iterator.NoneIterator;
import org.xydra.index.iterator.TransformingIterator;
import org.xydra.index.query.Constraint;
import org.xydra.index.query.EqualsConstraint;
import org.xydra.index.query.IndexFullException;
import org.xydra.index.query.KeyEntryTuple;
import org.xydra.index.query.KeyKeyEntryTuple;
import org.xydra.index.query.Wildcard;

/**
 * An implementation of {@link IMapMapIndex} using a IMapIndex of an IMapIndex. The IMapIndex is automatically switched
 * between a SmallMapIndex and a MapIndex as needed.
 *
 * The remove() method of iterators always works if c1 != * or c2 == *. Otherwise it might throw
 * UnsupportedOperationException.
 *
 * @author dscharrer
 *
 * @param <K> key1 type
 * @param <L> key2 type
 * @param <E> entity type
 */
public class MapMapIndex<K extends Serializable, L extends Serializable, E extends Serializable>
implements IMapMapIndex<K, L, E>, Serializable {

	protected IMapIndex<K, IMapIndex<L, E>> index;

	public MapMapIndex() {
		this.index = new SmallMapIndex<K, IMapIndex<L, E>>();
	}

	@Override
	public boolean containsKey(final Constraint<K> c1, final Constraint<L> c2) {
		if (c1.isStar()) {
			if (c2.isStar()) {
				return !isEmpty();
			} else {
				final L key2 = ((EqualsConstraint<L>) c2).getKey();
				final Iterator<IMapIndex<L, E>> it = this.index.iterator();
				while (it.hasNext()) {
					if (it.next().containsKey(key2)) {
						return true;
					}
				}
				return false;
			}
		}
		final K key1 = ((EqualsConstraint<K>) c1).getKey();
		final IMapIndex<L, E> map = this.index.lookup(key1);
		if (map == null) {
			return false;
		}

		if (c2.isStar()) {
			return true;
		} else {
			return map.containsKey(c2);
		}
	}

	@Override
	public void deIndex(final K key1, final L key2) {
		final IMapIndex<L, E> map = this.index.lookup(key1);
		if (map == null) {
			return;
		}
		map.deIndex(key2);
		if (map.isEmpty()) {
			this.index.deIndex(key1);
		}
	}

	@Override
	public void index(final K key1, final L key2, final E entry) {
		IMapIndex<L, E> map = this.index.lookup(key1);
		if (map == null) {
			map = new SmallMapIndex<L, E>();
			try {
				this.index.index(key1, map);
			} catch (final IndexFullException e) {
				final IMapIndex<K, IMapIndex<L, E>> newMap = new MapIndex<K, IMapIndex<L, E>>();
				final Iterator<KeyEntryTuple<K, IMapIndex<L, E>>> it = this.index.tupleIterator(new Wildcard<K>());
				while (it.hasNext()) {
					final KeyEntryTuple<K, IMapIndex<L, E>> tuple = it.next();
					newMap.index(tuple.getKey(), tuple.getEntry());
				}
				newMap.index(key1, map);
				this.index = newMap;
			}
		}
		try {
			map.index(key2, entry);
		} catch (final IndexFullException e) {
			final IMapIndex<L, E> newMap = new MapIndex<L, E>();
			final Iterator<KeyEntryTuple<L, E>> it = map.tupleIterator(new Wildcard<L>());
			while (it.hasNext()) {
				final KeyEntryTuple<L, E> tuple = it.next();
				newMap.index(tuple.getKey(), tuple.getEntry());
			}
			newMap.index(key2, entry);
			this.index.index(key1, newMap);
		}
	}

	@Override
	public E lookup(final K key1, final L key2) {
		final IMapIndex<L, E> map = this.index.lookup(key1);
		if (map == null) {
			return null;
		}
		return map.lookup(key2);
	}

	@Override
	public Iterator<KeyKeyEntryTuple<K, L, E>> tupleIterator(final Constraint<K> c1, final Constraint<L> c2) {
		if (c1.isStar()) {
			return new CascadingIterator<K, L, E>(this.index.tupleIterator(c1), c2);
		}
		final K key1 = ((EqualsConstraint<K>) c1).getKey();
		final IMapIndex<L, E> map = this.index.lookup(key1);
		if (map == null) {
			return NoneIterator.<KeyKeyEntryTuple<K, L, E>> create();
		}
		return new FixedFirstKeyIterator(key1, map, c2);
	}

	static private class CascadingIterator<K extends Serializable, L extends Serializable, E extends Serializable>
	implements Iterator<KeyKeyEntryTuple<K, L, E>> {

		Iterator<KeyEntryTuple<K, IMapIndex<L, E>>> outer;
		K key1;
		IMapIndex<L, E> map;
		Iterator<KeyEntryTuple<L, E>> inner;
		Iterator<KeyEntryTuple<L, E>> last;
		Constraint<L> c;

		public CascadingIterator(final Iterator<KeyEntryTuple<K, IMapIndex<L, E>>> it, final Constraint<L> c) {
			this.outer = it;
			this.c = c;
		}

		@Override
		public boolean hasNext() {

			// if the inner constraint is * we can assume that inner maps
			// always have at least one element
			// this allows the remove() method to work in that case
			if (this.c.isStar()) {
				return this.outer.hasNext() || this.inner != null && this.inner.hasNext();
			}

			while (this.inner == null || !this.inner.hasNext()) {
				if (!this.outer.hasNext()) {
					return false;
				}
				nextInner();
			}

			return true;

		}

		private void nextInner() {
			final KeyEntryTuple<K, IMapIndex<L, E>> tuple = this.outer.next();
			this.key1 = tuple.getKey();
			this.map = tuple.getEntry();
			this.inner = this.map.tupleIterator(this.c);
		}

		@Override
		public KeyKeyEntryTuple<K, L, E> next() {

			while (this.inner == null || !this.inner.hasNext()) {
				if (!this.outer.hasNext()) {
					return null;
				}
				nextInner();
			}

			this.last = this.inner;

			final KeyEntryTuple<L, E> tuple = this.inner.next();
			if (tuple == null) {
				return null;
			}
			return new KeyKeyEntryTuple<K, L, E>(this.key1, tuple.getKey(), tuple.getEntry());
		}

		@Override
		public void remove() {

			// should also remove when last != inner, but can't as outer already
			// is at the next inner map and modifying the map outside of the
			// iterator can cause undefined behavior
			if (this.last != this.inner) {
				throw new UnsupportedOperationException();
			}

			this.last.remove();

			if (this.map.isEmpty()) {
				this.last = this.inner = null;
				this.outer.remove();
			}
		}

	}

	private class FixedFirstKeyIterator implements Iterator<KeyKeyEntryTuple<K, L, E>> {

		private final K key1;
		private final IMapIndex<L, E> map;
		private final Iterator<KeyEntryTuple<L, E>> base;

		public FixedFirstKeyIterator(final K key1, final IMapIndex<L, E> map, final Constraint<L> c) {
			this.key1 = key1;
			this.map = map;
			this.base = map.tupleIterator(c);
		}

		@Override
		public void remove() {
			this.base.remove();
			if (this.map.isEmpty()) {
				MapMapIndex.this.index.deIndex(this.key1);
			}
		}

		@Override
		public boolean hasNext() {
			return this.base.hasNext();
		}

		@Override
		public KeyKeyEntryTuple<K, L, E> next() {
			final KeyEntryTuple<L, E> in = this.base.next();
			if (in == null) {
				return null;
			}
			return new KeyKeyEntryTuple<K, L, E>(this.key1, in.getKey(), in.getEntry());
		}

	}

	@Override
	public void clear() {
		this.index.clear();
	}

	@Override
	public boolean isEmpty() {
		return this.index.isEmpty();
	}

	@Override
	public String toString() {
		return this.index.toString();
	}

	@Override
	public Iterator<K> key1Iterator() {
		return this.index.keyIterator();
	}

	@Override
	public Iterator<L> key2Iterator() {
		// maybe slightly faster than Iterators.cascade(...)
		return new AbstractCascadedIterator<IMapIndex<L, E>, L>(this.index.iterator()) {

			@Override
			protected Iterator<? extends L> toIterator(final IMapIndex<L, E> baseEntry) {
				return baseEntry.keyIterator();
			}

		};
	}

	public Iterator<E> entryIterator() {
		// IMPROVE this can be done faster if never creating intermediate tuples
		return new TransformingIterator<KeyKeyEntryTuple<K, L, E>, E>(
				this.tupleIterator(new Wildcard<K>(), new Wildcard<L>()),
				new ITransformer<KeyKeyEntryTuple<K, L, E>, E>() {

					@Override
					public E transform(final KeyKeyEntryTuple<K, L, E> in) {
						return in.getEntry();
					}
				});
	}

	public boolean containsTuple(final K key1, final L key2, final E entry) {
		final IMapIndex<L, E> entry1 = this.index.lookup(key1);
		if (entry == null) {
			return false;
		}
		final E entry2 = entry1.lookup(key2);
		if (entry2 == null) {
			return false;
		}
		return entry2.equals(entry);
	}

	public Iterator<IMapIndex<L, E>> getIterator(final K key1) {
		return this.index.iterator();
	}

	public boolean containsKey1(final K key1) {
		return this.index.containsKey(key1);
	}

}
