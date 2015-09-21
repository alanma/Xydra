package org.xydra.index.impl;

import java.io.Serializable;
import java.util.Iterator;

import org.xydra.index.IMapIndex;
import org.xydra.index.XI;
import org.xydra.index.iterator.NoneIterator;
import org.xydra.index.iterator.SingleValueIterator;
import org.xydra.index.query.Constraint;
import org.xydra.index.query.IndexFullException;
import org.xydra.index.query.KeyEntryTuple;

/**
 * An implementation of {@link IMapIndex} that can hold exactly one entry.
 *
 * @author dscharrer
 * @param <K>
 * @param <E>
 *
 */
public class SmallMapIndex<K, E> implements IMapIndex<K, E>, Serializable {

	private static final long serialVersionUID = 2037583029777355928L;

	KeyEntryTuple<K, E> tuple;

	@Override
	public boolean containsKey(final K key) {
		return this.tuple != null && XI.equals(key, this.tuple.getKey());
	}

	@Override
	public boolean containsKey(final Constraint<K> c1) {
		return this.tuple != null && c1.matches(this.tuple.getKey());
	}

	@Override
	public void deIndex(final K key1) {
		if (containsKey(key1)) {
			this.tuple = null;
		}
	}

	@Override
	public void index(final K key1, final E entry) {
		if (this.tuple != null && !containsKey(key1)) {
			throw new IndexFullException();
		}
		this.tuple = new KeyEntryTuple<K, E>(key1, entry);
	}

	@Override
	public Iterator<E> iterator() {
		if (this.tuple == null) {
			return NoneIterator.<E> create();
		}
		return new SingleValueIterator<E>(this.tuple.getEntry()) {
			@Override
			public void remove() {
				clear();
			}
		};
	}

	@Override
	public E lookup(final K key) {
		if (!containsKey(key)) {
			return null;
		}
		return this.tuple.getEntry();
	}

	@Override
	public Iterator<KeyEntryTuple<K, E>> tupleIterator(final Constraint<K> c1) {
		if (c1.isExact() && !containsKey(c1)) {
			return NoneIterator.<KeyEntryTuple<K, E>> create();
		}
		return tupleIterator();
	}

	@Override
	public void clear() {
		this.tuple = null;
	}

	@Override
	public boolean isEmpty() {
		return this.tuple == null;
	}

	@Override
	public String toString() {
		if (this.tuple == null) {
			return "{}";
		} else {
			return "{" + this.tuple.getKey() + "=" + this.tuple.getEntry() + "}";
		}
	}

	@Override
	public Iterator<K> keyIterator() {
		if (isEmpty()) {
			return NoneIterator.<K> create();
		}
		return new SingleValueIterator<K>(this.tuple.getKey());
	}

	@Override
	public Iterator<KeyEntryTuple<K, E>> tupleIterator() {
		return new SingleValueIterator<KeyEntryTuple<K, E>>(this.tuple) {
			@Override
			public void remove() {
				clear();
			}
		};
	}

}
