package org.xydra.base.value;

import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.sharedutils.XyAssert;


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
    
    Binary(XBinaryValue.class),
    
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
        return this == Address || this == Boolean || this == Binary || this == Double || this == Id
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
     * @param type must be a collection type
     * @return the Java class component type of the given collection type or
     *         null if type is not a collection type.
     */
    public static Class<?> getComponentType(ValueType type) {
        XyAssert.xyAssert(type.isCollection());
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
        XyAssert.xyAssert(type.isSingle());
        switch(type) {
        case Address:
            return XAddress.class;
        case Boolean:
            return Boolean.class;
        case Binary:
            return byte[].class;
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
    
    /**
     * @param javaCollectionType
     * @param xydraComponentType
     * @return null if no such Xydra type exists
     */
    public static ValueType getXydraCollectionType(Class<?> javaCollectionType,
            ValueType xydraComponentType) {
        XyAssert.xyAssert(xydraComponentType.isSingle());
        
        if(javaCollectionType.equals(Set.class)) {
            switch(xydraComponentType) {
            case Address:
                return AddressSet;
            case String:
                return StringSet;
            case Id:
                return IdSet;
            default:
                return null;
            }
        } else if(javaCollectionType.equals(List.class)) {
            switch(xydraComponentType) {
            case Address:
                return AddressList;
            case Boolean:
                return BooleanList;
            case Double:
                return DoubleList;
            case Integer:
                return IntegerList;
            case Long:
                return LongList;
            case String:
                return StringList;
            case Id:
                return IdList;
            default:
                return null;
            }
        } else if(javaCollectionType.equals(SortedSet.class)) {
            switch(xydraComponentType) {
            case Address:
                return AddressSortedSet;
            case Id:
                return IdSortedSet;
            default:
                return null;
            }
        } else {
            return null;
        }
    }
    
    public static ValueType valueType(Class<?> xydraInterface) {
        if(xydraInterface.equals(XAddressListValue.class)) {
            return AddressList;
        }
        if(xydraInterface.equals(XAddressListValue.class)) {
            return AddressList;
        }
        if(xydraInterface.equals(XAddressSetValue.class)) {
            return AddressSet;
        }
        if(xydraInterface.equals(XAddressSortedSetValue.class)) {
            return AddressSortedSet;
        }
        if(xydraInterface.equals(XBooleanListValue.class)) {
            return BooleanList;
        }
        if(xydraInterface.equals(XBooleanValue.class)) {
            return Boolean;
        }
        if(xydraInterface.equals(XBinaryValue.class)) {
            return Binary;
        }
        if(xydraInterface.equals(XDoubleListValue.class)) {
            return DoubleList;
        }
        if(xydraInterface.equals(XDoubleValue.class)) {
            return Double;
        }
        if(xydraInterface.equals(XID.class)) {
            return Id;
        }
        if(xydraInterface.equals(XIDListValue.class)) {
            return IdList;
        }
        if(xydraInterface.equals(XIDSetValue.class)) {
            return IdSet;
        }
        if(xydraInterface.equals(XIDSortedSetValue.class)) {
            return IdSortedSet;
        }
        if(xydraInterface.equals(XIntegerListValue.class)) {
            return IntegerList;
        }
        if(xydraInterface.equals(XIntegerValue.class)) {
            return Integer;
        }
        if(xydraInterface.equals(XLongListValue.class)) {
            return LongList;
        }
        if(xydraInterface.equals(XLongValue.class)) {
            return Long;
        }
        if(xydraInterface.equals(XStringListValue.class)) {
            return StringList;
        }
        if(xydraInterface.equals(XStringSetValue.class)) {
            return StringSet;
        }
        if(xydraInterface.equals(XStringValue.class)) {
            return String;
        }
        throw new IllegalArgumentException("Don't know how to map '" + xydraInterface.getName()
                + "' to a ValueType");
    }
    
    // public static void main(String[] args) {
    // for(ValueType v : ValueType.values()) {
    // System.out.println(v.getXydraInterface().getSimpleName() + ".class, ");
    // }
    // }
    
}
