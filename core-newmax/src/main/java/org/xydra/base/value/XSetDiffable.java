package org.xydra.base.value;

import java.util.Set;


/**
 * Can compute diffs on {@link XSetValue}
 * 
 * @author xamde
 * 
 * @param <E> The type of value which is to be stored.
 */
public interface XSetDiffable<E> {
    
    /**
     * @param otherSet the other set is the future. What is found here and not
     *            present in this, has been added. @NeverNull
     * @return an {@link XSetDiff}
     */
    XSetDiff<E> computeDiff(XSetValue<E> otherSet);
    
    /**
     * A diff of two id sets
     * 
     * @param <E>
     */
    public static interface XSetDiff<E> {
        /**
         * @return all added tuples; writes to this data have no effect.
         */
        Set<E> getAdded();
        
        /**
         * @return all removed tuples; writes to this data have no effect.
         */
        Set<E> getRemoved();
    }
    
}
