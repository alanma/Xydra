package org.xydra.base.value;

import java.util.SortedSet;

import org.xydra.base.XID;


/**
 * An {@link XValue} for storing a sorted set of {@link XID XIDs}. Sorted by
 * insertion order.
 * 
 * @author dscharrer, voelkel
 * 
 */
public interface XIDSortedSetValue extends XIDSetValue {
	
	/**
	 * Creates a new {@link XIDSortedSetValue} containing all entries from this
	 * value as well as the specified entry. This value is not modified.
	 * 
	 * @param entry The new entry
	 * @return a new {@link XIDSortedSetValue} containing all entries from this
	 *         value as well as the given entry
	 */
	@Override
    XIDSortedSetValue add(XID entry);
	
	/**
	 * Returns the contents of this XIDSortedSetValue as an array.
	 * 
	 * Note: Changes to the returned array will not affect the
	 * XIDSortedSetValue.
	 * 
	 * @return an array containing the {@link XID} values of this
	 *         XIDSortedSetValue in insertion order
	 */
	@Override
    public XID[] contents();
	
	/**
	 * Creates a new {@link XIDSortedSetValue} containing all entries from this
	 * value except the specified entry. This value is not modified.
	 * 
	 * @param entry The entry which is to be removed
	 * @return a new {@link XIDSortedSetValue} containing all entries from this
	 *         value expect the given entry
	 */
	@Override
    XIDSortedSetValue remove(XID entry);
	
	/**
	 * Creates a {@link SortedSet} containing all {@link XID} entries in this
	 * {@link XIDSortedSetValue}.
	 * 
	 * Note: changes to the {@link SortedSet} are NOT reflected in this value
	 * 
	 * @return a {@link SortedSet} containing all {@link XID} entries in this
	 *         {@link XIDSortedSetValue}
	 */
	SortedSet<XID> toSortedSet();
	
}
