package org.xydra.index.iterator;

import java.util.Iterator;

/**
 * Encapsulates an iterator, returning only those elements matching a filter. Uses a pre-fetch strategy, so does not
 * support remove().
 *
 * @author voelkel
 *
 * @param <E> Type of objects returned by this iterator.
 */
public class FilteringIterator<E> implements ClosableIterator<E> {

	private final Iterator<E> base;

	private boolean hasNext;

	private E nextItem = null;

	private final IFilter<E> filter;

	/**
	 * @param base
	 * @param filter @NeverNull
	 */
	public FilteringIterator(final Iterator<E> base, final IFilter<E> filter) {
		assert filter != null;
		this.base = base;
		this.filter = filter;
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
		final E result = this.nextItem;
		this.hasNext = false;
		this.lookAhead();
		return result;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

	private void lookAhead() {
		if (this.hasNext) {
			return;
		}

		// advance until we find a match
		while (this.base.hasNext()) {
			this.nextItem = this.base.next();
			if (this.filter.matches(this.nextItem)) {
				this.hasNext = true;
				return;
			}
		}

		// we reached the end and never matched
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void close() {
		if (this.base instanceof ClosableIterator) {
			((ClosableIterator) this.base).close();
		}
	}
}
