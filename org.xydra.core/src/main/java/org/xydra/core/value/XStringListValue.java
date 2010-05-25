package org.xydra.core.value;

/**
 * An XValue for storing a list of String values.
 * 
 * @author Kaidel
 * 
 */

public interface XStringListValue extends XListValue<String> {
	
	/**
	 * @return the list of String values in order (changes to the returned array
	 *         won't affect the value)
	 */
	
	String[] contents();
	
}
