package org.xydra.base.value;

/**
 * An {@link XValue} for storing a single Java Integer value.
 * 
 * @author Kaidel
 * 
 */
public interface XIntegerValue extends XNumberValue, XSingleValue<Integer> {
	
	/**
	 * Returns the stored integer value.
	 * 
	 * @return The stored integer value.
	 */
	public int contents();
	
}
