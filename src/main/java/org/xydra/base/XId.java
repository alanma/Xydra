package org.xydra.base;

import java.io.Serializable;

import org.xydra.base.value.XSingleValue;


/**
 * A identification object that can be serialised.
 * 
 * <em>Important:</em> Implementations of XId must implement <tt>equals()</tt>
 * and <tt>hashCode</tt> correctly.
 * 
 * @author voelkel
 */
public interface XId extends XSingleValue<XId>, Serializable, Comparable<XId>, IHasXId {
	
	/**
	 * To convert this XId to a byte[] representation use the ByteUtils (TBD).
	 * 
	 * @return a compact String which can be turned into an XId again via. The
	 *         length of the string SHOULD be at most 100 characters for maximal
	 *         compatibility with all kinds of back-ends such as Google
	 *         AppEngine. {@link XIdProvider#fromString(String)}
	 */
	@Override
	String toString();
	
}
