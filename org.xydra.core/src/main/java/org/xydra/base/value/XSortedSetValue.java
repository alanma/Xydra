package org.xydra.base.value;

import java.util.SortedSet;


/**
 * Marker interface
 * 
 * @param <E>
 */
public interface XSortedSetValue<E> extends XSetValue<E> {
    
    /**
     * Returns a {@link SortedSet} containing the values in this XSetValue.
     * 
     * Note: Changes to the returned {@link SortedSet} will not affect the
     * XSetValue.
     * 
     * @return a {@link SortedSet} containing values in this
     *         {@link XSortedSetValue} - changes to the {@link SortedSet} are
     *         NOT reflected in this value
     */
    public SortedSet<E> toSortedSet();
    
}
