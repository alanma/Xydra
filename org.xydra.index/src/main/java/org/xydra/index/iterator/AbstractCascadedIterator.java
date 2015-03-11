package org.xydra.index.iterator;

import java.util.Iterator;

/**
 * Adapts an Iterator of Iterators so that it seems to be a single, continuous
 * Iterator
 * 
 * @author voelkel
 * 
 * @param <B> Type of objects returned by the encapsulated iterator.
 * @param <E> Type of objects to be returned by this iterator.
 */
public abstract class AbstractCascadedIterator<B, E> implements ClosableIterator<E> {

	private Iterator<B> base;

	private Iterator<? extends E> currentIterator;

	private E nextEntry;

	public AbstractCascadedIterator(Iterator<B> base) {
		assert base != null;
		this.base = base;
	}

	@Override
	public boolean hasNext() {
		this.lookAhead();
		return this.nextEntry != null;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void close() {
		if (this.currentIterator != null && this.currentIterator instanceof ClosableIterator) {
			((ClosableIterator) this.currentIterator).close();
		}

		while (this.base.hasNext()) {
			B baseElement = this.base.next();
			Iterator<? extends E> baseIt = this.toIterator(baseElement);
			if (baseIt instanceof ClosableIterator) {
				((ClosableIterator) baseIt).close();
			}
		}
	}

	@Override
	public E next() {
		this.lookAhead();
		E result = this.nextEntry;
		this.nextEntry = null;
		return result;
	}

	@Override
	public void remove() {
		// due to look-ahead-logic we don't know what to remove
		throw new UnsupportedOperationException();
	}

	protected abstract Iterator<? extends E> toIterator(B baseEntry);

	/**
	 * If nextEntry is null, we try to get a new one
	 */
	private void lookAhead() {
		if (this.nextEntry != null) {
			// we have a current next element, no need to do anything
			return;
		}

		// initialisation
		if (this.currentIterator == null) {
			if (this.base.hasNext()) {
				this.currentIterator = this.toIterator(this.base.next());
			}
		}

		if (this.currentIterator != null) {
			if (this.currentIterator.hasNext()) {
				this.nextEntry = this.currentIterator.next();
			} else {
				while (this.base.hasNext() && !this.currentIterator.hasNext()) {
					B baseNext = this.base.next();
					this.currentIterator = this.toIterator(baseNext);
				}
				if (this.currentIterator.hasNext()) {
					this.nextEntry = this.currentIterator.next();
				} else {
					this.nextEntry = null;
				}
			}
		}
	}

}
