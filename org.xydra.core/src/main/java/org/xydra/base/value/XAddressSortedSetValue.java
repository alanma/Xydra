package org.xydra.base.value;

import org.xydra.base.XAddress;

import java.util.SortedSet;


/**
 * An {@link XValue} for storing a sorted set of {@link XAddress XAddresss}.
 * Sorted by insertion order.
 * 
 * @author dscharrer, voelkel
 * 
 */
public interface XAddressSortedSetValue extends XAddressSetValue, XSortedSetValue<XAddress> {
    
    /**
     * Creates a new {@link XAddressSortedSetValue} containing all entries from
     * this value as well as the specified entry. This value is not modified.
     * 
     * @param entry The new entry
     * @return a new {@link XAddressSortedSetValue} containing all entries from
     *         this value as well as the given entry
     */
    @Override
    XAddressSortedSetValue add(XAddress entry);
    
    /**
     * Returns the contents of this {@link XAddressSortedSetValue} as an array.
     * 
     * Note: Changes to the returned array will not affect the
     * {@link XAddressSortedSetValue}.
     * 
     * @return an array containing the {@link XAddress} values of this
     *         XAddressSortedSetValue in insertion order
     */
    @Override
    public XAddress[] contents();
    
    /**
     * Creates a new {@link XAddressSortedSetValue} containing all entries from
     * this value except the specified entry. This value is not modified.
     * 
     * @param entry The entry which is to be removed
     * @return a new {@link XAddressSortedSetValue} containing all entries from
     *         this value expect the given entry
     */
    @Override
    XAddressSortedSetValue remove(XAddress entry);
    
    /**
     * Creates a {@link SortedSet} containing all {@link XAddress} entries in
     * this {@link XAddressSortedSetValue}.
     * 
     * Note: changes to the {@link SortedSet} are NOT reflected in this value
     * 
     * @return a {@link SortedSet} containing all {@link XAddress} entries in
     *         this {@link XAddressSortedSetValue}
     */
    @Override
	SortedSet<XAddress> toSortedSet();
    
}
