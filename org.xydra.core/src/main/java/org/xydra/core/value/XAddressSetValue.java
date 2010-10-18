package org.xydra.core.value;

import org.xydra.core.model.XAddress;
import org.xydra.core.model.XID;


/**
 * An {@link XValue} for storing a set of {@link XAddress XAddresss}.
 * 
 * @author voelkel, dscharrer
 * 
 */
public interface XAddressSetValue extends XSetValue<XAddress> {
	
	/**
	 * Returns the contents of this XIDSetValue as an array.
	 * 
	 * Note: Changes to the returned array will not affect the XIDSetValue.
	 * 
	 * @return an array containing the {@link XID} values of this XIDSetValue.
	 */
	public XAddress[] contents();
	
	/**
	 * Creates a new {@link XAddressSetValue} containing all entries from this
	 * value as well as the specified entry. This value is not modified.
	 * 
	 * @param entry The new entry
	 * @return a new {@link XAddressSetValue} containing all entries from this
	 *         value as well as the given entry
	 */
	XAddressSetValue add(XAddress entry);
	
	/**
	 * Creates a new {@link XAddressSetValue} containing all entries from this
	 * value except the specified entry. This value is not modified.
	 * 
	 * @param entry The entry which is to be removed
	 * @return a new {@link XAddressSetValue} containing all entries from this
	 *         value expect the given entry
	 */
	XAddressSetValue remove(XAddress entry);
	
}
