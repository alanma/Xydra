package org.xydra.base.value;

/**
 * An {@link XValue} for storing a single Java Boolean value.
 * 
 * @author Kaidel
 * 
 */
public interface XBooleanValue extends XValue {
	
	/**
	 * Returns the stored boolean value.
	 * 
	 * @return The stored boolean value.
	 */
	boolean contents();
	
}
