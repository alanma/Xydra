package org.xydra.core.value;

/**
 * An {@link XValue} for storing a list of Java Byte values.
 * 
 * @author Kaidel
 * 
 */
public interface XByteListValue extends XListValue<Byte> {
	
	/**
	 * Returns the Byte values as an array in the order they were added to the
	 * list.
	 * 
	 * Note: Changes to the returned array will not affect the XByteListValue.
	 * 
	 * @return an array containing the list of Byte values in the order they
	 *         were added to the list
	 */
	byte[] contents();
	
}
