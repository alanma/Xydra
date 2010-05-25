package de.xam.xindex.index.impl;

import java.util.Iterator;
import java.util.LinkedList;

import de.xam.xindex.index.IEntrySet;
import de.xam.xindex.iterator.NoneIterator;
import de.xam.xindex.iterator.SingleValueIterator;
import de.xam.xindex.query.Constraint;
import de.xam.xindex.query.EqualsConstraint;


/**
 * Based on a simple linked list - much more memory efficient than based on a
 * HashSet - and much slower, too.
 * 
 * @author voelkel
 * 
 * @param <E>
 */
public class SmallSetIndex<E> extends LinkedList<E> implements IEntrySet<E> {
	
	private static final long serialVersionUID = 1067549369843962009L;
	
	// public void clear() {
	// this.clear();
	// }
	
	public void deIndex(E entry) {
		this.remove(entry);
	}
	
	public void index(E entry) {
		this.remove(entry);
		this.add(entry);
	}
	
	public IEntrySetDiff<E> computeDiff(IEntrySet<E> other) {
		// assume other is also short
		SmallEntrySetDiff diff = new SmallEntrySetDiff();
		diff.added = new SmallSetIndex<E>();
		diff.removed = new SmallSetIndex<E>();
		
		// assume the worst
		diff.removed.addAll(this);
		// ..and then compensate
		for(E otherEntry : other) {
			if(diff.removed.contains(otherEntry)) {
				// compensate
				diff.removed.remove(otherEntry);
			} else {
				// it has been added
				diff.added.add(otherEntry);
			}
		}
		return diff;
	}
	
	class SmallEntrySetDiff implements IEntrySetDiff<E> {
		
		protected SmallSetIndex<E> added;
		protected SmallSetIndex<E> removed;
		
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
