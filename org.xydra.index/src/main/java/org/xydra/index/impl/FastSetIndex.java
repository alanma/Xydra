package org.xydra.index.impl;

import java.util.HashSet;
import java.util.Iterator;

import org.xydra.index.IEntrySet;
import org.xydra.index.iterator.NoneIterator;
import org.xydra.index.iterator.SingleValueIterator;
import org.xydra.index.query.Constraint;
import org.xydra.index.query.EqualsConstraint;



public class FastSetIndex<E> extends HashSet<E> implements IEntrySet<E> {
	
	public FastSetIndex() {
		super(4);
	}
	
	private static final long serialVersionUID = 1067549369843962009L;
	
	// public void clear() {
	// this.clear();
	// }
	
	public void deIndex(E entry) {
		this.remove(entry);
	}
	
	public void index(E entry) {
		this.add(entry);
	}
	
	/* we cannot call contains() on other, so we need this */
	public IEntrySetDiff<E> computeDiff(IEntrySet<E> other) {
		FastSetDiff<E> diff = new FastSetDiff<E>();
		diff.added = new FastSetIndex<E>();
		diff.removed = new FastSetIndex<E>();
		
		// consider everything as removed
		for(E thisEntry : this) {
			diff.removed.add(thisEntry);
		}
		
		for(E otherEntry : other) {
			if(this.contains(otherEntry)) {
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
		
		public IEntrySet<E> getAdded() {
			return this.added;
		}
		
		public IEntrySet<E> getRemoved() {
			return this.removed;
		}
		
	}
	
	public Iterator<E> constraintIterator(Constraint<E> entryConstraint) {
		if(entryConstraint.isStar()) {
			return iterator();
		} else {
			assert entryConstraint instanceof EqualsConstraint<?>;
			E entry = ((EqualsConstraint<E>)entryConstraint).getKey();
			if(contains(entry)) {
				return new SingleValueIterator<E>(entry);
			} else {
				return new NoneIterator<E>();
			}
		}
	}
	
}
