package org.xydra.base.value;

/**
 * An {@link XValue} for storing a single Java Boolean value.
 * 
 * @author kaidel
 * 
 */
public interface XBooleanValue extends XSingleValue<Boolean> {

	/**
	 * Returns the stored boolean value.
	 * 
	 * @return The stored boolean value.
	 */
	boolean contents();

}
