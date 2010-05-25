package org.xydra.core.xml;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.xydra.annotations.RunsInAppEngine;
import org.xydra.annotations.RunsInGWT;
import org.xydra.annotations.RunsInJava;
import org.xydra.core.X;
import org.xydra.core.model.XID;
import org.xydra.core.value.XBooleanListValue;
import org.xydra.core.value.XBooleanValue;
import org.xydra.core.value.XDoubleListValue;
import org.xydra.core.value.XDoubleValue;
import org.xydra.core.value.XIDListValue;
import org.xydra.core.value.XIDValue;
import org.xydra.core.value.XIntegerListValue;
import org.xydra.core.value.XIntegerValue;
import org.xydra.core.value.XListValue;
import org.xydra.core.value.XLongListValue;
import org.xydra.core.value.XLongValue;
import org.xydra.core.value.XStringListValue;
import org.xydra.core.value.XStringValue;
import org.xydra.core.value.XValue;
import org.xydra.core.value.XValueFactory;



/**
 * Collection of methods to (de-)serialize variants of {@link XValue} to and
 * from their XML representation.
 * 
 * @author dscharrer
 */
@RunsInGWT
@RunsInAppEngine
@RunsInJava
public class XmlValue {
	
	private static final String XBOOLEANLIST_ELEMENT = "xbooleanList";
	private static final String XDOUBLELIST_ELEMENT = "xdoubleList";
	private static final String XINTEGERLIST_ELEMENT = "xintegerList";
	private static final String XLONGLIST_ELEMENT = "xlongList";
	private static final String XSTRINGLIST_ELEMENT = "xstringList";
	private static final String XIDLIST_ELEMENT = "xidList";
	private static final String XBOOLEAN_ELEMENT = "xboolean";
	private static final String XDOUBLE_ELEMENT = "xdouble";
	private static final String XINTEGER_ELEMENT = "xinteger";
	private static final String XLONG_ELEMENT = "xlong";
	private static final String XSTRING_ELEMENT = "xstring";
	private static final String XID_ELEMENT = "xid";
	
	private final static XValueFactory factory = X.getValueFactory();
	
	private static void checkElementName(MiniElement xml, String expectedName) {
		if(!xml.getName().equals(expectedName)) {
			throw new IllegalArgumentException("Given element " + xml + " is not an <"
			        + expectedName + "> element.");
		}
	}
	
	/**
	 * @return The {@link XValue} represented by the given XML element.
	 * @throws IllegalArgumentException if the given XML element is not a valid
	 *             representation of an {@link XValue}
	 */
	public static XValue toValue(MiniElement xml) {
		String elementName = xml.getName();
		if(elementName.equals(XBOOLEAN_ELEMENT))
			return toBooleanValue(xml);
		else if(elementName.equals(XDOUBLE_ELEMENT))
			return toDoubleValue(xml);
		else if(elementName.equals(XINTEGER_ELEMENT))
			return toIntegerValue(xml);
		else if(elementName.equals(XLONG_ELEMENT))
			return toLongValue(xml);
		else if(elementName.equals(XSTRING_ELEMENT))
			return toStringValue(xml);
		else if(elementName.equals(XID_ELEMENT))
			return toIdValue(xml);
		else if(elementName.equals(XBOOLEANLIST_ELEMENT))
			return toBooleanListValue(xml);
		else if(elementName.equals(XDOUBLELIST_ELEMENT))
			return toDoubleListValue(xml);
		else if(elementName.equals(XINTEGERLIST_ELEMENT))
			return toIntegerListValue(xml);
		else if(elementName.equals(XLONGLIST_ELEMENT))
			return toLongListValue(xml);
		else if(elementName.equals(XSTRINGLIST_ELEMENT))
			return toStringListValue(xml);
		else if(elementName.equals(XIDLIST_ELEMENT))
			return toIdListValue(xml);
		throw new RuntimeException("Cannot deserialize " + xml + " as an XValue.");
	}
	
	/**
	 * @return The {@link XBooleanValue} represented by the given XML element.
	 * @throws IllegalArgumentException if the given XML element is not a valid
	 *             representation of an {@link XBooleanValue}
	 */
	public static XBooleanValue toBooleanValue(MiniElement xml) {
		
		checkElementName(xml, XBOOLEAN_ELEMENT);
		
		// TODO throw exception if the data is neither "true" nor "false"?
		boolean value = Boolean.parseBoolean(xml.getData());
		
		return factory.createBooleanValue(value);
		
	}
	
	/**
	 * @return The {@link XDoubleValue} represented by the given XML element.
	 * @throws IllegalArgumentException if the given XML element is not a valid
	 *             representation of an {@link XDoubleValue}
	 */
	public static XDoubleValue toDoubleValue(MiniElement xml) {
		
		checkElementName(xml, XDOUBLE_ELEMENT);
		
		String data = xml.getData();
		double value;
		
		try {
			value = Double.parseDouble(data);
		} catch(Exception e) {
			throw new RuntimeException("An <" + XDOUBLE_ELEMENT
			        + "> element must contain a valid double, got " + data, e);
		}
		
		return factory.createDoubleValue(value);
		
	}
	
	/**
	 * @return The {@link XIntegerValue} represented by the given XML element.
	 * @throws IllegalArgumentException if the given XML element is not a valid
	 *             representation of an {@link XIntegerValue}
	 */
	public static XIntegerValue toIntegerValue(MiniElement xml) {
		
		checkElementName(xml, XINTEGER_ELEMENT);
		
		String data = xml.getData();
		int value;
		
		try {
			value = Integer.parseInt(data);
		} catch(Exception e) {
			throw new RuntimeException("An <" + XINTEGER_ELEMENT
			        + "> element must contain a valid integer, got " + data, e);
		}
		
		return factory.createIntegerValue(value);
		
	}
	
	/**
	 * @return The {@link XLongValue} represented by the given XML element.
	 * @throws IllegalArgumentException if the given XML element is not a valid
	 *             representation of an {@link XLongValue}
	 */
	public static XLongValue toLongValue(MiniElement xml) {
		
		checkElementName(xml, XLONG_ELEMENT);
		
		String data = xml.getData();
		long value;
		
		try {
			value = Long.parseLong(data);
		} catch(Exception e) {
			throw new RuntimeException("An <" + XLONG_ELEMENT
			        + "> element must contain a valid long, got " + data, e);
		}
		
		return factory.createLongValue(value);
		
	}
	
	/**
	 * @return The {@link XStringValue} represented by the given XML element.
	 * @throws IllegalArgumentException if the given XML element is not a valid
	 *             representation of an {@link XStringValue}
	 */
	public static XStringValue toStringValue(MiniElement xml) {
		
		checkElementName(xml, XSTRING_ELEMENT);
		
		return factory.createStringValue(xml.getData());
		
	}
	
	/**
	 * @return The {@link XIDValue} represented by the given XML element.
	 * @throws IllegalArgumentException if the given XML element is not a valid
	 *             representation of an {@link XIDValue}
	 */
	public static XIDValue toIdValue(MiniElement xml) {
		
		checkElementName(xml, XID_ELEMENT);
		
		String data = xml.getData();
		XID value;
		
		try {
			value = X.getIDProvider().fromString(data);
		} catch(Exception e) {
			throw new RuntimeException("An <" + XID_ELEMENT
			        + "> element must contain a valid XID, got " + data, e);
		}
		
		return factory.createIDValue(value);
		
	}
	
	/**
	 * @return The {@link XBooleanListValue} represented by the given XML
	 *         element.
	 * @throws IllegalArgumentException if the given XML element is not a valid
	 *             representation of an {@link XBooleanListValue}
	 */
	@SuppressWarnings("boxing")
	public static XBooleanListValue toBooleanListValue(MiniElement xml) {
		
		checkElementName(xml, XBOOLEANLIST_ELEMENT);
		
		List<Boolean> list = new ArrayList<Boolean>();
		
		Iterator<MiniElement> entryIterator = xml.getElementsByTagName(XBOOLEAN_ELEMENT);
		while(entryIterator.hasNext()) {
			MiniElement entryElement = entryIterator.next();
			list.add(toBooleanValue(entryElement).contents());
		}
		
		Boolean[] array = new Boolean[list.size()];
		array = list.toArray(array);
		
		return factory.createBooleanListValue(array);
		
	}
	
	/**
	 * @return The {@link XDoubleListValue} represented by the given XML
	 *         element.
	 * @throws IllegalArgumentException if the given XML element is not a valid
	 *             representation of an {@link XDoubleListValue}
	 */
	@SuppressWarnings("boxing")
	public static XDoubleListValue toDoubleListValue(MiniElement xml) {
		
		checkElementName(xml, XDOUBLELIST_ELEMENT);
		
		List<Double> list = new ArrayList<Double>();
		
		Iterator<MiniElement> entryIterator = xml.getElementsByTagName(XDOUBLE_ELEMENT);
		while(entryIterator.hasNext()) {
			MiniElement entryElement = entryIterator.next();
			list.add(toDoubleValue(entryElement).contents());
		}
		
		Double[] array = new Double[list.size()];
		array = list.toArray(array);
		
		return factory.createDoubleListValue(array);
		
	}
	
	/**
	 * @return The {@link XIntegerListValue} represented by the given XML
	 *         element.
	 * @throws IllegalArgumentException if the given XML element is not a valid
	 *             representation of an {@link XIntegerListValue}
	 */
	@SuppressWarnings("boxing")
	public static XIntegerListValue toIntegerListValue(MiniElement xml) {
		
		checkElementName(xml, XINTEGERLIST_ELEMENT);
		
		List<Integer> list = new ArrayList<Integer>();
		
		Iterator<MiniElement> entryIterator = xml.getElementsByTagName(XINTEGER_ELEMENT);
		while(entryIterator.hasNext()) {
			MiniElement entryElement = entryIterator.next();
			list.add(toIntegerValue(entryElement).contents());
		}
		
		Integer[] array = new Integer[list.size()];
		array = list.toArray(array);
		
		return factory.createIntegerListValue(array);
		
	}
	
	/**
	 * @return The {@link XLongListValue} represented by the given XML element.
	 * @throws IllegalArgumentException if the given XML element is not a valid
	 *             representation of an {@link XLongListValue}
	 */
	@SuppressWarnings("boxing")
	public static XLongListValue toLongListValue(MiniElement xml) {
		
		checkElementName(xml, XLONGLIST_ELEMENT);
		
		List<Long> list = new ArrayList<Long>();
		
		Iterator<MiniElement> entryIterator = xml.getElementsByTagName(XLONG_ELEMENT);
		while(entryIterator.hasNext()) {
			MiniElement entryElement = entryIterator.next();
			list.add(toLongValue(entryElement).contents());
		}
		
		Long[] array = new Long[list.size()];
		array = list.toArray(array);
		
		return factory.createLongListValue(array);
		
	}
	
	/**
	 * @return The {@link XStringListValue} represented by the given XML
	 *         element.
	 * @throws IllegalArgumentException if the given XML element is not a valid
	 *             representation of an {@link XStringListValue}
	 */
	public static XStringListValue toStringListValue(MiniElement xml) {
		
		checkElementName(xml, XSTRINGLIST_ELEMENT);
		
		List<String> list = new ArrayList<String>();
		
		Iterator<MiniElement> entryIterator = xml.getElementsByTagName(XSTRING_ELEMENT);
		while(entryIterator.hasNext()) {
			MiniElement entryElement = entryIterator.next();
			list.add(toStringValue(entryElement).contents());
		}
		
		String[] array = new String[list.size()];
		array = list.toArray(array);
		
		return factory.createStringListValue(array);
		
	}
	
	/**
	 * @return The {@link XIDListValue} represented by the given XML element.
	 * @throws IllegalArgumentException if the given XML element is not a valid
	 *             representation of an {@link XIDListValue}
	 */
	public static XIDListValue toIdListValue(MiniElement xml) {
		
		checkElementName(xml, XIDLIST_ELEMENT);
		
		List<XID> list = new ArrayList<XID>();
		
		Iterator<MiniElement> entryIterator = xml.getElementsByTagName(XID_ELEMENT);
		while(entryIterator.hasNext()) {
			MiniElement entryElement = entryIterator.next();
			list.add(toIdValue(entryElement).contents());
		}
		
		XID[] array = new XID[list.size()];
		array = list.toArray(array);
		
		return factory.createIDListValue(array);
		
	}
	
	/**
	 * @return The XML representation of the given {@link XValue}.
	 * @throws IllegalArgumentException if given {@link XValue} is an
	 *             unrecognized type.
	 * @throws NullPointerException if xvalue or xo is null.
	 */
	public static void toXml(XValue xvalue, XmlOut xo) {
		
		if(xvalue == null) {
			throw new NullPointerException("value is null");
		}
		
		if(xvalue instanceof XListValue<?>) {
			if(xvalue instanceof XBooleanListValue)
				toXml((XBooleanListValue)xvalue, xo);
			else if(xvalue instanceof XDoubleListValue)
				toXml((XDoubleListValue)xvalue, xo);
			else if(xvalue instanceof XIntegerListValue)
				toXml((XIntegerListValue)xvalue, xo);
			else if(xvalue instanceof XLongListValue)
				toXml((XLongListValue)xvalue, xo);
			else if(xvalue instanceof XStringListValue)
				toXml((XStringListValue)xvalue, xo);
			else if(xvalue instanceof XIDListValue)
				toXml((XIDListValue)xvalue, xo);
			else
				throw new IllegalArgumentException("Cannot serialize XListValue " + xvalue
				        + " (unknown type: " + xvalue.getClass().getName() + ")");
		} else if(xvalue instanceof XBooleanValue)
			toXml((XBooleanValue)xvalue, xo);
		else if(xvalue instanceof XDoubleValue)
			toXml((XDoubleValue)xvalue, xo);
		else if(xvalue instanceof XIntegerValue)
			toXml((XIntegerValue)xvalue, xo);
		else if(xvalue instanceof XLongValue)
			toXml((XLongValue)xvalue, xo);
		else if(xvalue instanceof XStringValue)
			toXml((XStringValue)xvalue, xo);
		else if(xvalue instanceof XIDValue)
			toXml((XIDValue)xvalue, xo);
		else
			throw new IllegalArgumentException("Cannot serialize non-list XValue " + xvalue
			        + " (unknown type: " + xvalue.getClass().getName() + ")");
	}
	
	/**
	 * @return The XML representation of the given {@link XBooleanListValue}.
	 * @throws NullPointerException if xvalue or xo is null.
	 */
	public static void toXml(XBooleanListValue xvalue, XmlOut xo) {
		
		xo.open(XBOOLEANLIST_ELEMENT);
		
		for(Boolean value : xvalue)
			toXml(value.booleanValue(), xo);
		
		xo.close(XBOOLEANLIST_ELEMENT);
		
	}
	
	/**
	 * @return The XML representation of the given {@link XDoubleListValue}.
	 * @throws NullPointerException if xvalue or xo is null.
	 */
	public static void toXml(XDoubleListValue xvalue, XmlOut xo) {
		
		xo.open(XDOUBLELIST_ELEMENT);
		
		for(Double value : xvalue)
			toXml(value.doubleValue(), xo);
		
		xo.close(XDOUBLELIST_ELEMENT);
		
	}
	
	/**
	 * @return The XML representation of the given {@link XIntegerListValue}.
	 * @throws NullPointerException if xvalue or xo is null.
	 */
	public static void toXml(XIntegerListValue xvalue, XmlOut xo) {
		
		xo.open(XINTEGERLIST_ELEMENT);
		
		for(Integer value : xvalue)
			toXml(value.intValue(), xo);
		
		xo.close(XINTEGERLIST_ELEMENT);
		
	}
	
	/**
	 * @return The XML representation of the given {@link XLongListValue}.
	 * @throws NullPointerException if xvalue or xo is null.
	 */
	public static void toXml(XLongListValue xvalue, XmlOut xo) {
		
		xo.open(XLONGLIST_ELEMENT);
		
		for(Long value : xvalue)
			toXml(value.longValue(), xo);
		
		xo.close(XLONGLIST_ELEMENT);
		
	}
	
	/**
	 * @return The XML representation of the given {@link XStringListValue}.
	 * @throws NullPointerException if xvalue or xo is null.
	 */
	public static void toXml(XStringListValue xvalue, XmlOut xo) {
		
		xo.open(XSTRINGLIST_ELEMENT);
		
		for(String value : xvalue)
			toXml(value, xo);
		
		xo.close(XSTRINGLIST_ELEMENT);
		
	}
	
	/**
	 * @return The XML representation of the given {@link XIDListValue}.
	 * @throws NullPointerException if xvalue or xo is null.
	 */
	public static void toXml(XIDListValue xvalue, XmlOut xo) {
		
		xo.open(XIDLIST_ELEMENT);
		
		for(XID value : xvalue)
			toXml(value, xo);
		
		xo.close(XIDLIST_ELEMENT);
		
	}
	
	/**
	 * @return The XML representation of the given {@link XBooleanValue}.
	 * @throws NullPointerException if xvalue or xo is null.
	 */
	public static void toXml(XBooleanValue xvalue, XmlOut xo) {
		toXml(xvalue.contents(), xo);
	}
	
	/**
	 * @return The XML representation of the given {@link XDoubleValue}.
	 * @throws NullPointerException if xvalue or xo is null.
	 */
	public static void toXml(XDoubleValue xvalue, XmlOut xo) {
		toXml(xvalue.contents(), xo);
	}
	
	/**
	 * @return The XML representation of the given {@link XIntegerValue}.
	 * @throws NullPointerException if xvalue or xo is null.
	 */
	public static void toXml(XIntegerValue xvalue, XmlOut xo) {
		toXml(xvalue.contents(), xo);
	}
	
	/**
	 * @return The XML representation of the given {@link XLongValue}.
	 * @throws NullPointerException if xvalue or xo is null.
	 */
	public static void toXml(XLongValue xvalue, XmlOut xo) {
		toXml(xvalue.contents(), xo);
	}
	
	/**
	 * @return The XML representation of the given {@link XStringValue}.
	 * @throws NullPointerException if xvalue or xo is null.
	 */
	public static void toXml(XStringValue xvalue, XmlOut xo) {
		toXml(xvalue.contents(), xo);
	}
	
	/**
	 * @return The XML representation of the given {@link XIDValue}.
	 * @throws NullPointerException if xvalue or xo is null.
	 */
	public static void toXml(XIDValue xvalue, XmlOut xo) {
		toXml(xvalue.contents(), xo);
	}
	
	private static void toXml(boolean xvalue, XmlOut xo) {
		
		xo.open(XBOOLEAN_ELEMENT);
		
		xo.content(Boolean.toString(xvalue));
		
		xo.close(XBOOLEAN_ELEMENT);
		
	}
	
	private static void toXml(double xvalue, XmlOut xo) {
		
		xo.open(XDOUBLE_ELEMENT);
		
		xo.content(Double.toString(xvalue));
		
		xo.close(XDOUBLE_ELEMENT);
		
	}
	
	private static void toXml(int xvalue, XmlOut xo) {
		
		xo.open(XINTEGER_ELEMENT);
		
		xo.content(Integer.toString(xvalue));
		
		xo.close(XINTEGER_ELEMENT);
		
	}
	
	private static void toXml(long xvalue, XmlOut xo) {
		
		xo.open(XLONG_ELEMENT);
		
		xo.content(Long.toString(xvalue));
		
		xo.close(XLONG_ELEMENT);
		
	}
	
	private static void toXml(String xvalue, XmlOut xo) {
		
		xo.open(XSTRING_ELEMENT);
		
		xo.content(xvalue);
		
		xo.close(XSTRING_ELEMENT);
		
	}
	
	private static void toXml(XID xvalue, XmlOut xo) {
		
		xo.open(XID_ELEMENT);
		
		xo.content(xvalue.toString());
		
		xo.close(XID_ELEMENT);
		
	}
	
}
