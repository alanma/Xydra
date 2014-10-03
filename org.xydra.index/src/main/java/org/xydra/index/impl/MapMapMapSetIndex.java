package org.xydra.index.impl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.xydra.index.Factory;
import org.xydra.index.IEntrySet;
import org.xydra.index.IMapMapMapSetIndex;
import org.xydra.index.IMapMapSetIndex;
import org.xydra.index.iterator.AbstractCascadedIterator;
import org.xydra.index.iterator.NoneIterator;
import org.xydra.index.query.Constraint;
import org.xydra.index.query.EqualsConstraint;
import org.xydra.index.query.GenericKeyEntryTupleConstraintFilteringIterator;
import org.xydra.index.query.ITriple;
import org.xydra.index.query.KeyKeyKeyEntryTuple;
import org.xydra.index.query.Wildcard;

public class MapMapMapSetIndex<K, L, M, E> implements IMapMapMapSetIndex<K, L, M, E> {

	private static final long serialVersionUID = -7284474841937046470L;

	/*
	 * needed for tupleIterator()
	 * 
	 * This iterator knows key1. Together with the values from the tuple
	 * (tuple-key, tuple-entry) it forms the result-tuples (key, tuple-key,
	 * tuple-entry)
	 */
	private class AdaptMapEntryToTupleIterator implements Iterator<KeyKeyKeyEntryTuple<K, L, M, E>> {

		private K key1;
		private Iterator<ITriple<L, M, E>> tupleIterator;

		/**
		 * @param key1
		 * @param tupleIterator
		 */
		protected AdaptMapEntryToTupleIterator(K key1, Iterator<ITriple<L, M, E>> tupleIterator) {
			this.key1 = key1;
			this.tupleIterator = tupleIterator;
		}

		@Override
		public boolean hasNext() {
			return this.tupleIterator.hasNext();
		}

		@Override
		public KeyKeyKeyEntryTuple<K, L, M, E> next() {
			if (this.tupleIterator.hasNext()) {
				ITriple<L, M, E> x = this.tupleIterator.next();
				return new KeyKeyKeyEntryTuple<K, L, M, E>(this.key1, x.getKey1(), x.getKey2(),
						x.getEntry());
			} else
				return null;
		}

		@Override
		public void remove() {
			this.tupleIterator.remove();
		}

	}

	/* needed for tupleIterator() */
	private class CascadingMapEntry_K_MapMapSet_Iterator
			extends
			AbstractCascadedIterator<Map.Entry<K, IMapMapSetIndex<L, M, E>>, KeyKeyKeyEntryTuple<K, L, M, E>> {

		private Constraint<L> c1;
		private Constraint<M> c2;
		private Constraint<E> entryConstraint;

		protected CascadingMapEntry_K_MapMapSet_Iterator(
				Iterator<Map.Entry<K, IMapMapSetIndex<L, M, E>>> base, Constraint<L> c1,
				Constraint<M> c2, Constraint<E> entryConstraint) {
			super(base);
			this.c1 = c1;
			this.c2 = c2;
			this.entryConstraint = entryConstraint;
		}

		@Override
		protected Iterator<KeyKeyKeyEntryTuple<K, L, M, E>> toIterator(
				Entry<K, IMapMapSetIndex<L, M, E>> baseEntry) {

			return new AdaptMapEntryToTupleIterator(baseEntry.getKey(), baseEntry.getValue()
					.tupleIterator(this.c1, this.c2, this.entryConstraint));
		}

	}

	/* needed for constraintIterator() */
	private class CascadingMapMapSetIndexIterator extends
			AbstractCascadedIterator<IMapMapSetIndex<L, M, E>, E> {
		private Constraint<L> c1;
		private Constraint<M> c2;

		protected CascadingMapMapSetIndexIterator(Iterator<IMapMapSetIndex<L, M, E>> base,
				Constraint<L> c1, Constraint<M> c2) {
			super(base);
			this.c1 = c1;
			this.c2 = c2;
		}

		@Override
		protected Iterator<E> toIterator(IMapMapSetIndex<L, M, E> baseEntry) {
			return baseEntry.constraintIterator(this.c1, this.c2);
		}
	}

	/* needed for tupleIterator() */
	private class RememberKeyIterator implements Iterator<KeyKeyKeyEntryTuple<K, L, M, E>> {

		private K key;
		private Iterator<ITriple<L, M, E>> tupleIterator;

		protected RememberKeyIterator(K key, Iterator<ITriple<L, M, E>> tupleIterator) {
			this.key = key;
			this.tupleIterator = tupleIterator;
		}

		@Override
		public boolean hasNext() {
			return this.tupleIterator.hasNext();
		}

		@Override
		public KeyKeyKeyEntryTuple<K, L, M, E> next() {
			ITriple<L, M, E> e = this.tupleIterator.next();
			return new KeyKeyKeyEntryTuple<K, L, M, E>(this.key, e.getKey1(), e.getKey2(),
					e.getEntry());
		}

		@Override
		public void remove() {
			this.tupleIterator.remove();
		}
	}

	private Factory<IEntrySet<E>> entrySetFactory;

	public MapMapMapSetIndex(Factory<IEntrySet<E>> entrySetFactory) {
		super();
		this.map = new HashMap<K, IMapMapSetIndex<L, M, E>>(16);
		this.entrySetFactory = entrySetFactory;
	}

	private Map<K, IMapMapSetIndex<L, M, E>> map;

	@Override
	public void clear() {
		this.map.clear();
	}

	@Override
	public Iterator<E> constraintIterator(Constraint<K> c1, Constraint<L> c2, Constraint<M> c3) {
		if (c1 instanceof Wildcard<?>) {
			return new CascadingMapMapSetIndexIterator(this.map.values().iterator(), c2, c3);
		} else if (c1 instanceof EqualsConstraint<?>) {
			EqualsConstraint<K> keyConstraint = (EqualsConstraint<K>) c1;
			K key = keyConstraint.getKey();
			IMapMapSetIndex<L, M, E> index2 = this.map.get(key);
			return index2 == null ? NoneIterator.<E> create() : index2.constraintIterator(c2, c3);
		} else {
			throw new AssertionError("unknown constraint type " + c2.getClass());
		}
	}

	public boolean containsKey(K key) {
		return this.map.containsKey(key);
	}

	@Override
	public void deIndex(K key1, L key2, M key3, E entry) {
		IMapMapSetIndex<L, M, E> index2 = this.map.get(key1);
		if (index2 != null) {
			index2.deIndex(key2, key3, entry);
			if (index2.isEmpty()) {
				this.map.remove(key1);
			}
		}
	}

	@Override
	public void index(K key1, L key2, M key3, E entry) {
		IMapMapSetIndex<L, M, E> index2 = this.map.get(key1);
		if (index2 == null) {
			index2 = new MapMapSetIndex<L, M, E>(this.entrySetFactory);
			this.map.put(key1, index2);
		}
		index2.index(key2, key3, entry);
	}

	@Override
	public boolean isEmpty() {
		return this.map.isEmpty();
	}

	@Override
	public Iterator<KeyKeyKeyEntryTuple<K, L, M, E>> tupleIterator(Constraint<K> c1,
			Constraint<L> c2, Constraint<M> c3, Constraint<E> entryConstraint) {
		if (c1 instanceof Wildcard<?>) {
			Iterator<Map.Entry<K, IMapMapSetIndex<L, M, E>>> entryIt = this.map.entrySet()
					.iterator();
			// cascade to tuples
			Iterator<KeyKeyKeyEntryTuple<K, L, M, E>> cascaded = new CascadingMapEntry_K_MapMapSet_Iterator(
					entryIt, c2, c3, entryConstraint);
			// filter entries
			Iterator<KeyKeyKeyEntryTuple<K, L, M, E>> filtered = new GenericKeyEntryTupleConstraintFilteringIterator<KeyKeyKeyEntryTuple<K, L, M, E>, E>(
					cascaded, entryConstraint);
			return filtered;
		} else if (c1 instanceof EqualsConstraint<?>) {
			EqualsConstraint<K> keyConstraint = (EqualsConstraint<K>) c1;
			K key = keyConstraint.getKey();
			IMapMapSetIndex<L, M, E> index2 = this.map.get(key);
			if (index2 == null) {
				return NoneIterator.<KeyKeyKeyEntryTuple<K, L, M, E>> create();
			} else {
				return new RememberKeyIterator(key, index2.tupleIterator(c2, c3, entryConstraint));
			}
		} else {
			throw new AssertionError("unknown constraint type " + c1.getClass());
		}
	}

	@Override
	public String toString() {
		return this.map.toString();
	}

}
