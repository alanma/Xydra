package org.xydra.index;

import org.xydra.index.query.Constraint;
import org.xydra.index.query.KeyEntryTuple;

import java.util.Iterator;


/**
 * Multiple entries can be indexed for a certain key-combination.
 * 
 * 
 * @author voelkel
 * @param <K> key type
 * @param <E> entity type
 */
public interface IMapIndex<K, E> extends IIndex {
    
    /**
     * @param key
     * @param name
     * @return true if the index contains an entry for the key
     */
    boolean containsKey(K key);
    
    /**
     * @param key
     */
    void deIndex(K key);
    
    /**
     * @param key
     * @param entry
     */
    void index(K key, E entry);
    
    /**
     * @return all entries
     */
    Iterator<E> iterator();
    
    /**
     * @param key Depending on implementation, a null-key might be permitted and
     *            usually maps to null.
     * @return the value stored for the given key or null
     */
    E lookup(K key);
    
    /**
     * @param c1
     * @return true iff this index contains at least one key matching the
     *         constraint
     */
    boolean containsKey(Constraint<K> c1);
    
    /**
     * @param keyConstraint
     * @return all tuples matching the key-constraint
     */
    Iterator<KeyEntryTuple<K,E>> tupleIterator(Constraint<K> keyConstraint);
    
    /**
     * @return an iterator over all keys
     */
    Iterator<K> keyIterator();
    
}
