package org.xydra.core.serialize;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.xydra.annotations.RunsInGWT;
import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.XX;
import org.xydra.base.value.ValueType;
import org.xydra.base.value.XBinaryValue;
import org.xydra.base.value.XV;
import org.xydra.base.value.XValue;
import org.xydra.index.query.Pair;


@RunsInGWT(true)
public class ValueDeSerializer {
	
	/**
	 * @param value not null
	 * @return a pair of type/value, both represented as strings
	 */
	public static Pair<String,String> toStringPair(XValue value) {
		if(value == null)
			throw new IllegalArgumentException("vaue was null");
		String typeStr = value.getType().name();
		String valueStr;
		if(value.getType() == ValueType.Binary) {
			valueStr = SerializedValue.serializeBinaryContent(((XBinaryValue)value).contents());
		} else {
			valueStr = value.toString();
		}
		return new Pair<String,String>(typeStr, valueStr);
	}
	
	public static XValue fromStrings(String typeStr, String valueStr) {
		if(typeStr == null)
			throw new IllegalArgumentException("typeStr was null");
		String val = valueStr;
		if(val == null)
			throw new IllegalArgumentException("valueStr was null");
		if(val.equals("null")) {
			return null;
		}
		try {
			ValueType type = ValueType.valueOf(typeStr);
			
			if(val.startsWith("=")) {
				/* remove encoding trick to represent '123' as '="123"' */
				val = val.substring(2, val.length() - 1);
			}
			XValue value = toValue(type, val);
			return value;
		} catch(IllegalArgumentException e) {
			throw new IllegalArgumentException("Could not parse type '" + typeStr + "' for value '"
			        + val + "'", e);
		}
	}
	
	public static XValue fromStringPair(Pair<String,String> typeValue) {
		if(typeValue == null)
			throw new IllegalArgumentException("pair was null");
		String typeStr = typeValue.getFirst();
		String valueStr = typeValue.getSecond();
		return fromStrings(typeStr, valueStr);
	}
	
	/**
	 * @param type
	 * @param valueStr can be null
	 * @return a value or null
	 */
	@SuppressWarnings("incomplete-switch")
	public static XValue toValue(ValueType type, String valueStr) {
		if(type == null)
			throw new IllegalArgumentException("type was null");
		if(valueStr == null) {
			throw new IllegalArgumentException("value was null");
		}
		if(valueStr.trim().equals("null")) {
			return null;
		}
		
		if(type.isCollection()) {
			// parse '[' ... ',' ... ']'
			List<String> list;
			String values = valueStr.trim();
			if(values.startsWith("[")) {
				values = values.substring(1);
			} else {
				throw new IllegalArgumentException("list value of type " + type
				        + " does not start with '['");
			}
			if(values.endsWith("]")) {
				values = values.substring(0, values.length() - 1);
			} else {
				throw new IllegalArgumentException("list value of type " + type
				        + " does not end with ']'");
			}
			String[] parts = values.split(",[ ]?");
			list = Arrays.asList(parts);
			XValue collectionValue = toCollectionValue(type, list);
			return collectionValue;
		} else {
			switch(type) {
			case Address:
				return XX.toAddress(valueStr.trim());
			case Boolean:
				boolean b = Boolean.parseBoolean(valueStr.trim());
				return XV.toValue(b);
			case Binary:
				byte[] bytes = SerializedValue.deserializeBinaryContent(valueStr);
				return XV.toValue(bytes);
			case Double:
				double d = Double.parseDouble(valueStr.trim());
				return XV.toValue(d);
			case Id:
				return XX.toId(valueStr.trim());
			case Integer:
				int i = Integer.parseInt(valueStr.trim());
				return XV.toValue(i);
			case Long:
				long l = Long.parseLong(valueStr.trim());
				return XV.toValue(l);
			case String:
				return XV.toValue(valueStr);
			}
		}
		throw new IllegalArgumentException("Could not parse " + type + " from string '" + valueStr
		        + "'");
	}
	
	/**
	 * @param primitiveType Xydra type
	 * @param valueStrs
	 * @return a list of corresponding Java type
	 */
	@SuppressWarnings("unchecked")
	private static <T> List<T> toListOfType(ValueType primitiveType, List<String> valueStrs) {
		
		List<T> list = new ArrayList<T>();
		for(String s : valueStrs) {
			switch(primitiveType) {
			case Address:
			case Id:
				XValue value = toValue(primitiveType, s);
				list.add((T)value);
				break;
			case Boolean:
				list.add((T)(Boolean)Boolean.parseBoolean(s));
				break;
			case Double:
				list.add((T)(Double)Double.parseDouble(s));
				break;
			case Integer:
				list.add((T)(Integer)Integer.parseInt(s));
				break;
			case Long:
				list.add((T)(Long)Long.parseLong(s));
				break;
			case String:
				list.add((T)s);
				break;
			case Binary:
				
			default:
				throw new RuntimeException("Could not convert list to type " + primitiveType);
			}
		}
		return list;
	}
	
	private static <T> XValue toCollectionValue(ValueType type, List<String> list) {
		switch(type) {
		case AddressList:
			List<XAddress> al = toListOfType(ValueType.Address, list);
			return XV.toAddressListValue(al);
		case AddressSet:
			List<XAddress> as = toListOfType(ValueType.Address, list);
			return XV.toAddressSetValue(as);
		case AddressSortedSet:
			List<XAddress> ass = toListOfType(ValueType.Address, list);
			return XV.toAddressSortedSetValue(ass);
		case BooleanList:
			List<Boolean> booll = toListOfType(ValueType.Boolean, list);
			return XV.toBooleanListValue(booll);
		case DoubleList:
			List<Double> dl = toListOfType(ValueType.Double, list);
			return XV.toDoubleListValue(dl);
		case IdList:
			List<XID> idl = toListOfType(ValueType.Id, list);
			return XV.toIDListValue(idl);
		case IdSet:
			List<XID> ids = toListOfType(ValueType.Id, list);
			return XV.toIDSetValue(ids);
		case IdSortedSet:
			List<XID> idss = toListOfType(ValueType.Id, list);
			return XV.toIDSortedSetValue(idss);
		case IntegerList:
			List<Integer> intl = toListOfType(ValueType.Integer, list);
			return XV.toIntegerListValue(intl);
		case LongList:
			List<Long> longl = toListOfType(ValueType.Long, list);
			return XV.toLongListValue(longl);
		case StringList:
			List<String> sl = toListOfType(ValueType.String, list);
			return XV.toStringListValue(sl);
		case StringSet:
			List<String> ss = toListOfType(ValueType.String, list);
			return XV.toStringSetValue(ss);
		default:
			throw new IllegalArgumentException("Could not parse " + type + " from strings '" + list
			        + "'");
		}
	}
	
}
