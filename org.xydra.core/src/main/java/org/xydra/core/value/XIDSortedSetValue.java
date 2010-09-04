package org.xydra.core.value;

import org.xydra.core.model.XID;


/**
 * An {@link XValue} for storing a sorted set of {@link XID XIDs}. Sorted by
 * insertion order.
 * 
 * @author dscharrer, voelkel
 * 
 */
public interface XIDSortedSetValue extends XIDSetValue {
	
	/**
	 * Returns the contents of this XIDSetValue as an array.
	 * 
	 * Note: Changes to the returned array will not affect the XIDSetValue.
	 * 
	 * @return an array containing the {@link XID} values of this XIDSetValue in
	 *         insertion order
	 */
	public XID[] contents();
	
	/**
	 * Create a new {@link XIDSortedSetValue} contains all entries from this
	 * value as well as the specified entry. This value is not modified.
	 */
	XIDSortedSetValue add(XID entry);
	
	/**
	 * Create a new {@link XIDSortedSetValue} contains all entries from this
	 * value except the specified entry. This value is not modified.
	 */
	XIDSortedSetValue remove(XID entry);
	
}
