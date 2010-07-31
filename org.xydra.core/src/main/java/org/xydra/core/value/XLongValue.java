package org.xydra.core.value;

/**
 * An XValue for storing a long value.
 * 
 * @author Kaidel
 * 
 */
public interface XLongValue extends XNumberValue {
	
	/**
	 * Return the stored long value.
	 * 
	 * @return The stored long value.
	 */
	public long contents();
	
}
