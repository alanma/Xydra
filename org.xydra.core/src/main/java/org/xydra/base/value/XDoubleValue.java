package org.xydra.base.value;

/**
 * An {@link XValue} for storing a single Java Double value.
 * 
 * @author kaidel
 * 
 */
public interface XDoubleValue extends XNumberValue, XSingleValue<Double> {

	/**
	 * Returns the stored double value.
	 * 
	 * @return The stored double value.
	 */
	double contents();

}
