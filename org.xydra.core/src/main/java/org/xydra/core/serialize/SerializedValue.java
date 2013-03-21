package org.xydra.core.serialize;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.xydra.annotations.RequiresAppEngine;
import org.xydra.annotations.RunsInAppEngine;
import org.xydra.annotations.RunsInGWT;
import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.XX;
import org.xydra.base.value.ValueType;
import org.xydra.base.value.XBinaryValue;
import org.xydra.base.value.XCollectionValue;
import org.xydra.base.value.XSingleValue;
import org.xydra.base.value.XV;
import org.xydra.base.value.XValue;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;


/**
 * Collection of methods to (de-)serialize variants of {@link XValue} to and
 * from their XML or JSON representation.
 * 
 * @author dscharrer
 */
@RunsInGWT(true)
@RunsInAppEngine(true)
@RequiresAppEngine(false)
public class SerializedValue {
    
    private static final Logger log = LoggerFactory.getLogger(SerializedValue.class);
    
    private static final String XADDRESS_ELEMENT = "xaddress";
    private static final String XADDRESSLIST_ELEMENT = "xaddressList";
    private static final String XADDRESSSET_ELEMENT = "xaddressSet";
    private static final String XADDRESSSORTEDSET_ELEMENT = "xaddressSortedSet";
    private static final String XBOOLEAN_ELEMENT = "xboolean";
    private static final String XBOOLEANLIST_ELEMENT = "xbooleanList";
    private static final String XBINARY_ELEMENT = "xbinary";
    private static final String XDOUBLE_ELEMENT = "xdouble";
    private static final String XDOUBLELIST_ELEMENT = "xdoubleList";
    private static final String XID_ELEMENT = "xid";
    private static final String XIdLIST_ELEMENT = "xidList";
    private static final String XIdSET_ELEMENT = "xidSet";
    private static final String XIdSORTEDSET_ELEMENT = "xidSortedSet";
    private static final String XINTEGER_ELEMENT = "xinteger";
    private static final String XINTEGERLIST_ELEMENT = "xintegerList";
    private static final String XLONG_ELEMENT = "xlong";
    private static final String XLONGLIST_ELEMENT = "xlongList";
    private static final String XSTRING_ELEMENT = "xstring";
    private static final String XSTRINGLIST_ELEMENT = "xstringList";
    private static final String XSTRINGSET_ELEMENT = "xstringSet";
    private static final String NAME_DATA = "data";
    
    private static List<XAddress> getAddressListContents(XydraElement element) {
        
        List<XAddress> list = new ArrayList<XAddress>();
        
        Iterator<Object> entryIterator = element.getValues(NAME_DATA, XADDRESS_ELEMENT);
        while(entryIterator.hasNext()) {
            Object value = entryIterator.next();
            list.add(SerializingUtils.toAddress(value));
        }
        return list;
    }
    
    protected static List<XId> getIdListContents(XydraElement element) {
        
        List<XId> list = new ArrayList<XId>();
        
        Iterator<Object> entryIterator = element.getValues(NAME_DATA, XID_ELEMENT);
        while(entryIterator.hasNext()) {
            Object value = entryIterator.next();
            list.add(SerializingUtils.toId(value));
        }
        return list;
    }
    
    private static List<String> getStringListContents(XydraElement element) {
        
        List<String> list = new ArrayList<String>();
        
        Iterator<Object> entryIterator = element.getValues(NAME_DATA, XSTRING_ELEMENT);
        while(entryIterator.hasNext()) {
            Object value = entryIterator.next();
            list.add(SerializingUtils.toString(value));
        }
        return list;
    }
    
    /**
     * @param element
     * @return The {@link XAddress} represented by the given XML/JSON element.
     * @throws IllegalArgumentException if the given XML/JSON element is not a
     *             valid representation of an {@link XAddress}
     */
    public static XAddress toAddress(XydraElement element) {
        
        if(element == null) {
            return null;
        }
        
        SerializingUtils.checkElementType(element, XADDRESS_ELEMENT);
        
        String data = getStringContent(element);
        
        try {
            return XX.toAddress(data);
        } catch(Exception e) {
            throw new RuntimeException("An <" + XADDRESS_ELEMENT
                    + "> element must contain a valid XAddress, got \"" + data + '"', e);
        }
        
    }
    
    private static String getStringContent(XydraElement element) {
        
        String data = SerializingUtils.toString(element.getContent(NAME_DATA));
        if(data == null) {
            throw new ParsingError(element, "Content must not be null.");
        }
        
        return data;
    }
    
    private static List<Boolean> getBooleanListContents(XydraElement element) {
        
        List<Boolean> list = new ArrayList<Boolean>();
        
        Iterator<Object> entryIterator = element.getValues(NAME_DATA, XBOOLEAN_ELEMENT);
        while(entryIterator.hasNext()) {
            Object value = entryIterator.next();
            list.add(SerializingUtils.toBoolean(value));
        }
        return list;
    }
    
    private static List<Double> getDoubleListContents(XydraElement element) {
        
        List<Double> list = new ArrayList<Double>();
        
        Iterator<Object> entryIterator = element.getValues(NAME_DATA, XDOUBLE_ELEMENT);
        while(entryIterator.hasNext()) {
            Object value = entryIterator.next();
            list.add(SerializingUtils.toDouble(value));
        }
        return list;
    }
    
    /**
     * @param element
     * @return The {@link XId} represented by the given XML/JSON element.
     * @throws IllegalArgumentException if the given XML/JSON element is not a
     *             valid representation of an {@link XId}
     */
    public static XId toId(XydraElement element) {
        
        if(element == null) {
            return null;
        }
        
        SerializingUtils.checkElementType(element, XID_ELEMENT);
        
        String data = getStringContent(element);
        
        try {
            return XX.toId(data);
        } catch(Exception e) {
            throw new ParsingError(element, "Expected a valid XId, got " + data, e);
        }
        
    }
    
    private static List<Integer> getIntegerListContents(XydraElement element) {
        
        List<Integer> list = new ArrayList<Integer>();
        
        Iterator<Object> entryIterator = element.getValues(NAME_DATA, XINTEGER_ELEMENT);
        while(entryIterator.hasNext()) {
            Object value = entryIterator.next();
            list.add(SerializingUtils.toInteger(value));
        }
        return list;
    }
    
    private static List<Long> getLongListContents(XydraElement element) {
        
        List<Long> list = new ArrayList<Long>();
        
        Iterator<Object> entryIterator = element.getValues(NAME_DATA, XLONG_ELEMENT);
        while(entryIterator.hasNext()) {
            Object value = entryIterator.next();
            list.add(SerializingUtils.toLong(value));
        }
        return list;
    }
    
    /**
     * @param element
     * @return The {@link XValue} (can be null) represented by the given
     *         XML/JSON element.
     * @throws IllegalArgumentException if the given XML/JSON element is not a
     *             valid representation of an {@link XValue}
     */
    public static XValue toValue(XydraElement element) {
        
        if(element == null) {
            return null;
        }
        
        // IMPROVE consider using a (hash)map as the number of XValue types
        // increases
        String elementName = element.getType();
        if(elementName.equals(XBOOLEAN_ELEMENT)) {
            return XV.toValue(SerializingUtils.toBoolean(element.getContent(NAME_DATA)));
        } else if(elementName.equals(XDOUBLE_ELEMENT)) {
            return XV.toValue(SerializingUtils.toDouble(element.getContent(NAME_DATA)));
        } else if(elementName.equals(XINTEGER_ELEMENT)) {
            return XV.toValue(SerializingUtils.toInteger(element.getContent(NAME_DATA)));
        } else if(elementName.equals(XLONG_ELEMENT)) {
            return XV.toValue(SerializingUtils.toLong(element.getContent(NAME_DATA)));
        } else if(elementName.equals(XSTRING_ELEMENT)) {
            return XV.toValue(SerializingUtils.toString(element.getContent(NAME_DATA)));
        } else if(elementName.equals(XID_ELEMENT)) {
            return toId(element);
        } else if(elementName.equals(XADDRESS_ELEMENT)) {
            return toAddress(element);
        } else if(elementName.equals(XBOOLEANLIST_ELEMENT)) {
            return XV.toBooleanListValue(getBooleanListContents(element));
        } else if(elementName.equals(XDOUBLELIST_ELEMENT)) {
            return XV.toDoubleListValue(getDoubleListContents(element));
        } else if(elementName.equals(XINTEGERLIST_ELEMENT)) {
            return XV.toIntegerListValue(getIntegerListContents(element));
        } else if(elementName.equals(XLONGLIST_ELEMENT)) {
            return XV.toLongListValue(getLongListContents(element));
        } else if(elementName.equals(XSTRINGLIST_ELEMENT)) {
            return XV.toStringListValue(getStringListContents(element));
        } else if(elementName.equals(XIdLIST_ELEMENT)) {
            return XV.toIdListValue(getIdListContents(element));
        } else if(elementName.equals(XADDRESSLIST_ELEMENT)) {
            return XV.toAddressListValue(getAddressListContents(element));
        } else if(elementName.equals(XSTRINGSET_ELEMENT)) {
            return XV.toStringSetValue(getStringListContents(element));
        } else if(elementName.equals(XIdSET_ELEMENT)) {
            return XV.toIdSetValue(getIdListContents(element));
        } else if(elementName.equals(XADDRESSSET_ELEMENT)) {
            return XV.toAddressSetValue(getAddressListContents(element));
        } else if(elementName.equals(XBINARY_ELEMENT)) {
            return XV.toValue(Base64.decode(getStringContent(element)));
        } else
        /* parse legacy data */
        if(elementName.equals("xbyteList")) {
            return XV.toValue(Base64.decode(getStringContent(element)));
        } else if(elementName.equals(XIdSORTEDSET_ELEMENT)) {
            return toIdSortedSetValue(element);
        } else if(elementName.equals(XADDRESSSORTEDSET_ELEMENT)) {
            return XV.toAddressSortedSetValue(getAddressListContents(element));
        }
        throw new ParsingError(element, "Unexpected element for an XValue.");
    }
    
    private static XValue toIdSortedSetValue(XydraElement element) {
        
        List<XId> xids = getIdListContents(element);
        
        XValue value = XV.toIdSortedSetValue(xids);
        
        log.debug("Serialised XydraElement to '" + value.toString() + "'");
        
        return value;
    }
    
    private static final Map<ValueType,String> singleElements = new HashMap<ValueType,String>();
    
    private static String getSingleElement(ValueType type) {
        
        synchronized(singleElements) {
            if(singleElements.isEmpty()) {
                singleElements.put(ValueType.String, XSTRING_ELEMENT);
                singleElements.put(ValueType.Address, XADDRESS_ELEMENT);
                singleElements.put(ValueType.Id, XID_ELEMENT);
                singleElements.put(ValueType.Integer, XINTEGER_ELEMENT);
                singleElements.put(ValueType.Long, XLONG_ELEMENT);
                singleElements.put(ValueType.Double, XDOUBLE_ELEMENT);
                singleElements.put(ValueType.Boolean, XBOOLEAN_ELEMENT);
            }
        }
        
        return singleElements.get(type);
    }
    
    private static final Map<ValueType,String> collectionElements = new HashMap<ValueType,String>();
    
    private static String getCollectionElement(ValueType type) {
        
        synchronized(collectionElements) {
            if(collectionElements.isEmpty()) {
                collectionElements.put(ValueType.AddressList, XADDRESSLIST_ELEMENT);
                collectionElements.put(ValueType.AddressSet, XADDRESSSET_ELEMENT);
                collectionElements.put(ValueType.AddressSortedSet, XADDRESSSORTEDSET_ELEMENT);
                collectionElements.put(ValueType.IdList, XIdLIST_ELEMENT);
                collectionElements.put(ValueType.IdSet, XIdSET_ELEMENT);
                collectionElements.put(ValueType.IdSortedSet, XIdSORTEDSET_ELEMENT);
                collectionElements.put(ValueType.StringList, XSTRINGLIST_ELEMENT);
                collectionElements.put(ValueType.StringSet, XSTRINGSET_ELEMENT);
                collectionElements.put(ValueType.IntegerList, XINTEGERLIST_ELEMENT);
                collectionElements.put(ValueType.LongList, XLONGLIST_ELEMENT);
                collectionElements.put(ValueType.DoubleList, XDOUBLELIST_ELEMENT);
                collectionElements.put(ValueType.BooleanList, XBOOLEANLIST_ELEMENT);
            }
        }
        
        return collectionElements.get(type);
    }
    
    private static void serialize(XSingleValue<?> xvalue, XydraOut out) {
        
        String element = getSingleElement(xvalue.getType());
        if(element == null) {
            throw new IllegalArgumentException("Cannot serialize XSingleValue " + xvalue
                    + " (unknown type: " + xvalue.getClass().getName() + ")");
        }
        
        out.element(element, NAME_DATA, xvalue.getValue());
    }
    
    private static void serialize(XCollectionValue<?> xvalue, XydraOut out) {
        
        String element = getCollectionElement(xvalue.getType());
        String componentElement = getSingleElement(xvalue.getComponentType());
        if(element == null || componentElement == null) {
            throw new IllegalArgumentException("Cannot serialize XCollectionValue " + xvalue
                    + " (unknown type: " + xvalue.getClass().getName() + ")");
        }
        
        out.open(element);
        out.values(NAME_DATA, componentElement, xvalue);
        out.close(element);
    }
    
    private static void serialize(XBinaryValue xvalue, XydraOut out) {
        out.element(XBINARY_ELEMENT, NAME_DATA, serializeBinaryContent(xvalue.contents()));
    }
    
    public static String serializeBinaryContent(byte[] content) {
        return Base64.encode(content, true);
    }
    
    public static byte[] deserializeBinaryContent(String base64) {
        return Base64.decode(base64);
    }
    
    /**
     * Emit the XML/JSON representation of the given {@link XValue}.
     * 
     * @param xvalue The value to serialize. This may be null.
     * @param out
     * @throws IllegalArgumentException if given {@link XValue} is an
     *             unrecognized type.
     */
    public static void serialize(XValue xvalue, XydraOut out) {
        
        if(xvalue == null) {
            out.nullElement();
        } else if(xvalue instanceof XBinaryValue) {
            serialize((XBinaryValue)xvalue, out);
        } else if(xvalue instanceof XCollectionValue<?>) {
            serialize((XCollectionValue<?>)xvalue, out);
        } else if(xvalue instanceof XSingleValue<?>) {
            serialize((XSingleValue<?>)xvalue, out);
        } else {
            throw new IllegalArgumentException("Cannot serialize non-list XValue " + xvalue
                    + " (unknown type: " + xvalue.getClass().getName() + ")");
        }
        
    }
    
    protected static void setIdListContents(Iterable<XId> list, XydraOut xo) {
        xo.values(NAME_DATA, XID_ELEMENT, list);
    }
    
}
