package org.xydra.index.iterator;

import java.util.Iterator;

/**
 * Returns only as many elements as indicated in "limit".
 * 
 * Using a pre-fetch strategy.
 * 
 * @author voelkel
 * 
 * @param <E>
 *            entity type
 */
public class LimitIterator<E> implements ClosableIterator<E> {

	private Iterator<E> base;

	private long count;

	private long limit;

	private E nextItem = null;

	public LimitIterator(Iterator<E> base, long limit) {
		this.base = base;
		this.limit = limit;
		this.count = 0;
	}

	@Override
	public boolean hasNext() {
		if (this.count == this.limit) {
			return false;
		}
		this.lookAhead();
		return this.nextItem != null;
	}

	@Override
	public E next() {
		if (this.count == this.limit) {
			return null;
		}
		E result = this.nextItem;
		this.nextItem = null;
		this.lookAhead();
		this.count++;
		return result;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

	private void lookAhead() {
		if (this.nextItem != null)
			return;

		// else: advance one step
		if (this.base.hasNext()) {
			this.nextItem = this.base.next();
		}
	}

	@Override
	public void close() {
		if (this.base instanceof ClosableIterator<?>) {
			((ClosableIterator<E>) this.base).close();
		}
	}

}
