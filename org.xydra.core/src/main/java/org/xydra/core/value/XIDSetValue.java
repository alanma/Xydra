package org.xydra.core.value;

import org.xydra.core.model.XID;


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
	 * Create a new {@link XIDSetValue} contains all entries from this value as
	 * well as the specified entry. This value is not modified.
	 */
	XIDSetValue add(XID entry);
	
	/**
	 * Create a new {@link XIDSetValue} contains all entries from this value
	 * except the specified entry. This value is not modified.
	 */
	XIDSetValue remove(XID entry);
	
}
