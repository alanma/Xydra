package org.xydra.index.iterator;

import java.util.Iterator;

public abstract class AbstractLookAheadIterator<E> implements Iterator<E> {

	boolean hasNext;
	private E nextItem = null;

	public AbstractLookAheadIterator() {
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

	private void lookAhead() {
		if (this.hasNext)
			return;

		// advance until we find a match
		while (this.baseHasNext()) {
			this.nextItem = this.baseNext();
			this.hasNext = true;
			return;
		}

		// we reached the end and never matched, so:
	}

	/**
	 * @return the next item from the base
	 */
	protected abstract E baseNext();

	/**
	 * @return true iff base has another element
	 */
	protected abstract boolean baseHasNext();

}
