package org.xydra.index.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.xydra.index.ITripleIndex;
import org.xydra.index.query.EqualsConstraint;


public class TripleIndexTest {
    
    final Integer s1 = new Integer(101);
    final Integer s2 = new Integer(102);
    final Integer p1 = new Integer(011);
    final Integer p2 = new Integer(012);
    final Integer o1 = new Integer(001);
    final Integer o2 = new Integer(002);
    
    @Test
    public void testIndexAndDeindexWithContainsAndQuery() {
        ITripleIndex<Integer,Integer,Integer> ti = new FastTripleIndex<Integer,Integer,Integer>();
        
        assertFalse(ti.contains(new EqualsConstraint<Integer>(this.s1),
                new EqualsConstraint<Integer>(this.p1), new EqualsConstraint<Integer>(this.o1)));
        assertFalse(ti.getTriples(new EqualsConstraint<Integer>(this.s1),
                new EqualsConstraint<Integer>(this.p1), new EqualsConstraint<Integer>(this.o1))
                .hasNext());
        
        ti.index(this.s1, this.p1, this.o1);
        
        assertTrue(ti.contains(new EqualsConstraint<Integer>(this.s1),
                new EqualsConstraint<Integer>(this.p1), new EqualsConstraint<Integer>(this.o1)));
        assertTrue(ti.getTriples(new EqualsConstraint<Integer>(this.s1),
                new EqualsConstraint<Integer>(this.p1), new EqualsConstraint<Integer>(this.o1))
                .hasNext());
        
        ti.deIndex(this.s1, this.p1, this.o1);
        
        assertFalse(ti.contains(new EqualsConstraint<Integer>(this.s1),
                new EqualsConstraint<Integer>(this.p1), new EqualsConstraint<Integer>(this.o1)));
        assertFalse(ti.getTriples(new EqualsConstraint<Integer>(this.s1),
                new EqualsConstraint<Integer>(this.p1), new EqualsConstraint<Integer>(this.o1))
                .hasNext());
    }
    
}
