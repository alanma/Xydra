package org.xydra.index.impl.trie;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;


/**
 * Tests a {@link java.util.SortedMap}
 * 
 * @author xamde
 */
public abstract class SortedMapTest {
    
    private SortedMap<String,Integer> map;
    
    @Before
    public void before() {
        this.map = createMap();
    }
    
    protected abstract SortedMap<String,Integer> createMap();
    
    @Test
    public void testHashCode() {
        this.map.hashCode();
    }
    
    @Test
    public void testClear() {
        this.map.clear();
        assertTrue(this.map.isEmpty());
    }
    
    @Test
    public void testContainsKey() {
        this.map.put("a", 1);
        this.map.put("b", 2);
        assertTrue(this.map.containsKey("a"));
        assertTrue(this.map.containsKey("b"));
        assertFalse(this.map.containsKey("c"));
    }
    
    @Test
    public void testContainsValue() {
        this.map.put("a", 1);
        this.map.put("b", 2);
        assertTrue(this.map.containsValue(1));
        assertTrue(this.map.containsValue(2));
        assertFalse(this.map.containsValue(3));
    }
    
    @Test
    public void testEntrySet() {
        for(Entry<String,Integer> e : this.map.entrySet()) {
            System.out.println(e.toString());
        }
    }
    
    @Test
    public void testEqualsObject() {
        assertTrue(this.map.equals(this.map));
    }
    
    @Test
    public void testGet() {
        this.map.put("a", 1);
        this.map.put("b", 2);
        assertEquals((Integer)1, this.map.get("a"));
        assertEquals((Integer)2, this.map.get("b"));
        assertEquals(null, this.map.get("c"));
    }
    
    @Test
    public void testIsEmpty() {
        assertTrue(this.map.isEmpty());
        this.map.put("a", 1);
        this.map.put("b", 2);
        assertFalse(this.map.isEmpty());
    }
    
    @Test
    public void testKeySet() {
        this.map.put("a", 1);
        this.map.put("b", 2);
        Set<String> keyset = this.map.keySet();
        assertTrue(keyset.contains("a"));
        assertTrue(keyset.contains("b"));
        assertFalse(keyset.contains("c"));
    }
    
    @Test
    public void testPut() {
        Integer a = this.map.put("a", 1);
        assertNull(a);
        a = this.map.put("a", 2);
        assertNotNull(a);
    }
    
    @SuppressWarnings("unused")
    @Test
    public void testPutInBetween() {
        Integer a = this.map.put("a", 1);
        Integer c = this.map.put("c", 3);
        Integer b = this.map.put("b", 2);
        List<Integer> values = new ArrayList<>();
        values.addAll(this.map.values());
        assertEquals(3, values.size());
        assertEquals((Integer)1, values.get(0));
        assertEquals((Integer)2, values.get(1));
        assertEquals((Integer)3, values.get(2));
    }
    
    @Test
    @Ignore
    public void testPutAll() {
        fail("Not yet implemented");
    }
    
    @Test
    public void testRemove() {
        this.map.put("a", 1);
        this.map.put("b", 2);
        this.map.remove("b");
        this.map.remove("c");
        this.map.remove("a");
        assertTrue(this.map.isEmpty());
    }
    
    @Test
    public void testSize() {
        assertEquals(0, this.map.size());
        this.map.put("a", 1);
        this.map.put("b", 2);
        assertEquals(2, this.map.size());
        this.map.put("a", 3);
        assertEquals(2, this.map.size());
    }
    
    @Test
    public void testToString() {
        this.map.put("a", 1);
        this.map.put("b", 2);
        System.out.println(this.map.toString());
    }
    
    @Test
    public void testValues() {
        this.map.put("a", 1);
        this.map.put("b", 2);
        Collection<Integer> values = this.map.values();
        assertTrue(values.contains(1));
        assertTrue(values.contains(2));
        assertFalse(values.contains(3));
    }
    
    @Test
    public void testSubMap() {
        this.map.put("a", 1);
        this.map.put("b", 2);
        this.map.put("c", 3);
        this.map.put("d", 4);
        this.map.put("e", 5);
        
        SortedMap<String,Integer> submap = this.map.subMap("b", "d");
        dump(submap);
        assertEquals(2, submap.size());
    }
    
    @SuppressWarnings("rawtypes")
    private void dump(SortedMap<String,Integer> map) {
        if(map instanceof SortedArrayMap) {
            ((SortedArrayMap)map).dump();
        }
    }
    
    @Test
    public void testHeadMap() {
        this.map.put("a", 1);
        this.map.put("b", 2);
        this.map.put("c", 3);
        this.map.put("d", 4);
        this.map.put("e", 5);
        
        SortedMap<String,Integer> submap = this.map.headMap("d");
        assertEquals(3, submap.size());
    }
    
    @Test
    public void testTailMap() {
        this.map.put("a", 1);
        this.map.put("b", 2);
        this.map.put("c", 3);
        this.map.put("d", 4);
        this.map.put("e", 5);
        
        SortedMap<String,Integer> submap = this.map.tailMap("b");
        assertEquals(4, submap.size());
    }
    
    @Test
    public void testFirstKey() {
        this.map.put("a", 1);
        this.map.put("b", 2);
        this.map.put("c", 3);
        assertTrue(this.map.firstKey().equals("a"));
    }
    
    @Test
    public void testLastKey() {
        this.map.put("a", 1);
        this.map.put("b", 2);
        this.map.put("c", 3);
        assertTrue(this.map.lastKey().equals("c"));
    }
    
    @Test
    public void testSubmapIsEmpty() {
        this.map.put("a", 1);
        this.map.put("b", 2);
        this.map.put("c", 3);
        this.map.put("d", 4);
        this.map.put("e", 5);
        
        SortedMap<String,Integer> submap = this.map.subMap("c", "z");
        assertEquals(3, submap.size());
        assertFalse(submap.isEmpty());
        submap = this.map.subMap("x", "z");
        assertEquals(0, submap.size());
        assertTrue(submap.isEmpty());
    }
    
    @Test
    public void testSubmapValuesIterator() {
        this.map.put("a", 1);
        this.map.put("b", 2);
        this.map.put("c", 3);
        this.map.put("d", 4);
        this.map.put("e", 5);
        
        SortedMap<String,Integer> submap = this.map.subMap("c", "z");
        Collection<Integer> values = submap.values();
        Set<Integer> vs = new HashSet<>();
        vs.addAll(values);
        System.out.println(values);
        assertEquals(3, vs.size());
        assertTrue(vs.contains(3));
        assertTrue(vs.contains(4));
        assertTrue(vs.contains(5));
    }
    
    @Test
    public void testSubmapFirstKey() {
        this.map.put("a", 1);
        this.map.put("b", 2);
        this.map.put("c", 3);
        this.map.put("d", 4);
        this.map.put("e", 5);
        
        SortedMap<String,Integer> submap = this.map.subMap("c", "z");
        assertEquals("c", submap.firstKey());
    }
    
}
