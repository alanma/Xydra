package org.xydra.index.impl;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.xydra.index.IEntrySet;
import org.xydra.index.iterator.NoneIterator;
import org.xydra.index.iterator.SingleValueIterator;
import org.xydra.index.query.Constraint;
import org.xydra.index.query.EqualsConstraint;

import com.google.common.collect.Sets;

public class FastSetIndex<E> extends HashSet<E> implements IEntrySet<E>, Serializable {

	private boolean concurrent;

	public FastSetIndex() {
		this(false);
	}

	public FastSetIndex(final boolean concurrent) {
		super(4);
		this.concurrent = concurrent;
	}

	private static final long serialVersionUID = 1067549369843962009L;

	@Override
	public boolean deIndex(final E entry) {
		return remove(entry);
	}

	@Override
	public boolean index(final E entry) {
		return add(entry);
	}

	/* we cannot call contains() on other, so we need this */
	@Override
	public IEntrySetDiff<E> computeDiff(final IEntrySet<E> other) {
		final FastSetDiff<E> diff = new FastSetDiff<E>();
		diff.added = new FastSetIndex<E>();
		diff.removed = new FastSetIndex<E>();

		// consider everything as removed
		for (final E thisEntry : this) {
			diff.removed.add(thisEntry);
		}

		for (final E otherEntry : other) {
			if (contains(otherEntry)) {
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
			return Sets.newHashSet(this);
		} else {
			return this;
		}
	}

	@Override
	public String toString() {
		final StringBuilder b = new StringBuilder();
		b.append("{");
		final Iterator<E> it = iterator();
		while (it.hasNext()) {
			final E e = it.next();
			b.append(e.toString());
			if(it.hasNext()) {
				b.append(", ");
			}
		}
		b.append("}");
		return b.toString();
	}

}
