package org.xydra.base.value;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.xydra.base.X;
import org.xydra.base.XAddress;
import org.xydra.base.XID;


/**
 * Methods for creating and dealing with {@link XValue XValues}
 * 
 * @author dscharrer
 * 
 */
/**
 * @author xamde
 * 
 */
public class XV {
	
	private final static XValueFactory vf = X.getValueFactory();
	
	/**
	 * Returns the content of the given {@link XListValue} as a {@link List}
	 * 
	 * @param <E> The content type
	 * @param listValue The {@link XListValue}
	 * @return a copy of the listValue as a {@link List}
	 */
	public static <E> List<E> asList(XListValue<E> listValue) {
		return Arrays.asList(listValue.toArray());
	}
	
	/**
	 * Returns the content of the given {@link Collection} as an
	 * {@link XAddressListValue}
	 * 
	 * @param list The {@link Collection} which is to be converted into an
	 *            {@link XAddressListValue}
	 * @return an {@link XAddressListValue} with the content of the given list
	 */
	public static XAddressListValue toAddressListValue(Collection<XAddress> list) {
		return vf.createAddressListValue(list);
	}
	
	/**
	 * Returns the content of the given {@link Collection} as an
	 * {@link XAddressSetValue}
	 * 
	 * @param list The {@link Collection} which is to be converted into an
	 *            {@link XAddressSetValue}
	 * @return an {@link XAddressSetValue} with the content of the given list
	 */
	public static XAddressSetValue toAddressSetValue(Collection<XAddress> list) {
		return vf.createAddressSetValue(list);
	}
	
	/**
	 * Returns the content of the given {@link XAddress} array as an
	 * {@link XAddressSetValue}
	 * 
	 * @param list The {@link XAddress} array which is to be converted into an
	 *            {@link XAddressSetValue}
	 * @return an {@link XAddressSetValue} with the content of the given list
	 */
	public static XAddressSetValue toAddressSetValue(XAddress[] list) {
		return vf.createAddressSetValue(list);
	}
	
	/**
	 * Returns the content of the given {@link Collection} as an
	 * {@link XAddressSortedSetValue}
	 * 
	 * @param list The {@link Collection} which is to be converted into an
	 *            {@link XAddressSortedSetValue}
	 * @return an {@link XAddressSortedSetValue} with the content of the given
	 *         list, preserving sort oder.
	 */
	public static XAddressSortedSetValue toAddressSortedSetValue(Collection<XAddress> list) {
		return vf.createAddressSortedSetValue(list);
	}
	
	/**
	 * Returns the content of the given {@link XAddress} array as an
	 * {@link XAddressSortedSetValue}
	 * 
	 * @param list The {@link XAddress} array which is to be converted into an
	 *            {@link XAddressSortedSetValue}
	 * @return an {@link XAddressSortedSetValue} with the content of the given
	 *         list, maintaining sort oder.
	 */
	public static XAddressSortedSetValue toAddressSortedSetValue(XAddress[] list) {
		return vf.createAddressSortedSetValue(list);
	}
	
	/**
	 * Returns the content of the given {@link Collection} as an
	 * {@link XBooleanListValue}
	 * 
	 * @param list The {@link Collection} which is to be converted into an
	 *            {@link XBooleanListValue}
	 * @return an {@link XBooleanListValue} with the content of the given list
	 */
	public static XBooleanListValue toBooleanListValue(Collection<Boolean> list) {
		return vf.createBooleanListValue(list);
	}
	
	/**
	 * Returns the content of the given {@link Collection} as an
	 * {@link XByteListValue}
	 * 
	 * @param list The {@link Collection} which is to be converted into an
	 *            {@link XByteListValue}
	 * @return an {@link XByteListValue} with the content of the given list
	 */
	public static XByteListValue toByteListValue(Collection<Byte> list) {
		return vf.createByteListValue(list);
	}
	
	/**
	 * Returns the content of the given {@link Collection} as an
	 * {@link XDoubleListValue}
	 * 
	 * @param list The {@link Collection} which is to be converted into an
	 *            {@link XDoubleListValue}
	 * @return an {@link XDoubleListValue} with the content of the given list
	 */
	public static XDoubleListValue toDoubleListValue(Collection<Double> list) {
		return vf.createDoubleListValue(list);
	}
	
	/**
	 * Returns the content of the given {@link Collection} as an
	 * {@link XIDListValue}
	 * 
	 * @param list The {@link Collection} which is to be converted into an
	 *            {@link XIDListValue}
	 * @return an {@link XIDListValue} with the content of the given list
	 */
	public static XIDListValue toIDListValue(Collection<XID> list) {
		return vf.createIDListValue(list);
	}
	
	/**
	 * @param value may be null or {@link XIDSetValue}
	 * @return always a Set<XID>, never null
	 * @throws IllegalArgumentException if value is not an {@link XIDSetValue}
	 */
	public static Set<XID> toIDSet(XValue value) {
		if(value == null) {
			return Collections.emptySet();
		} else {
			try {
				XIDSetValue idSetValue = (XIDSetValue)value;
				return idSetValue.toSet();
			} catch(ClassCastException e) {
				throw new IllegalArgumentException("Given value is not an XIDSetValue", e);
			}
		}
	}
	
	/**
	 * Returns the content of the given {@link Collection} as an
	 * {@link XIDSetValue}
	 * 
	 * @param list The {@link Collection} which is to be converted into an
	 *            {@link XIDSetValue}
	 * @return an {@link XIDSetValue} with the content of the given list
	 */
	public static XIDSetValue toIDSetValue(Collection<XID> list) {
		return vf.createIDSetValue(list);
	}
	
	/**
	 * Returns the content of the given {@link Collection} as an
	 * {@link XIDSetValue}
	 * 
	 * @param list The {@link Collection} which is to be converted into an
	 *            {@link XIDSetValue}
	 * @return an {@link XIDSetValue} with the content of the given list
	 */
	public static XIDSetValue toIDSetValue(Set<XID> list) {
		return vf.createIDSetValue(list);
	}
	
	/**
	 * Returns the content of the given {@link XID} array as an
	 * {@link XIDSetValue}
	 * 
	 * @param list The {@link XID} array which is to be converted into an
	 *            {@link XIDSetValue}
	 * @return an {@link XIDSetValue} with the content of the given list
	 */
	public static XIDSetValue toIDSetValue(XID[] list) {
		return vf.createIDSetValue(list);
	}
	
	/**
	 * Returns the content of the given {@link Collection} as an
	 * {@link XIDSortedSetValue}
	 * 
	 * @param list The {@link Collection} which is to be converted into an
	 *            {@link XIDSortedSetValue}
	 * @return an {@link XIDSortedSetValue} with the content of the given list,
	 *         preserving sort oder.
	 */
	public static XIDSortedSetValue toIDSortedSetValue(Collection<XID> list) {
		return vf.createIDSortedSetValue(list);
	}
	
	/**
	 * Returns the content of the given {@link XID} array as an
	 * {@link XIDSortedSetValue}
	 * 
	 * @param list The {@link XID} array which is to be converted into an
	 *            {@link XIDSortedSetValue}
	 * @return an {@link XIDSortedSetValue} with the content of the given list,
	 *         maintaining sort oder.
	 */
	public static XIDSortedSetValue toIDSortedSetValue(XID[] list) {
		return vf.createIDSortedSetValue(list);
	}
	
	/**
	 * @param value may be null or {@link XIntegerValue}
	 * @return null as 0 and {@link XIntegerValue} as their contents().
	 * @throws IllegalArgumentException if value is not an {@link XIntegerValue}
	 */
	public static int toInteger(XValue value) {
		if(value == null) {
			return 0;
		} else {
			try {
				XIntegerValue integerValue = (XIntegerValue)value;
				return integerValue.contents();
			} catch(ClassCastException e) {
				throw new IllegalArgumentException("Given value is not an XIntegerValue", e);
			}
		}
	}
	
	/**
	 * Returns the content of the given {@link Collection} as an
	 * {@link XIntegerListValue}
	 * 
	 * @param list The {@link Collection} which is to be converted into an
	 *            {@link XIntegerListValue}
	 * @return an {@link XIntegerListValue} with the content of the given list
	 */
	public static XIntegerListValue toIntegerListValue(Collection<Integer> list) {
		return vf.createIntegerListValue(list);
	}
	
	/**
	 * Returns the content of the given {@link Collection} as an
	 * {@link XLongListValue}
	 * 
	 * @param list {@link Collection} which is to be converted into an
	 *            {@link XLongListValue}
	 * @return an {@link XLongListValue} with the content of the given list
	 */
	public static XLongListValue toLongListValue(Collection<Long> list) {
		return vf.createLongListValue(list);
	}
	
	/**
	 * @param value may be null or {@link XStringValue}
	 * @return null as null and {@link XStringValue} as their contents().
	 * @throws IllegalArgumentException if value is not an {@link XStringValue}
	 */
	public static String toString(XValue value) {
		if(value == null) {
			return null;
		} else {
			try {
				XStringValue stringValue = (XStringValue)value;
				return stringValue.contents();
			} catch(ClassCastException e) {
				throw new IllegalArgumentException("Given value is not an XStringValue", e);
			}
		}
	}
	
	/**
	 * Returns the content of the given {@link Collection} as an
	 * {@link XStringListValue}
	 * 
	 * @param list The {@link Collection} which is to be converted into an
	 *            {@link XStringListValue}
	 * @return an {@link XStringListValue} with the content of the given list
	 */
	public static XStringListValue toStringListValue(Collection<String> list) {
		return vf.createStringListValue(list);
	}
	
	/**
	 * Returns the content of the given {@link Collection} as an
	 * {@link XStringSetValue}
	 * 
	 * @param list The {@link Collection} which is to be converted into an
	 *            {@link XStringSetValue}
	 * @return an {@link XStringSetValue} with the content of the given list
	 */
	public static XStringSetValue toStringSetValue(Collection<String> list) {
		return vf.createStringSetValue(list);
	}
	
	/**
	 * Returns the content of the given {@link Collection} as an
	 * {@link XStringSetValue}
	 * 
	 * @param list The {@link Collection} which is to be converted into an
	 *            {@link XStringSetValue}
	 * @return an {@link XStringSetValue} with the content of the given list
	 */
	public static XStringSetValue toStringSetValue(Set<String> list) {
		return vf.createStringSetValue(list);
	}
	
	/**
	 * Returns the content of the given {@link String} array as an
	 * {@link XStringSetValue}
	 * 
	 * @param list The {@link String} array which is to be converted into an
	 *            {@link XStringSetValue}
	 * @return an {@link XStringSetValue} with the content of the given list
	 */
	public static XStringSetValue toStringSetValue(String[] list) {
		return vf.createStringSetValue(list);
	}
	
	/**
	 * @return the boolean wrapped in an {@link XBooleanValue}
	 */
	public static XBooleanValue toValue(boolean value) {
		return vf.createBooleanValue(value);
	}
	
	/**
	 * Returns the content of the given boolean array as an
	 * {@link XBooleanListValue}
	 * 
	 * @param list The boolean array which is to be converted into an
	 *            {@link XBooleanListValue}
	 * @return an {@link XBooleanListValue} with the content of the given list
	 */
	public static XBooleanListValue toValue(boolean[] list) {
		return vf.createBooleanListValue(list);
	}
	
	/**
	 * Returns the content of the given {@link Boolean} array as an
	 * {@link XBooleanListValue}
	 * 
	 * @param list The {@link Boolean} array which is to be converted into an
	 *            {@link XBooleanListValue}
	 * @return an {@link XBooleanListValue} with the content of the given list
	 */
	public static XBooleanListValue toValue(Boolean[] list) {
		return vf.createBooleanListValue(Arrays.asList(list));
	}
	
	/**
	 * Returns the content of the given byte array as an {@link XByteListValue}
	 * 
	 * @param list The byte array which is to be converted into an
	 *            {@link XByteListValue}
	 * @return an {@link XByteListValue} with the content of the given list
	 */
	public static XByteListValue toValue(byte[] list) {
		return vf.createByteListValue(list);
	}
	
	/**
	 * Returns the content of the given {@link Byte} array as an
	 * {@link XByteListValue}
	 * 
	 * @param list The {@link Byte} array which is to be converted into an
	 *            {@link XByteListValue}
	 * @return an {@link XByteListValue} with the content of the given list
	 */
	public static XByteListValue toValue(Byte[] list) {
		return vf.createByteListValue(Arrays.asList(list));
	}
	
	/**
	 * @return the double wrapped in an {@link XDoubleValue}
	 */
	public static XDoubleValue toValue(double value) {
		return vf.createDoubleValue(value);
	}
	
	/**
	 * Returns the content of the given double array as an
	 * {@link XDoubleListValue}
	 * 
	 * @param list The double array which is to be converted into an
	 *            {@link XDoubleListValue}
	 * @return an {@link XDoubleListValue} with the content of the given list
	 */
	public static XDoubleListValue toValue(double[] list) {
		return vf.createDoubleListValue(list);
	}
	
	/**
	 * Returns the content of the given {@link Double} array as an
	 * {@link XDoubleListValue}
	 * 
	 * @param list The {@link Double} array which is to be converted into an
	 *            {@link XDoubleListValue}
	 * @return an {@link XDoubleListValue} with the content of the given list
	 */
	public static XDoubleListValue toValue(Double[] list) {
		return vf.createDoubleListValue(Arrays.asList(list));
	}
	
	/**
	 * @return the int wrapped in an {@link XIntegerValue}
	 */
	public static XIntegerValue toValue(int value) {
		return vf.createIntegerValue(value);
	}
	
	/**
	 * Returns the content of the given int array as an
	 * {@link XIntegerListValue}
	 * 
	 * @param list The int array which is to be converted into an
	 *            {@link XIntegerListValue}
	 * @return an {@link XIntegerListValue} with the content of the given list
	 */
	public static XIntegerListValue toValue(int[] list) {
		return vf.createIntegerListValue(list);
	}
	
	/**
	 * Returns the content of the given {@link Integer} array as an
	 * {@link XIntegerListValue}
	 * 
	 * @param list The {@link Integer} array which is to be converted into an
	 *            {@link XIntegerListValue}
	 * @return an {@link XIntegerListValue} with the content of the given list
	 */
	public static XIntegerListValue toValue(Integer[] list) {
		return vf.createIntegerListValue(Arrays.asList(list));
	}
	
	/**
	 * @return the long wrapped in an {@link XLongValue}
	 */
	public static XLongValue toValue(long value) {
		return vf.createLongValue(value);
	}
	
	/**
	 * Returns the content of the given long array as an {@link XLongListValue}
	 * 
	 * @param list long array which is to be converted into an
	 *            {@link XLongListValue}
	 * @return an {@link XLongListValue} with the content of the given list
	 */
	public static XLongListValue toValue(long[] list) {
		return vf.createLongListValue(list);
	}
	
	/**
	 * Returns the content of the given {@link Long} array as an
	 * {@link XLongListValue}
	 * 
	 * @param list {@link Long} array which is to be converted into an
	 *            {@link XLongListValue}
	 * @return an {@link XLongListValue} with the content of the given list
	 */
	public static XLongListValue toValue(Long[] list) {
		return vf.createLongListValue(Arrays.asList(list));
	}
	
	/**
	 * @return the {@link String} wrapped in an {@link XStringValue}
	 */
	public static XStringValue toValue(String value) {
		return vf.createStringValue(value);
	}
	
	/**
	 * Returns the content of the given {@link String} array as an
	 * {@link XStringListValue}
	 * 
	 * @param list The {@link String} array which is to be converted into an
	 *            {@link XStringListValue}
	 * @return an {@link XStringListValue} with the content of the given list
	 */
	public static XStringListValue toValue(String[] list) {
		return vf.createStringListValue(list);
	}
	
	/**
	 * Returns the content of the given {@link XAddress} array as an
	 * {@link XAddressListValue}
	 * 
	 * @param list The {@link XAddress} array which is to be converted into an
	 *            {@link XAddressListValue}
	 * @return an {@link XAddressListValue} with the content of the given list
	 */
	public static XAddressListValue toValue(XAddress[] list) {
		return vf.createAddressListValue(list);
	}
	
	/**
	 * Returns the content of the given {@link XID} array as an
	 * {@link XIDListValue}
	 * 
	 * @param list The {@link XID} array which is to be converted into an
	 *            {@link XIDListValue}
	 * @return an {@link XIDListValue} with the content of the given list
	 */
	public static XIDListValue toValue(XID[] list) {
		return vf.createIDListValue(list);
	}
	
	@SuppressWarnings("unchecked")
	public static void toValueStream(XValue value, XValueStream stream) {
		stream.startValue();
		if(value.getType().isCollection()) {
			stream.startCollection(value.getType());
			Class<?> componentType = ValueType.getComponentType(value.getType());
			for(Object a : ((XCollectionValue<? extends XValue>)value)) {
				if(componentType == XAddress.class) {
					stream.address((XAddress)a);
				} else if(componentType == Boolean.class) {
					stream.javaBoolean((Boolean)a);
				} else if(componentType == Double.class) {
					stream.javaDouble((Double)a);
				} else if(componentType == Integer.class) {
					stream.javaInteger((Integer)a);
				} else if(componentType == Long.class) {
					stream.javaLong((Long)a);
				} else if(componentType == String.class) {
					stream.javaString((String)a);
				} else if(componentType == org.xydra.base.XID.class) {
					stream.xid((XID)a);
				}
			}
			stream.endCollection();
		} else {
			toValueStream(value, ValueType.getPrimitiveType(value.getType()), stream);
		}
		stream.endValue();
	}
	
	private static void toValueStream(XValue value, Class<?> type, XValueStream stream) {
		if(type == XAddress.class) {
			stream.address((XAddress)value);
		} else if(type == Boolean.class) {
			stream.javaBoolean(((XBooleanValue)value).contents());
		} else if(type == Double.class) {
			stream.javaDouble(((XDoubleValue)value).contents());
		} else if(type == Integer.class) {
			stream.javaInteger(((XIntegerValue)value).contents());
		} else if(type == Long.class) {
			stream.javaLong(((XLongValue)value).contents());
		} else if(type == String.class) {
			stream.javaString(((XStringValue)value).contents());
		} else if(type == org.xydra.base.XID.class) {
			stream.xid((XID)value);
		}
	}
}
