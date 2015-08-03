package org.xydra.index.iterator;

import java.util.Iterator;

/**
 * An iterator adaptor that turns any iterator into a closable iterator over
 * objects of a supertype of those returned by the wrapped iterator.
 *
 * Iterator is auto-closed if {@link #hasNext()} returns false;
 *
 * Can be closed many times, will only close the underlying iterator once.
 *
 * @author voelkel
 *
 * @param <T> entity type
 */
public class ClosableIteratorAdapter<T> implements ClosableIterator<T> {

	private final Iterator<? extends T> iterator;

	public ClosableIteratorAdapter(final Iterator<? extends T> it) {
		this.iterator = it;
	}

	@Override
	public void close() {
		if (this.iterator instanceof ClosableIterator<?>) {
			((ClosableIterator<? extends T>) this.iterator).close();
		}
	}

	@Override
	public boolean hasNext() {
		final boolean b = this.iterator.hasNext();
		if (!b) {
			close();
		}
		return b;
	}

	@Override
	public T next() {
		return this.iterator.next();
	}

	@Override
	public void remove() {
		this.iterator.remove();
	}

}
