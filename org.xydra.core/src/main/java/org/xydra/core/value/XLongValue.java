package org.xydra.core.value;

/**
 * An {@link XValue} for storing a single Java Long value.
 * 
 * @author Kaidel
 * 
 */
public interface XLongValue extends XNumberValue {
	
	/**
	 * Return the stored Long value.
	 * 
	 * @return The stored Long value.
	 */
	public long contents();
	
}
