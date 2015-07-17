package org.xydra.index.impl;

import java.io.Serializable;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.WeakHashMap;

import org.xydra.index.IEntrySet;
import org.xydra.index.iterator.NoneIterator;
import org.xydra.index.iterator.SingleValueIterator;
import org.xydra.index.query.Constraint;
import org.xydra.index.query.EqualsConstraint;

import com.google.common.collect.Sets;

public class FastWeakSetIndex<E> implements IEntrySet<E>, Serializable {

	private Set<E> set;

	private boolean concurrent;

	public FastWeakSetIndex() {
		this(false);
	}

	public FastWeakSetIndex(final boolean concurrent) {
		this.set = Collections.newSetFromMap(new WeakHashMap<E, Boolean>(4));
		this.concurrent = concurrent;
	}

	private static final long serialVersionUID = 1067549369843962009L;

	@Override
	public void clear() {
		this.set.clear();
	}

	@Override
	public boolean deIndex(final E entry) {
		return this.set.remove(entry);
	}

	@Override
	public boolean index(final E entry) {
		return this.set.add(entry);
	}

	/* we cannot call contains() on other, so we need this */
	@Override
	public IEntrySetDiff<E> computeDiff(final IEntrySet<E> other) {
		final FastSetDiff<E> diff = new FastSetDiff<E>();
		diff.added = new FastWeakSetIndex<E>();
		diff.removed = new FastWeakSetIndex<E>();

		// consider everything as removed
		for (final E thisEntry : this) {
			diff.removed.index(thisEntry);
		}

		for (final E otherEntry : other) {
			if (this.contains(otherEntry)) {
				// if it is still there, it has NOT been removed
				diff.removed.deIndex(otherEntry);
			} else {
				// not in this, but in other => added
				diff.added.index(otherEntry);
			}
		}

		return diff;
	}

	public static class FastSetDiff<E> implements IEntrySetDiff<E> {

		protected FastWeakSetIndex<E> added, removed;

		@Override
		public IEntrySet<E> getAdded() {
			return this.added;
		}

		@Override
		public IEntrySet<E> getRemoved() {
			return this.removed;
		}

	}

	@Override
	public Iterator<E> constraintIterator(final Constraint<E> entryConstraint) {
		if (entryConstraint.isStar()) {
			if (this.concurrent) {
				return toSet().iterator();
			}
			return iterator();
		} else {
			// this additional check is making things faster?
			if (isEmpty()) {
				return NoneIterator.create();
			}

			assert entryConstraint instanceof EqualsConstraint<?>;
			final E entry = ((EqualsConstraint<E>) entryConstraint).getKey();
			if (contains(entry)) {
				return new SingleValueIterator<E>(entry);
			} else {
				return NoneIterator.create();
			}
		}
	}

	@Override
	public Set<E> toSet() {
		if (this.concurrent) {
			return Sets.newHashSet(this.set);
		} else {
			return this.set;
		}
	}

	@Override
	public boolean isEmpty() {
		return this.set.isEmpty();
	}

	@Override
	public Iterator<E> iterator() {
		return this.set.iterator();
	}

	@Override
	public boolean contains(final E entry) {
		return this.set.contains(entry);
	}

	@Override
	public int size() {
		return this.set.size();
	}

}
