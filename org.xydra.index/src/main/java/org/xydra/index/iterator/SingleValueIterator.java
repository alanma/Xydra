package org.xydra.index.iterator;

import java.util.Iterator;


/**
 * An iterator on top of a single entry.
 * 
 * @author voelkel
 * 
 * @param <E> entity type
 */
public class SingleValueIterator<E> implements Iterator<E> {
	
	private boolean done = false;
	private E singleEntry;
	
	public SingleValueIterator(E singleEntry) {
		this.singleEntry = singleEntry;
	}
	
	@Override
    public boolean hasNext() {
		return !this.done && (this.singleEntry != null);
	}
	
	@Override
    public E next() {
		this.done = true;
		return this.singleEntry;
	}
	
	@Override
    public void remove() {
		throw new UnsupportedOperationException();
	}
	
}
