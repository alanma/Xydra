package org.xydra.base.value;

import org.xydra.base.XAddress;


/**
 * The type system of Xydra XValues.
 * 
 * @author xamde
 */
public enum ValueType {
	
	/* in alphabetic order */

	Address(XAddress.class), AddressList(XAddressListValue.class), AddressSet(
	        XAddressSetValue.class), AddressSortedSet(XAddressSortedSetValue.class),

	BooleanList(XBooleanListValue.class), Boolean(XBooleanValue.class),

	ByteList(XByteListValue.class),

	DoubleList(XDoubleListValue.class), Double(XDoubleValue.class),

	XID(org.xydra.base.XID.class), XIDList(XIDListValue.class), XIDSet(XIDSetValue.class), XIDSortedSet(
	        XIDSortedSetValue.class),

	IntegerList(XIntegerListValue.class), Integer(XIntegerValue.class), LongList(
	        XLongListValue.class), Long(XLongValue.class),

	StringList(XStringListValue.class), StringSet(XStringSetValue.class), String(XStringValue.class);
	
	private Class<?> xydraInterface;
	
	public boolean isSortedCollection() {
		return this == AddressList || this == AddressSortedSet || this == BooleanList
		        || this == DoubleList || this == XIDList || this == XIDSortedSet
		        || this == IntegerList || this == LongList || this == StringSet
		        || this == StringList;
	}
	
	public boolean isCollection() {
		return this == AddressList || this == AddressSet || this == AddressSortedSet
		        || this == BooleanList || this == DoubleList || this == XIDList || this == XIDSet
		        || this == XIDSortedSet || this == IntegerList || this == LongList
		        || this == StringSet || this == StringList;
	}
	
	public boolean isSet() {
		return this == AddressSet || this == AddressSortedSet || this == XIDSet
		        || this == XIDSortedSet || this == StringSet;
	}
	
	ValueType(Class<?> xydraInterface) {
		this.xydraInterface = xydraInterface;
	}
	
	public Class<?> getXydraInterface() {
		return this.xydraInterface;
	}
	
	/**
	 * Note that {@link XByteListValue} is considered a non-collection type.
	 * 
	 * @param type must be a collection type
	 * @return the Java class component type of the given collection type or
	 *         null if type is not a collection type.
	 */
	public static Class<?> getComponentType(ValueType type) {
		assert type.isCollection();
		switch(type) {
		case AddressList:
		case AddressSet:
		case AddressSortedSet:
			return XAddress.class;
		case BooleanList:
			return Boolean.class;
		case DoubleList:
			return Double.class;
		case IntegerList:
			return Integer.class;
		case LongList:
			return Long.class;
		case StringList:
		case StringSet:
			return String.class;
		case XIDList:
		case XIDSet:
		case XIDSortedSet:
			return org.xydra.base.XID.class;
		default:
			return null;
		}
	}
	
	public static Class<?> getPrimitiveType(ValueType type) {
		assert !type.isCollection();
		switch(type) {
		case Address:
			return XAddress.class;
		case Boolean:
			return Boolean.class;
		case Double:
			return Double.class;
		case Integer:
			return Integer.class;
		case Long:
			return Long.class;
		case String:
			return String.class;
		case XID:
			return org.xydra.base.XID.class;
		default:
			return null;
		}
	}
}
