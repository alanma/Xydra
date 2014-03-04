package org.xydra.index;

import java.util.Iterator;
import java.util.Set;

import org.xydra.index.query.Constraint;


/**
 * @author voelkel
 * 
 *         Multiple entries can be indexed for a certain key-combination.
 * 
 * @param <E> entity type
 */
public interface IEntrySet<E> extends IIndex, Iterable<E> {
    
    @Override
    public Iterator<E> iterator();
    
    /**
     * @param entry
     * @return true if entry was in the set
     */
    boolean deIndex(E entry);
    
    /**
     * @param entry
     * @return true if entry was not in the set yet
     */
    boolean index(E entry);
    
    IEntrySetDiff<E> computeDiff(IEntrySet<E> other);
    
    static interface IEntrySetDiff<E> {
        IEntrySet<E> getAdded();
        
        IEntrySet<E> getRemoved();
    }
    
    boolean contains(E entry);
    
    Iterator<E> constraintIterator(Constraint<E> entryConstraint);
    
    int size();
    
    /**
     * @return a read-only Set
     */
    Set<E> toSet();
    
}
