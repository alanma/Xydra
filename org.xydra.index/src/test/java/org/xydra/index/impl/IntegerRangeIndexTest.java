package org.xydra.index.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map.Entry;

import org.junit.Test;
import org.xydra.index.IIntegerRangeIndex;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;


public class IntegerRangeIndexTest {
    
    @SuppressWarnings("unused")
    private static final Logger log = LoggerFactory.getLogger(IntegerRangeIndexTest.class);
    
    @Test
    public void testIsInInterval() {
        List<Entry<Integer,Integer>> list;
        
        IIntegerRangeIndex iri = new IntegerRangeIndex();
        assertTrue(iri.isEmpty());
        list = IteratorUtils.toList(iri.rangesIterator());
        assertEquals(0, list.size());
        
        iri.index(13, 16);
        assertFalse(iri.isEmpty());
        list = IteratorUtils.toList(iri.rangesIterator());
        assertEquals(1, list.size());
        assertInterval(13, 16, list.get(0));
        
        iri.index(19, 25);
        list = IteratorUtils.toList(iri.rangesIterator());
        assertEquals(2, list.size());
        assertInterval(13, 16, list.get(0));
        assertInterval(19, 25, list.get(1));
        
        iri.index(10, 180);
        list = IteratorUtils.toList(iri.rangesIterator());
        assertEquals(1, list.size());
        assertInterval(10, 180, list.get(0));
    }
    
    @Test
    public void testDeindex() {
        List<Entry<Integer,Integer>> list;
        
        IIntegerRangeIndex iii = new IntegerRangeIndex();
        iii.index(10, 180);
        iii.deIndex(20, 30);
        
        list = IteratorUtils.toList(iii.rangesIterator());
        assertEquals(2, list.size());
        assertInterval(10, 19, list.get(0));
        assertInterval(31, 180, list.get(1));
    }
    
    @Test
    public void testDeindex2() {
        List<Entry<Integer,Integer>> list;
        
        IIntegerRangeIndex iii = new IntegerRangeIndex();
        iii.index(10, 180);
        iii.deIndex(20, 200);
        
        list = IteratorUtils.toList(iii.rangesIterator());
        assertEquals(1, list.size());
        assertInterval(10, 19, list.get(0));
    }
    
    @Test
    public void testDeindex3() {
        List<Entry<Integer,Integer>> list;
        
        IIntegerRangeIndex iii = new IntegerRangeIndex();
        iii.index(10, 180);
        iii.deIndex(5, 20);
        
        list = IteratorUtils.toList(iii.rangesIterator());
        assertEquals(1, list.size());
        assertInterval(21, 180, list.get(0));
    }
    
    private static void assertInterval(int s, int e, Entry<Integer,Integer> entry) {
        assertEquals(s, (int)entry.getKey());
        assertEquals(e, (int)entry.getValue());
    }
    
}
