package org.xydra.index.iterator;

import java.util.Iterator;

/**
 * Read only iterator wrapper that forbids {@link #remove()}.
 *
 * @author dscharrer
 *
 * @param <T>
 *            The type being iterated.
 */
public class ReadOnlyIterator<T> implements Iterator<T> {

	private final Iterator<T> base;

	public ReadOnlyIterator(final Iterator<T> base) {
		this.base = base;
	}

	/**
	 * Convenience constructor.
	 *
	 * @param base
	 */
	public ReadOnlyIterator(final Iterable<T> base) {
		this(base.iterator());
	}

	@Override
	public boolean hasNext() {
		return this.base.hasNext();
	}

	@Override
	public T next() {
		return this.base.next();
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException("read only iterator");
	}

}
