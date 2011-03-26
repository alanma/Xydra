package org.xydra.base;

import java.io.Serializable;

import org.xydra.base.value.XValue;


/**
 * A identification object that can be serialised.
 * 
 * <em>Important:</em> Implementations of XID must implement <tt>equals()</tt>
 * and <tt>hashCode</tt> correctly.
 * 
 * @author voelkel
 */
public interface XID extends XValue, Serializable, Comparable<XID> {
	
	/**
	 * To convert this XID to a byte[] representation use the ByteUtils (TBD).
	 * 
	 * @return a compact String which can be turned into an XID again via
	 *         {@link XIDProvider#fromString(String)}. The length of the string
	 *         SHOULD be at most 100 characters. TODO Why?
	 */
	String toString();
	
}
