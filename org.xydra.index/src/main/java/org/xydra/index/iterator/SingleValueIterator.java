package org.xydra.index.iterator;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * An iterator on top of a single entry.
 *
 * @author voelkel
 *
 * @param <E>
 *            entity type
 */
public class SingleValueIterator<E> implements Iterator<E> {

	private boolean done = false;
	private final E singleEntry;

	public SingleValueIterator(final E singleEntry) {
		this.singleEntry = singleEntry;
	}

	@Override
	public boolean hasNext() {
		return !this.done && this.singleEntry != null;
	}

	@Override
	public E next() {
		if (this.done) {
			throw new NoSuchElementException();
		}
		this.done = true;
		return this.singleEntry;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

}
