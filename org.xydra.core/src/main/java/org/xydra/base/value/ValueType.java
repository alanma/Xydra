package org.xydra.base.value;

import org.xydra.base.XAddress;
import org.xydra.base.XID;


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

	Id(org.xydra.base.XID.class), IdList(XIDListValue.class), IdSet(XIDSetValue.class), IdSortedSet(
	        XIDSortedSetValue.class),

	IntegerList(XIntegerListValue.class), Integer(XIntegerValue.class), LongList(
	        XLongListValue.class), Long(XLongValue.class),

	StringList(XStringListValue.class), StringSet(XStringSetValue.class), String(XStringValue.class);
	
	private Class<?> xydraInterface;
	
	public boolean isSortedCollection() {
		return this == AddressList || this == AddressSortedSet || this == BooleanList
		        || this == DoubleList || this == IdList || this == IdSortedSet
		        || this == IntegerList || this == LongList || this == StringSet
		        || this == StringList;
	}
	
	public boolean isCollection() {
		return this == AddressList || this == AddressSet || this == AddressSortedSet
		        || this == BooleanList || this == DoubleList || this == IdList || this == IdSet
		        || this == IdSortedSet || this == IntegerList || this == LongList
		        || this == StringSet || this == StringList;
	}
	
	public boolean isSingle() {
		return this == Address || this == Boolean || this == Double || this == Id
		        || this == Integer || this == Long || this == String;
	}
	
	public boolean isSet() {
		return this == AddressSet || this == AddressSortedSet || this == IdSet
		        || this == IdSortedSet || this == StringSet;
	}
	
	ValueType(Class<?> xydraInterface) {
		this.xydraInterface = xydraInterface;
	}
	
	public Class<?> getXydraInterface() {
		return this.xydraInterface;
	}
	
	/**
	 * Note that {@link XByteListValue} is considered a non-collection type.
	 * TODO this is inconsistent with {@link XByteListValue} extending
	 * {@link XCollectionValue}
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
		case IdList:
		case IdSet:
		case IdSortedSet:
			return XID.class;
		default:
			return null;
		}
	}
	
	public static Class<?> getPrimitiveType(ValueType type) {
		assert type.isSingle();
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
		case Id:
			return XID.class;
		default:
			return null;
		}
	}
}
