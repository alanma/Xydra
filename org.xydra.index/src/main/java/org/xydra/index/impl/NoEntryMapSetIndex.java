package org.xydra.index.impl;

import java.io.Serializable;
import java.util.Iterator;

import org.xydra.index.IEntrySet;
import org.xydra.index.IMapSetIndex;
import org.xydra.index.iterator.NoneIterator;
import org.xydra.index.query.Constraint;
import org.xydra.index.query.KeyEntryTuple;

/**
 * An empty, read-only {@link IMapSetIndex}.
 *
 * @author voelkel
 * @param <K>
 *
 * @param <E>
 *            entity type
 */
public class NoEntryMapSetIndex<K, E> implements IMapSetIndex<K, E>, Serializable {

	private static final long serialVersionUID = -4800590888115736374L;

	@Override
	public org.xydra.index.IMapSetIndex.IMapSetDiff<K, E> computeDiff(final IMapSetIndex<K, E> otherFuture) {

		// comparing this to other: all content has been added, nothing has been
		// removed
		return new EmptyMapSetDiff(otherFuture);
	}

	class EmptyMapSetDiff implements IMapSetDiff<K, E> {

		private final IMapSetIndex<K, E> added;

		public EmptyMapSetDiff(final IMapSetIndex<K, E> other) {
			this.added = other;
		}

		@Override
		public IMapSetIndex<K, E> getAdded() {
			return this.added;
		}

		@Override
		public IMapSetIndex<K, E> getRemoved() {
			return NoEntryMapSetIndex.this;
		}

	}

	@Override
	public Iterator<E> constraintIterator(final Constraint<K> c1) {
		return NoneIterator.<E> create();
	}

	@Override
	public boolean contains(final Constraint<K> c1, final Constraint<E> entryConstraint) {
		return false;
	}

	@Override
	public boolean containsKey(final K key) {
		return false;
	}

	@Override
	public boolean deIndex(final K key1, final E entry) {
		throw new RuntimeException("this index is not meant to write");
	}

	@Override
	public void deIndex(final K key1) {
		throw new RuntimeException("this index is not meant to write");
	}

	@Override
	public boolean index(final K key1, final E entry) {
		throw new RuntimeException("this index is not meant to write");
	}

	@Override
	public Iterator<KeyEntryTuple<K, E>> tupleIterator(final Constraint<K> c1,
			final Constraint<E> entryConstraint) {
		return NoneIterator.<KeyEntryTuple<K, E>> create();
	}

	@Override
	public void clear() {
		throw new RuntimeException("this index is not meant to write");
	}

	@Override
	public boolean isEmpty() {
		return true;
	}

	@Override
	public Iterator<K> keyIterator() {
		return NoneIterator.<K> create();
	}

	@Override
	public IEntrySet<E> lookup(final K key) {
		return null;
	}

	@Override
	public boolean contains(final K k, final E e) {
		return false;
	}

}
