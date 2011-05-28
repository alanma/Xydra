package org.xydra.core.serialize;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.xydra.annotations.RequiresAppEngine;
import org.xydra.annotations.RunsInAppEngine;
import org.xydra.annotations.RunsInGWT;
import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.XX;
import org.xydra.base.value.XAddressListValue;
import org.xydra.base.value.XAddressSetValue;
import org.xydra.base.value.XAddressSortedSetValue;
import org.xydra.base.value.XBooleanListValue;
import org.xydra.base.value.XBooleanValue;
import org.xydra.base.value.XByteListValue;
import org.xydra.base.value.XCollectionValue;
import org.xydra.base.value.XDoubleListValue;
import org.xydra.base.value.XDoubleValue;
import org.xydra.base.value.XIDListValue;
import org.xydra.base.value.XIDSetValue;
import org.xydra.base.value.XIDSortedSetValue;
import org.xydra.base.value.XIntegerListValue;
import org.xydra.base.value.XIntegerValue;
import org.xydra.base.value.XListValue;
import org.xydra.base.value.XLongListValue;
import org.xydra.base.value.XLongValue;
import org.xydra.base.value.XSetValue;
import org.xydra.base.value.XStringListValue;
import org.xydra.base.value.XStringSetValue;
import org.xydra.base.value.XStringValue;
import org.xydra.base.value.XV;
import org.xydra.base.value.XValue;


/**
 * Collection of methods to (de-)serialize variants of {@link XValue} to and
 * from their XML or JSON representation.
 * 
 * @author dscharrer
 */
@RunsInGWT(true)
@RunsInAppEngine(true)
@RequiresAppEngine(false)
public class XmlValue {
	
	private static final String NULL_ATTRIBUTE = "isNull";
	private static final String XADDRESS_ELEMENT = "xaddress";
	private static final String XADDRESSLIST_ELEMENT = "xaddressList";
	private static final String XADDRESSSET_ELEMENT = "xaddressSet";
	private static final String XADDRESSSORTEDSET_ELEMENT = "xaddressSortedSet";
	private static final String XBOOLEAN_ELEMENT = "xboolean";
	private static final String XBOOLEANLIST_ELEMENT = "xbooleanList";
	private static final String XBYTELIST_ELEMENT = "xbyteList";
	private static final String XDOUBLE_ELEMENT = "xdouble";
	private static final String XDOUBLELIST_ELEMENT = "xdoubleList";
	private static final String XID_ELEMENT = "xid";
	private static final String XIDLIST_ELEMENT = "xidList";
	private static final String XIDSET_ELEMENT = "xidSet";
	private static final String XIDSORTEDSET_ELEMENT = "xidSortedSet";
	private static final String XINTEGER_ELEMENT = "xinteger";
	private static final String XINTEGERLIST_ELEMENT = "xintegerList";
	private static final String XLONG_ELEMENT = "xlong";
	private static final String XLONGLIST_ELEMENT = "xlongList";
	private static final String XSTRING_ELEMENT = "xstring";
	private static final String XSTRINGLIST_ELEMENT = "xstringList";
	private static final String XSTRINGSET_ELEMENT = "xstringSet";
	private static final String XNULL_ELEMENT = "xnull";
	private static final String NAME_CONTENT = "value";
	private static final String NAME_VALUES = "values";
	private static final String NULL_VALUE = "true";
	
	public static List<XAddress> getAddressListContents(MiniElement ele) {
		
		List<XAddress> list = new ArrayList<XAddress>();
		
		Iterator<Object> entryIterator = ele.getValues(NAME_VALUES, XADDRESS_ELEMENT);
		while(entryIterator.hasNext()) {
			Object value = entryIterator.next();
			list.add(value == null ? null : XX.toAddress(value.toString()));
		}
		return list;
	}
	
	public static List<XID> getIdListContents(MiniElement ele) {
		
		List<XID> list = new ArrayList<XID>();
		
		Iterator<Object> entryIterator = ele.getValues(NAME_VALUES, XID_ELEMENT);
		while(entryIterator.hasNext()) {
			Object value = entryIterator.next();
			list.add(value == null ? null : XX.toId(value.toString()));
		}
		return list;
	}
	
	public static List<String> getStringListContents(MiniElement ele) {
		List<String> list = new ArrayList<String>();
		
		Iterator<Object> entryIterator = ele.getValues(NAME_VALUES, XSTRING_ELEMENT);
		while(entryIterator.hasNext()) {
			Object value = entryIterator.next();
			list.add(value == null ? null : value.toString());
		}
		return list;
	}
	
	/**
	 * @return The {@link XAddress} represented by the given XML element.
	 * @throws IllegalArgumentException if the given XML element is not a valid
	 *             representation of an {@link XAddress}
	 */
	public static XAddress toAddress(MiniElement ele) {
		
		XmlUtils.checkElementName(ele, XADDRESS_ELEMENT);
		
		if(ele.getAttribute(NULL_ATTRIBUTE) != null) {
			return null;
		}
		
		String data = ele.getContent(NAME_CONTENT).toString();
		
		try {
			return XX.toAddress(data);
		} catch(Exception e) {
			throw new RuntimeException("An <" + XADDRESS_ELEMENT
			        + "> element must contain a valid XAddress, got \"" + data + '"', e);
		}
		
	}
	
	/**
	 * @return The {@link XAddressListValue} represented by the given XML
	 *         element.
	 * @throws IllegalArgumentException if the given XML element is not a valid
	 *             representation of an {@link XAddressListValue}
	 */
	public static XAddressListValue toAddressListValue(MiniElement ele) {
		
		XmlUtils.checkElementName(ele, XADDRESSLIST_ELEMENT);
		
		List<XAddress> list = getAddressListContents(ele);
		
		return XV.toAddressListValue(list);
		
	}
	
	/**
	 * @return The {@link XAddressSetValue} represented by the given XML
	 *         element.
	 * @throws IllegalArgumentException if the given XML element is not a valid
	 *             representation of an {@link XAddressSetValue}
	 */
	public static XAddressSetValue toAddressSetValue(MiniElement ele) {
		
		if(ele.getType().equals(XADDRESSSORTEDSET_ELEMENT)) {
			return toAddressSortedSetValue(ele);
		}
		
		XmlUtils.checkElementName(ele, XADDRESSSET_ELEMENT);
		
		List<XAddress> list = getAddressListContents(ele);
		
		return XV.toAddressSetValue(list);
		
	}
	
	/**
	 * @return The {@link XAddressSortedSetValue} represented by the given XML
	 *         element.
	 * @throws IllegalArgumentException if the given XML element is not a valid
	 *             representation of an {@link XAddressSortedSetValue}
	 */
	public static XAddressSortedSetValue toAddressSortedSetValue(MiniElement ele) {
		
		XmlUtils.checkElementName(ele, XADDRESSSORTEDSET_ELEMENT);
		
		List<XAddress> list = getAddressListContents(ele);
		
		return XV.toAddressSortedSetValue(list);
		
	}
	
	/**
	 * @return The {@link XBooleanListValue} represented by the given XML
	 *         element.
	 * @throws IllegalArgumentException if the given XML element is not a valid
	 *             representation of an {@link XBooleanListValue}
	 */
	@SuppressWarnings("boxing")
	public static XBooleanListValue toBooleanListValue(MiniElement ele) {
		
		XmlUtils.checkElementName(ele, XBOOLEANLIST_ELEMENT);
		
		List<Boolean> list = new ArrayList<Boolean>();
		
		Iterator<Object> entryIterator = ele.getValues(NAME_VALUES, XBOOLEAN_ELEMENT);
		while(entryIterator.hasNext()) {
			Object value = entryIterator.next();
			if(value == null) {
				list.add(Boolean.FALSE);
			} else {
				list.add(toBoolean(value));
			}
		}
		
		return XV.toBooleanListValue(list);
		
	}
	
	public static boolean toBoolean(Object value) {
		if(value instanceof Boolean) {
			return (Boolean)value;
		} else {
			return Boolean.valueOf(value.toString());
		}
	}
	
	/**
	 * @return The {@link XBooleanValue} represented by the given XML element.
	 * @throws IllegalArgumentException if the given XML element is not a valid
	 *             representation of an {@link XBooleanValue}
	 */
	public static XBooleanValue toBooleanValue(MiniElement ele) {
		
		XmlUtils.checkElementName(ele, XBOOLEAN_ELEMENT);
		
		boolean value = toBoolean(ele.getContent(NAME_CONTENT));
		
		return XV.toValue(value);
	}
	
	/**
	 * @return The {@link XByteListValue} represented by the given XML element.
	 * @throws IllegalArgumentException if the given XML element is not a valid
	 *             representation of an {@link XByteListValue}
	 */
	public static XByteListValue toByteListValue(MiniElement ele) {
		
		XmlUtils.checkElementName(ele, XBYTELIST_ELEMENT);
		
		byte[] array = Base64.decode(ele.getContent(NAME_CONTENT).toString());
		
		return XV.toValue(array);
		
	}
	
	public static double toDouble(Object value) {
		
		if(value instanceof Number) {
			return ((Number)value).doubleValue();
		}
		
		try {
			return Double.parseDouble(value.toString());
		} catch(Exception e) {
			throw new RuntimeException("Expected a valid double, got " + value, e);
		}
		
	}
	
	/**
	 * @return The {@link XDoubleListValue} represented by the given XML
	 *         element.
	 * @throws IllegalArgumentException if the given XML element is not a valid
	 *             representation of an {@link XDoubleListValue}
	 */
	@SuppressWarnings("boxing")
	public static XDoubleListValue toDoubleListValue(MiniElement ele) {
		
		XmlUtils.checkElementName(ele, XDOUBLELIST_ELEMENT);
		
		List<Double> list = new ArrayList<Double>();
		
		Iterator<Object> entryIterator = ele.getValues(NAME_VALUES, XDOUBLE_ELEMENT);
		while(entryIterator.hasNext()) {
			Object value = entryIterator.next();
			list.add(toDouble(value));
		}
		
		return XV.toDoubleListValue(list);
		
	}
	
	/**
	 * @return The {@link XDoubleValue} represented by the given XML element.
	 * @throws IllegalArgumentException if the given XML element is not a valid
	 *             representation of an {@link XDoubleValue}
	 */
	public static XDoubleValue toDoubleValue(MiniElement ele) {
		
		XmlUtils.checkElementName(ele, XDOUBLE_ELEMENT);
		
		return XV.toValue(toDouble(ele.getContent(NAME_CONTENT)));
	}
	
	/**
	 * @return The {@link XID} represented by the given XML element.
	 * @throws IllegalArgumentException if the given XML element is not a valid
	 *             representation of an {@link XID}
	 */
	public static XID toId(MiniElement ele) {
		
		XmlUtils.checkElementName(ele, XID_ELEMENT);
		
		if(ele.getAttribute(NULL_ATTRIBUTE) != null) {
			return null;
		}
		
		String data = ele.getContent(NAME_CONTENT).toString();
		
		try {
			return XX.toId(data);
		} catch(Exception e) {
			throw new RuntimeException("An <" + XID_ELEMENT
			        + "> element must contain a valid XID, got " + data, e);
		}
		
	}
	
	/**
	 * @return The {@link XIDListValue} represented by the given XML element.
	 * @throws IllegalArgumentException if the given XML element is not a valid
	 *             representation of an {@link XIDListValue}
	 */
	public static XIDListValue toIdListValue(MiniElement ele) {
		
		XmlUtils.checkElementName(ele, XIDLIST_ELEMENT);
		
		List<XID> list = getIdListContents(ele);
		
		return XV.toIDListValue(list);
		
	}
	
	/**
	 * @return The {@link XIDSetValue} represented by the given XML element.
	 * @throws IllegalArgumentException if the given XML element is not a valid
	 *             representation of an {@link XIDSetValue}
	 */
	public static XIDSetValue toIdSetValue(MiniElement ele) {
		
		if(ele.getType().equals(XIDSORTEDSET_ELEMENT)) {
			return toIdSortedSetValue(ele);
		}
		
		XmlUtils.checkElementName(ele, XIDSET_ELEMENT);
		
		List<XID> list = getIdListContents(ele);
		
		return XV.toIDSetValue(list);
		
	}
	
	/**
	 * @return The {@link XIDSortedSetValue} represented by the given XML
	 *         element.
	 * @throws IllegalArgumentException if the given XML element is not a valid
	 *             representation of an {@link XIDSortedSetValue}
	 */
	public static XIDSortedSetValue toIdSortedSetValue(MiniElement ele) {
		
		XmlUtils.checkElementName(ele, XIDSORTEDSET_ELEMENT);
		
		List<XID> list = getIdListContents(ele);
		
		return XV.toIDSortedSetValue(list);
		
	}
	
	public static int toInteger(Object value) {
		
		if(value instanceof Number) {
			return ((Number)value).intValue();
		}
		
		try {
			return Integer.parseInt(value.toString());
		} catch(Exception e) {
			throw new RuntimeException("Expected a valid integer, got " + value, e);
		}
		
	}
	
	/**
	 * @return The {@link XIntegerListValue} represented by the given XML
	 *         element.
	 * @throws IllegalArgumentException if the given XML element is not a valid
	 *             representation of an {@link XIntegerListValue}
	 */
	@SuppressWarnings("boxing")
	public static XIntegerListValue toIntegerListValue(MiniElement ele) {
		
		XmlUtils.checkElementName(ele, XINTEGERLIST_ELEMENT);
		
		List<Integer> list = new ArrayList<Integer>();
		
		Iterator<Object> entryIterator = ele.getValues(NAME_VALUES, XINTEGER_ELEMENT);
		while(entryIterator.hasNext()) {
			Object value = entryIterator.next();
			list.add(toInteger(value));
		}
		
		return XV.toIntegerListValue(list);
		
	}
	
	/**
	 * @return The {@link XIntegerValue} represented by the given XML element.
	 * @throws IllegalArgumentException if the given XML element is not a valid
	 *             representation of an {@link XIntegerValue}
	 */
	public static XIntegerValue toIntegerValue(MiniElement ele) {
		
		XmlUtils.checkElementName(ele, XINTEGER_ELEMENT);
		
		return XV.toValue(toInteger(ele.getContent(NAME_CONTENT)));
	}
	
	public static long toLong(Object value) {
		
		if(value instanceof Number) {
			return ((Number)value).longValue();
		}
		
		try {
			return Long.parseLong(value.toString());
		} catch(Exception e) {
			throw new RuntimeException("Expected a valid long, got " + value, e);
		}
		
	}
	
	/**
	 * @return The {@link XLongListValue} represented by the given XML element.
	 * @throws IllegalArgumentException if the given XML element is not a valid
	 *             representation of an {@link XLongListValue}
	 */
	@SuppressWarnings("boxing")
	public static XLongListValue toLongListValue(MiniElement ele) {
		
		XmlUtils.checkElementName(ele, XLONGLIST_ELEMENT);
		
		List<Long> list = new ArrayList<Long>();
		
		Iterator<Object> entryIterator = ele.getValues(NAME_VALUES, XLONG_ELEMENT);
		while(entryIterator.hasNext()) {
			Object value = entryIterator.next();
			list.add(toLong(value));
		}
		
		return XV.toLongListValue(list);
		
	}
	
	/**
	 * @return The {@link XLongValue} represented by the given XML element.
	 * @throws IllegalArgumentException if the given XML element is not a valid
	 *             representation of an {@link XLongValue}
	 */
	public static XLongValue toLongValue(MiniElement ele) {
		
		XmlUtils.checkElementName(ele, XLONG_ELEMENT);
		
		return XV.toValue(toLong(ele.getContent(NAME_CONTENT)));
	}
	
	/**
	 * @return The {@link XStringListValue} represented by the given XML
	 *         element.
	 * @throws IllegalArgumentException if the given XML element is not a valid
	 *             representation of an {@link XStringListValue}
	 */
	public static XStringListValue toStringListValue(MiniElement ele) {
		
		XmlUtils.checkElementName(ele, XSTRINGLIST_ELEMENT);
		
		List<String> list = getStringListContents(ele);
		
		return XV.toStringListValue(list);
		
	}
	
	/**
	 * @return The {@link XStringSetValue} represented by the given XML element.
	 * @throws IllegalArgumentException if the given XML element is not a valid
	 *             representation of an {@link XStringSetValue}
	 */
	public static XStringSetValue toStringSetValue(MiniElement ele) {
		
		XmlUtils.checkElementName(ele, XSTRINGSET_ELEMENT);
		
		List<String> list = getStringListContents(ele);
		
		return XV.toStringSetValue(list);
		
	}
	
	/**
	 * @return The {@link XStringValue} represented by the given XML element.
	 * @throws IllegalArgumentException if the given XML element is not a valid
	 *             representation of an {@link XStringValue}
	 */
	public static XStringValue toStringValue(MiniElement ele) {
		
		XmlUtils.checkElementName(ele, XSTRING_ELEMENT);
		
		if(ele.getAttribute(NULL_ATTRIBUTE) != null) {
			return XV.toValue((String)null);
		}
		
		return XV.toValue(ele.getContent(NAME_CONTENT).toString());
	}
	
	/**
	 * @return The {@link XValue} represented by the given XML element.
	 * @throws IllegalArgumentException if the given XML element is not a valid
	 *             representation of an {@link XValue}
	 */
	public static XValue toValue(MiniElement ele) {
		
		if(isNullElement(ele)) {
			return null;
		}
		
		// IMPROVE consider using a (hash)map as the number of XValue types
		// increases
		String elementName = ele.getType();
		if(elementName.equals(XBOOLEAN_ELEMENT)) {
			return toBooleanValue(ele);
		} else if(elementName.equals(XDOUBLE_ELEMENT)) {
			return toDoubleValue(ele);
		} else if(elementName.equals(XINTEGER_ELEMENT)) {
			return toIntegerValue(ele);
		} else if(elementName.equals(XLONG_ELEMENT)) {
			return toLongValue(ele);
		} else if(elementName.equals(XSTRING_ELEMENT)) {
			return toStringValue(ele);
		} else if(elementName.equals(XID_ELEMENT)) {
			return toId(ele);
		} else if(elementName.equals(XADDRESS_ELEMENT)) {
			return toAddress(ele);
		} else if(elementName.equals(XBOOLEANLIST_ELEMENT)) {
			return toBooleanListValue(ele);
		} else if(elementName.equals(XDOUBLELIST_ELEMENT)) {
			return toDoubleListValue(ele);
		} else if(elementName.equals(XINTEGERLIST_ELEMENT)) {
			return toIntegerListValue(ele);
		} else if(elementName.equals(XLONGLIST_ELEMENT)) {
			return toLongListValue(ele);
		} else if(elementName.equals(XSTRINGLIST_ELEMENT)) {
			return toStringListValue(ele);
		} else if(elementName.equals(XIDLIST_ELEMENT)) {
			return toIdListValue(ele);
		} else if(elementName.equals(XADDRESSLIST_ELEMENT)) {
			return toAddressListValue(ele);
		} else if(elementName.equals(XSTRINGSET_ELEMENT)) {
			return toStringSetValue(ele);
		} else if(elementName.equals(XIDSET_ELEMENT)) {
			return toIdSetValue(ele);
		} else if(elementName.equals(XADDRESSSET_ELEMENT)) {
			return toAddressSetValue(ele);
		} else if(elementName.equals(XBYTELIST_ELEMENT)) {
			return toByteListValue(ele);
		} else if(elementName.equals(XIDSORTEDSET_ELEMENT)) {
			return toIdSortedSetValue(ele);
		} else if(elementName.equals(XADDRESSSORTEDSET_ELEMENT)) {
			return toAddressSortedSetValue(ele);
		}
		throw new RuntimeException("Cannot deserialize " + ele + " as an XValue.");
	}
	
	/**
	 * Emit the XML representation of the given {@link XAddress}.
	 */
	private static void toXml(XAddress xvalue, XydraOut xo) {
		
		xo.open(XADDRESS_ELEMENT);
		xo.content(NAME_CONTENT, xvalue.toURI());
		xo.close(XADDRESS_ELEMENT);
		
	}
	
	/**
	 * Emit the XML representation of the given {@link XAddressListValue}.
	 * 
	 * @throws NullPointerException if xvalue or xo is null.
	 */
	private static void toXml(XAddressListValue xvalue, XydraOut xo) {
		
		xo.open(XADDRESSLIST_ELEMENT);
		
		setAddressListContents(xvalue, xo);
		
		xo.close(XADDRESSLIST_ELEMENT);
		
	}
	
	/**
	 * Emit the XML representation of the given {@link XAddressSetValue}.
	 * 
	 * @throws NullPointerException if xvalue or xo is null.
	 */
	private static void toXml(XAddressSetValue xvalue, XydraOut xo) {
		
		if(xvalue instanceof XAddressSortedSetValue) {
			toXml((XAddressSortedSetValue)xvalue, xo);
			return;
		}
		
		xo.open(XADDRESSSET_ELEMENT);
		
		setAddressListContents(xvalue, xo);
		
		xo.close(XADDRESSSET_ELEMENT);
		
	}
	
	/**
	 * Emit the XML representation of the given {@link XAddressSortedSetValue}.
	 * 
	 * @throws NullPointerException if xvalue or xo is null.
	 */
	private static void toXml(XAddressSortedSetValue xvalue, XydraOut xo) {
		
		xo.open(XADDRESSSORTEDSET_ELEMENT);
		
		setAddressListContents(xvalue, xo);
		
		xo.close(XADDRESSSORTEDSET_ELEMENT);
		
	}
	
	public static void setAddressListContents(Iterable<XAddress> xvalue, XydraOut xo) {
		
		xo.children(NAME_VALUES, true);
		for(XAddress value : xvalue) {
			xo.value(XADDRESS_ELEMENT, value == null ? null : value.toURI());
		}
		
	}
	
	/**
	 * Emit the XML representation of the given {@link XBooleanListValue}.
	 * 
	 * @throws NullPointerException if xvalue or xo is null.
	 */
	private static void toXml(XBooleanListValue xvalue, XydraOut xo) {
		
		xo.open(XBOOLEANLIST_ELEMENT);
		
		xo.children(NAME_VALUES, true);
		for(Boolean value : xvalue) {
			xo.value(XBOOLEAN_ELEMENT, value);
		}
		
		xo.close(XBOOLEANLIST_ELEMENT);
		
	}
	
	/**
	 * Emit the XML representation of the given {@link XBooleanValue}.
	 * 
	 * @throws NullPointerException if xvalue or xo is null.
	 */
	private static void toXml(XBooleanValue xvalue, XydraOut xo) {
		
		xo.open(XBOOLEAN_ELEMENT);
		xo.content(NAME_CONTENT, xvalue.contents());
		xo.close(XBOOLEAN_ELEMENT);
		
	}
	
	/**
	 * Emit the XML representation of the given {@link XByteListValue}.
	 * 
	 * @throws NullPointerException if xvalue or xo is null.
	 */
	private static void toXml(XByteListValue xvalue, XydraOut xo) {
		
		xo.open(XBYTELIST_ELEMENT);
		xo.content(NAME_CONTENT, Base64.encode(xvalue.contents(), true));
		xo.close(XBYTELIST_ELEMENT);
		
	}
	
	/**
	 * Emit the XML representation of the given {@link XCollectionValue}.
	 * 
	 * @throws IllegalArgumentException if given {@link XCollectionValue} is an
	 *             unrecognized type.
	 * @throws NullPointerException if xvalue or xo is null.
	 */
	private static void toXml(XCollectionValue<?> xvalue, XydraOut xo) {
		
		if(xvalue instanceof XListValue<?>) {
			toXml((XListValue<?>)xvalue, xo);
		} else if(xvalue instanceof XSetValue<?>) {
			toXml((XSetValue<?>)xvalue, xo);
		} else {
			throw new IllegalArgumentException("Cannot serialize XCollectionValue " + xvalue
			        + " (unknown type: " + xvalue.getClass().getName() + ")");
		}
		
	}
	
	/**
	 * Emit the XML representation of the given {@link XDoubleListValue}.
	 * 
	 * @throws NullPointerException if xvalue or xo is null.
	 */
	private static void toXml(XDoubleListValue xvalue, XydraOut xo) {
		
		xo.open(XDOUBLELIST_ELEMENT);
		
		xo.children(NAME_VALUES, true);
		for(Double value : xvalue) {
			xo.value(XDOUBLE_ELEMENT, value);
		}
		
		xo.close(XDOUBLELIST_ELEMENT);
		
	}
	
	/**
	 * Emit the XML representation of the given {@link XDoubleValue}.
	 * 
	 * @throws NullPointerException if xvalue or xo is null.
	 */
	private static void toXml(XDoubleValue xvalue, XydraOut xo) {
		
		xo.open(XDOUBLE_ELEMENT);
		xo.content(NAME_CONTENT, xvalue.contents());
		xo.close(XDOUBLE_ELEMENT);
		
	}
	
	/**
	 * Emit the XML representation of the given {@link XID}.
	 */
	private static void toXml(XID xvalue, XydraOut xo) {
		
		xo.open(XID_ELEMENT);
		
		xo.content(NAME_CONTENT, xvalue);
		
		xo.close(XID_ELEMENT);
		
	}
	
	/**
	 * Emit the XML representation of the given {@link XIDListValue}.
	 * 
	 * @throws NullPointerException if xvalue or xo is null.
	 */
	private static void toXml(XIDListValue xvalue, XydraOut xo) {
		
		xo.open(XIDLIST_ELEMENT);
		
		setIdListContents(xvalue, xo);
		
		xo.close(XIDLIST_ELEMENT);
		
	}
	
	/**
	 * Emit the XML representation of the given {@link XIDSetValue}.
	 * 
	 * @throws NullPointerException if xvalue or xo is null.
	 */
	private static void toXml(XIDSetValue xvalue, XydraOut xo) {
		
		if(xvalue instanceof XIDSortedSetValue) {
			toXml((XIDSortedSetValue)xvalue, xo);
			return;
		}
		
		xo.open(XIDSET_ELEMENT);
		
		setIdListContents(xvalue, xo);
		
		xo.close(XIDSET_ELEMENT);
		
	}
	
	public static void setIdListContents(Iterable<XID> list, XydraOut xo) {
		
		xo.children(NAME_VALUES, true);
		for(XID value : list) {
			xo.value(XID_ELEMENT, value);
		}
		
	}
	
	/**
	 * Emit the XML representation of the given {@link XIDSortedSetValue}.
	 * 
	 * @throws NullPointerException if xvalue or xo is null.
	 */
	private static void toXml(XIDSortedSetValue xvalue, XydraOut xo) {
		
		xo.open(XIDSORTEDSET_ELEMENT);
		
		setIdListContents(xvalue, xo);
		
		xo.close(XIDSORTEDSET_ELEMENT);
		
	}
	
	/**
	 * Emit the XML representation of the given {@link XIntegerListValue}.
	 * 
	 * @throws NullPointerException if xvalue or xo is null.
	 */
	private static void toXml(XIntegerListValue xvalue, XydraOut xo) {
		
		xo.open(XINTEGERLIST_ELEMENT);
		
		xo.children(NAME_VALUES, true);
		for(Integer value : xvalue) {
			xo.value(XINTEGER_ELEMENT, value);
		}
		
		xo.close(XINTEGERLIST_ELEMENT);
		
	}
	
	/**
	 * Emit the XML representation of the given {@link XIntegerValue}.
	 * 
	 * @throws NullPointerException if xvalue or xo is null.
	 */
	private static void toXml(XIntegerValue xvalue, XydraOut xo) {
		
		xo.open(XINTEGER_ELEMENT);
		xo.content(NAME_CONTENT, xvalue.contents());
		xo.close(XINTEGER_ELEMENT);
		
	}
	
	/**
	 * Emit the XML representation of the given {@link XListValue}.
	 * 
	 * @throws IllegalArgumentException if given {@link XListValue} is an
	 *             unrecognized type.
	 * @throws NullPointerException if xvalue or xo is null.
	 */
	private static void toXml(XListValue<?> xvalue, XydraOut xo) {
		
		if(xvalue == null) {
			throw new NullPointerException("value is null");
		}
		
		if(xvalue instanceof XBooleanListValue) {
			toXml((XBooleanListValue)xvalue, xo);
		} else if(xvalue instanceof XDoubleListValue) {
			toXml((XDoubleListValue)xvalue, xo);
		} else if(xvalue instanceof XIntegerListValue) {
			toXml((XIntegerListValue)xvalue, xo);
		} else if(xvalue instanceof XLongListValue) {
			toXml((XLongListValue)xvalue, xo);
		} else if(xvalue instanceof XStringListValue) {
			toXml((XStringListValue)xvalue, xo);
		} else if(xvalue instanceof XIDListValue) {
			toXml((XIDListValue)xvalue, xo);
		} else if(xvalue instanceof XByteListValue) {
			toXml((XByteListValue)xvalue, xo);
		} else if(xvalue instanceof XAddressListValue) {
			toXml((XAddressListValue)xvalue, xo);
		} else {
			throw new IllegalArgumentException("Cannot serialize XListValue " + xvalue
			        + " (unknown type: " + xvalue.getClass().getName() + ")");
		}
		
	}
	
	/**
	 * Emit the XML representation of the given {@link XLongListValue}.
	 * 
	 * @throws NullPointerException if xvalue or xo is null.
	 */
	private static void toXml(XLongListValue xvalue, XydraOut xo) {
		
		xo.open(XLONGLIST_ELEMENT);
		
		xo.children(NAME_VALUES, true);
		for(Long value : xvalue) {
			xo.value(XLONG_ELEMENT, value);
		}
		
		xo.close(XLONGLIST_ELEMENT);
		
	}
	
	/**
	 * Emit the XML representation of the given {@link XLongValue}.
	 * 
	 * @throws NullPointerException if xvalue or xo is null.
	 */
	private static void toXml(XLongValue xvalue, XydraOut xo) {
		
		xo.open(XLONG_ELEMENT);
		xo.content(NAME_CONTENT, xvalue.contents());
		xo.close(XLONG_ELEMENT);
		
	}
	
	/**
	 * Emit the XML representation of the given {@link XSetValue}.
	 * 
	 * @throws IllegalArgumentException if given {@link XSetValue} is an
	 *             unrecognized type.
	 * @throws NullPointerException if xvalue or xo is null.
	 */
	private static void toXml(XSetValue<?> xvalue, XydraOut xo) {
		
		if(xvalue instanceof XIDSetValue) {
			toXml((XIDSetValue)xvalue, xo);
		} else if(xvalue instanceof XAddressSetValue) {
			toXml((XAddressSetValue)xvalue, xo);
		} else if(xvalue instanceof XStringSetValue) {
			toXml((XStringSetValue)xvalue, xo);
		} else {
			throw new IllegalArgumentException("Cannot serialize XSetValue " + xvalue
			        + " (unknown type: " + xvalue.getClass().getName() + ")");
		}
		
	}
	
	/**
	 * Emit the XML representation of the given {@link XStringListValue}.
	 * 
	 * @throws NullPointerException if xvalue or xo is null.
	 */
	private static void toXml(XStringListValue xvalue, XydraOut xo) {
		
		xo.open(XSTRINGLIST_ELEMENT);
		
		xo.children(NAME_VALUES, true);
		for(String value : xvalue) {
			xo.value(XSTRING_ELEMENT, value);
		}
		
		xo.close(XSTRINGLIST_ELEMENT);
		
	}
	
	/**
	 * Emit the XML representation of the given {@link XStringSetValue}.
	 * 
	 * @throws NullPointerException if xvalue or xo is null.
	 */
	private static void toXml(XStringSetValue xvalue, XydraOut xo) {
		
		xo.open(XSTRINGSET_ELEMENT);
		
		xo.children(NAME_VALUES, true);
		for(String value : xvalue) {
			xo.value(XSTRING_ELEMENT, value);
		}
		
		xo.close(XSTRINGSET_ELEMENT);
		
	}
	
	/**
	 * Emit the XML representation of the given {@link XStringValue}.
	 * 
	 * @throws NullPointerException if xvalue or xo is null.
	 */
	private static void toXml(XStringValue xvalue, XydraOut xo) {
		
		xo.open(XSTRING_ELEMENT);
		
		if(xvalue.contents() == null) {
			xo.attribute(NULL_ATTRIBUTE, NULL_VALUE);
		} else {
			xo.content(NAME_CONTENT, xvalue.contents());
		}
		
		xo.close(XSTRING_ELEMENT);
		
	}
	
	/**
	 * Emit the XML representation of the given {@link XValue}.
	 * 
	 * @throws IllegalArgumentException if given {@link XValue} is an
	 *             unrecognized type.
	 * @throws NullPointerException if xvalue or xo is null.
	 */
	public static void toXml(XValue xvalue, XydraOut xo) {
		
		if(xvalue == null) {
			saveNullElement(xo);
		} else if(xvalue instanceof XCollectionValue<?>) {
			toXml((XCollectionValue<?>)xvalue, xo);
		} else if(xvalue instanceof XBooleanValue) {
			toXml((XBooleanValue)xvalue, xo);
		} else if(xvalue instanceof XDoubleValue) {
			toXml((XDoubleValue)xvalue, xo);
		} else if(xvalue instanceof XIntegerValue) {
			toXml((XIntegerValue)xvalue, xo);
		} else if(xvalue instanceof XLongValue) {
			toXml((XLongValue)xvalue, xo);
		} else if(xvalue instanceof XStringValue) {
			toXml((XStringValue)xvalue, xo);
		} else if(xvalue instanceof XID) {
			toXml((XID)xvalue, xo);
		} else if(xvalue instanceof XAddress) {
			toXml((XAddress)xvalue, xo);
		} else {
			throw new IllegalArgumentException("Cannot serialize non-list XValue " + xvalue
			        + " (unknown type: " + xvalue.getClass().getName() + ")");
		}
		
	}
	
	public static boolean isNullElement(MiniElement ele) {
		return XNULL_ELEMENT.equals(ele.getType());
	}
	
	public static void saveNullElement(XydraOut out) {
		out.open(XNULL_ELEMENT);
		out.close(XNULL_ELEMENT);
	}
}
