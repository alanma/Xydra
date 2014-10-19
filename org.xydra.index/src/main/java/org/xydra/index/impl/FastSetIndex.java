package org.xydra.index.impl;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.xydra.index.IEntrySet;
import org.xydra.index.iterator.NoneIterator;
import org.xydra.index.iterator.SingleValueIterator;
import org.xydra.index.query.Constraint;
import org.xydra.index.query.EqualsConstraint;

import com.google.common.collect.Sets;

public class FastSetIndex<E> extends HashSet<E> implements IEntrySet<E> {

	private boolean concurrent;

	public FastSetIndex() {
		this(false);
	}

	public FastSetIndex(boolean concurrent) {
		super(4);
		this.concurrent = concurrent;
	}

	private static final long serialVersionUID = 1067549369843962009L;

	// public void clear() {
	// this.clear();
	// }

	@Override
	public boolean deIndex(E entry) {
		return this.remove(entry);
	}

	@Override
	public boolean index(E entry) {
		return this.add(entry);
	}

	/* we cannot call contains() on other, so we need this */
	@Override
	public IEntrySetDiff<E> computeDiff(IEntrySet<E> other) {
		FastSetDiff<E> diff = new FastSetDiff<E>();
		diff.added = new FastSetIndex<E>();
		diff.removed = new FastSetIndex<E>();

		// consider everything as removed
		for (E thisEntry : this) {
			diff.removed.add(thisEntry);
		}

		for (E otherEntry : other) {
			if (this.contains(otherEntry)) {
				// if it is still there, it has NOT been removed
				diff.removed.remove(otherEntry);
			} else {
				// not in this, but in other => added
				diff.added.add(otherEntry);
			}
		}

		return diff;
	}

	public static class FastSetDiff<E> implements IEntrySetDiff<E> {

		protected FastSetIndex<E> added, removed;

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
	public Iterator<E> constraintIterator(Constraint<E> entryConstraint) {
		if (entryConstraint.isStar()) {
			if (this.concurrent) {
				return toSet().iterator();
			}
			return iterator();
		} else {
			// this additional check is making things faster?
			if (isEmpty())
				return NoneIterator.create();

			assert entryConstraint instanceof EqualsConstraint<?>;
			E entry = ((EqualsConstraint<E>) entryConstraint).getKey();
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
			return Sets.newHashSet(this);
		} else {
			return this;
		}
	}

}
