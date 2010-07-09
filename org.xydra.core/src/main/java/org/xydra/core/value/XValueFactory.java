package org.xydra.core.value;

import java.util.Collection;

import org.xydra.core.model.XID;


/**
 * A class for creating instances of the different XValue types.
 * 
 * @author Kaidel
 * 
 */
public interface XValueFactory {
	
	/**
	 * Creates an {@link XStringValue}.
	 * 
	 * @param string The String which is to be stored in the XStringValue.
	 * @return a {@link XStringValue} storing the given String.
	 */
	XStringValue createStringValue(String string);
	
	/**
	 * Creates an {@link XIDValue}.
	 * 
	 * @param id The XID which is to be stored in the XIDValue.
	 * @return a {@link XIDValue} storing the given XID.
	 */
	XIDValue createIDValue(XID id);
	
	/**
	 * Creates an {@link XBooleanValue}.
	 * 
	 * @param value .
	 * @return a {@link XBooleanValue} storing the given boolean value.
	 */
	XBooleanValue createBooleanValue(boolean value);
	
	/**
	 * Creates an {@link XDoubleValue}.
	 * 
	 * @param value The double value which is to be stored in the XDoubleValue.
	 * @return a {@link XDoubleValue} storing the given double value.
	 */
	XDoubleValue createDoubleValue(double value);
	
	/**
	 * Creates an {@link XIntegerValue}.
	 * 
	 * @param value The integer value which is to be stored in the
	 *            XIntegerValue.
	 * @return a {@link XIntegerValue} storing the given integer value
	 */
	XIntegerValue createIntegerValue(int value);
	
	/**
	 * Creates an {@link XLongValue}.
	 * 
	 * @param value The long value which is to be stored in the XLongValue.
	 * @return a {@link XLongValue} storing the given long value.
	 */
	XLongValue createLongValue(long value);
	
	/**
	 * Creates an {@link XStringListValue}.
	 * 
	 * @param strings The Strings which are to be stored in the
	 *            XStringListValue.
	 * @return a {@link XStringListValue} storing the given Strings.
	 */
	XStringListValue createStringListValue(String[] strings);
	
	/**
	 * Creates an {@link XIDListValue}.
	 * 
	 * @param xids The XIDS which are to be stored in the XIDListValue.
	 * @return a {@link XIDListValue} with the content given in 'xids'.
	 */
	XIDListValue createIDListValue(XID[] xids);
	
	/**
	 * Creates an {@link XIDListValue}.
	 * 
	 * @param xids The XIDS which are to be stored in the XIDListValue.
	 * @return a {@link XIDListValue} with the content given in 'xids'.
	 */
	XIDListValue createIDListValue(Collection<XID> xids);
	
	/**
	 * Creates an {@link XStringListValue}.
	 * 
	 * @param strings The Strings which are to be stored in the
	 *            XStringListValue.
	 * @return a {@link XStringListValue} storing the given Strings.
	 */
	XStringListValue createStringListValue(Collection<String> strings);
	
	/**
	 * Creates an {@link XBooleanListValue}.
	 * 
	 * @param values The boolean values which are to be stored in the
	 *            XBooleanListValue.
	 * @return a {@link XBooleanListValue} storing the given boolean values.
	 */
	XBooleanListValue createBooleanListValue(boolean[] values);
	
	/**
	 * Creates an {@link XBooleanListValue}.
	 * 
	 * @param values The boolean values which are to be stored in the
	 *            XBooleanListValue.
	 * @return a {@link XBooleanListValue} storing the given boolean values.
	 */
	XBooleanListValue createBooleanListValue(Collection<Boolean> values);
	
	/**
	 * Creates an {@link XDoubleListValue}.
	 * 
	 * @param values The double values which are to be stored in the
	 *            XDoubleListValue.
	 * @return a {@link XDoubleListValue} storing the given double values
	 */
	XDoubleListValue createDoubleListValue(double[] values);
	
	/**
	 * Creates an {@link XDoubleListValue}.
	 * 
	 * @param values The double values which are to be stored in the
	 *            XDoubleListValue.
	 * @return a {@link XDoubleListValue} storing the given double values
	 */
	XDoubleListValue createDoubleListValue(Collection<Double> values);
	
	/**
	 * Creates an {@link XIntegerListValue}.
	 * 
	 * @param values The integer values which are to be stored in the
	 *            XIntegerListValue.
	 * @return a {@link XIntegerListValue} storing the given integer values.
	 */
	XIntegerListValue createIntegerListValue(int[] values);
	
	/**
	 * Creates an {@link XIntegerListValue}.
	 * 
	 * @param values The integer values which are to be stored in the
	 *            XIntegerListValue.
	 * @return a {@link XIntegerListValue} storing the given integer values.
	 */
	XIntegerListValue createIntegerListValue(Collection<Integer> values);
	
	/**
	 * Creates an {@link XLongListValue}.
	 * 
	 * @param values The long values which are to be stored in the
	 *            XLongListValue.
	 * @return a {@link XLongListValue} storing the given long values
	 */
	XLongListValue createLongListValue(long[] values);
	
	/**
	 * Creates an {@link XLongListValue}.
	 * 
	 * @param values The long values which are to be stored in the
	 *            XLongListValue.
	 * @return a {@link XLongListValue} storing the given long values
	 */
	XLongListValue createLongListValue(Collection<Long> values);
	
	/**
	 * Creates an {@link XByteListValue}.
	 * 
	 * @param values The byte values which are to be stored in the
	 *            {@link XByteListValue}.
	 * @return a {@link XByteListValue} storing the given byte values
	 */
	XByteListValue createByteListValue(byte[] values);
	
	/**
	 * Creates an {@link XByteListValue}.
	 * 
	 * @param values The {@link Byte} values which are to be stored in the
	 *            {@link XByteListValue}.
	 * @return a {@link XByteListValue} storing the given byte values
	 */
	XByteListValue createByteListValue(Collection<Byte> values);
	
	/**
	 * Creates an {@link XStringSetValue}.
	 * 
	 * @param values The {@link String} values which are to be stored in the
	 *            {@link XStringSetValue}.
	 * @return a {@link XStringSetValue} storing the given {@link String} values
	 */
	XStringSetValue createStringSetValue(String[] values);
	
	/**
	 * Creates an {@link XStringSetValue}.
	 * 
	 * @param values The {@link String} values which are to be stored in the
	 *            {@link XStringSetValue}.
	 * @return a {@link XStringSetValue} storing the given {@link String} values
	 */
	XStringSetValue createStringSetValue(Collection<String> values);
	
	/**
	 * Creates an {@link XXIDSetValue}.
	 * 
	 * @param values The {@link XID} values which are to be stored in the
	 *            {@link XXIDSetValue}.
	 * @return a {@link XXIDSetValue} storing the given {@link XID} values
	 */
	XIDSetValue createIDSetValue(XID[] values);
	
	/**
	 * Creates an {@link XXIDSetValue}.
	 * 
	 * @param values The {@link XID} values which are to be stored in the
	 *            {@link XXIDSetValue}.
	 * @return a {@link XXIDSetValue} storing the given {@link XID} values
	 */
	XIDSetValue createIDSetValue(Collection<XID> values);
	
}
