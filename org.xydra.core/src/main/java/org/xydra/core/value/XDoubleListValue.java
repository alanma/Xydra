package org.xydra.core.value;

/**
 * An XValue for storing a list of double values.
 * 
 * @author Kaidel
 * 
 */
public interface XDoubleListValue extends XNumberListValue<Double> {
	
	/**
	 * @return the list of double values in order (changes to the returned array
	 *         won't affect the value)
	 */
	double[] contents();
	
}
