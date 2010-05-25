package de.xam.xindex.iterator;

import java.util.Iterator;


/**
 * An iterator on top of a single entry.
 * 
 * @author voelkel
 * 
 * @param <E>
 */
public class SingleValueIterator<E> implements Iterator<E> {
	
	private boolean done = false;
	private E singleEntry;
	
	public SingleValueIterator(E singleEntry) {
		this.singleEntry = singleEntry;
	}
	
	public boolean hasNext() {
		return !this.done && (this.singleEntry != null);
	}
	
	public E next() {
		this.done = true;
		return this.singleEntry;
	}
	
	public void remove() {
		throw new UnsupportedOperationException();
	}
	
}
