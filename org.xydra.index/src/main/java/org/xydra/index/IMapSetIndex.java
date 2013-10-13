package org.xydra.index;

import java.util.Iterator;

import org.xydra.index.query.Constraint;
import org.xydra.index.query.KeyEntryTuple;


/**
 * @author voelkel
 * 
 *         Multiple entries can be indexed for a certain key-combination.
 * 
 * @param <K> key type
 * @param <E> entity type
 */
public interface IMapSetIndex<K, E> extends IIndex {
    
    /**
     * @param c1
     * @return an iterator that ranges over all entries indexes by keys, where
     *         the keys match c1
     */
    Iterator<E> constraintIterator(Constraint<K> c1);
    
    boolean contains(Constraint<K> c1, Constraint<E> entryConstraint);
    
    boolean containsKey(K key);
    
    /**
     * @param key1
     * @param entry
     * @return true iff set K contained entry
     */
    boolean deIndex(K key1, E entry);
    
    /**
     * Deindex all current entries with (key1, ?).
     * 
     * @param key1
     */
    void deIndex(K key1);
    
    /**
     * @param key1
     * @param entry
     * @return true iff set K did not contain entry yet
     */
    boolean index(K key1, E entry);
    
    /**
     * @param c1 constraint in the key
     * @param entryConstraint constraint on the value
     * @return an iterator over all result tuples matching the constraints
     */
    Iterator<KeyEntryTuple<K,E>> tupleIterator(Constraint<K> c1, Constraint<E> entryConstraint);
    
    Iterator<K> keyIterator();
    
    /**
     * @param otherFuture the other map index is the future. What is found here
     *            and not present in this, has been added.
     * @return an {@link IMapSetDiff}
     */
    IMapSetDiff<K,E> computeDiff(IMapSetIndex<K,E> otherFuture);
    
    public static interface IMapSetDiff<K, E> {
        IMapSetIndex<K,E> getAdded();
        
        IMapSetIndex<K,E> getRemoved();
    }
    
    IEntrySet<E> lookup(K key);
    
}
