package org.xydra.index.impl;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.xydra.index.IEntrySet;
import org.xydra.index.IMapSetIndex;
import org.xydra.index.XI;
import org.xydra.index.impl.SmallSetIndex.SmallEntrySetDiff;
import org.xydra.index.iterator.NoneIterator;
import org.xydra.index.iterator.SingleValueIterator;
import org.xydra.index.query.Constraint;
import org.xydra.index.query.EqualsConstraint;
import org.xydra.index.query.KeyEntryTuple;

/**
 * An {@link IMapSetIndex} that can store exactly one key-entry mapping. Small.
 * 
 * @author voelkel
 * 
 * @param <K>
 *            key type
 * @param <E>
 *            entry type
 */
public class SingleEntryMapSetIndex<K, E> extends KeyEntryTuple<K, E> implements IMapSetIndex<K, E>, Serializable {

	private static final long serialVersionUID = 3040641314902060159L;

	boolean empty = false;

	public SingleEntryMapSetIndex(K key, E value) {
		super(key, value);
	}

	@Override
	public void clear() {
		this.empty = true;
	}

	@Override
	public boolean containsKey(K key) {
		return !this.empty && XI.equals(key, getKey());
	}

	public boolean containsValue(E value) {
		return !this.empty && XI.equals(value, getEntry());
	}

	public E get(K key) {
		if (containsKey(key)) {
			return getEntry();
		} else
			return null;
	}

	@Override
	public boolean isEmpty() {
		return this.empty;
	}

	public int size() {
		return isEmpty() ? 0 : 1;
	}

	@Override
	public Iterator<E> constraintIterator(Constraint<K> c1) {
		if (!isEmpty() && c1.matches(getKey())) {
			return new SingleValueIterator<E>(getEntry());
		} else {
			return NoneIterator.<E> create();
		}
	}

	@Override
	public boolean contains(Constraint<K> c1, Constraint<E> entryConstraint) {

		if (isEmpty()) {
			return false;
		}

		// 1. component
		if (!c1.matches(getKey())) {
			return false;
		}

		// 2nd component
		return entryConstraint.matches(getEntry());
	}

	@Override
	public boolean contains(K key, E entry) {
		if (isEmpty()) {
			return false;
		}
		return getKey().equals(key) && getEntry().equals(entry);
	}

	@Override
	public boolean deIndex(K key1, E entry) {
		boolean contains = this.getKey() == key1 && this.getEntry() == entry;
		this.clear();
		return contains;
	}

	@Override
	public void deIndex(K key1) {
		/*
		 * This implementation can at most store a single entry, so clear is
		 * correct
		 */
		this.clear();
	}

	@Override
	public boolean index(K key1, E entry) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Iterator<KeyEntryTuple<K, E>> tupleIterator(Constraint<K> c1,
			Constraint<E> entryConstraint) {
		if (contains(c1, entryConstraint)) {
			return new SingleValueIterator<KeyEntryTuple<K, E>>(this);
		} else {
			return NoneIterator.<KeyEntryTuple<K, E>> create();
		}
	}

	@Override
	public IMapSetDiff<K, E> computeDiff(IMapSetIndex<K, E> otherFuture) {
		SingleEntryMapSetDiff<K, E> diff = new SingleEntryMapSetDiff<K, E>();

		if (this.getKey() == null) {
			diff.added = otherFuture;
			diff.removed = new NoEntryMapSetIndex<K, E>();
		} else {
			// if not null, the Key-Value is either

			// a) still present => no removes
			// b) no longer present => one remove

			// c) many other values might have been added
			diff.added = otherFuture;

			if (otherFuture.contains(new EqualsConstraint<K>(getKey()), new EqualsConstraint<E>(
					getEntry()))) {
				// still present => no adds, no removes
				diff.removed = new NoEntryMapSetIndex<K, E>();
				diff.added.deIndex(getKey(), getEntry());
			} else {
				// missing? so everything besides this has been added
				diff.removed = this;
			}
		}

		return diff;
	}

	public static class SingleEntryMapSetDiff<K, E> implements IMapSetDiff<K, E> {

		protected IMapSetIndex<K, E> added, removed;

		@Override
		public IMapSetIndex<K, E> getAdded() {
			return this.added;
		}

		@Override
		public IMapSetIndex<K, E> getRemoved() {
			return this.removed;
		}

	}

	@Override
	public Iterator<K> keyIterator() {
		return new SingleValueIterator<K>(getKey());
	}

	@Override
	public IEntrySet<E> lookup(K key) {
		if (isEmpty()) {
			return null;
		}

		if (getKey().equals(key)) {
			SmallSetIndex<E> set = new SmallSetIndex<E>();
			set.add(getEntry());
			return set;
		} else {
			return null;
		}
	}

	class EntrySet implements IEntrySet<E>, Serializable{

		private static final long serialVersionUID = 1L;

		@Override
		public void clear() {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean isEmpty() {
			return SingleEntryMapSetIndex.this.isEmpty();
		}

		@Override
		public Iterator<E> iterator() {
			return isEmpty() ? NoneIterator.<E> create() : new SingleValueIterator<E>(
					SingleEntryMapSetIndex.this.getEntry());
		}

		@Override
		public boolean deIndex(E entry) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean index(E entry) {
			throw new UnsupportedOperationException();
		}

		@Override
		public org.xydra.index.IEntrySet.IEntrySetDiff<E> computeDiff(IEntrySet<E> other) {
			SmallEntrySetDiff<E> diff = new SmallSetIndex.SmallEntrySetDiff<E>();
			// this entryset can contain only 0 or 1 entry
			if (isEmpty()) {
				diff.added = other;
				diff.removed = new SmallSetIndex<E>();
			} else {
				// a) still present => no removes
				// b) no longer present => one remove
				// c) many other values might have been added
				diff.added = other;
				if (other.contains(getEntry())) {
					// still present => no adds, no removes
					diff.removed = new SmallSetIndex<E>();
					diff.added.deIndex(getEntry());
				} else {
					// missing? so everything besides this has been added
					diff.removed = this;
				}
			}
			return diff;
		}

		@Override
		public boolean contains(E entry) {
			return !isEmpty() && SingleEntryMapSetIndex.this.getEntry().equals(entry);
		}

		@Override
		public Iterator<E> constraintIterator(Constraint<E> entryConstraint) {
			if (isEmpty()) {
				return NoneIterator.<E> create();
			} else {
				return new SingleValueIterator<E>(getEntry());
			}
		}

		@Override
		public int size() {
			return SingleEntryMapSetIndex.this.isEmpty() ? 0 : 1;
		}

		@Override
		public Set<E> toSet() {
			Set<E> set = new HashSet<E>();
			if (!isEmpty())
				set.add(getEntry());
			return set;
		}

	}

}
