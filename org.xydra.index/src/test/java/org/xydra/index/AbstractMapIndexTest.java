package org.xydra.index;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.xydra.index.iterator.Iterators;
import org.xydra.index.query.EqualsConstraint;
import org.xydra.index.query.KeyEntryTuple;
import org.xydra.index.query.Wildcard;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;

import java.util.Iterator;
import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;


public abstract class AbstractMapIndexTest<K, E> {
    
    private static final Logger log = LoggerFactory.getLogger(AbstractMapIndexTest.class);
    
    E entry1 = createEntry();
    E entry2 = createEntry();
    E entry3 = createEntry();
    E entry4 = createEntry();
    K key1 = createKey();
    K key2 = createKey();
    K key3 = createKey();
    
    protected IMapIndex<K,E> mapIndex;
    
    public AbstractMapIndexTest(IMapIndex<K,E> mapIndex) {
        super();
        this.mapIndex = mapIndex;
    }
    
    @Before
    public void before() {
        this.mapIndex.clear();
    }
    
    protected abstract E createEntry();
    
    protected abstract K createKey();
    
    @Test
    @Ignore
    public void testComputeDiff() {
        // TODO implement diff test
    }
    
    @Test
    public void testContainsKeyString() {
        this.mapIndex.index(this.key1, this.entry1);
        this.mapIndex.index(this.key2, this.entry2);
        this.mapIndex.index(this.key1, this.entry3);
        
        assertTrue(this.mapIndex.containsKey(this.key1));
        assertTrue(this.mapIndex.containsKey(this.key2));
        assertFalse(this.mapIndex.containsKey(this.key3));
    }
    
    @Test
    public void testDeIndex() {
        K key1 = createKey();
        E entry1 = createEntry();
        E entry2 = createEntry();
        
        this.mapIndex.index(key1, entry1);
        this.mapIndex.deIndex(key1);
        log.info("empty again " + this.mapIndex.toString());
        assertTrue(this.mapIndex.isEmpty());
        
        this.mapIndex.index(key1, entry1);
        log.info("one key, one entry " + this.mapIndex.toString());
        this.mapIndex.index(key1, entry2);
        log.info("one key, two entries " + this.mapIndex.toString());
        assertFalse(this.mapIndex.isEmpty());
        
        this.mapIndex.deIndex(key1);
        log.info("empty again " + this.mapIndex.toString());
        assertTrue(this.mapIndex.isEmpty());
        
        this.mapIndex.index(key1, entry1);
        this.mapIndex.index(key1, entry2);
        this.mapIndex.deIndex(key1);
        log.info("empty again " + this.mapIndex.toString());
        assertTrue(this.mapIndex.isEmpty());
    }
    
    @Test
    public void testIndexAndClear() {
        assertTrue(this.mapIndex.isEmpty());
        
        this.mapIndex.index(createKey(), createEntry());
        assertFalse(this.mapIndex.isEmpty());
        
        this.mapIndex.clear();
        assertTrue(this.mapIndex.isEmpty());
    }
    
    @Test
    public void testKeyIterator() {
        this.mapIndex.index(this.key1, this.entry1);
        this.mapIndex.index(this.key2, this.entry2);
        this.mapIndex.index(this.key1, this.entry3);
        
        Iterator<K> it = this.mapIndex.keyIterator();
        List<K> list = Iterators.toList(it);
        
        assertEquals(2, list.size());
        assertTrue(list.contains(this.key1));
        assertTrue(list.contains(this.key2));
    }
    
    @Test
    public void testLookup() {
        this.mapIndex.index(this.key1, this.entry1);
        this.mapIndex.index(this.key2, this.entry2);
        // overwrite
        this.mapIndex.index(this.key1, this.entry3);
        
        E res = this.mapIndex.lookup(this.key3);
        assertNull(res);
        res = this.mapIndex.lookup(this.key1);
        assertTrue(res == this.entry3);
        res = this.mapIndex.lookup(this.key2);
        assertTrue(res == this.entry2);
    }
    
    @Test
    public void testTupleIterator() {
        this.mapIndex.index(this.key1, this.entry1);
        this.mapIndex.index(this.key2, this.entry2);
        // overwrites the previous mapping
        this.mapIndex.index(this.key1, this.entry3);
        // map = {s1=3, s2=2}
        
        List<KeyEntryTuple<K,E>> list;
        list = Iterators.toList(this.mapIndex.tupleIterator(new Wildcard<K>()));
        assertEquals(2, list.size());
        
        list = Iterators
                .toList(this.mapIndex.tupleIterator(new EqualsConstraint<K>(this.key1)));
        assertEquals("list=" + list, 1, list.size());
        assertTrue(list.get(0).getEntry() == this.entry3);
        
        list = Iterators
                .toList(this.mapIndex.tupleIterator(new EqualsConstraint<K>(this.key2)));
        assertEquals(1, list.size());
        assertTrue(list.get(0).getEntry() == this.entry2);
        
        list = Iterators
                .toList(this.mapIndex.tupleIterator(new EqualsConstraint<K>(this.key3)));
        assertEquals(0, list.size());
    }
}
