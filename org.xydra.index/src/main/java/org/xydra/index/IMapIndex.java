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
public interface IMapIndex<K, E> extends IIndex {
    
    /**
     * @param key
     * @param name
     * @return true if the index contains an entry for the key
     */
    boolean containsKey(K key);
    
    void deIndex(K key1);
    
    void index(K key1, E entry);
    
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
    
    boolean containsKey(Constraint<K> c1);
    
    Iterator<KeyEntryTuple<K,E>> tupleIterator(Constraint<K> c1);
    
    Iterator<K> keyIterator();
    
}
