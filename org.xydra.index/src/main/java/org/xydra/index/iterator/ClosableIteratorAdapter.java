package org.xydra.index.iterator;

import java.util.Iterator;


/**
 * An iterator adaptor that turns any iterator into a closable iterator over
 * objects of a supertype of those returned by the wrapped iterator.
 * 
 * @author voelkel
 * 
 * @param <T>
 */
public class ClosableIteratorAdapter<T> implements ClosableIterator<T> {
	
	/**
     * 
     */
    private static final long serialVersionUID = -5135133229964853452L;
	private Iterator<? extends T> iterator;
	
	public ClosableIteratorAdapter(Iterator<? extends T> it) {
		this.iterator = it;
	}
	
	public void close() {
		if(this.iterator instanceof ClosableIterator<?>) {
			((ClosableIterator<? extends T>)this.iterator).close();
		}
	}
	
	public boolean hasNext() {
		return this.iterator.hasNext();
	}
	
	public T next() {
		return this.iterator.next();
	}
	
	public void remove() {
		this.iterator.remove();
	}
	
}
