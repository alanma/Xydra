package org.xydra.sharedutils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Iterator;

import org.xydra.annotations.RunsInGWT;
import org.xydra.index.impl.IteratorUtils;


/**
 * A helper class to simplify writing tests
 * 
 * @author xamde
 */
@RunsInGWT(false)
public class TestUtils {
    
    /**
     * @param it
     * @param expected
     */
    public static <E> void assertIteratorContains(Iterator<E> it, E ... expected) {
        HashSet<E> exp = new HashSet<E>();
        for(E e : expected)
            exp.add(e);
        HashSet<E> found = new HashSet<E>();
        IteratorUtils.addAll(it, found);
        
        assertEquals("Should have the same size", exp.size(), found.size());
        for(E e : exp) {
            assertTrue("Should be returned: " + e + " found only " + found, found.contains(e));
        }
    }
    
}
