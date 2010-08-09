package org.xydra.core.value;

/**
 * An {@link XValue} for storing a list of Java Double values.
 * 
 * @author Kaidel
 * 
 */
public interface XDoubleListValue extends XNumberListValue<Double> {
	
	/**
	 * Returns the Double values as an array in the order they were added to the
	 * list.
	 * 
	 * Note: Changes to the returned array will not affect the XDoubleListValue.
	 * 
	 * @return an array containing the list of Double values in the order they
	 *         were added to the list
	 */
	double[] contents();
	
}
