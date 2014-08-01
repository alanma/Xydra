package org.xydra.index.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.xydra.index.IIntegerRangeIndex;
import org.xydra.index.impl.IntegerRangeIndex.Span;
import org.xydra.index.iterator.Iterators;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;

import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.junit.Test;


public class IntegerRangeIndexTest {
    
    @SuppressWarnings("unused")
    private static final Logger log = LoggerFactory.getLogger(IntegerRangeIndexTest.class);
    
    @Test
    public void testIndex() {
        List<Entry<Integer,Integer>> list;
        
        IIntegerRangeIndex iri = new IntegerRangeIndex();
        assertTrue(iri.isEmpty());
        list = Iterators.toList(iri.rangesIterator());
        assertEquals(0, list.size());
        
        iri.index(13, 16);
        assertFalse(iri.isEmpty());
        list = Iterators.toList(iri.rangesIterator());
        assertEquals(1, list.size());
        assertInterval(13, 16, list.get(0));
        
        iri.index(19, 25);
        list = Iterators.toList(iri.rangesIterator());
        assertEquals(2, list.size());
        assertInterval(13, 16, list.get(0));
        assertInterval(19, 25, list.get(1));
        
        iri.index(10, 180);
        list = Iterators.toList(iri.rangesIterator());
        assertEquals(1, list.size());
        assertInterval(10, 180, list.get(0));
    }
    
    @Test
    public void testIndex2() {
        List<Entry<Integer,Integer>> list;
        IIntegerRangeIndex iri = new IntegerRangeIndex();
        
        iri.index(10, 20);
        iri.index(5, 20);
        
        list = Iterators.toList(iri.rangesIterator());
        assertEquals(1, list.size());
        assertInterval(5, 20, list.get(0));
    }
    
    @Test
    public void testDeindex() {
        List<Entry<Integer,Integer>> list;
        
        IIntegerRangeIndex iii = new IntegerRangeIndex();
        iii.index(10, 180);
        iii.deIndex(20, 30);
        
        list = Iterators.toList(iii.rangesIterator());
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
        
        list = Iterators.toList(iii.rangesIterator());
        assertEquals(1, list.size());
        assertInterval(10, 19, list.get(0));
    }
    
    @Test
    public void testDeindex3() {
        List<Entry<Integer,Integer>> list;
        
        IIntegerRangeIndex iii = new IntegerRangeIndex();
        iii.index(10, 180);
        iii.deIndex(5, 20);
        
        list = Iterators.toList(iii.rangesIterator());
        assertEquals(1, list.size());
        assertInterval(21, 180, list.get(0));
    }
    
    private static void assertInterval(int s, int e, Entry<Integer,Integer> entry) {
        assertEquals(s, (int)entry.getKey());
        assertEquals(e, (int)entry.getValue());
    }
    
    @Test
    public void testSpanIterator1() {
        IntegerRangeIndex iri = new IntegerRangeIndex();
        iri.index(10, 20);
        iri.index(30, 40);
        
        Iterator<Span> spanIt = iri.spanIterator(100);
        List<Span> list = Iterators.toList(spanIt);
        // for(Span span : list) {
        // System.out.println(span);
        // }
        assertEquals(5, list.size());
        assertSpan(0, 9, false, list.get(0));
        assertSpan(10, 20, true, list.get(1));
        assertSpan(21, 29, false, list.get(2));
        assertSpan(30, 40, true, list.get(3));
        assertSpan(41, 100, false, list.get(4));
        
    }
    
    private static void assertSpan(int s, int e, boolean inRange, Span span) {
        assertEquals(s, span.startInclusive);
        assertEquals(e, span.endInclusive);
        assertEquals(inRange, span.isInRange);
    }
}
