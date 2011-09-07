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
public abstract class AbstractCascadedIterator<B, E> implements Iterator<E> {
	
	private Iterator<B> base;
	
	private Iterator<? extends E> currentIterator;
	
	private E nextEntry;
	
	public AbstractCascadedIterator(Iterator<B> base) {
		this.base = base;
	}
	
	@Override
    public boolean hasNext() {
		this.lookAhead();
		return this.nextEntry != null;
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
		throw new UnsupportedOperationException();
	}
	
	protected abstract Iterator<? extends E> toIterator(B baseEntry);
	
	/**
	 * If nextEntry is null, we try to get a new one
	 */
	private void lookAhead() {
		if(this.nextEntry != null) {
			// we have a current next element, no need to do anything
			return;
		}
		
		// initialisation
		if(this.currentIterator == null) {
			if(this.base.hasNext()) {
				this.currentIterator = this.toIterator(this.base.next());
			}
		}
		
		if(this.currentIterator != null) {
			if(this.currentIterator.hasNext()) {
				this.nextEntry = this.currentIterator.next();
			} else {
				while(this.base.hasNext() && !this.currentIterator.hasNext()) {
					this.currentIterator = this.toIterator(this.base.next());
				}
				if(this.currentIterator.hasNext()) {
					this.nextEntry = this.currentIterator.next();
				} else {
					this.nextEntry = null;
				}
			}
		}
	}
	
}
