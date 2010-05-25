package org.xydra.core.value;

/**
 * An XValue for storing a list of integer values.
 * 
 * @author Kaidel
 * 
 */

public interface XIntegerListValue extends XListValue<Integer> {
	
	/**
	 * @return the list of integer values in order (changes to the returned
	 *         array won't affect the value)
	 */
	
	Integer[] contents();
}
