package org.xydra.index.iterator;

import java.util.Iterator;

/**
 * Encapsulates an iterator, returning only those elements matching a filter.
 * Uses a pre-fetch strategy.
 * 
 * @author voelkel
 * 
 * @param <E>
 *            Type of objects returned by this iterator.
 */
public abstract class AbstractFilteringIterator<E> implements Iterator<E> {

	protected Iterator<E> base;

	boolean hasNext;
	private E nextItem = null;

	public AbstractFilteringIterator(Iterator<E> base) {
		this.base = base;
	}

	@Override
	public boolean hasNext() {
		this.lookAhead();
		return this.hasNext;
	}

	@Override
	public E next() {
		// might be the first call ever and we are lazy
		this.lookAhead();
		E result = this.nextItem;
		this.hasNext = false;
		this.lookAhead();
		return result;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

	/**
	 * @param entry
	 * @return true if we want to return entry in the result of the iterator
	 */
	protected abstract boolean matchesFilter(E entry);

	private void lookAhead() {
		if (this.hasNext)
			return;

		// advance until we find a match
		while (this.base.hasNext()) {
			this.nextItem = this.base.next();
			if (this.matchesFilter(this.nextItem)) {
				this.hasNext = true;
				return;
			}
		}

		// we reached the end and never matched, so:
	}
}
