package org.xydra.index.iterator;

import java.util.Iterator;
import java.util.NoSuchElementException;


/**
 * Adapts an Iterator to transform the iterated objects.
 * 
 * @param <I> Type of objects returned by the wrapped iterator.
 * @param <O> Type of objects returned by this iterator.
 */
public abstract class AbstractTransformingIterator<I, O> implements ClosableIterator<O> {
    
    Iterator<? extends I> base;
    
    /**
     * @param base @NeverNull
     */
    public AbstractTransformingIterator(Iterator<? extends I> base) {
        assert base != null;
        this.base = base;
    }
    
    @Override
    public boolean hasNext() {
        return this.base.hasNext();
    }
    
    @Override
    public O next() {
        if(!hasNext()) {
            throw new NoSuchElementException();
            // return null;
        }
        I in = this.base.next();
        O out = this.transform(in);
        return out;
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
