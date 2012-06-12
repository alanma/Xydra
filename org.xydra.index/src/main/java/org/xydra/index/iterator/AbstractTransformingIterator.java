package org.xydra.index.iterator;

import java.util.Iterator;


/**
 * Adapts an Iterator to transform the iterated objects.
 * 
 * @param <I> Type of objects returned by the wrapped iterator.
 * @param <O> Type of objects returned by this iterator.
 */
public abstract class AbstractTransformingIterator<I, O> implements ClosableIterator<O> {
	
	Iterator<? extends I> base;
	
	public AbstractTransformingIterator(Iterator<? extends I> base) {
		this.base = base;
	}
	
	@Override
	public boolean hasNext() {
		return this.base.hasNext();
	}
	
	@Override
	public O next() {
		if(!hasNext()) {
			return null;
		}
		return this.transform(this.base.next());
	}
	
	@Override
	public void remove() {
		this.base.remove();
	}
	
	@Override
	public void close() {
		if(this.base instanceof ClosableIterator<?>) {
			((ClosableIterator<? extends I>)this.base).close();
		}
	}
	
	public abstract O transform(I in);
	
}
