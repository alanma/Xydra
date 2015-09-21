package org.xydra.index.impl;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.xydra.index.IMapIndex;
import org.xydra.index.iterator.AbstractTransformingIterator;
import org.xydra.index.iterator.NoneIterator;
import org.xydra.index.iterator.SingleValueIterator;
import org.xydra.index.query.Constraint;
import org.xydra.index.query.EqualsConstraint;
import org.xydra.index.query.KeyEntryTuple;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;

/**
 * An implementation of {@link IMapIndex} using a HashMap.
 *
 * @author dscharrer
 *
 * @param <K> key type
 * @param <E> entity type
 */
public class MapIndex<K, E> implements IMapIndex<K, E>, Serializable, Iterable<E> {

	private static final long serialVersionUID = -156688788520337376L;

	private final Map<K, E> index;

	public MapIndex() {
		this(false);
	}

	/**
	 * @param concurrent iff true, a concurrent data structure is used internally
	 */
	public MapIndex(final boolean concurrent) {
		if (concurrent) {
			this.index = new ConcurrentHashMap<K, E>();
		} else {
			this.index = new HashMap<K, E>();
		}
	}

	@Override
	public boolean containsKey(final K key) {
		return this.index.containsKey(key);
	}

	@Override
	public void deIndex(final K key1) {
		this.index.remove(key1);
	}

	@Override
	public void index(final K key1, final E entry) {
		this.index.put(key1, entry);
	}

	@Override
	public Iterator<E> iterator() {
		return this.index.values().iterator();
	}

	@Override
	public E lookup(final K key) {
		return this.index.get(key);
	}

	@Override
	public boolean containsKey(final Constraint<K> c1) {
		if (c1.isStar()) {
			return !isEmpty();
		} else {
			final K key = ((EqualsConstraint<K>) c1).getKey();
			return this.index.containsKey(key);
		}
	}

	@Override
	public Iterator<KeyEntryTuple<K, E>> tupleIterator(final Constraint<K> c1) {
		if (c1.isStar()) {
			return tupleIterator();
		}
		final K key = ((EqualsConstraint<K>) c1).getKey();
		if (this.index.containsKey(key)) {
			return new SingleValueIterator<KeyEntryTuple<K, E>>(new KeyEntryTuple<K, E>(key, this.index.get(key))) {
				@Override
				public void remove() {
					MapIndex.this.deIndex(key);
				}
			};
		} else {
			return NoneIterator.<KeyEntryTuple<K, E>> create();
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
	public Iterator<K> keyIterator() {
		return this.index.keySet().iterator();
	}

	private static final Logger log = LoggerFactory.getLogger(MapIndex.class);

	public void dump() {
		for (final Entry<K, E> e : this.index.entrySet()) {
			log.info("'" + e.getKey() + "' = '" + e.getValue() + "'");
		}
	}

	public int size() {
		return this.index.size();
	}

	@Override
	public Iterator<KeyEntryTuple<K, E>> tupleIterator() {
		return new AbstractTransformingIterator<Map.Entry<K, E>, KeyEntryTuple<K, E>>(
				this.index.entrySet().iterator()) {

			@Override
			public KeyEntryTuple<K, E> transform(final Entry<K, E> in) {
				return new KeyEntryTuple<K, E>(in.getKey(), in.getValue());
			}

		};
	}

}
