package org.xydra.index.iterator;

import java.util.Iterator;


/**
 * Adapts an Iterator to transform the iterated objects.
 * 
 * @param <I> Type of objects returned by the wrapped iterator.
 * @param <O> Type of objects returned by this iterator.
 */
public abstract class AbstractTransformingIterator<I, O> implements ClosableIterator<O> {
	
	/**
     * 
     */
    private static final long serialVersionUID = 6606331134434363937L;
	Iterator<? extends I> base;
	
	public AbstractTransformingIterator(Iterator<? extends I> base) {
		this.base = base;
	}
	
	public boolean hasNext() {
		return this.base.hasNext();
	}
	
	public O next() {
		return this.transform(this.base.next());
	}
	
	public void remove() {
		this.base.remove();
	}
	
	public void close() {
		if(this.base instanceof ClosableIterator<?>) {
			((ClosableIterator<? extends I>)this.base).close();
		}
	}
	
	public abstract O transform(I in);
	
}
