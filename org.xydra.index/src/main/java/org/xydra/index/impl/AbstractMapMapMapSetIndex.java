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
import org.xydra.index.IMapMapMapSetIndex;
import org.xydra.index.IMapMapSetIndex;
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
import org.xydra.index.query.KeyKeyKeyEntryTuple;
import org.xydra.index.query.Pair;
import org.xydra.index.query.Wildcard;

/**
 * <pre>
 * K -> ( L -> ( M -> Set(E) ) )
 * </pre>
 *
 * @author xamde
 * @param <K>
 * @param <L>
 * @param <M>
 * @param <E>
 */
public abstract class AbstractMapMapMapSetIndex<K, L, M, E, MMSI_KLM extends IMapMapSetIndex<K, L, M>, MMSI_LME extends IMapMapSetIndex<L, M, E>>
		implements IMapMapMapSetIndex<K, L, M, E> {

	/* needed for tupleIterator()
	 *
	 * This iterator knows key1. Together with the values from the tuple (tuple-key, tuple-entry) it forms the
	 * result-tuples (key, tuple-key, tuple-entry) */
	private class AdaptMapEntryToTupleIterator implements Iterator<KeyKeyKeyEntryTuple<K, L, M, E>> {

		private final K key1;
		private final Iterator<ITriple<L, M, E>> tupleIterator;

		/**
		 * @param key1
		 * @param tupleIterator
		 */
		protected AdaptMapEntryToTupleIterator(final K key1, final Iterator<ITriple<L, M, E>> tupleIterator) {
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
				final ITriple<L, M, E> x = this.tupleIterator.next();
				return new KeyKeyKeyEntryTuple<K, L, M, E>(this.key1, x.getKey1(), x.getKey2(), x.getEntry());
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
	private class CascadingMapEntry_K_MapMapSet_Iterator
			extends AbstractCascadedIterator<Map.Entry<K, MMSI_LME>, KeyKeyKeyEntryTuple<K, L, M, E>> {

		private final Constraint<L> c1;
		private final Constraint<M> c2;
		private final Constraint<E> entryConstraint;

		protected CascadingMapEntry_K_MapMapSet_Iterator(final Iterator<Map.Entry<K, MMSI_LME>> base,
				final Constraint<L> c1, final Constraint<M> c2, final Constraint<E> entryConstraint) {
			super(base);
			this.c1 = c1;
			this.c2 = c2;
			this.entryConstraint = entryConstraint;
		}

		@Override
		protected Iterator<KeyKeyKeyEntryTuple<K, L, M, E>> toIterator(final Entry<K, MMSI_LME> baseEntry) {

			return new AdaptMapEntryToTupleIterator(baseEntry.getKey(),
					baseEntry.getValue().tupleIterator(this.c1, this.c2, this.entryConstraint));
		}

	}

	/* needed for constraintIterator() */
	private class CascadingMapMapSetIndexIterator extends AbstractCascadedIterator<MMSI_LME, E> {
		private final Constraint<L> c1;
		private final Constraint<M> c2;

		protected CascadingMapMapSetIndexIterator(final Iterator<MMSI_LME> base, final Constraint<L> c1,
				final Constraint<M> c2) {
			super(base);
			this.c1 = c1;
			this.c2 = c2;
		}

		@Override
		protected Iterator<E> toIterator(final MMSI_LME baseEntry) {
			return baseEntry.constraintIterator(this.c1, this.c2);
		}
	}

	/* needed for tupleIterator() */
	private class RememberKeyIterator implements Iterator<KeyKeyKeyEntryTuple<K, L, M, E>> {

		private final K key;
		private final Iterator<ITriple<L, M, E>> tupleIterator;

		protected RememberKeyIterator(final K key, final Iterator<ITriple<L, M, E>> tupleIterator) {
			this.key = key;
			this.tupleIterator = tupleIterator;
		}

		@Override
		public boolean hasNext() {
			return this.tupleIterator.hasNext();
		}

		@Override
		public KeyKeyKeyEntryTuple<K, L, M, E> next() {
			final ITriple<L, M, E> e = this.tupleIterator.next();
			return new KeyKeyKeyEntryTuple<K, L, M, E>(this.key, e.getKey1(), e.getKey2(), e.getEntry());
		}

		@Override
		public void remove() {
			this.tupleIterator.remove();
		}
	}

	private final Factory<IEntrySet<E>> entrySetFactory;

	public AbstractMapMapMapSetIndex(final Factory<IEntrySet<E>> entrySetFactory) {
		super();
		this.map = new HashMap<K, MMSI_LME>(16);
		this.entrySetFactory = entrySetFactory;
	}

	private final Map<K, MMSI_LME> map;

	@Override
	public void clear() {
		this.map.clear();
	}

	@Override
	public Iterator<E> constraintIterator(final Constraint<K> c1, final Constraint<L> c2, final Constraint<M> c3) {
		if (c1 instanceof Wildcard<?>) {
			return new CascadingMapMapSetIndexIterator(this.map.values().iterator(), c2, c3);
		} else if (c1 instanceof EqualsConstraint<?>) {
			final EqualsConstraint<K> keyConstraint = (EqualsConstraint<K>) c1;
			final K key = keyConstraint.getKey();
			final IMapMapSetIndex<L, M, E> index2 = this.map.get(key);
			return index2 == null ? NoneIterator.<E> create() : index2.constraintIterator(c2, c3);
		} else {
			throw new AssertionError("unknown constraint type " + c2.getClass());
		}
	}

	public boolean containsKey(final K key) {
		return this.map.containsKey(key);
	}

	@Override
	public void deIndex(final K key1, final L key2, final M key3, final E entry) {
		final MMSI_LME index2 = this.map.get(key1);
		if (index2 != null) {
			index2.deIndex(key2, key3, entry);
			if (index2.isEmpty()) {
				this.map.remove(key1);
			}
		}
	}

	protected abstract MMSI_LME createMMSI_LME(Factory<IEntrySet<E>> entrySetFactory);

	@Override
	public void index(final K key1, final L key2, final M key3, final E entry) {
		MMSI_LME index2 = this.map.get(key1);
		if (index2 == null) {
			index2 = createMMSI_LME(this.entrySetFactory);
			this.map.put(key1, index2);
		}
		index2.index(key2, key3, entry);
	}

	@Override
	public boolean isEmpty() {
		return this.map.isEmpty();
	}

	@Override
	public Iterator<KeyKeyKeyEntryTuple<K, L, M, E>> tupleIterator(final Constraint<K> c1, final Constraint<L> c2,
			final Constraint<M> c3, final Constraint<E> entryConstraint) {
		if (c1 instanceof Wildcard<?>) {
			final Iterator<Map.Entry<K, MMSI_LME>> entryIt = this.map.entrySet().iterator();
			// cascade to tuples
			final Iterator<KeyKeyKeyEntryTuple<K, L, M, E>> cascaded = new CascadingMapEntry_K_MapMapSet_Iterator(
					entryIt, c2, c3, entryConstraint);
			// filter entries
			final Iterator<KeyKeyKeyEntryTuple<K, L, M, E>> filtered = new GenericKeyEntryTupleConstraintFilteringIterator<KeyKeyKeyEntryTuple<K, L, M, E>, E>(
					cascaded, entryConstraint);
			return filtered;
		} else if (c1 instanceof EqualsConstraint<?>) {
			final EqualsConstraint<K> keyConstraint = (EqualsConstraint<K>) c1;
			final K key = keyConstraint.getKey();
			final IMapMapSetIndex<L, M, E> index2 = this.map.get(key);
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
	public Iterator<KeyKeyEntryTuple<K, L, M>> keyKeyKeyIterator(final Constraint<K> c1, final Constraint<L> c2,
			final Constraint<M> c3) {
		if (c1 instanceof Wildcard<?>) {
			final Iterator<Map.Entry<K, MMSI_LME>> entryIt = this.map.entrySet().iterator();
			// cascade to tuples

			return Iterators.cascade(entryIt,
					new ITransformer<Map.Entry<K, MMSI_LME>, Iterator<KeyKeyEntryTuple<K, L, M>>>() {

						@Override
						public Iterator<KeyKeyEntryTuple<K, L, M>> transform(final Map.Entry<K, MMSI_LME> in) {
							final K key = in.getKey();
							return toKeyKeyEntryTuples(in.getValue(), key, c2, c3);
						}
					});
		} else if (c1 instanceof EqualsConstraint<?>) {
			final EqualsConstraint<K> keyConstraint = (EqualsConstraint<K>) c1;
			final K key = keyConstraint.getKey();
			final IMapMapSetIndex<L, M, E> index2 = this.map.get(key);
			if (index2 == null) {
				return NoneIterator.<KeyKeyEntryTuple<K, L, M>> create();
			} else {
				return toKeyKeyEntryTuples(index2, key, c2, c3);
			}
		} else {
			throw new AssertionError("unknown constraint type " + c1.getClass());
		}
	}

	protected Iterator<KeyKeyEntryTuple<K, L, M>> toKeyKeyEntryTuples(final IMapMapSetIndex<L, M, E> value, final K key,
			final Constraint<L> c2, final Constraint<M> c3) {
		return Iterators.transform(value.keyKeyIterator(c2, c3),
				new ITransformer<KeyEntryTuple<L, M>, KeyKeyEntryTuple<K, L, M>>() {

					@Override
					public KeyKeyEntryTuple<K, L, M> transform(final KeyEntryTuple<L, M> in) {
						return new KeyKeyEntryTuple<K, L, M>(key, in.getKey(), in.getEntry());
					}
				});
	}

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
			final MMSI_LME subMap = this.map.get(pair.getSecond());
			b.append(subMap.toString(indent + "  "));
		}

		return b.toString();
	}

	@Override
	public String toString() {
		return toString("");
	}

}
