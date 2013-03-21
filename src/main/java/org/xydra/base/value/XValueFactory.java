package org.xydra.base.value;

import java.util.Collection;

import org.xydra.base.XAddress;
import org.xydra.base.XId;


/**
 * A class for creating instances of the different [@link XValue} types.
 * 
 * Convenience functions for creating {@link XValue XValues} can be found in
 * {@link XV}.
 * 
 * @author Kaidel
 * 
 */
public interface XValueFactory {
	
	/**
	 * Creates an {@link XAddressListValue}.
	 * 
	 * The returned {@link XAddressListValue} will contain the given byte values
	 * in the order of their occurrence in the given {@link Collection}.
	 * 
	 * @param values The {@link Collection} containing the byte values which are
	 *            to be stored by the {@link XAddressListValue} this method will
	 *            create.
	 * @return an {@link XAddressListValue} storing the given long values.
	 */
	XAddressListValue createAddressListValue(Collection<XAddress> values);
	
	/**
	 * Creates an {@link XAddressListValue}.
	 * 
	 * The returned {@link XAddressListValue} will contain the given byte values
	 * in the order of their occurrence in the given array.
	 * 
	 * @param values The array containing the byte values which are to be stored
	 *            by the {@link XAddressListValue} this method will create.
	 * @return an {@link XAddressListValue} storing the given long values.
	 */
	XAddressListValue createAddressListValue(XAddress[] values);
	
	/**
	 * Creates an {@link XAddressSetValue}.
	 * 
	 * @param values The {@link Collection} containing the {@link XId XIds}
	 *            which are to be stored.
	 * @return an {@link XAddressSetValue} storing the given {@link XAddress
	 *         XAddresss}
	 */
	XAddressSetValue createAddressSetValue(Collection<XAddress> values);
	
	/**
	 * Creates an {@link XAddressSetValue}.
	 * 
	 * @param values The array containing the {@link XAddress XAddresss} which
	 *            are to be stored.
	 * @return an {@link XAddressSetValue} storing the given {@link XAddress
	 *         XAddresss}
	 */
	XAddressSetValue createAddressSetValue(XAddress[] values);
	
	/**
	 * Creates an {@link XAddressSortedSetValue}.
	 * 
	 * @param values The {@link Collection} containing the {@link XId XIds}
	 *            which are to be stored.
	 * @return an {@link XAddressSortedSetValue} storing the given
	 *         {@link XAddress XAddresss}, keeping the given order.
	 */
	XAddressSortedSetValue createAddressSortedSetValue(Collection<XAddress> values);
	
	/**
	 * Creates an {@link XAddressSortedSetValue}.
	 * 
	 * @param values The array containing the {@link XAddress XAddresss} which
	 *            are to be stored.
	 * @return an {@link XAddressSortedSetValue} storing the given
	 *         {@link XAddress XAddresss}, keeping the given order.
	 */
	XAddressSortedSetValue createAddressSortedSetValue(XAddress[] values);
	
	/**
	 * Creates an {@link XBooleanListValue}.
	 * 
	 * The returned {@link XBooleanListValue} will contain the given boolean
	 * values in the order of their occurrence in the given array.
	 * 
	 * @param values The array containing the boolean values which are to be
	 *            stored by the {@link XBooleanListValue} this method will
	 *            create.
	 * @return an {@link XBooleanListValue} storing the given boolean values.
	 */
	XBooleanListValue createBooleanListValue(boolean[] values);
	
	/**
	 * Creates an {@link XBooleanListValue}.
	 * 
	 * The returned {@link XBooleanListValue} will contain the given boolean
	 * values in the order of their occurrence in the given {@link Collection}.
	 * 
	 * @param values The {@link Collection} containing the boolean values which
	 *            are to be stored by the {@link XBooleanListValue} this method
	 *            will create.
	 * @return an {@link XBooleanListValue} storing the given boolean values.
	 */
	XBooleanListValue createBooleanListValue(Collection<Boolean> values);
	
	/**
	 * Creates an {@link XBooleanValue}.
	 * 
	 * @param value The boolean value which is to be stored by the
	 *            {@link XBooleanValue} this method will create.
	 * @return a {@link XBooleanValue} storing the given boolean value.
	 */
	XBooleanValue createBooleanValue(boolean value);
	
	/**
	 * Creates an {@link XBinaryValue}.
	 * 
	 * The returned {@link XBinaryValue} will contain the given byte values in
	 * the order of their occurrence in the given array.
	 * 
	 * @param values The array containing the byte values which are to be stored
	 *            by the {@link XBinaryValue} this method will create.
	 * @return an {@link XBinaryValue} storing the given long values.
	 */
	XBinaryValue createBinaryValue(byte[] values);
	
	/**
	 * Creates an {@link XBinaryValue}.
	 * 
	 * The returned {@link XBinaryValue} will contain the given byte values in
	 * the order of their occurrence in the given {@link Collection}.
	 * 
	 * @param values The {@link Collection} containing the byte values which are
	 *            to be stored by the {@link XBinaryValue} this method will
	 *            create.
	 * @return an {@link XBinaryValue} storing the given long values.
	 */
	XBinaryValue createBinaryValue(Collection<Byte> values);
	
	/**
	 * Creates an {@link XDoubleListValue}.
	 * 
	 * The returned {@link XDoubleListValue} will contain the given double
	 * values in the order of their occurrence in the given {@link Collection}.
	 * 
	 * @param values The {@link Collection} containing the double values which
	 *            are to be stored by the {@link XDoubleListValue} this method
	 *            will create.
	 * @return an {@link XDoubleListValue} storing the given double values.
	 */
	
	XDoubleListValue createDoubleListValue(Collection<Double> values);
	
	/**
	 * Creates an {@link XDoubleListValue}.
	 * 
	 * The returned {@link XDoubleListValue} will contain the given double
	 * values in the order of their occurrence in the given array.
	 * 
	 * @param values The array containing the double values which are to be
	 *            stored by the {@link XDoubleListValue} this method will
	 *            create.
	 * @return an {@link XDoubleListValue} storing the given double values.
	 */
	XDoubleListValue createDoubleListValue(double[] values);
	
	/**
	 * Creates an {@link XDoubleValue}.
	 * 
	 * @param value The double value which is to be stored by the
	 *            {@link XDoubleValue} this method will create.
	 * @return a {@link XDoubleValue} storing the given double value.
	 */
	XDoubleValue createDoubleValue(double value);
	
	/**
	 * Creates an {@link XIdListValue}.
	 * 
	 * The returned {@link XIdListValue} will contain the given {@link XId XIds}
	 * in the order of their occurrence in the given {@link Collection}.
	 * 
	 * @param xids The {@link Collection} containing the {@link XId XIds} which
	 *            are to be stored by the {@link XIdListValue} this method will
	 *            create.
	 * @return an {@link XIdListValue} storing the given {@link XId XIds}.
	 */
	XIdListValue createIdListValue(Collection<XId> xids);
	
	/**
	 * Creates an {@link XIdListValue}.
	 * 
	 * The returned {@link XIdListValue} will contain the given {@link XId XIds}
	 * in the order of their occurrence in the given array.
	 * 
	 * @param xids The array containing the {@link XId XIds} which are to be
	 *            stored by the {@link XIdListValue} this method will create.
	 * @return an {@link XIdListValue} storing the given {@link XId XIds}.
	 */
	XIdListValue createIdListValue(XId[] xids);
	
	/**
	 * Creates an {@link XIdSetValue}.
	 * 
	 * @param values The {@link Collection} containing the {@link XId XIds}
	 *            which are to be stored by the {@link XIdListValue} this method
	 *            will create.
	 * @return an {@link XIdListValue} storing the given {@link XId XIds}.
	 */
	XIdSetValue createIdSetValue(Collection<XId> values);
	
	/**
	 * Creates an {@link XIdSetValue}.
	 * 
	 * @param values The array containing the {@link XId XIds} which are to be
	 *            stored by the {@link XIdListValue} this method will create.
	 * @return an {@link XIdListValue} storing the given {@link XId XIds}.
	 */
	XIdSetValue createIdSetValue(XId[] values);
	
	/**
	 * Creates an {@link XIdSortedSetValue}.
	 * 
	 * @param values The {@link Collection} containing the {@link XId XIds}
	 *            which are to be stored by the {@link XIdListValue} this method
	 *            will create.
	 * @return an {@link XIdSortedSetValue} storing the given {@link XId XIds}m
	 *         keeping the given order.
	 */
	XIdSortedSetValue createIdSortedSetValue(Collection<XId> values);
	
	/**
	 * Creates an {@link XIdSortedSetValue}.
	 * 
	 * @param values The array containing the {@link XId XIds} which are to be
	 *            stored by the {@link XIdListValue} this method will create.
	 * @return an {@link XIdSortedSetValue} storing the given {@link XId XIds},
	 *         keeping the given order.
	 */
	XIdSortedSetValue createIdSortedSetValue(XId[] values);
	
	/**
	 * Creates an {@link XIntegerListValue}.
	 * 
	 * The returned {@link XIntegerListValue} will contain the given integer
	 * values in the order of their occurrence in the given {@link Collection}.
	 * 
	 * @param values The {@link Collection} containing the integer values which
	 *            are to be stored by the {@link XIntegerListValue} this method
	 *            will create.
	 * @return an {@link XIntegerListValue} storing the given integer values.
	 */
	XIntegerListValue createIntegerListValue(Collection<Integer> values);
	
	/**
	 * Creates an {@link XIntegerListValue}.
	 * 
	 * The returned {@link XIntegerListValue} will contain the given integer
	 * values in the order of their occurrence in the given array.
	 * 
	 * @param values The array containing the integer values which are to be
	 *            stored by the {@link XIntegerListValue} this method will
	 *            create.
	 * @return an {@link XIntegerListValue} storing the given integer values.
	 */
	XIntegerListValue createIntegerListValue(int[] values);
	
	/**
	 * Creates an {@link XIntegerValue}.
	 * 
	 * @param value The integer value which is to be stored by the
	 *            {@link XIntegerValue} this method will create.
	 * @return an {@link XIntegerValue} storing the given integer value
	 */
	XIntegerValue createIntegerValue(int value);
	
	/**
	 * Creates an {@link XLongListValue}.
	 * 
	 * The returned {@link XLongListValue} will contain the given long values in
	 * the order of their occurrence in the given {@link Collection}.
	 * 
	 * @param values The {@link Collection} containing the long values which are
	 *            to be stored by the {@link XLongListValue} this method will
	 *            create.
	 * @return an {@link XLongListValue} storing the given long values.
	 */
	XLongListValue createLongListValue(Collection<Long> values);
	
	/**
	 * Creates an {@link XLongListValue}.
	 * 
	 * The returned {@link XLongListValue} will contain the given long values in
	 * the order of their occurrence in the given array.
	 * 
	 * @param values The array containing the long values which are to be stored
	 *            by the {@link XLongListValue} this method will create.
	 * @return an {@link XLongListValue} storing the given long values.
	 */
	XLongListValue createLongListValue(long[] values);
	
	/**
	 * Creates an {@link XLongValue}.
	 * 
	 * @param value The long value which is to be stored by the
	 *            {@link XLongValue} this method will create.
	 * @return a {@link XLongValue} storing the given long value.
	 */
	XLongValue createLongValue(long value);
	
	/**
	 * Creates an {@link XStringListValue}.
	 * 
	 * The returned {@link XStringListValue} will contain the given Strings in
	 * the order of their occurrence in the given {@link Collection}.
	 * 
	 * @param strings The {@link Collection} containing the Strings which are to
	 *            be stored by the {@link XStringListValue} this method will
	 *            create.
	 * @return an {@link XStringListValue} storing the given Strings.
	 */
	XStringListValue createStringListValue(Collection<String> strings);
	
	/**
	 * Creates an {@link XStringListValue}.
	 * 
	 * The returned {@link XStringListValue} will contain the given Strings in
	 * the order of their occurrence in the given array.
	 * 
	 * @param strings The array containing the Strings which are to be stored by
	 *            the {@link XStringListValue} this method will create.
	 * @return a {@link XStringListValue} storing the given Strings.
	 */
	XStringListValue createStringListValue(String[] strings);
	
	/**
	 * Creates an {@link XStringListValue}.
	 * 
	 * The returned {@link XStringListValue} will contain the given String
	 * values in the order of their occurrence in the given {@link Collection}.
	 * 
	 * @param values The {@link Collection} containing the String values which
	 *            are to be stored by the {@link XStringListValue} this method
	 *            will create.
	 * @return an {@link XStringListValue} storing the given String values.
	 */
	XStringSetValue createStringSetValue(Collection<String> values);
	
	/**
	 * Creates an {@link XStringListValue}.
	 * 
	 * The returned {@link XStringListValue} will contain the given String
	 * values in the order of their occurrence in the given array.
	 * 
	 * @param values The array containing the String values which are to be
	 *            stored by the {@link XStringListValue} this method will
	 *            create.
	 * @return an {@link XStringListValue} storing the given String values.
	 */
	XStringSetValue createStringSetValue(String[] values);
	
	/**
	 * Creates an {@link XStringValue}.
	 * 
	 * @param string The String which is to be stored by the
	 *            {@link XStringValue} this method will create.
	 * @return an {@link XStringValue} storing the given String.
	 */
	XStringValue createStringValue(String string);
	
}
