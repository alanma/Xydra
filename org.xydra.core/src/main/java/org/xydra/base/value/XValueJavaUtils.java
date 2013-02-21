package org.xydra.base.value;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import org.xydra.annotations.CanBeNull;
import org.xydra.annotations.NeverNull;
import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.rmof.XReadableField;
import org.xydra.base.rmof.XReadableObject;
import org.xydra.base.rmof.XWritableField;
import org.xydra.base.rmof.XWritableObject;


public class XValueJavaUtils {
    
    /**
     * @param writableObject
     * @param fieldId
     * @return
     * @throws IllegalArgumentException if fieldId or writableObject are null
     */
    private static XWritableField _getOrCreateField(XWritableObject writableObject, XID fieldId)
            throws IllegalArgumentException {
        if(writableObject == null)
            throw new IllegalArgumentException("writableObject is null");
        if(fieldId == null)
            throw new IllegalArgumentException("fieldId is null");
        XWritableField f = writableObject.getField(fieldId);
        if(f == null)
            f = writableObject.createField(fieldId);
        return f;
    }
    
    /**
     * @param readableObject
     * @param fieldId
     * @return null if field or value does not exist; a {@link XValue} if set
     * @throws IllegalArgumentException if given readableObject is null
     */
    private static XValue _getValue(@NeverNull XReadableObject readableObject,
            @NeverNull XID fieldId) {
        if(readableObject == null)
            throw new IllegalArgumentException("readableObject is null");
        if(fieldId == null)
            throw new IllegalArgumentException("fieldId is null");
        XReadableField f = readableObject.getField(fieldId);
        if(f == null)
            return null;
        XValue v = f.getValue();
        return v;
    }
    
    /**
     * @param readableObject @NeverNull
     * @param fieldId @NeverNull
     * @return null if field or value does not exist; a XAddress value if set
     * @throws ClassCastException if value exists but is not a {@link XAddress}
     * @throws IllegalArgumentException if given readableObject is null
     */
    @CanBeNull
    public static XAddress getAddress(@NeverNull XReadableObject readableObject,
            @NeverNull XID fieldId) throws ClassCastException, IllegalArgumentException {
        XValue v = _getValue(readableObject, fieldId);
        if(v == null)
            return null;
        if(v instanceof XAddress) {
            XAddress specificV = (XAddress)v;
            return specificV;
        } else {
            throw new ClassCastException("XValue is a " + v.getClass() + " require an XAddress");
        }
    }
    
    /**
     * @param readableObject @NeverNull
     * @param fieldId @NeverNull
     * @return null if field or value does not exist; a byte[] value if set
     * @throws ClassCastException if value exists but is not a
     *             {@link XBinaryValue}
     * @throws IllegalArgumentException if given readableObject is null
     */
    @CanBeNull
    public static byte[] getBinary(@NeverNull XReadableObject readableObject, @NeverNull XID fieldId)
            throws ClassCastException, IllegalArgumentException {
        XValue v = _getValue(readableObject, fieldId);
        if(v == null)
            return null;
        if(v instanceof XBinaryValue) {
            XBinaryValue specificV = (XBinaryValue)v;
            return specificV.contents();
        } else {
            throw new ClassCastException("XValue is a " + v.getClass() + " require an XBinaryValue");
        }
    }
    
    /**
     * @param readableObject @NeverNull
     * @param fieldId @NeverNull
     * @return null if field or value does not exist; a Boolean value if set
     * @throws ClassCastException if value exists but is not a
     *             {@link XBooleanValue}
     * @throws IllegalArgumentException if given readableObject is null
     */
    @CanBeNull
    public static Boolean getBoolean(@NeverNull XReadableObject readableObject,
            @NeverNull XID fieldId) throws ClassCastException, IllegalArgumentException {
        XValue v = _getValue(readableObject, fieldId);
        if(v == null)
            return null;
        if(v instanceof XBooleanValue) {
            XBooleanValue specificV = (XBooleanValue)v;
            return specificV.getValue();
        } else {
            throw new ClassCastException("XValue is a " + v.getClass()
                    + " require an XBooleanValue");
        }
    }
    
    /**
     * @param readableObject @NeverNull
     * @param fieldId @NeverNull
     * @return null if field or value does not exist; a Double value if set
     * @throws ClassCastException if value exists but is not a
     *             {@link XDoubleValue}
     * @throws IllegalArgumentException if given readableObject is null
     */
    @CanBeNull
    public static Double getDouble(@NeverNull XReadableObject readableObject, @NeverNull XID fieldId)
            throws ClassCastException, IllegalArgumentException {
        XValue v = _getValue(readableObject, fieldId);
        if(v == null)
            return null;
        if(v instanceof XDoubleValue) {
            XDoubleValue specificV = (XDoubleValue)v;
            return specificV.getValue();
        } else {
            throw new ClassCastException("XValue is a " + v.getClass() + " require an XDoubleValue");
        }
    }
    
    /**
     * @param readableObject @NeverNull
     * @param fieldId @NeverNull
     * @return null if field or value does not exist; a XID value if set
     * @throws ClassCastException if value exists but is not a {@link XID}
     * @throws IllegalArgumentException if given readableObject is null
     */
    @CanBeNull
    public static XID getID(@NeverNull XReadableObject readableObject, @NeverNull XID fieldId)
            throws ClassCastException, IllegalArgumentException {
        XValue v = _getValue(readableObject, fieldId);
        if(v == null)
            return null;
        if(v instanceof XID) {
            XID specificV = (XID)v;
            return specificV;
        } else {
            throw new ClassCastException("XValue is a " + v.getClass() + " require an XID");
        }
    }
    
    /**
     * @param readableObject @NeverNull
     * @param fieldId @NeverNull
     * @return null if field or value does not exist; a Integer value if set
     * @throws ClassCastException if value exists but is not a
     *             {@link XIntegerValue}
     * @throws IllegalArgumentException if given readableObject is null
     */
    @CanBeNull
    public static Integer getInteger(@NeverNull XReadableObject readableObject,
            @NeverNull XID fieldId) throws ClassCastException, IllegalArgumentException {
        XValue v = _getValue(readableObject, fieldId);
        if(v == null)
            return null;
        if(v instanceof XIntegerValue) {
            XIntegerValue specificV = (XIntegerValue)v;
            return specificV.getValue();
        } else {
            throw new ClassCastException("XValue is a " + v.getClass()
                    + " require an XIntegerValue");
        }
    }
    
    /**
     * @param readableObject @NeverNull
     * @param fieldId @NeverNull
     * @return null if field or value does not exist; a Long value if set
     * @throws ClassCastException if value exists but is not a
     *             {@link XLongValue}
     * @throws IllegalArgumentException if given readableObject is null
     */
    @CanBeNull
    public static Long getLong(@NeverNull XReadableObject readableObject, @NeverNull XID fieldId)
            throws ClassCastException, IllegalArgumentException {
        XValue v = _getValue(readableObject, fieldId);
        if(v == null)
            return null;
        if(v instanceof XLongValue) {
            XLongValue specificV = (XLongValue)v;
            return specificV.getValue();
        } else {
            throw new ClassCastException("XValue is a " + v.getClass() + " require an XLongValue");
        }
    }
    
    /**
     * @param readableObject @NeverNull
     * @param fieldId @NeverNull
     * @return null if field or value does not exist; a String value if set
     * @throws ClassCastException if value exists but is not a
     *             {@link XStringValue}
     * @throws IllegalArgumentException if given readableObject is null
     */
    @CanBeNull
    public static String getString(@NeverNull XReadableObject readableObject, @NeverNull XID fieldId)
            throws ClassCastException, IllegalArgumentException {
        XValue v = _getValue(readableObject, fieldId);
        if(v == null)
            return null;
        if(v instanceof XStringValue) {
            XStringValue specificV = (XStringValue)v;
            return specificV.getValue();
        } else {
            throw new ClassCastException("XValue is a " + v.getClass() + " require an XStringValue");
        }
    }
    
    /**
     * Sets the value of given field in given object to desired value. Creates
     * the field if required.
     * 
     * @param writableObject @NeverNull
     * @param fieldId @NeverNull
     * @param value @CanBeNull
     * @throws IllegalArgumentException if given writableObject is null
     */
    public static void setAddress(@NeverNull XWritableObject writableObject,
            @NeverNull XID fieldId, @CanBeNull XAddress value) throws IllegalArgumentException {
        XWritableField f = _getOrCreateField(writableObject, fieldId);
        f.setValue(value);
    }
    
    /**
     * Sets the value of given field in given object to desired value. Creates
     * the field if required.
     * 
     * @param writableObject @NeverNull
     * @param fieldId @NeverNull
     * @param value @CanBeNull
     * @throws IllegalArgumentException if given writableObject is null
     */
    public static void setBinary(@NeverNull XWritableObject writableObject, @NeverNull XID fieldId,
            @CanBeNull byte[] value) throws IllegalArgumentException {
        XWritableField f = _getOrCreateField(writableObject, fieldId);
        @CanBeNull
        XBinaryValue v = XV.toValue(value);
        f.setValue(v);
    }
    
    /**
     * Sets the value of given field in given object to desired value. Creates
     * the field if required.
     * 
     * @param writableObject @NeverNull
     * @param fieldId @NeverNull
     * @param value @CanBeNull
     * @throws IllegalArgumentException if given writableObject is null
     */
    public static void setBoolean(@NeverNull XWritableObject writableObject,
            @NeverNull XID fieldId, @CanBeNull boolean value) throws IllegalArgumentException {
        XWritableField f = _getOrCreateField(writableObject, fieldId);
        @CanBeNull
        XBooleanValue v = XV.toValue(value);
        f.setValue(v);
    }
    
    /**
     * Sets the value of given field in given object to desired value. Creates
     * the field if required.
     * 
     * @param writableObject @NeverNull
     * @param fieldId @NeverNull
     * @param value @CanBeNull
     * @throws IllegalArgumentException if given writableObject is null
     */
    public static void setDouble(@NeverNull XWritableObject writableObject, @NeverNull XID fieldId,
            @CanBeNull double value) throws IllegalArgumentException {
        XWritableField f = _getOrCreateField(writableObject, fieldId);
        @CanBeNull
        XDoubleValue v = XV.toValue(value);
        f.setValue(v);
    }
    
    /**
     * Sets the value of given field in given object to desired value. Creates
     * the field if required.
     * 
     * @param writableObject @NeverNull
     * @param fieldId @NeverNull
     * @param value @CanBeNull
     * @throws IllegalArgumentException if given writableObject is null
     */
    public static void setID(@NeverNull XWritableObject writableObject, @NeverNull XID fieldId,
            @CanBeNull XID value) throws IllegalArgumentException {
        XWritableField f = _getOrCreateField(writableObject, fieldId);
        f.setValue(value);
    }
    
    /**
     * Sets the value of given field in given object to desired value. Creates
     * the field if required.
     * 
     * @param writableObject @NeverNull
     * @param fieldId @NeverNull
     * @param value @CanBeNull
     * @throws IllegalArgumentException if given writableObject is null
     */
    public static void setInteger(@NeverNull XWritableObject writableObject,
            @NeverNull XID fieldId, @CanBeNull int value) throws IllegalArgumentException {
        XWritableField f = _getOrCreateField(writableObject, fieldId);
        @CanBeNull
        XIntegerValue v = XV.toValue(value);
        f.setValue(v);
    }
    
    /**
     * Sets the value of given field in given object to desired value. Creates
     * the field if required.
     * 
     * @param writableObject @NeverNull
     * @param fieldId @NeverNull
     * @param value @CanBeNull
     * @throws IllegalArgumentException if given writableObject is null
     */
    public static void setLong(@NeverNull XWritableObject writableObject, @NeverNull XID fieldId,
            @CanBeNull String value) throws IllegalArgumentException {
        XWritableField f = _getOrCreateField(writableObject, fieldId);
        @CanBeNull
        XStringValue v = XV.toValue(value);
        f.setValue(v);
    }
    
    /**
     * Sets the value of given field in given object to desired value. Creates
     * the field if required.
     * 
     * @param writableObject @NeverNull
     * @param fieldId @NeverNull
     * @param value @CanBeNull
     * @throws IllegalArgumentException if given writableObject is null
     */
    public static void setString(@NeverNull XWritableObject writableObject, @NeverNull XID fieldId,
            @CanBeNull String value) throws IllegalArgumentException {
        XWritableField f = _getOrCreateField(writableObject, fieldId);
        @CanBeNull
        XStringValue v = XV.toValue(value);
        f.setValue(v);
    }
    
    /**
     * @param readableObject @NeverNull
     * @param fieldId @NeverNull
     * @return null if field or value does not exist; a String value if set
     * @throws ClassCastException if value exists but is not a
     *             {@link XStringListValue}
     * @throws IllegalArgumentException if given readableObject is null
     */
    @CanBeNull
    public static List<String> getStringList(@NeverNull XReadableObject readableObject,
            @NeverNull XID fieldId) throws ClassCastException, IllegalArgumentException {
        XValue v = _getValue(readableObject, fieldId);
        if(v == null)
            return null;
        if(v instanceof XStringListValue) {
            XStringListValue specificV = (XStringListValue)v;
            String[] array = specificV.contents();
            return Arrays.asList(array);
        } else {
            throw new ClassCastException("XValue is a " + v.getClass()
                    + " require an XStringListValue");
        }
    }
    
    /**
     * Sets the value of given field in given object to desired value. Creates
     * the field if required.
     * 
     * @param writableObject @NeverNull
     * @param fieldId @NeverNull
     * @param value @CanBeNull
     * @throws IllegalArgumentException if given writableObject is null
     */
    public static void setStringList(@NeverNull XWritableObject writableObject,
            @NeverNull XID fieldId, @CanBeNull List<String> value) throws IllegalArgumentException {
        XWritableField f = _getOrCreateField(writableObject, fieldId);
        @CanBeNull
        XStringListValue v = XV.toStringListValue(value);
        f.setValue(v);
    }
    
    /**
     * @param readableObject @NeverNull
     * @param fieldId @NeverNull
     * @return null if field or value does not exist; a String value if set
     * @throws ClassCastException if value exists but is not a
     *             {@link XBooleanListValue}
     * @throws IllegalArgumentException if given readableObject is null
     */
    @CanBeNull
    public static boolean[] getBooleanArray(@NeverNull XReadableObject readableObject,
            @NeverNull XID fieldId) throws ClassCastException, IllegalArgumentException {
        XValue v = _getValue(readableObject, fieldId);
        if(v == null)
            return null;
        if(v instanceof XBooleanListValue) {
            XBooleanListValue specificV = (XBooleanListValue)v;
            boolean[] array = specificV.contents();
            return array;
        } else {
            throw new ClassCastException("XValue is a " + v.getClass()
                    + " require an XBooleanListValue");
        }
    }
    
    /**
     * Sets the value of given field in given object to desired value. Creates
     * the field if required.
     * 
     * @param writableObject @NeverNull
     * @param fieldId @NeverNull
     * @param value @CanBeNull
     * @throws IllegalArgumentException if given writableObject is null
     */
    public static void setBooleanCollection(@NeverNull XWritableObject writableObject,
            @NeverNull XID fieldId, @CanBeNull Collection<Boolean> value)
            throws IllegalArgumentException {
        XWritableField f = _getOrCreateField(writableObject, fieldId);
        @CanBeNull
        XBooleanListValue v = XV.toBooleanListValue(value);
        f.setValue(v);
    }
    
    /**
     * @param readableObject @NeverNull
     * @param fieldId @NeverNull
     * @return null if field or value does not exist; a String value if set
     * @throws ClassCastException if value exists but is not a
     *             {@link XDoubleListValue}
     * @throws IllegalArgumentException if given readableObject is null
     */
    @CanBeNull
    public static double[] getDoubleArray(@NeverNull XReadableObject readableObject,
            @NeverNull XID fieldId) throws ClassCastException, IllegalArgumentException {
        XValue v = _getValue(readableObject, fieldId);
        if(v == null)
            return null;
        if(v instanceof XDoubleListValue) {
            XDoubleListValue specificV = (XDoubleListValue)v;
            double[] array = specificV.contents();
            return array;
        } else {
            throw new ClassCastException("XValue is a " + v.getClass()
                    + " require an XDoubleListValue");
        }
    }
    
    /**
     * Sets the value of given field in given object to desired value. Creates
     * the field if required.
     * 
     * @param writableObject @NeverNull
     * @param fieldId @NeverNull
     * @param value @CanBeNull
     * @throws IllegalArgumentException if given writableObject is null
     */
    public static void setDoubleCollection(@NeverNull XWritableObject writableObject,
            @NeverNull XID fieldId, @CanBeNull Collection<Double> value)
            throws IllegalArgumentException {
        XWritableField f = _getOrCreateField(writableObject, fieldId);
        @CanBeNull
        XDoubleListValue v = XV.toDoubleListValue(value);
        f.setValue(v);
    }
    
    /**
     * @param readableObject @NeverNull
     * @param fieldId @NeverNull
     * @return null if field or value does not exist; a String value if set
     * @throws ClassCastException if value exists but is not a
     *             {@link XIntegerListValue}
     * @throws IllegalArgumentException if given readableObject is null
     */
    @CanBeNull
    public static int[] getIntegerArray(@NeverNull XReadableObject readableObject,
            @NeverNull XID fieldId) throws ClassCastException, IllegalArgumentException {
        XValue v = _getValue(readableObject, fieldId);
        if(v == null)
            return null;
        if(v instanceof XIntegerListValue) {
            XIntegerListValue specificV = (XIntegerListValue)v;
            int[] array = specificV.contents();
            return array;
        } else {
            throw new ClassCastException("XValue is a " + v.getClass()
                    + " require an XIntegerListValue");
        }
    }
    
    /**
     * Sets the value of given field in given object to desired value. Creates
     * the field if required.
     * 
     * @param writableObject @NeverNull
     * @param fieldId @NeverNull
     * @param value @CanBeNull
     * @throws IllegalArgumentException if given writableObject is null
     */
    public static void setIntegerCollection(@NeverNull XWritableObject writableObject,
            @NeverNull XID fieldId, @CanBeNull Collection<Integer> value)
            throws IllegalArgumentException {
        XWritableField f = _getOrCreateField(writableObject, fieldId);
        @CanBeNull
        XIntegerListValue v = XV.toIntegerListValue(value);
        f.setValue(v);
    }
    
    /**
     * @param readableObject @NeverNull
     * @param fieldId @NeverNull
     * @return null if field or value does not exist; a String value if set
     * @throws ClassCastException if value exists but is not a
     *             {@link XLongListValue}
     * @throws IllegalArgumentException if given readableObject is null
     */
    @CanBeNull
    public static long[] getLongArray(@NeverNull XReadableObject readableObject,
            @NeverNull XID fieldId) throws ClassCastException, IllegalArgumentException {
        XValue v = _getValue(readableObject, fieldId);
        if(v == null)
            return null;
        if(v instanceof XLongListValue) {
            XLongListValue specificV = (XLongListValue)v;
            long[] array = specificV.contents();
            return array;
        } else {
            throw new ClassCastException("XValue is a " + v.getClass()
                    + " require an XLongListValue");
        }
    }
    
    /**
     * Sets the value of given field in given object to desired value. Creates
     * the field if required.
     * 
     * @param writableObject @NeverNull
     * @param fieldId @NeverNull
     * @param value @CanBeNull
     * @throws IllegalArgumentException if given writableObject is null
     */
    public static void setLongCollection(@NeverNull XWritableObject writableObject,
            @NeverNull XID fieldId, @CanBeNull Collection<Long> value)
            throws IllegalArgumentException {
        XWritableField f = _getOrCreateField(writableObject, fieldId);
        @CanBeNull
        XLongListValue v = XV.toLongListValue(value);
        f.setValue(v);
    }
    
    /**
     * @param readableObject @NeverNull
     * @param fieldId @NeverNull
     * @return null if field or value does not exist; a ID value if set
     * @throws ClassCastException if value exists but is not a
     *             {@link XIDListValue}
     * @throws IllegalArgumentException if given readableObject is null
     */
    @CanBeNull
    public static List<XID> getIDList(@NeverNull XReadableObject readableObject,
            @NeverNull XID fieldId) throws ClassCastException, IllegalArgumentException {
        XValue v = _getValue(readableObject, fieldId);
        if(v == null)
            return null;
        if(v instanceof XIDListValue) {
            XIDListValue specificV = (XIDListValue)v;
            XID[] array = specificV.contents();
            return Arrays.asList(array);
        } else {
            throw new ClassCastException("XValue is a " + v.getClass() + " require an XIDListValue");
        }
    }
    
    /**
     * Sets the value of given field in given object to desired value. Creates
     * the field if required.
     * 
     * @param writableObject @NeverNull
     * @param fieldId @NeverNull
     * @param value @CanBeNull
     * @throws IllegalArgumentException if given writableObject is null
     */
    public static void setIDList(@NeverNull XWritableObject writableObject, @NeverNull XID fieldId,
            @CanBeNull List<XID> value) throws IllegalArgumentException {
        XWritableField f = _getOrCreateField(writableObject, fieldId);
        @CanBeNull
        XIDListValue v = XV.toIDListValue(value);
        f.setValue(v);
    }
    
    /**
     * @param readableObject @NeverNull
     * @param fieldId @NeverNull
     * @return null if field or value does not exist; a Address value if set
     * @throws ClassCastException if value exists but is not a
     *             {@link XAddressListValue}
     * @throws IllegalArgumentException if given readableObject is null
     */
    @CanBeNull
    public static List<XAddress> getAddressList(@NeverNull XReadableObject readableObject,
            @NeverNull XID fieldId) throws ClassCastException, IllegalArgumentException {
        XValue v = _getValue(readableObject, fieldId);
        if(v == null)
            return null;
        if(v instanceof XAddressListValue) {
            XAddressListValue specificV = (XAddressListValue)v;
            XAddress[] array = specificV.contents();
            return Arrays.asList(array);
        } else {
            throw new ClassCastException("XValue is a " + v.getClass()
                    + " require an XAddressListValue");
        }
    }
    
    /**
     * Sets the value of given field in given object to desired value. Creates
     * the field if required.
     * 
     * @param writableObject @NeverNull
     * @param fieldId @NeverNull
     * @param value @CanBeNull
     * @throws IllegalArgumentException if given writableObject is null
     */
    public static void setAddressList(@NeverNull XWritableObject writableObject,
            @NeverNull XID fieldId, @CanBeNull List<XAddress> value)
            throws IllegalArgumentException {
        XWritableField f = _getOrCreateField(writableObject, fieldId);
        @CanBeNull
        XAddressListValue v = XV.toAddressListValue(value);
        f.setValue(v);
    }
    
    /**
     * @param readableObject @NeverNull
     * @param fieldId @NeverNull
     * @return null if field or value does not exist; a ID value if set
     * @throws ClassCastException if value exists but is not a
     *             {@link XIDSetValue}
     * @throws IllegalArgumentException if given readableObject is null
     */
    @CanBeNull
    public static Set<XID> getIDSet(@NeverNull XReadableObject readableObject,
            @NeverNull XID fieldId) throws ClassCastException, IllegalArgumentException {
        XValue v = _getValue(readableObject, fieldId);
        if(v == null)
            return null;
        if(v instanceof XIDSetValue) {
            XIDSetValue specificV = (XIDSetValue)v;
            Set<XID> set = specificV.toSet();
            return set;
        } else {
            throw new ClassCastException("XValue is a " + v.getClass() + " require an XIDSetValue");
        }
    }
    
    /**
     * Sets the value of given field in given object to desired value. Creates
     * the field if required.
     * 
     * @param writableObject @NeverNull
     * @param fieldId @NeverNull
     * @param value @CanBeNull
     * @throws IllegalArgumentException if given writableObject is null
     */
    public static void setIDSet(@NeverNull XWritableObject writableObject, @NeverNull XID fieldId,
            @CanBeNull Set<XID> value) throws IllegalArgumentException {
        XWritableField f = _getOrCreateField(writableObject, fieldId);
        @CanBeNull
        XIDSetValue v = XV.toIDSetValue(value);
        f.setValue(v);
    }
    
    /**
     * @param readableObject @NeverNull
     * @param fieldId @NeverNull
     * @return null if field or value does not exist; a ID value if set
     * @throws ClassCastException if value exists but is not a
     *             {@link XAddressSetValue}
     * @throws IllegalArgumentException if given readableObject is null
     */
    @CanBeNull
    public static Set<XAddress> getAddressSet(@NeverNull XReadableObject readableObject,
            @NeverNull XID fieldId) throws ClassCastException, IllegalArgumentException {
        XValue v = _getValue(readableObject, fieldId);
        if(v == null)
            return null;
        if(v instanceof XAddressSetValue) {
            XAddressSetValue specificV = (XAddressSetValue)v;
            Set<XAddress> set = specificV.toSet();
            return set;
        } else {
            throw new ClassCastException("XValue is a " + v.getClass()
                    + " require an XAddressSetValue");
        }
    }
    
    /**
     * Sets the value of given field in given object to desired value. Creates
     * the field if required.
     * 
     * @param writableObject @NeverNull
     * @param fieldId @NeverNull
     * @param value @CanBeNull
     * @throws IllegalArgumentException if given writableObject is null
     */
    public static void setAddressSet(@NeverNull XWritableObject writableObject,
            @NeverNull XID fieldId, @CanBeNull Set<XAddress> value) throws IllegalArgumentException {
        XWritableField f = _getOrCreateField(writableObject, fieldId);
        @CanBeNull
        XAddressSetValue v = XV.toAddressSetValue(value);
        f.setValue(v);
    }
    
    /**
     * @param readableObject @NeverNull
     * @param fieldId @NeverNull
     * @return null if field or value does not exist; a ID value if set
     * @throws ClassCastException if value exists but is not a
     *             {@link XStringSetValue}
     * @throws IllegalArgumentException if given readableObject is null
     */
    @CanBeNull
    public static Set<String> getStringSet(@NeverNull XReadableObject readableObject,
            @NeverNull XID fieldId) throws ClassCastException, IllegalArgumentException {
        XValue v = _getValue(readableObject, fieldId);
        if(v == null)
            return null;
        if(v instanceof XStringSetValue) {
            XStringSetValue specificV = (XStringSetValue)v;
            Set<String> set = specificV.toSet();
            return set;
        } else {
            throw new ClassCastException("XValue is a " + v.getClass()
                    + " require an XStringSetValue");
        }
    }
    
    /**
     * Sets the value of given field in given object to desired value. Creates
     * the field if required.
     * 
     * @param writableObject @NeverNull
     * @param fieldId @NeverNull
     * @param value @CanBeNull
     * @throws IllegalArgumentException if given writableObject is null
     */
    public static void setStringSet(@NeverNull XWritableObject writableObject,
            @NeverNull XID fieldId, @CanBeNull Set<String> value) throws IllegalArgumentException {
        XWritableField f = _getOrCreateField(writableObject, fieldId);
        @CanBeNull
        XStringSetValue v = XV.toStringSetValue(value);
        f.setValue(v);
    }
    
    /**
     * @param readableObject @NeverNull
     * @param fieldId @NeverNull
     * @return null if field or value does not exist; a ID value if set
     * @throws ClassCastException if value exists but is not a
     *             {@link XIDSortedSetValue}
     * @throws IllegalArgumentException if given readableObject is null
     */
    @CanBeNull
    public static SortedSet<XID> getIDSortedSet(@NeverNull XReadableObject readableObject,
            @NeverNull XID fieldId) throws ClassCastException, IllegalArgumentException {
        XValue v = _getValue(readableObject, fieldId);
        if(v == null)
            return null;
        if(v instanceof XIDSortedSetValue) {
            XIDSortedSetValue specificV = (XIDSortedSetValue)v;
            SortedSet<XID> set = specificV.toSortedSet();
            return set;
        } else {
            throw new ClassCastException("XValue is a " + v.getClass()
                    + " require an XIDSortedSetValue");
        }
    }
    
    /**
     * Sets the value of given field in given object to desired value. Creates
     * the field if required.
     * 
     * @param writableObject @NeverNull
     * @param fieldId @NeverNull
     * @param value @CanBeNull
     * @throws IllegalArgumentException if given writableObject is null
     */
    public static void setIDSortedSet(@NeverNull XWritableObject writableObject,
            @NeverNull XID fieldId, @CanBeNull SortedSet<XID> value)
            throws IllegalArgumentException {
        XWritableField f = _getOrCreateField(writableObject, fieldId);
        @CanBeNull
        XIDSortedSetValue v = XV.toIDSortedSetValue(value);
        f.setValue(v);
    }
    
    /**
     * @param readableObject @NeverNull
     * @param fieldId @NeverNull
     * @return null if field or value does not exist; a Address value if set
     * @throws ClassCastException if value exists but is not a
     *             {@link XAddressSortedSetValue}
     * @throws IllegalArgumentException if given readableObject is null
     */
    @CanBeNull
    public static SortedSet<XAddress> getAddressSortedSet(
            @NeverNull XReadableObject readableObject, @NeverNull XID fieldId)
            throws ClassCastException, IllegalArgumentException {
        XValue v = _getValue(readableObject, fieldId);
        if(v == null)
            return null;
        if(v instanceof XAddressSortedSetValue) {
            XAddressSortedSetValue specificV = (XAddressSortedSetValue)v;
            SortedSet<XAddress> set = specificV.toSortedSet();
            return set;
        } else {
            throw new ClassCastException("XValue is a " + v.getClass()
                    + " require an XAddressSortedSetValue");
        }
    }
    
    /**
     * Sets the value of given field in given object to desired value. Creates
     * the field if required.
     * 
     * @param writableObject @NeverNull
     * @param fieldId @NeverNull
     * @param value @CanBeNull
     * @throws IllegalArgumentException if given writableObject is null
     */
    public static void setAddressSortedSet(@NeverNull XWritableObject writableObject,
            @NeverNull XID fieldId, @CanBeNull SortedSet<XAddress> value)
            throws IllegalArgumentException {
        XWritableField f = _getOrCreateField(writableObject, fieldId);
        @CanBeNull
        XAddressSortedSetValue v = XV.toAddressSortedSetValue(value);
        f.setValue(v);
    }
    
}
