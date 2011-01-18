package org.xydra.base.value;

import org.xydra.base.XID;


/**
 * An {@link XValue} for storing a set of {@link XID XIDs}.
 * 
 * @author dscharrer
 * 
 */
public interface XIDSetValue extends XSetValue<XID> {
	
	/**
	 * Returns the contents of this XIDSetValue as an array.
	 * 
	 * Note: Changes to the returned array will not affect the XIDSetValue.
	 * 
	 * @return an array containing the {@link XID} values of this XIDSetValue.
	 */
	public XID[] contents();
	
	/**
	 * Creates a new {@link XIDSetValue} containing all entries from this value
	 * as well as the specified entry. This value is not modified.
	 * 
	 * @param entry The new entry
	 * @return a new {@link XIDSetValue} containing all entries from this value
	 *         as well as the given entry
	 */
	XIDSetValue add(XID entry);
	
	/**
	 * Creates a new {@link XIDSetValue} containing all entries from this value
	 * except the specified entry. This value is not modified.
	 * 
	 * @param entry The entry which is to be removed
	 * @return a new {@link XIDSetValue} containing all entries from this value
	 *         expect the given entry
	 */
	XIDSetValue remove(XID entry);
	
}
