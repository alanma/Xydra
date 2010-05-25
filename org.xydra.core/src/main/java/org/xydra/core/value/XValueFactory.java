package org.xydra.core.value;

import org.xydra.core.model.XID;


/**
 * A class for creating instances of the different XValue types.
 * 
 * @author Kaidel
 * 
 */
public interface XValueFactory {
	
	/**
	 * Creates an XStringValue.
	 * 
	 * @param string The String which is to be stored in the XStringValue.
	 * @return a {@link XStringValue} storing the given String.
	 */
	XStringValue createStringValue(String string);
	
	/**
	 * Creates an XIDValue.
	 * 
	 * @param id The XID which is to be stored in the XIDValue.
	 * @return a {@link XIDValue} storing the given XID.
	 */
	XIDValue createIDValue(XID id);
	
	/**
	 * Creates an XIDListValue.
	 * 
	 * @param xids The XIDS which are to be stored in the XIDListValue.
	 * @return a {@link XIDListValue} with the content given in 'xids'.
	 */
	XIDListValue createIDListValue(XID[] xids);
	
	/**
	 * Creates an XBooleanValue.
	 * 
	 * @param value .
	 * @return a {@link XBooleanValue} storing the given boolean value.
	 */
	XBooleanValue createBooleanValue(boolean value);
	
	/**
	 * Creates an XDoubleValue.
	 * 
	 * @param value The double value which is to be stored in the XDoubleValue.
	 * @return a {@link XDoubleValue} storing the given double value.
	 */
	XDoubleValue createDoubleValue(double value);
	
	/**
	 * Creates an XIntegerValue.
	 * 
	 * @param value The integer value which is to be stored in the
	 *            XIntegerValue.
	 * @return a {@link XIntegerValue} storing the given integer value
	 */
	XIntegerValue createIntegerValue(int value);
	
	/**
	 * Creates an XLongValue.
	 * 
	 * @param value The long value which is to be stored in the XLongValue.
	 * @return a {@link XLongValue} storing the given long value.
	 */
	XLongValue createLongValue(long value);
	
	/**
	 * Creates an XStringListValue.
	 * 
	 * @param strings The Strings which are to be stored in the
	 *            XStringListValue.
	 * @return a {@link XStringListValue} storing the given Strings.
	 */
	XStringListValue createStringListValue(String[] strings);
	
	/**
	 * Creates an XBooleanListValue.
	 * 
	 * @param values The boolean values which are to be stored in the
	 *            XBooleanListValue.
	 * @return a {@link XBooleanListValue} storing the given boolean values.
	 */
	XBooleanListValue createBooleanListValue(boolean[] values);
	
	/**
	 * Creates an XBooleanListValue.
	 * 
	 * @param values The boolean values which are to be stored in the
	 *            XBooleanListValue.
	 * @return a {@link XBooleanListValue} storing the given boolean values.
	 */
	XBooleanListValue createBooleanListValue(Boolean[] values);
	
	/**
	 * Creates an XDoubleListValue.
	 * 
	 * @param values The double values which are to be stored in the
	 *            XDoubleListValue.
	 * @return a {@link XDoubleListValue} storing the given double values
	 */
	XDoubleListValue createDoubleListValue(double[] values);
	
	/**
	 * Creates an XDoubleListValue.
	 * 
	 * @param values The double values which are to be stored in the
	 *            XDoubleListValue.
	 * @return a {@link XDoubleListValue} storing the given double values
	 */
	XDoubleListValue createDoubleListValue(Double[] values);
	
	/**
	 * Creates an XIntegerListValue.
	 * 
	 * @param values The integer values which are to be stored in the
	 *            XIntegerListValue.
	 * @return a {@link XIntegerListValue} storing the given integer values.
	 */
	XIntegerListValue createIntegerListValue(int[] values);
	
	/**
	 * Creates an XIntegerListValue.
	 * 
	 * @param values The integer values which are to be stored in the
	 *            XIntegerListValue.
	 * @return a {@link XIntegerListValue} storing the given integer values.
	 */
	XIntegerListValue createIntegerListValue(Integer[] values);
	
	/**
	 * Creates an XLongListValue.
	 * 
	 * @param values The long values which are to be stored in the
	 *            XLongListValue.
	 * @return a {@link XLongListValue} storing the given long values
	 */
	XLongListValue createLongListValue(long[] values);
	
	/**
	 * Creates an XLongListValue.
	 * 
	 * @param values The long values which are to be stored in the
	 *            XLongListValue.
	 * @return a {@link XLongListValue} storing the given long values
	 */
	XLongListValue createLongListValue(Long[] values);
	
}
