package org.xydra.core.value;

/**
 * An {@link XValue} for storing a list of Java Long values.
 * 
 * @author Kaidel
 * 
 */
public interface XLongListValue extends XNumberListValue<Long> {
	
	/**
	 * Returns the Long values as an array in the order they were added to the
	 * list.
	 * 
	 * Note: Changes to the returned array will not affect the XLongListValue.
	 * 
	 * @return an array containing the list of Long values in the order they
	 *         were added to the list
	 */
	long[] contents();
	
}
