package org.xydra.base.value;

import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import org.xydra.annotations.CanBeNull;
import org.xydra.annotations.NeverNull;
import org.xydra.base.XAddress;
import org.xydra.base.XID;


/**
 * The type system of Xydra XValues.
 * 
 * Xydra types can be distinguished into collection types (..Set, ..List) and
 * single types.
 * 
 * Here are all primitive Java types with a corresponding Xydra types:
 * <table>
 * <tr>
 * <th>Java type</th>
 * <th>Xydra type</th>
 * </tr>
 * <tr>
 * <td>boolean</td>
 * <td> {@link XBooleanValue}</td>
 * </tr>
 * <tr>
 * <td>int</td>
 * <td> {@link XIntegerValue}</td>
 * </tr>
 * <tr>
 * <td>double</td>
 * <td> {@link XDoubleValue}</td>
 * </tr>
 * <tr>
 * <td>long</td>
 * <td> {@link XLongValue}</td>
 * </tr>
 * </table>
 * 
 * These are all arrays of primitive Java types with a corresponding Xydra types
 * (yes, its just one):
 * <table>
 * <tr>
 * <th>Java type</th>
 * <th>Xydra type</th>
 * </tr>
 * <tr>
 * <td>byte[]</td>
 * <td> {@link XBinaryValue}</td>
 * </tr>
 * </table>
 * 
 * And this is the mapping for all Java object types:
 * <table>
 * <tr>
 * <th>Java type</th>
 * <th>Xydra type</th>
 * </tr>
 * <tr>
 * <td>java.lang.Boolean</td>
 * <td> {@link XBooleanValue}</td>
 * </tr>
 * <tr>
 * <td>java.lang.Integer</td>
 * <td> {@link XIntegerValue}</td>
 * </tr>
 * <tr>
 * <td>java.lang.Double</td>
 * <td> {@link XDoubleValue}</td>
 * </tr>
 * <tr>
 * <td>java.lang.Long</td>
 * <td> {@link XLongValue}</td>
 * </tr>
 * <tr>
 * <td>java.lang.String</td>
 * <td> {@link XStringValue}</td>
 * </tr>
 * </table>
 * 
 * And these types map really only onto themselves:
 * <table>
 * <tr>
 * <th>Java type</th>
 * <th>Xydra type</th>
 * </tr>
 * <tr>
 * <td> {@link XID}</td>
 * <td> {@link XID}</td>
 * </tr>
 * <tr>
 * <td> {@link XAddress}</td>
 * <td> {@link XAddress}</td>
 * </tr>
 * </table>
 * 
 * And finally, these are currently all Xydra collection types and their Java
 * equivalents:
 * <table>
 * <tr>
 * <th>Java type</th>
 * <th>Xydra type</th>
 * </tr>
 * <tr>
 * <td>List&lt;XAddress&gt;</td>
 * <td> {@link XAddressListValue}</td>
 * </tr>
 * <tr>
 * <td>Set&lt;XAddress&gt;</td>
 * <td> {@link XAddressSetValue}</td>
 * </tr>
 * <tr>
 * <td>SortedSet&lt;XAddress&gt;</td>
 * <td> {@link XAddressSortedSetValue}</td>
 * </tr>
 * <tr>
 * <td>List&lt;XID&gt;</td>
 * <td> {@link XIDListValue}</td>
 * </tr>
 * <tr>
 * <td>Set&lt;XID&gt;</td>
 * <td> {@link XIDSetValue}</td>
 * </tr>
 * <tr>
 * <td>SortedSet&lt;XID&gt;</td>
 * <td> {@link XIDSortedSetValue}</td>
 * </tr>
 * <tr>
 * <td>List&lt;Boolean&gt;</td>
 * <td> {@link XBooleanListValue}</td>
 * </tr>
 * <tr>
 * <td>List&lt;Integer&gt;</td>
 * <td> {@link XIntegerListValue}</td>
 * </tr>
 * <tr>
 * <td>List&lt;Double&gt;</td>
 * <td> {@link XDoubleListValue}</td>
 * </tr>
 * <tr>
 * <td>List&lt;Long&gt;</td>
 * <td> {@link XLongListValue}</td>
 * </tr>
 * </table>
 * 
 * @author xamde
 */
public enum ValueType {
    
    /* single types first, so that they can be referenced, alphabetically */
    
    Address(XAddress.class, XAddress.class, null, false, false, false),
    
    Boolean(XBooleanValue.class, Boolean.class, null, false, false, false),
    
    Binary(XBinaryValue.class, byte[].class, null, false, false, false),
    
    Double(XDoubleValue.class, Double.class, null, false, false, true),
    
    Id(org.xydra.base.XID.class, XID.class, null, false, false, false),
    
    Integer(XIntegerValue.class, Integer.class, null, false, false, true),
    
    Long(XLongValue.class, Long.class, null, false, false, true),
    
    String(XStringValue.class, String.class, null, false, false, true),
    
    /* collection types, alphabetically */
    AddressList(XAddressListValue.class, List.class, Address, false, true, false),
    
    AddressSet(XAddressSetValue.class, Set.class, Address, true, false, false),
    
    AddressSortedSet(XAddressSortedSetValue.class, SortedSet.class, Address, true, true, false),
    
    BooleanList(XBooleanListValue.class, List.class, Boolean, false, true, false),
    
    DoubleList(XDoubleListValue.class, List.class, Double, false, true, false),
    
    IdList(XIDListValue.class, List.class, Id, false, true, false),
    
    IdSet(XIDSetValue.class, Set.class, Id, true, false, false),
    
    IdSortedSet(XIDSortedSetValue.class, SortedSet.class, Id, true, true, false),
    
    IntegerList(XIntegerListValue.class, List.class, Integer, false, true, false),
    
    LongList(XLongListValue.class, List.class, Long, false, true, false),
    
    StringList(XStringListValue.class, List.class, String, false, true, false),
    
    StringSet(XStringSetValue.class, Set.class, String, true, false, false);
    
    private Class<?> xydraInterface;
    private ValueType componentValueType;
    private boolean isSortedCollection;
    private boolean isNumeric;
    private boolean isSet;
    private Class<?> javaClass;
    
    /**
     * @return true iff this type is a collection that has a defined order.
     */
    public boolean isSortedCollection() {
        return this.isSortedCollection;
    }
    
    /**
     * @return true iff this type represents any collection type. Xydra knows
     *         only List, Set and SortedSet.
     */
    public boolean isCollection() {
        return this.componentValueType != null;
    }
    
    public boolean isSingle() {
        return !isCollection();
    }
    
    /**
     * @return true iff this type is a collection with set-semantics, i.e. each
     *         element can appear only once.
     */
    public boolean isSet() {
        return this.isSet;
    }
    
    /**
     * @return true iff this type is a numberic type, i.e. it can store numbers.
     *         Collections are never numeric, but their components can.
     */
    public boolean isNumeric() {
        return this.isNumeric;
    }
    
    /**
     * @param xydraInterface
     * @param javaClass The Java class that this Xydra type maps to
     * @param componentValueType If this value type represents a kind of
     *            collection, this is the value type of its components. Null for
     *            single types.
     * @param isSet
     * @param isSortedCollection
     * @param isNumeric
     */
    ValueType(@NeverNull Class<?> xydraInterface, Class<?> javaClass,
            @CanBeNull ValueType componentValueType, boolean isSet, boolean isSortedCollection,
            boolean isNumeric) {
        assert componentValueType != null || !(isSet || isSortedCollection) : xydraInterface
                .getCanonicalName();
        assert !isNumeric || componentValueType == null : xydraInterface.getCanonicalName();
        this.xydraInterface = xydraInterface;
        this.javaClass = javaClass;
        this.componentValueType = componentValueType;
        this.isSet = isSet;
        this.isSortedCollection = isSortedCollection;
        this.isNumeric = isNumeric;
    }
    
    /**
     * @return the interface B in Xydra that this ValueType A represents. If you
     *         take this interface B and call {@link XValue#getType()} on it,
     *         you get back this value type A.
     */
    public Class<?> getXydraInterface() {
        return this.xydraInterface;
    }
    
    /**
     * @return null if this type is not a collection type. Otherwise the
     *         ValueType of the component type. E.g. XDoubleList returns
     *         XDouble.
     */
    public ValueType getComponentType() {
        return this.componentValueType;
    }
    
    /**
     * @return the native Java class that this Xydra type represents. E.g.
     *         Xydra's XInteger has the Java class Integer.
     */
    public Class<?> getJavaClass() {
        return this.javaClass;
    }
    
    /**
     * @param xydraInterface
     * @return ...
     * @throws IllegalArgumentException if class could not be recognised as a
     *             ValueType
     */
    public static ValueType valueType(Class<?> xydraInterface) {
        assert xydraInterface != null;
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
