package org.xydra.index.iterator;

import org.xydra.annotations.RunsInGWT;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Set;


/**
 * See also {@link Iterators}
 * 
 * @author xamde
 * 
 */
@RunsInGWT(false)
public class Iterators_NoGwt {
    
    /**
     * A very pragmatic way to deal with {@link ConcurrentModificationException}
     * . If we get one, we just retry the call after a given time-out. We
     * remember seen elements, so there won't be duplicates. Use at your own
     * risk.
     * 
     * @param base
     * @param timeout to sleep in case of exception
     * @return a concurrency-free copy
     */
    public static <E> Iterator<E> retryConcurrent(Iterator<E> base, int timeout) {
        int t = timeout;
        while(true) {
            try {
                Set<E> set = Iterators.toSet(base);
                return set.iterator();
            } catch(ConcurrentModificationException e) {
                try {
                    Thread.sleep(t);
                    t *= 2;
                } catch(InterruptedException e1) {
                    t = timeout;
                }
            }
        }
    }
    
}
