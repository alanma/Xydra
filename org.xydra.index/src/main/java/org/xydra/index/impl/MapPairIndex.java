package org.xydra.index.impl;

import java.util.Iterator;

import org.xydra.index.IMapSetIndex;
import org.xydra.index.IPairIndex;
import org.xydra.index.iterator.AbstractTransformingIterator;
import org.xydra.index.query.Constraint;
import org.xydra.index.query.KeyEntryTuple;
import org.xydra.index.query.Pair;
import org.xydra.index.query.Wildcard;

/**
 * An implementation of IPairIndex that uses a map to allow efficient queries on
 * the first key.
 *
 * @author dscharrer
 * @param <K>
 * @param <L>
 */
public class MapPairIndex<K, L> implements IPairIndex<K, L> {

	/**
	 * key1 -> set of key2
	 */
	private final IMapSetIndex<K, L> index;

	public MapPairIndex() {
		this.index = new MapSetIndex<K, L>(new FastEntrySetFactory<L>());
	}

	@Override
	public void clear() {
		this.index.clear();
	}

	@Override
	public void index(final K k1, final L k2) {
		this.index.index(k1, k2);
	}

	@Override
	public void deIndex(final K k1, final L k2) {
		this.index.deIndex(k1, k2);
	}

	@Override
	public Iterator<Pair<K, L>> constraintIterator(final Constraint<K> c1, final Constraint<L> c2) {

		return new AbstractTransformingIterator<KeyEntryTuple<K, L>, Pair<K, L>>(
				this.index.tupleIterator(c1, c2)) {

			@Override
			public Pair<K, L> transform(final KeyEntryTuple<K, L> in) {
				return in;
			}

		};

	}

	@Override
	public boolean contains(final Constraint<K> c1, final Constraint<L> c2) {
		return this.index.contains(c1, c2);
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
	public Iterator<Pair<K, L>> iterator() {
		return constraintIterator(new Wildcard<K>(), new Wildcard<L>());
	}

	@Override
	public Iterator<K> key1Iterator() {
		return this.index.keyIterator();
	}

	@Override
	public Iterator<L> key2Iterator() {
		return new AbstractTransformingIterator<Pair<K, L>, L>(iterator()) {

			@Override
			public L transform(final Pair<K, L> in) {
				return in.getSecond();
			}

		};
	}

}
