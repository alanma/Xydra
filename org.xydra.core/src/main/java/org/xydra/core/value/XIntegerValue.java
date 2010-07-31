package org.xydra.core.value;

/**
 * An XValue for storing an integer value.
 * 
 * @author Kaidel
 * 
 */
public interface XIntegerValue extends XNumberValue {
	
	/**
	 * Returns the stored integer value.
	 * 
	 * @return The stored integer value.
	 */
	public int contents();
	
}
