package org.xydra.base.value;

/**
 * An {@link XValue} for storing a Java byte array.
 * 
 * @author Kaidel
 * 
 */
public interface XBinaryValue extends XValue {
	
	/**
	 * Returns the Byte values as an array in the order they were added to the
	 * list.
	 * 
	 * Note: Changes to the returned array will not affect the XBinaryValue.
	 * 
	 * @return an array containing the list of Byte values in the order they
	 *         were added to the list
	 */
	byte[] contents();
	
}
