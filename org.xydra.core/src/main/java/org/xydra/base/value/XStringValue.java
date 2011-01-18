package org.xydra.base.value;

/**
 * An {@link XValue} for storing a single Java String.
 * 
 * @author Kaidel
 * 
 */
public interface XStringValue extends XValue {
	
	/**
	 * Returns the stored String value.
	 * 
	 * @return The stored String value.
	 */
	String contents();
	
}
