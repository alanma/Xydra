package org.xydra.core.value;

/**
 * An {@link XValue} for storing a list of Java Number values.
 * 
 * @author Kaidel
 * 
 */
public interface XNumberListValue<T extends Number> extends XListValue<T> {
	
	/**
	 * Returns the Number values as an array in the order they were added to the
	 * list.
	 * 
	 * Note: Changes to the returned array will not affect the XNumberListValue.
	 * 
	 * @return an array containing the list of Number values in the order they
	 *         were added to the list
	 */
	Number[] toNumberArray();
	
}
