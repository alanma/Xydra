package org.xydra.core.model;

import java.io.Serializable;


/**
 * A identification object that can be serialised.
 * 
 * <em>Important:</em> Implementations of XID must implement <tt>equals()</tt>
 * and <tt>hashCode</tt> correctly.
 * 
 * @author voelkel
 */
public interface XID extends Serializable, Comparable<XID> {
	
	// TODO conversion to URI, byte[] and String
	
	/**
	 * To convert this XID to a byte[] representation use the ByteUtils.
	 * 
	 * @return a valid URI TODO explain exact notion of valid
	 */
	String toURI();
	
}
