package org.xydra.base.value;

import java.util.SortedSet;

import org.xydra.base.XId;


/**
 * An {@link XValue} for storing a sorted set of {@link XId XIds}.
 * 
 * Sorted by insertion order.
 * 
 * @author dscharrer, voelkel
 * 
 */
public interface XIdSortedSetValue extends XIdSetValue, XSortedSetValue<XId> {
    
    /**
     * Creates a new {@link XIdSortedSetValue} containing all entries from this
     * value as well as the specified entry. This value is not modified.
     * 
     * @param entry The new entry
     * @return a new {@link XIdSortedSetValue} containing all entries from this
     *         value as well as the given entry
     */
    @Override
    XIdSortedSetValue add(XId entry);
    
    /**
     * Returns the contents of this XIdSortedSetValue as an array.
     * 
     * Note: Changes to the returned array will not affect the
     * XIdSortedSetValue.
     * 
     * @return an array containing the {@link XId} values of this
     *         XIdSortedSetValue in insertion order
     */
    @Override
    public XId[] contents();
    
    /**
     * Creates a new {@link XIdSortedSetValue} containing all entries from this
     * value except the specified entry. This value is not modified.
     * 
     * @param entry The entry which is to be removed
     * @return a new {@link XIdSortedSetValue} containing all entries from this
     *         value expect the given entry
     */
    @Override
    XIdSortedSetValue remove(XId entry);
    
    /**
     * Creates a {@link SortedSet} containing all {@link XId} entries in this
     * {@link XIdSortedSetValue}. The sort order is NOT the insertion order, but
     * the natural ordering.
     * 
     * Note: changes to the {@link SortedSet} are NOT reflected in this value
     * 
     * @return a {@link SortedSet} containing all {@link XId} entries in this
     *         {@link XIdSortedSetValue}
     */
    @Override
	SortedSet<XId> toSortedSet();
    
}
