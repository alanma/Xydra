package org.xydra.index.iterator;

import java.util.Iterator;


/**
 * An iterator adaptor that turns any iterator into a closable iterator over
 * objects of a supertype of those returned by the wrapped iterator.
 * 
 * @author voelkel
 * 
 * @param <T> entity type
 */
public class ClosableIteratorAdapter<T> implements ClosableIterator<T> {
	
	private Iterator<? extends T> iterator;
	
	public ClosableIteratorAdapter(Iterator<? extends T> it) {
		this.iterator = it;
	}
	
	@Override
    public void close() {
		if(this.iterator instanceof ClosableIterator<?>) {
			((ClosableIterator<? extends T>)this.iterator).close();
		}
	}
	
	@Override
    public boolean hasNext() {
		return this.iterator.hasNext();
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
