package org.xydra.index.iterator;

import java.util.Iterator;

public class RememberLastIterator<T> implements Iterator<T> {
	private final Iterator<T> base;
	private T last = null;

	/**
	 * @return the last element that was returned, can be null
	 */
	public T getLast() {
		return this.last;
	}

	public RememberLastIterator(final Iterator<T> base) {
		super();
		this.base = base;
	}

	@Override
	public boolean hasNext() {
		return this.base.hasNext();
	}

	@Override
	public T next() {
		this.last = this.base.next();
		return this.last;
	}

	@Override
	public void remove() {
		this.base.remove();
	}

}
