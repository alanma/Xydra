package org.xydra.core.value;

/**
 * An {@link XValue} for storing a list of Java Integer values.
 * 
 * @author Kaidel
 * 
 */
public interface XIntegerListValue extends XNumberListValue<Integer> {
	
	/**
	 * Returns the Integer values as an array in the order they were added to
	 * the list.
	 * 
	 * Note: Changes to the returned array will not affect the
	 * XIntegerListValue.
	 * 
	 * @return an array containing the list of Integer values in the order they
	 *         were added to the list
	 */
	int[] contents();
	
}
