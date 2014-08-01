package org.xydra.index;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;
import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import org.xydra.index.iterator.Iterators;
import org.xydra.index.query.EqualsConstraint;
import org.xydra.index.query.KeyEntryTuple;
import org.xydra.index.query.Wildcard;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;


public abstract class AbstractMapSetIndexTest<K, E> {
    
    private static final Logger log = LoggerFactory.getLogger(AbstractMapSetIndexTest.class);
    
    E entry1 = createEntry();
    E entry2 = createEntry();
    E entry3 = createEntry();
    E entry4 = createEntry();
    K key1 = createKey();
    K key2 = createKey();
    K key3 = createKey();
    
    protected IMapSetIndex<K,E> mapSetIndex;
    
    public AbstractMapSetIndexTest(IMapSetIndex<K,E> mapSetIndex) {
        super();
        this.mapSetIndex = mapSetIndex;
    }
    
    @Before
    public void before() {
        this.mapSetIndex.clear();
    }
    
    protected abstract E createEntry();
    
    protected abstract K createKey();
    
    @Test
    @Ignore
    public void testComputeDiff() {
        // TODO implement diff test
    }
    
    @Test
    public void testConstraintIterator() {
        this.mapSetIndex.index(this.key1, this.entry1);
        this.mapSetIndex.index(this.key2, this.entry2);
        this.mapSetIndex.index(this.key1, this.entry3);
        
        Iterator<E> it = this.mapSetIndex.constraintIterator(new EqualsConstraint<K>(this.key1));
        List<E> list = Iterators.toList(it);
        assertEquals(2, list.size());
        assertTrue(list.contains(this.entry1));
        assertTrue(list.contains(this.entry3));
        
        it = this.mapSetIndex.constraintIterator(new Wildcard<K>());
        list = Iterators.toList(it);
        assertEquals(3, list.size());
        assertTrue(list.contains(this.entry1));
        assertTrue(list.contains(this.entry2));
        assertTrue(list.contains(this.entry3));
    }
    
    @Test
    public void testContains() {
        
        this.mapSetIndex.index(this.key1, this.entry1);
        this.mapSetIndex.index(this.key1, this.entry2);
        
        log.info("Indexing (key2,entry3) in " + this.mapSetIndex);
        this.mapSetIndex.index(this.key2, this.entry3);
        
        assertTrue(this.mapSetIndex.contains(new Wildcard<K>(), new Wildcard<E>()));
        assertTrue(this.mapSetIndex.contains(new EqualsConstraint<K>(this.key1), new Wildcard<E>()));
        
        log.info("looking for (key2,*) " + this.mapSetIndex);
        assertTrue(this.mapSetIndex.contains(new EqualsConstraint<K>(this.key2), new Wildcard<E>()));
        assertFalse(this.mapSetIndex
                .contains(new EqualsConstraint<K>(this.key3), new Wildcard<E>()));
        assertTrue(this.mapSetIndex.contains(new EqualsConstraint<K>(this.key1),
                new EqualsConstraint<E>(this.entry1)));
        assertTrue(this.mapSetIndex.contains(new Wildcard<K>(),
                new EqualsConstraint<E>(this.entry1)));
        assertFalse(this.mapSetIndex.contains(new Wildcard<K>(), new EqualsConstraint<E>(
                this.entry4)));
    }
    
    @Test
    public void testContainsKeyString() {
        this.mapSetIndex.index(this.key1, this.entry1);
        this.mapSetIndex.index(this.key2, this.entry2);
        this.mapSetIndex.index(this.key1, this.entry3);
        
        assertTrue(this.mapSetIndex.containsKey(this.key1));
        assertTrue(this.mapSetIndex.containsKey(this.key2));
        assertFalse(this.mapSetIndex.containsKey(this.key3));
    }
    
    @Test
    public void testDeIndex() {
        K key1 = createKey();
        E entry1 = createEntry();
        E entry2 = createEntry();
        
        this.mapSetIndex.index(key1, entry1);
        this.mapSetIndex.deIndex(key1, entry1);
        log.info("empty again " + this.mapSetIndex.toString());
        assertTrue(this.mapSetIndex.isEmpty());
        
        this.mapSetIndex.index(key1, entry1);
        log.info("one key, one entry " + this.mapSetIndex.toString());
        this.mapSetIndex.index(key1, entry2);
        log.info("one key, two entries " + this.mapSetIndex.toString());
        assertFalse(this.mapSetIndex.isEmpty());
        
        this.mapSetIndex.deIndex(key1);
        log.info("empty again " + this.mapSetIndex.toString());
        assertTrue(this.mapSetIndex.isEmpty());
        
        this.mapSetIndex.index(key1, entry1);
        this.mapSetIndex.index(key1, entry2);
        this.mapSetIndex.deIndex(key1, entry1);
        log.info("one key, one entry " + this.mapSetIndex.toString());
        assertFalse(this.mapSetIndex.isEmpty());
        
        this.mapSetIndex.deIndex(key1, entry2);
        log.info("empty again " + this.mapSetIndex.toString());
        assertTrue(this.mapSetIndex.isEmpty());
    }
    
    @Test
    public void testIndexAndClear() {
        assertTrue(this.mapSetIndex.isEmpty());
        
        this.mapSetIndex.index(createKey(), createEntry());
        assertFalse(this.mapSetIndex.isEmpty());
        
        this.mapSetIndex.clear();
        assertTrue(this.mapSetIndex.isEmpty());
    }
    
    @Test
    public void testKeyIterator() {
        this.mapSetIndex.index(this.key1, this.entry1);
        this.mapSetIndex.index(this.key2, this.entry2);
        this.mapSetIndex.index(this.key1, this.entry3);
        
        Iterator<K> it = this.mapSetIndex.keyIterator();
        List<K> list = Iterators.toList(it);
        
        assertEquals(2, list.size());
        assertTrue(list.contains(this.key1));
        assertTrue(list.contains(this.key2));
    }
    
    @Test
    public void testLookup() {
        this.mapSetIndex.index(this.key1, this.entry1);
        this.mapSetIndex.index(this.key2, this.entry2);
        this.mapSetIndex.index(this.key1, this.entry3);
        
        IEntrySet<E> res = this.mapSetIndex.lookup(this.key3);
        assertNull(res);
        res = this.mapSetIndex.lookup(this.key1);
        assertEquals(2, res.size());
        assertTrue(res.contains(this.entry1));
        assertTrue(res.contains(this.entry3));
        res = this.mapSetIndex.lookup(this.key2);
        assertEquals(1, res.size());
        assertTrue(res.contains(this.entry2));
    }
    
    @Test
    public void testTupleIterator() {
        this.mapSetIndex.index(this.key1, this.entry1);
        this.mapSetIndex.index(this.key2, this.entry2);
        this.mapSetIndex.index(this.key1, this.entry3);
        
        List<KeyEntryTuple<K,E>> list;
        list = Iterators.toList(this.mapSetIndex.tupleIterator(new Wildcard<K>(),
                new Wildcard<E>()));
        assertEquals(3, list.size());
        
        list = Iterators.toList(this.mapSetIndex.tupleIterator(new EqualsConstraint<K>(
                this.key1), new Wildcard<E>()));
        assertEquals(2, list.size());
        
        list = Iterators.toList(this.mapSetIndex.tupleIterator(new EqualsConstraint<K>(
                this.key2), new Wildcard<E>()));
        assertEquals(1, list.size());
        
        list = Iterators.toList(this.mapSetIndex.tupleIterator(new EqualsConstraint<K>(
                this.key3), new Wildcard<E>()));
        assertEquals(0, list.size());
        
        list = Iterators.toList(this.mapSetIndex.tupleIterator(new Wildcard<K>(),
                new EqualsConstraint<E>(this.entry1)));
        assertEquals(1, list.size());
        
        list = Iterators.toList(this.mapSetIndex.tupleIterator(new Wildcard<K>(),
                new EqualsConstraint<E>(this.entry2)));
        assertEquals(1, list.size());
        
        list = Iterators.toList(this.mapSetIndex.tupleIterator(new Wildcard<K>(),
                new EqualsConstraint<E>(this.entry3)));
        assertEquals(1, list.size());
        
        list = Iterators.toList(this.mapSetIndex.tupleIterator(new Wildcard<K>(),
                new EqualsConstraint<E>(this.entry4)));
        assertEquals(0, list.size());
        
        list = Iterators.toList(this.mapSetIndex.tupleIterator(new EqualsConstraint<K>(
                this.key1), new EqualsConstraint<E>(this.entry1)));
        assertEquals(1, list.size());
        
        list = Iterators.toList(this.mapSetIndex.tupleIterator(new EqualsConstraint<K>(
                this.key2), new EqualsConstraint<E>(this.entry1)));
        assertEquals(0, list.size());
    }
}
