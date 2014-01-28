package org.xydra.index.iterator;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.xydra.index.impl.IteratorUtils;


/**
 * Turns two iterators (one probably smaller than the other one) into a single
 * iterator, which cannot duplicates.
 * 
 * The smaller of the two iterators is materialised into a HashSet and than the
 * resulting iterator is done as a filter, that checks every time into the temp
 * index to assert unique entries only.
 * 
 * @author voelkel
 * 
 * @param <E> entity type
 */
public class SetUnionIterator<E> implements ClosableIterator<E> {
    
    private final Set<E> smallSet;
    private Iterator<? extends E> largeIt;
    private BagUnionIterator<E> combinedIt;
    
    public SetUnionIterator(Iterator<? extends E> smallIt, Iterator<E> largeIt) {
        this.smallSet = new HashSet<E>();
        IteratorUtils.addAll(smallIt, this.smallSet);
        if(smallIt instanceof ClosableIterator<?>) {
            ((ClosableIterator<? extends E>)smallIt).close();
        }
        Iterator<E> largeUniqueIt = new AbstractFilteringIterator<E>(largeIt) {
            
            @Override
            protected boolean matchesFilter(E entry) {
                return !SetUnionIterator.this.smallSet.contains(entry);
            }
        };
        this.combinedIt = new BagUnionIterator<E>(this.smallSet.iterator(), largeUniqueIt);
    }
    
    @Override
    public void close() {
        if(this.largeIt instanceof ClosableIterator<?>) {
            ((ClosableIterator<? extends E>)this.largeIt).close();
        }
    }
    
    @Override
    public boolean hasNext() {
        return this.combinedIt.hasNext();
    }
    
    @Override
    public E next() {
        return this.combinedIt.next();
    }
    
    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
    
}
