package org.xydra.index.iterator;

import java.util.Arrays;
import java.util.Iterator;


/**
 * Turns two iterators (one probably smaller than the other one) into a single
 * iterator, which can contain duplicates.
 * 
 * @author voelkel
 * 
 * @param <E> entity type
 */
public class BagUnionIterator<E> extends AbstractCascadedIterator<Iterator<? extends E>,E>
        implements ClosableIterator<E> {
    
    private Iterator<? extends E> smallIt;
    private Iterator<? extends E> largeIt;
    
    public BagUnionIterator(Iterator<? extends E> smallIt, Iterator<? extends E> largeIt) {
        super(Arrays.asList(smallIt, largeIt).iterator());
        this.smallIt = smallIt;
        this.largeIt = largeIt;
    }
    
    @Override
    protected Iterator<? extends E> toIterator(Iterator<? extends E> element) {
        return element;
    }
    
    @Override
    public void close() {
        if(this.smallIt instanceof ClosableIterator<?>) {
            ((ClosableIterator<? extends E>)this.smallIt).close();
        }
        if(this.largeIt instanceof ClosableIterator<?>) {
            ((ClosableIterator<? extends E>)this.largeIt).close();
        }
    }
    
}
