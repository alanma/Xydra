package org.xydra.core.value;

/**
 * An XValue for storing a list of long values.
 * 
 * @author Kaidel
 * 
 */

public interface XLongListValue extends XListValue<Long> {
	
	/**
	 * @return the list of long values in order (changes to the returned array
	 *         won't affect the value)
	 */
	
	Long[] contents();
}
