package org.xydra.index.impl.trie;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import org.xydra.index.IMapIndex;
import org.xydra.index.iterator.ITransformer;
import org.xydra.index.iterator.Iterators;
import org.xydra.index.query.Constraint;
import org.xydra.index.query.KeyEntryTuple;


/**
 * A {@link TreeMap} put under an {@link IMapIndex}<String,E> interface.
 * 
 * @author xamde
 * 
 * @param <E>
 */
public class SortedStringMap<E> implements IMapIndex<String,E> {
    
    private static final long serialVersionUID = 1L;
    
    private TreeMap<String,E> map = new TreeMap<String,E>();
    
    public void index(String key, E value) {
        this.map.put(key, value);
    }
    
    @Override
    public void clear() {
        this.map.clear();
    }
    
    @Override
    public boolean isEmpty() {
        return this.map.isEmpty();
    }
    
    @Override
    public boolean containsKey(String key) {
        return this.map.containsKey(key);
    }
    
    @Override
    public void deIndex(String key) {
        this.map.remove(key);
    }
    
    @Override
    public Iterator<E> iterator() {
        return this.map.values().iterator();
    }
    
    @Override
    public E lookup(String key) {
        return this.map.get(key);
    }
    
    @Override
    public boolean containsKey(Constraint<String> c1) {
        if(c1.isStar()) {
            return !this.map.isEmpty();
        } else {
            return this.map.containsKey(c1.getExpected());
        }
    }
    
    @Override
    public Iterator<KeyEntryTuple<String,E>> tupleIterator(Constraint<String> c1) {
        return Iterators.transform(this.map.entrySet().iterator(),
                new ITransformer<Map.Entry<String,E>,KeyEntryTuple<String,E>>() {
                    
                    @Override
                    public KeyEntryTuple<String,E> transform(Entry<String,E> in) {
                        return new KeyEntryTuple<String,E>(in.getKey(), in.getValue());
                    }
                });
    }
    
    @Override
    public Iterator<String> keyIterator() {
        return this.map.keySet().iterator();
    }
    
    /**
     * Handy for constructing range-queries
     * 
     * IMPROVE deal with unicode outside of BMP
     */
    public static final String LAST_UNICODE_CHAR = "\uFFFF";
    
    /**
     * Special function of Sorted...
     * 
     * @param keyPrefix
     * @return true iff at least one key has been indexed which starts with the
     *         given keyPrefix
     */
    public boolean containsKeysStartingWith(String keyPrefix) {
        SortedMap<String,E> subMap = this.map.subMap(keyPrefix, keyPrefix + LAST_UNICODE_CHAR);
        return !subMap.isEmpty();
    }
    
    /**
     * @param keyPrefix
     * @return all entries which have been indexed at a key starting with the
     *         given prefix. Collects the results of potentially many such keys.
     */
    public Iterator<E> lookupStartingWith(String keyPrefix) {
        SortedMap<String,E> subMap = this.map.subMap(keyPrefix, keyPrefix + LAST_UNICODE_CHAR);
        return subMap.values().iterator();
    }
    
    /**
     * This method is required for a trie, such as {@link SmallStringSetTrie}
     * 
     * @param keyPrefix
     * @return the first (lowest) complete key starting with the given prefix @CanBeNull
     *         if no such key exists.
     */
    public String lookupFirstPrefix(String keyPrefix) {
        SortedMap<String,E> subMap = this.map.subMap(keyPrefix, keyPrefix + LAST_UNICODE_CHAR);
        return subMap.isEmpty() ? null : subMap.firstKey();
    }
    
    public int size() {
        return this.map.size();
    }
    
}
