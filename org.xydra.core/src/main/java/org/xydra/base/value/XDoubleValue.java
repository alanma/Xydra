package org.xydra.base.value;

/**
 * An {@link XValue} for storing a single Java Double value.
 *
 * The double data type is a double-precision 64-bit IEEE 754 floating point.
 * Its range of values is 4.94065645841246544e-324d to 1.79769313486231570e+308d
 * (positive or negative).
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
