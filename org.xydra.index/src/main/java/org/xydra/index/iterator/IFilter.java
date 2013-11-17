package org.xydra.index.iterator;

public interface IFilter<E> {
    
    /**
     * @param entry
     * @return true iff the entry matches the filter and should therefore be
     *         included in the resulting iterator
     */
    boolean matches(E entry);
    
}
