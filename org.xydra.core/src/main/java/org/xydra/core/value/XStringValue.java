package org.xydra.core.value;

/**
 * An XValue for storing a String.
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
