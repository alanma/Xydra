package org.xydra.core.value;

/**
 * An XValue for storing a list of boolean values.
 * 
 * @author Kaidel
 * 
 */

public interface XBooleanListValue extends XListValue<Boolean> {
	
	/**
	 * @return the list of boolean values in order (changes to the returned
	 *         array won't affect the value)
	 */
	Boolean[] contents();
	
}
