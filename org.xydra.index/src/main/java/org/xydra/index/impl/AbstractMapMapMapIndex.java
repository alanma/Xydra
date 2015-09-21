package org.xydra.index.impl;

import java.util.Iterator;

import org.xydra.index.IMapIndex;
import org.xydra.index.IMapMapIndex;
import org.xydra.index.IMapMapMapIndex;
import org.xydra.index.iterator.AbstractCascadedIterator;
import org.xydra.index.iterator.ITransformer;
import org.xydra.index.iterator.Iterators;
import org.xydra.index.iterator.NoneIterator;
import org.xydra.index.query.Constraint;
import org.xydra.index.query.EqualsConstraint;
import org.xydra.index.query.IndexFullException;
import org.xydra.index.query.KeyEntryTuple;
import org.xydra.index.query.KeyKeyEntryTuple;
import org.xydra.index.query.KeyKeyKeyEntryTuple;
import org.xydra.index.query.Wildcard;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;

/**
 * An implementation of {@link IMapMapMapIndex} using a IMapIndex of an IMapMapIndex. The IMapIndex is automatically
 * switched between a SmallMapIndex and a MapIndex as needed.
 *
 * The remove() method of iterators always works if c1 != * or (c2,c3) == (*,*). Otherwise it might throw
 * UnsupportedOperationException.
 *
 * @author dscharrer
 *
 * @param <K> key1 type
 * @param <L> key2 type
 * @param <M> key3 type
 * @param <E> entity type
 */
public abstract class AbstractMapMapMapIndex<K, L, M, E> implements IMapMapMapIndex<K, L, M, E> {

	protected IMapIndex<K, IMapMapIndex<L, M, E>> index;

	public AbstractMapMapMapIndex() {
		this.index = new SmallMapIndex<K, IMapMapIndex<L, M, E>>();
	}

	protected abstract IMapMapIndex<L, M, E> createMapMapIndex();

	@Override
	public boolean containsKey(final Constraint<K> c1, final Constraint<L> c2, final Constraint<M> c3) {
		if (c1.isStar()) {
			if (c2.isStar() && c3.isStar()) {
				return !isEmpty();
			} else {
				final Iterator<IMapMapIndex<L, M, E>> it = this.index.iterator();
				while (it.hasNext()) {
					if (it.next().containsKey(c2, c3)) {
						return true;
					}
				}
				return false;
			}
		}
		final K key1 = ((EqualsConstraint<K>) c1).getKey();
		final IMapMapIndex<L, M, E> map = this.index.lookup(key1);
		if (map == null) {
			return false;
		}
		return map.containsKey(c2, c3);
	}

	@Override
	public void deIndex(final K key1, final L key2, final M key3) {
		final IMapMapIndex<L, M, E> map = this.index.lookup(key1);
		if (map == null) {
			return;
		}
		map.deIndex(key2, key3);
		if (map.isEmpty()) {
			this.index.deIndex(key1);
		}
	}

	@Override
	public void index(final K key1, final L key2, final M key3, final E entry) {
		IMapMapIndex<L, M, E> map = this.index.lookup(key1);
		if (map == null) {
			map = createMapMapIndex();
			try {
				this.index.index(key1, map);
			} catch (final IndexFullException e) {
				final IMapIndex<K, IMapMapIndex<L, M, E>> newMap = new MapIndex<K, IMapMapIndex<L, M, E>>();
				final Iterator<KeyEntryTuple<K, IMapMapIndex<L, M, E>>> it = this.index
						.tupleIterator(new Wildcard<K>());
				while (it.hasNext()) {
					final KeyEntryTuple<K, IMapMapIndex<L, M, E>> tuple = it.next();
					newMap.index(tuple.getKey(), tuple.getEntry());
				}
				newMap.index(key1, map);
				this.index = newMap;
			}
		}
		map.index(key2, key3, entry);
	}

	@Override
	public E lookup(final K key1, final L key2, final M key3) {
		final IMapMapIndex<L, M, E> map = this.index.lookup(key1);
		if (map == null) {
			return null;
		}
		return map.lookup(key2, key3);
	}

	@Override
	public Iterator<KeyKeyKeyEntryTuple<K, L, M, E>> tupleIterator(final Constraint<K> c1, final Constraint<L> c2,
			final Constraint<M> c3) {
		if (c1.isStar()) {
			return new CascadingIterator<K, L, M, E>(this.index.tupleIterator(c1), c2, c3);
		}
		final K key1 = ((EqualsConstraint<K>) c1).getKey();
		final IMapMapIndex<L, M, E> map = this.index.lookup(key1);
		if (map == null) {
			return NoneIterator.<KeyKeyKeyEntryTuple<K, L, M, E>> create();
		}
		return new FixedFirstKeyIterator(key1, map, c2, c3);
	}

	static private class CascadingIterator<K, L, M, E> implements Iterator<KeyKeyKeyEntryTuple<K, L, M, E>> {

		Iterator<KeyEntryTuple<K, IMapMapIndex<L, M, E>>> outer;
		K key1;
		IMapMapIndex<L, M, E> map;
		Iterator<KeyKeyEntryTuple<L, M, E>> inner;
		Iterator<KeyKeyEntryTuple<L, M, E>> last;
		Constraint<L> c1;
		Constraint<M> c2;

		public CascadingIterator(final Iterator<KeyEntryTuple<K, IMapMapIndex<L, M, E>>> it, final Constraint<L> c1,
				final Constraint<M> c2) {
			this.outer = it;
			this.c1 = c1;
			this.c2 = c2;
		}

		@Override
		public boolean hasNext() {

			// if the inner constraint is (*,*) we can assume that inner maps
			// always have at least one element
			// this allows the remove() method to work in that case
			if (this.c1.isStar() && this.c2.isStar()) {
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
			final KeyEntryTuple<K, IMapMapIndex<L, M, E>> tuple = this.outer.next();
			this.key1 = tuple.getKey();
			this.map = tuple.getEntry();
			this.inner = this.map.tupleIterator(this.c1, this.c2);
		}

		@Override
		public KeyKeyKeyEntryTuple<K, L, M, E> next() {

			while (this.inner == null || !this.inner.hasNext()) {
				if (!this.outer.hasNext()) {
					return null;
				}
				nextInner();
			}

			this.last = this.inner;

			final KeyKeyEntryTuple<L, M, E> tuple = this.inner.next();
			if (tuple == null) {
				return null;
			}
			return new KeyKeyKeyEntryTuple<K, L, M, E>(this.key1, tuple.getKey1(), tuple.getKey2(), tuple.getEntry());
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

	private class FixedFirstKeyIterator implements Iterator<KeyKeyKeyEntryTuple<K, L, M, E>> {

		private final K key1;
		private final IMapMapIndex<L, M, E> map;
		private final Iterator<KeyKeyEntryTuple<L, M, E>> base;

		public FixedFirstKeyIterator(final K key1, final IMapMapIndex<L, M, E> map, final Constraint<L> c1,
				final Constraint<M> c2) {
			this.key1 = key1;
			this.map = map;
			this.base = map.tupleIterator(c1, c2);
		}

		@Override
		public void remove() {
			this.base.remove();
			if (this.map.isEmpty()) {
				AbstractMapMapMapIndex.this.index.deIndex(this.key1);
			}
		}

		@Override
		public boolean hasNext() {
			return this.base.hasNext();
		}

		@Override
		public KeyKeyKeyEntryTuple<K, L, M, E> next() {
			final KeyKeyEntryTuple<L, M, E> in = this.base.next();
			if (in == null) {
				return null;
			}
			return new KeyKeyKeyEntryTuple<K, L, M, E>(this.key1, in.getKey1(), in.getKey2(), in.getEntry());
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
		return new AbstractCascadedIterator<IMapMapIndex<L, M, E>, L>(this.index.iterator()) {

			@Override
			protected Iterator<? extends L> toIterator(final IMapMapIndex<L, M, E> baseEntry) {
				return baseEntry.key1Iterator();
			}

		};
	}

	@Override
	public Iterator<M> key3Iterator() {
		return new AbstractCascadedIterator<IMapMapIndex<L, M, E>, M>(this.index.iterator()) {

			@Override
			protected Iterator<? extends M> toIterator(final IMapMapIndex<L, M, E> baseEntry) {
				return baseEntry.key2Iterator();
			}

		};
	}

	@Override
	public Iterator<KeyKeyEntryTuple<K, L, M>> keyKeyKeyIterator(final Constraint<K> c1, final Constraint<L> c2,
			final Constraint<M> c3) {
		if (c1 instanceof Wildcard<?>) {

			final Iterator<KeyEntryTuple<K, IMapMapIndex<L, M, E>>> entryIt = this.index.tupleIterator();
			// cascade to tuples

			return Iterators.cascade(entryIt,
					new ITransformer<KeyEntryTuple<K, IMapMapIndex<L, M, E>>, Iterator<KeyKeyEntryTuple<K, L, M>>>() {

						@Override
						public Iterator<KeyKeyEntryTuple<K, L, M>> transform(
								final KeyEntryTuple<K, IMapMapIndex<L, M, E>> in) {
							final K key = in.getKey();
							return toKeyKeyEntryTuples(in.getEntry(), key, c2, c3);
						}
					});
		} else if (c1 instanceof EqualsConstraint<?>) {
			final EqualsConstraint<K> keyConstraint = (EqualsConstraint<K>) c1;
			final K key = keyConstraint.getKey();
			final IMapMapIndex<L, M, E> index2 = this.index.lookup(key);
			if (index2 == null) {
				return NoneIterator.<KeyKeyEntryTuple<K, L, M>> create();
			} else {
				return toKeyKeyEntryTuples(index2, key, c2, c3);
			}
		} else {
			throw new AssertionError("unknown constraint type " + c1.getClass());
		}
	}

	protected Iterator<KeyKeyEntryTuple<K, L, M>> toKeyKeyEntryTuples(final IMapMapIndex<L, M, E> value, final K key,
			final Constraint<L> c2, final Constraint<M> c3) {
		return Iterators.transform(value.keyKeyIterator(c2, c3),
				new ITransformer<KeyEntryTuple<L, M>, KeyKeyEntryTuple<K, L, M>>() {

					@Override
					public KeyKeyEntryTuple<K, L, M> transform(final KeyEntryTuple<L, M> in) {
						return new KeyKeyEntryTuple<K, L, M>(key, in.getKey(), in.getEntry());
					}
				});
	}

	private static final Logger log = LoggerFactory.getLogger(AbstractMapMapMapIndex.class);

	@Override
	public void dump() {
		final Iterator<KeyKeyKeyEntryTuple<K, L, M, E>> it = tupleIterator(new Wildcard<K>(), new Wildcard<L>(),
				new Wildcard<M>());
		final StringBuilder b = new StringBuilder();
		while (it.hasNext()) {
			final KeyKeyKeyEntryTuple<K, L, M, E> ket = it.next();
			b.append("(" + ket.s() + "," + ket.p() + "," + ket.o() + ")->" + ket.getEntry() + "\n");
		}
		log.info("Dump of KLM->E:" + b);
	}

}
