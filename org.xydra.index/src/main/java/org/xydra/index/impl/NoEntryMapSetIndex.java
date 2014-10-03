package org.xydra.index.impl;

import org.xydra.index.IEntrySet;
import org.xydra.index.IMapSetIndex;
import org.xydra.index.iterator.NoneIterator;
import org.xydra.index.query.Constraint;
import org.xydra.index.query.KeyEntryTuple;

import java.util.Iterator;

/**
 * An empty, read-only {@link IMapSetIndex}.
 * 
 * @author voelkel
 * @param <K>
 * 
 * @param <E>
 *            entity type
 */
public class NoEntryMapSetIndex<K, E> implements IMapSetIndex<K, E> {

	private static final long serialVersionUID = -4800590888115736374L;

	@Override
	public org.xydra.index.IMapSetIndex.IMapSetDiff<K, E> computeDiff(IMapSetIndex<K, E> otherFuture) {

		// comparing this to other: all content has been added, nothing has been
		// removed
		return new EmptyMapSetDiff(otherFuture);
	}

	class EmptyMapSetDiff implements IMapSetDiff<K, E> {

		private IMapSetIndex<K, E> added;

		public EmptyMapSetDiff(IMapSetIndex<K, E> other) {
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
	public Iterator<E> constraintIterator(Constraint<K> c1) {
		return NoneIterator.<E> create();
	}

	@Override
	public boolean contains(Constraint<K> c1, Constraint<E> entryConstraint) {
		return false;
	}

	@Override
	public boolean containsKey(K key) {
		return false;
	}

	@Override
	public boolean deIndex(K key1, E entry) {
		throw new RuntimeException("this index is not meant to write");
	}

	@Override
	public void deIndex(K key1) {
		throw new RuntimeException("this index is not meant to write");
	}

	@Override
	public boolean index(K key1, E entry) {
		throw new RuntimeException("this index is not meant to write");
	}

	@Override
	public Iterator<KeyEntryTuple<K, E>> tupleIterator(Constraint<K> c1,
			Constraint<E> entryConstraint) {
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
	public IEntrySet<E> lookup(K key) {
		return null;
	}

	@Override
	public boolean contains(K k, E e) {
		return false;
	}

}
