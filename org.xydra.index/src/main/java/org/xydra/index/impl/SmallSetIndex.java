package org.xydra.index.impl;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import org.xydra.index.IEntrySet;
import org.xydra.index.iterator.NoneIterator;
import org.xydra.index.iterator.SingleValueIterator;
import org.xydra.index.query.Constraint;
import org.xydra.index.query.EqualsConstraint;

/**
 * Based on a simple linked list - much more memory efficient than based on a
 * HashSet - and much slower, too.
 * 
 * @author voelkel
 * 
 * @param <E>
 *            entity type
 */
public class SmallSetIndex<E> extends LinkedList<E> implements IEntrySet<E> {

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
		boolean contains = this.contains(entry);
		if (!contains) {
			this.add(entry);
		}
		return !contains;
	}

	@Override
	public IEntrySetDiff<E> computeDiff(IEntrySet<E> other) {
		// assume other is also short
		SmallEntrySetDiff<E> diff = new SmallEntrySetDiff<E>();
		SmallSetIndex<E> added = new SmallSetIndex<E>();
		SmallSetIndex<E> removed = new SmallSetIndex<E>();

		// assume the worst
		removed.addAll(this);
		// ..and then compensate
		for (E otherEntry : other) {
			if (removed.contains(otherEntry)) {
				// compensate
				removed.remove(otherEntry);
			} else {
				// it has been added
				added.add(otherEntry);
			}
		}

		diff.added = added;
		diff.removed = removed;
		return diff;
	}

	static class SmallEntrySetDiff<E> implements IEntrySetDiff<E> {

		IEntrySet<E> added;
		IEntrySet<E> removed;

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
			return iterator();
		} else {
			assert entryConstraint instanceof EqualsConstraint<?>;
			E entry = ((EqualsConstraint<E>) entryConstraint).getKey();
			if (contains(entry)) {
				return new SingleValueIterator<E>(entry);
			} else {
				return NoneIterator.<E> create();
			}
		}
	}

	@Override
	public Set<E> toSet() {
		Set<E> set = new HashSet<E>();
		set.addAll(this);
		return set;
	}

}
