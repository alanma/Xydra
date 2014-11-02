package org.xydra.base.value;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.xydra.annotations.CanBeNull;
import org.xydra.annotations.NeverNull;
import org.xydra.annotations.RunsInGWT;
import org.xydra.base.BaseRuntime;
import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.rmof.XReadableField;
import org.xydra.base.rmof.XReadableObject;
import org.xydra.base.rmof.XWritableField;
import org.xydra.base.rmof.XWritableObject;

/**
 * Helper class to use a {@link XWritableObject} more like a Java class with
 * typed fields.
 * 
 * For each ValueType there are methods toT and fromT with T = ValueType.name().
 * They convert to the respective Java object types (e.g. Boolean to
 * XBooleanValue). For the primitive types, use the corresponding Java primitive
 * name, e.g. 'toint' to convert from XIntgerValue to int.
 * 
 * @author xamde
 */
@RunsInGWT(true)
public class XValueJavaUtils {

	/**
	 * @param writableObject
	 * @param fieldId
	 * @return
	 * @throws IllegalArgumentException
	 *             if fieldId or writableObject are null
	 */
	private static XWritableField _getOrCreateField(XWritableObject writableObject, XId fieldId)
			throws IllegalArgumentException {
		if (writableObject == null)
			throw new IllegalArgumentException("writableObject is null");
		if (fieldId == null)
			throw new IllegalArgumentException("fieldId is null");
		XWritableField f = writableObject.getField(fieldId);
		if (f == null)
			f = writableObject.createField(fieldId);
		return f;
	}

	/**
	 * @param readableObject
	 * @param fieldId
	 * @return null if field or value does not exist; a {@link XValue} if set
	 * @throws IllegalArgumentException
	 *             if given readableObject is null
	 */
	private static XValue _getValue(@NeverNull XReadableObject readableObject,
			@NeverNull XId fieldId) {
		if (readableObject == null)
			throw new IllegalArgumentException("readableObject is null");
		if (fieldId == null)
			throw new IllegalArgumentException("fieldId is null");
		XReadableField f = readableObject.getField(fieldId);
		if (f == null)
			return null;
		XValue v = f.getValue();
		return v;
	}

	public static List<Boolean> asList(boolean[] contents) {
		ArrayList<Boolean> list = new ArrayList<Boolean>(contents.length);
		for (int i = 0; i < contents.length; i++) {
			list.add(contents[i]);
		}
		return list;
	}

	public static List<Byte> asList(byte[] contents) {
		ArrayList<Byte> list = new ArrayList<Byte>(contents.length);
		for (int i = 0; i < contents.length; i++) {
			list.add(contents[i]);
		}
		return list;
	}

	public static List<Character> asList(char[] contents) {
		ArrayList<Character> list = new ArrayList<Character>(contents.length);
		for (int i = 0; i < contents.length; i++) {
			list.add(contents[i]);
		}
		return list;
	}

	public static List<Double> asList(double[] contents) {
		ArrayList<Double> list = new ArrayList<Double>(contents.length);
		for (int i = 0; i < contents.length; i++) {
			list.add(contents[i]);
		}
		return list;
	}

	public static List<Float> asList(float[] contents) {
		ArrayList<Float> list = new ArrayList<Float>(contents.length);
		for (int i = 0; i < contents.length; i++) {
			list.add(contents[i]);
		}
		return list;
	}

	public static List<Integer> asList(int[] contents) {
		ArrayList<Integer> list = new ArrayList<Integer>(contents.length);
		for (int i = 0; i < contents.length; i++) {
			list.add(contents[i]);
		}
		return list;
	}

	public static List<Long> asList(long[] contents) {
		ArrayList<Long> list = new ArrayList<Long>(contents.length);
		for (int i = 0; i < contents.length; i++) {
			list.add(contents[i]);
		}
		return list;
	}

	public static List<Short> asList(short[] contents) {
		ArrayList<Short> list = new ArrayList<Short>(contents.length);
		for (int i = 0; i < contents.length; i++) {
			list.add(contents[i]);
		}
		return list;
	}

	public static <T> List<T> asList(T[] contents) {
		return Arrays.asList(contents);
	}

	public static <T> Set<T> asSet(T[] contents) {
		return new HashSet<T>(asList(contents));
	}

	public static <T> SortedSet<T> asSortedSet(T[] contents) {
		return new TreeSet<T>(asList(contents));
	}

	// Address
	public static XAddress fromAddress(XAddress xydraValue) {
		return xydraValue;
	}

	// AddressList
	public static List<XAddress> fromAddressList(XAddressListValue xydraValue) {
		if (xydraValue == null) {
			return null;
		}
		return asList(xydraValue.contents());
	}

	// AddressSet
	public static Set<XAddress> fromAddressSet(XAddressSetValue xydraValue) {
		if (xydraValue == null) {
			return null;
		}
		return asSet(xydraValue.contents());
	}

	// AddressSortedSet
	public static SortedSet<XAddress> fromAddressSortedSet(XAddressSortedSetValue xydraValue) {
		if (xydraValue == null) {
			return null;
		}
		return asSortedSet(xydraValue.contents());
	}

	// Binary
	public static byte[] fromBinary(XBinaryValue xydraValue) {
		if (xydraValue == null) {
			return null;
		}
		return xydraValue.getValue();
	}

	// boolean
	public static boolean fromboolean(XBooleanValue xydraValue) {
		if (xydraValue == null) {
			return false;
		}
		return xydraValue.getValue();
	}

	// Boolean
	public static Boolean fromBoolean(XBooleanValue xydraValue) {
		if (xydraValue == null) {
			return null;
		}
		return xydraValue.getValue();
	}

	// BooleanList
	public static List<Boolean> fromBooleanList(XBooleanListValue xydraValue) {
		if (xydraValue == null) {
			return null;
		}
		return asList(xydraValue.contents());
	}

	// double
	public static double fromdouble(XDoubleValue xydraValue) {
		if (xydraValue == null) {
			return 0d;
		}
		return xydraValue.getValue();
	}

	// Double
	public static Double fromDouble(XDoubleValue xydraValue) {
		if (xydraValue == null) {
			return null;
		}
		return xydraValue.getValue();
	}

	// DoubleList
	public static List<Double> fromDoubleList(XDoubleListValue xydraValue) {
		if (xydraValue == null) {
			return null;
		}
		return asList(xydraValue.contents());
	}

	// Id
	public static XId fromId(XId xydraValue) {
		return xydraValue;
	}

	// IdList
	public static List<XId> fromIdList(XIdListValue xydraValue) {
		if (xydraValue == null) {
			return null;
		}
		return asList(xydraValue.contents());
	}

	// IdSet
	public static Set<XId> fromIdSet(XIdSetValue xydraValue) {
		if (xydraValue == null) {
			return null;
		}
		return asSet(xydraValue.contents());
	}

	// IdSortedSet
	public static SortedSet<XId> fromIdSortedSet(XIdSortedSetValue xydraValue) {
		if (xydraValue == null) {
			return null;
		}
		return asSortedSet(xydraValue.contents());
	}

	// int
	public static int fromint(XIntegerValue xydraValue) {
		if (xydraValue == null) {
			return 0;
		}
		return xydraValue.getValue();
	}

	// Integer
	public static Integer fromInteger(XIntegerValue xydraValue) {
		if (xydraValue == null) {
			return null;
		}
		return xydraValue.getValue();
	}

	// IntegerList
	public static List<Integer> fromIntegerList(XIntegerListValue xydraValue) {
		if (xydraValue == null) {
			return null;
		}
		return asList(xydraValue.contents());
	}

	// long
	public static long fromlong(XLongValue xydraValue) {
		if (xydraValue == null) {
			return 0l;
		}
		return xydraValue.getValue();
	}

	// Long
	public static Long fromLong(XLongValue xydraValue) {
		if (xydraValue == null) {
			return null;
		}
		return xydraValue.getValue();
	}

	// LongList
	public static List<Long> fromLongList(XLongListValue xydraValue) {
		if (xydraValue == null) {
			return null;
		}
		return asList(xydraValue.contents());
	}

	// String
	public static String fromString(XStringValue xydraValue) {
		if (xydraValue == null) {
			return null;
		}
		return xydraValue.getValue();
	}

	// StringList
	public static List<String> fromStringList(XStringListValue xydraValue) {
		if (xydraValue == null) {
			return null;
		}
		return asList(xydraValue.contents());
	}

	// StringSet
	public static Set<String> fromStringSet(XStringSetValue xydraValue) {
		if (xydraValue == null) {
			return null;
		}
		return asSet(xydraValue.contents());
	}

	/**
	 * @param readableObject
	 * @NeverNull
	 * @param fieldId
	 * @NeverNull
	 * @return null if field or value does not exist; a XAddress value if set
	 * @throws ClassCastException
	 *             if value exists but is not a {@link XAddress}
	 * @throws IllegalArgumentException
	 *             if given readableObject is null
	 */
	@CanBeNull
	public static XAddress getAddress(@NeverNull XReadableObject readableObject,
			@NeverNull XId fieldId) throws ClassCastException, IllegalArgumentException {
		XValue v = _getValue(readableObject, fieldId);
		if (v == null)
			return null;
		if (v instanceof XAddress) {
			XAddress specificV = (XAddress) v;
			return specificV;
		} else {
			throw new ClassCastException("XValue is a " + v.getClass() + " require an XAddress");
		}
	}

	public static XAddress[] getAddressArray(XWritableObject readableObject, XId fieldId) {
		List<XAddress> list = getAddressList(readableObject, fieldId);
		if (list == null)
			return null;
		return list.toArray(new XAddress[list.size()]);
	}

	/**
	 * @param readableObject
	 * @NeverNull
	 * @param fieldId
	 * @NeverNull
	 * @return null if field or value does not exist; a Address value if set
	 * @throws ClassCastException
	 *             if value exists but is not a {@link XAddressListValue}
	 * @throws IllegalArgumentException
	 *             if given readableObject is null
	 */
	@CanBeNull
	public static List<XAddress> getAddressList(@NeverNull XReadableObject readableObject,
			@NeverNull XId fieldId) throws ClassCastException, IllegalArgumentException {
		XValue v = _getValue(readableObject, fieldId);
		if (v == null)
			return null;
		if (v instanceof XAddressListValue) {
			XAddressListValue specificV = (XAddressListValue) v;
			XAddress[] array = specificV.contents();
			return Arrays.asList(array);
		} else {
			throw new ClassCastException("XValue is a " + v.getClass()
					+ " require an XAddressListValue");
		}
	}

	/**
	 * @param readableObject
	 * @NeverNull
	 * @param fieldId
	 * @NeverNull
	 * @return null if field or value does not exist; a ID value if set
	 * @throws ClassCastException
	 *             if value exists but is not a {@link XAddressSetValue}
	 * @throws IllegalArgumentException
	 *             if given readableObject is null
	 */
	@CanBeNull
	public static Set<XAddress> getAddressSet(@NeverNull XReadableObject readableObject,
			@NeverNull XId fieldId) throws ClassCastException, IllegalArgumentException {
		XValue v = _getValue(readableObject, fieldId);
		if (v == null)
			return null;
		if (v instanceof XAddressSetValue) {
			XAddressSetValue specificV = (XAddressSetValue) v;
			Set<XAddress> set = specificV.toSet();
			return set;
		} else {
			throw new ClassCastException("XValue is a " + v.getClass()
					+ " require an XAddressSetValue");
		}
	}

	/**
	 * @param readableObject
	 * @NeverNull
	 * @param fieldId
	 * @NeverNull
	 * @return null if field or value does not exist; a Address value if set
	 * @throws ClassCastException
	 *             if value exists but is not a {@link XAddressSortedSetValue}
	 * @throws IllegalArgumentException
	 *             if given readableObject is null
	 */
	@CanBeNull
	public static SortedSet<XAddress> getAddressSortedSet(
			@NeverNull XReadableObject readableObject, @NeverNull XId fieldId)
			throws ClassCastException, IllegalArgumentException {
		XValue v = _getValue(readableObject, fieldId);
		if (v == null)
			return null;
		if (v instanceof XAddressSortedSetValue) {
			XAddressSortedSetValue specificV = (XAddressSortedSetValue) v;
			SortedSet<XAddress> set = specificV.toSortedSet();
			return set;
		} else {
			throw new ClassCastException("XValue is a " + v.getClass()
					+ " require an XAddressSortedSetValue");
		}
	}

	/**
	 * @param readableObject
	 * @NeverNull
	 * @param fieldId
	 * @NeverNull
	 * @return null if field or value does not exist; a byte[] value if set
	 * @throws ClassCastException
	 *             if value exists but is not a {@link XBinaryValue}
	 * @throws IllegalArgumentException
	 *             if given readableObject is null
	 */
	@CanBeNull
	public static byte[] getBinary(@NeverNull XReadableObject readableObject, @NeverNull XId fieldId)
			throws ClassCastException, IllegalArgumentException {
		XValue v = _getValue(readableObject, fieldId);
		if (v == null)
			return null;
		if (v instanceof XBinaryValue) {
			XBinaryValue specificV = (XBinaryValue) v;
			return specificV.contents();
		} else {
			throw new ClassCastException("XValue is a " + v.getClass() + " require an XBinaryValue");
		}
	}

	/**
	 * @param readableObject
	 * @NeverNull
	 * @param fieldId
	 * @NeverNull
	 * @return null if field or value does not exist; a Boolean value if set
	 * @throws ClassCastException
	 *             if value exists but is not a {@link XBooleanValue}
	 * @throws IllegalArgumentException
	 *             if given readableObject is null
	 */
	@CanBeNull
	public static Boolean getBoolean(@NeverNull XReadableObject readableObject,
			@NeverNull XId fieldId) throws ClassCastException, IllegalArgumentException {
		XValue v = _getValue(readableObject, fieldId);
		if (v == null)
			return null;
		if (v instanceof XBooleanValue) {
			XBooleanValue specificV = (XBooleanValue) v;
			return specificV.getValue();
		} else {
			throw new ClassCastException("XValue is a " + v.getClass()
					+ " require an XBooleanValue");
		}
	}

	/**
	 * @param readableObject
	 * @NeverNull
	 * @param fieldId
	 * @NeverNull
	 * @return null if field or value does not exist; a String value if set
	 * @throws ClassCastException
	 *             if value exists but is not a {@link XBooleanListValue}
	 * @throws IllegalArgumentException
	 *             if given readableObject is null
	 */
	@CanBeNull
	public static boolean[] getBooleanArray(@NeverNull XReadableObject readableObject,
			@NeverNull XId fieldId) throws ClassCastException, IllegalArgumentException {
		XValue v = _getValue(readableObject, fieldId);
		if (v == null)
			return null;
		if (v instanceof XBooleanListValue) {
			XBooleanListValue specificV = (XBooleanListValue) v;
			boolean[] array = specificV.contents();
			return array;
		} else {
			throw new ClassCastException("XValue is a " + v.getClass()
					+ " require an XBooleanListValue");
		}
	}

	/**
	 * @param readableObject
	 * @NeverNull
	 * @param fieldId
	 * @NeverNull
	 * @return null if field or value does not exist; a Double value if set
	 * @throws ClassCastException
	 *             if value exists but is not a {@link XDoubleValue}
	 * @throws IllegalArgumentException
	 *             if given readableObject is null
	 */
	@CanBeNull
	public static Double getDouble(@NeverNull XReadableObject readableObject, @NeverNull XId fieldId)
			throws ClassCastException, IllegalArgumentException {
		XValue v = _getValue(readableObject, fieldId);
		if (v == null)
			return null;
		if (v instanceof XDoubleValue) {
			XDoubleValue specificV = (XDoubleValue) v;
			return specificV.getValue();
		} else {
			throw new ClassCastException("XValue is a " + v.getClass() + " require an XDoubleValue");
		}
	}

	/**
	 * @param readableObject
	 * @NeverNull
	 * @param fieldId
	 * @NeverNull
	 * @return null if field or value does not exist; a String value if set
	 * @throws ClassCastException
	 *             if value exists but is not a {@link XDoubleListValue}
	 * @throws IllegalArgumentException
	 *             if given readableObject is null
	 */
	@CanBeNull
	public static double[] getDoubleArray(@NeverNull XReadableObject readableObject,
			@NeverNull XId fieldId) throws ClassCastException, IllegalArgumentException {
		XValue v = _getValue(readableObject, fieldId);
		if (v == null)
			return null;
		if (v instanceof XDoubleListValue) {
			XDoubleListValue specificV = (XDoubleListValue) v;
			double[] array = specificV.contents();
			return array;
		} else {
			throw new ClassCastException("XValue is a " + v.getClass()
					+ " require an XDoubleListValue");
		}
	}

	/**
	 * @param readableObject
	 * @NeverNull
	 * @param fieldId
	 * @NeverNull
	 * @return null if field or value does not exist; a XId value if set
	 * @throws ClassCastException
	 *             if value exists but is not a {@link XId}
	 * @throws IllegalArgumentException
	 *             if given readableObject is null
	 */
	@CanBeNull
	public static XId getId(@NeverNull XReadableObject readableObject, @NeverNull XId fieldId)
			throws ClassCastException, IllegalArgumentException {
		XValue v = _getValue(readableObject, fieldId);
		if (v == null)
			return null;
		if (v instanceof XId) {
			XId specificV = (XId) v;
			return specificV;
		} else {
			throw new ClassCastException("XValue is a " + v.getClass() + " require an XId");
		}
	}

	public static XId[] getIdArray(XWritableObject readableObject, XId fieldId) {
		List<XId> list = getIdList(readableObject, fieldId);
		if (list == null)
			return null;
		return list.toArray(new XId[list.size()]);
	}

	/**
	 * @param readableObject
	 * @NeverNull
	 * @param fieldId
	 * @NeverNull
	 * @return null if field or value does not exist; a ID value if set
	 * @throws ClassCastException
	 *             if value exists but is not a {@link XIdListValue}
	 * @throws IllegalArgumentException
	 *             if given readableObject is null
	 */
	@CanBeNull
	public static List<XId> getIdList(@NeverNull XReadableObject readableObject,
			@NeverNull XId fieldId) throws ClassCastException, IllegalArgumentException {
		XValue v = _getValue(readableObject, fieldId);
		if (v == null)
			return null;
		if (v instanceof XIdListValue) {
			XIdListValue specificV = (XIdListValue) v;
			XId[] array = specificV.contents();
			return Arrays.asList(array);
		} else {
			throw new ClassCastException("XValue is a " + v.getClass() + " require an XIdListValue");
		}
	}

	/**
	 * @param readableObject
	 * @NeverNull
	 * @param fieldId
	 * @NeverNull
	 * @return null if field or value does not exist; a ID value if set
	 * @throws ClassCastException
	 *             if value exists but is not a {@link XIdSetValue}
	 * @throws IllegalArgumentException
	 *             if given readableObject is null
	 */
	@CanBeNull
	public static Set<XId> getIdSet(@NeverNull XReadableObject readableObject,
			@NeverNull XId fieldId) throws ClassCastException, IllegalArgumentException {
		XValue v = _getValue(readableObject, fieldId);
		if (v == null)
			return null;
		if (v instanceof XIdSetValue) {
			XIdSetValue specificV = (XIdSetValue) v;
			Set<XId> set = specificV.toSet();
			return set;
		} else {
			throw new ClassCastException("XValue is a " + v.getClass() + " require an XIdSetValue");
		}
	}

	/**
	 * @param readableObject
	 * @NeverNull
	 * @param fieldId
	 * @NeverNull
	 * @return null if field or value does not exist; a ID value if set
	 * @throws ClassCastException
	 *             if value exists but is not a {@link XIdSortedSetValue}
	 * @throws IllegalArgumentException
	 *             if given readableObject is null
	 */
	@CanBeNull
	public static SortedSet<XId> getIdSortedSet(@NeverNull XReadableObject readableObject,
			@NeverNull XId fieldId) throws ClassCastException, IllegalArgumentException {
		XValue v = _getValue(readableObject, fieldId);
		if (v == null)
			return null;
		if (v instanceof XIdSortedSetValue) {
			XIdSortedSetValue specificV = (XIdSortedSetValue) v;
			SortedSet<XId> set = specificV.toSortedSet();
			return set;
		} else {
			throw new ClassCastException("XValue is a " + v.getClass()
					+ " require an XIdSortedSetValue");
		}
	}

	/**
	 * @param readableObject
	 * @NeverNull
	 * @param fieldId
	 * @NeverNull
	 * @return null if field or value does not exist; a Integer value if set
	 * @throws ClassCastException
	 *             if value exists but is not a {@link XIntegerValue}
	 * @throws IllegalArgumentException
	 *             if given readableObject is null
	 */
	@CanBeNull
	public static Integer getInteger(@NeverNull XReadableObject readableObject,
			@NeverNull XId fieldId) throws ClassCastException, IllegalArgumentException {
		XValue v = _getValue(readableObject, fieldId);
		if (v == null)
			return null;
		if (v instanceof XIntegerValue) {
			XIntegerValue specificV = (XIntegerValue) v;
			return specificV.getValue();
		} else {
			throw new ClassCastException("XValue is a " + v.getClass()
					+ " require an XIntegerValue");
		}
	}

	/**
	 * @param readableObject
	 * @NeverNull
	 * @param fieldId
	 * @NeverNull
	 * @return null if field or value does not exist; a String value if set
	 * @throws ClassCastException
	 *             if value exists but is not a {@link XIntegerListValue}
	 * @throws IllegalArgumentException
	 *             if given readableObject is null
	 */
	@CanBeNull
	public static int[] getIntegerArray(@NeverNull XReadableObject readableObject,
			@NeverNull XId fieldId) throws ClassCastException, IllegalArgumentException {
		XValue v = _getValue(readableObject, fieldId);
		if (v == null)
			return null;
		if (v instanceof XIntegerListValue) {
			XIntegerListValue specificV = (XIntegerListValue) v;
			int[] array = specificV.contents();
			return array;
		} else {
			throw new ClassCastException("XValue is a " + v.getClass()
					+ " require an XIntegerListValue");
		}
	}

	/**
	 * @param readableObject
	 * @NeverNull
	 * @param fieldId
	 * @NeverNull
	 * @return null if field or value does not exist; a Long value if set
	 * @throws ClassCastException
	 *             if value exists but is not a {@link XLongValue}
	 * @throws IllegalArgumentException
	 *             if given readableObject is null
	 */
	@CanBeNull
	public static Long getLong(@NeverNull XReadableObject readableObject, @NeverNull XId fieldId)
			throws ClassCastException, IllegalArgumentException {
		XValue v = _getValue(readableObject, fieldId);
		if (v == null)
			return null;
		if (v instanceof XLongValue) {
			XLongValue specificV = (XLongValue) v;
			return specificV.getValue();
		} else {
			throw new ClassCastException("XValue is a " + v.getClass() + " require an XLongValue");
		}
	}

	/**
	 * @param readableObject
	 * @NeverNull
	 * @param fieldId
	 * @NeverNull
	 * @return null if field or value does not exist; a String value if set
	 * @throws ClassCastException
	 *             if value exists but is not a {@link XLongListValue}
	 * @throws IllegalArgumentException
	 *             if given readableObject is null
	 */
	@CanBeNull
	public static long[] getLongArray(@NeverNull XReadableObject readableObject,
			@NeverNull XId fieldId) throws ClassCastException, IllegalArgumentException {
		XValue v = _getValue(readableObject, fieldId);
		if (v == null)
			return null;
		if (v instanceof XLongListValue) {
			XLongListValue specificV = (XLongListValue) v;
			long[] array = specificV.contents();
			return array;
		} else {
			throw new ClassCastException("XValue is a " + v.getClass()
					+ " require an XLongListValue");
		}
	}

	/**
	 * @param readableObject
	 * @NeverNull
	 * @param fieldId
	 * @NeverNull
	 * @return null if field or value does not exist; a String value if set
	 * @throws ClassCastException
	 *             if value exists but is not a {@link XStringValue}
	 * @throws IllegalArgumentException
	 *             if given readableObject is null
	 */
	@CanBeNull
	public static String getString(@NeverNull XReadableObject readableObject, @NeverNull XId fieldId)
			throws ClassCastException, IllegalArgumentException {
		XValue v = _getValue(readableObject, fieldId);
		if (v == null)
			return null;
		if (v instanceof XStringValue) {
			XStringValue specificV = (XStringValue) v;
			return specificV.getValue();
		} else {
			throw new ClassCastException("XValue is a " + v.getClass() + " require an XStringValue");
		}
	}

	public static String[] getStringArray(XWritableObject readableObject, XId fieldId) {
		List<String> list = getStringList(readableObject, fieldId);
		if (list == null)
			return null;
		return list.toArray(new String[list.size()]);
	}

	/**
	 * @param readableObject
	 * @NeverNull
	 * @param fieldId
	 * @NeverNull
	 * @return null if field or value does not exist; a String value if set
	 * @throws ClassCastException
	 *             if value exists but is not a {@link XStringListValue}
	 * @throws IllegalArgumentException
	 *             if given readableObject is null
	 */
	@CanBeNull
	public static List<String> getStringList(@NeverNull XReadableObject readableObject,
			@NeverNull XId fieldId) throws ClassCastException, IllegalArgumentException {
		XValue v = _getValue(readableObject, fieldId);
		if (v == null)
			return null;
		if (v instanceof XStringListValue) {
			XStringListValue specificV = (XStringListValue) v;
			String[] array = specificV.contents();
			return Arrays.asList(array);
		} else {
			throw new ClassCastException("XValue is a " + v.getClass()
					+ " require an XStringListValue");
		}
	}

	/**
	 * @param readableObject
	 * @NeverNull
	 * @param fieldId
	 * @NeverNull
	 * @return null if field or value does not exist; a ID value if set
	 * @throws ClassCastException
	 *             if value exists but is not a {@link XStringSetValue}
	 * @throws IllegalArgumentException
	 *             if given readableObject is null
	 */
	@CanBeNull
	public static Set<String> getStringSet(@NeverNull XReadableObject readableObject,
			@NeverNull XId fieldId) throws ClassCastException, IllegalArgumentException {
		XValue v = _getValue(readableObject, fieldId);
		if (v == null)
			return null;
		if (v instanceof XStringSetValue) {
			XStringSetValue specificV = (XStringSetValue) v;
			Set<String> set = specificV.toSet();
			return set;
		} else {
			throw new ClassCastException("XValue is a " + v.getClass()
					+ " require an XStringSetValue");
		}
	}

	/**
	 * @param type
	 * @return the default value used in the Java type system if a value is not
	 *         initialised. It's null for all non-primitive types.
	 */
	public static Object getUninitializedValue(Class<?> type) {
		if (type.equals(boolean.class))
			return false;
		if (type.equals(int.class))
			return 0;
		if (type.equals(double.class))
			return 0d;
		if (type.equals(byte.class))
			return 0;
		if (type.equals(float.class))
			return 0f;
		if (type.equals(byte.class))
			return 0;
		if (type.equals(char.class))
			return 0;
		if (type.equals(short.class))
			return 0;
		if (type.equals(long.class))
			return 0l;
		return null;
	}

	// ========== manually added code

	/**
	 * Sets the value of given field in given object to desired value. Creates
	 * the field if required.
	 * 
	 * @param writableObject
	 * @NeverNull
	 * @param fieldId
	 * @NeverNull
	 * @param value
	 * @CanBeNull
	 * @throws IllegalArgumentException
	 *             if given writableObject is null
	 */
	public static void setAddress(@NeverNull XWritableObject writableObject,
			@NeverNull XId fieldId, @CanBeNull XAddress value) throws IllegalArgumentException {
		XWritableField f = _getOrCreateField(writableObject, fieldId);
		f.setValue(value);
	}

	public static void setAddressArray(@NeverNull XWritableObject writableObject,
			@NeverNull XId fieldId, @CanBeNull XAddress[] values) {
		XAddressListValue v = null;
		if (values != null) {
			v = BaseRuntime.getValueFactory().createAddressListValue(values);
		}
		XWritableField f = _getOrCreateField(writableObject, fieldId);
		f.setValue(v);
	}

	/**
	 * Sets the value of given field in given object to desired value. Creates
	 * the field if required.
	 * 
	 * @param writableObject
	 * @NeverNull
	 * @param fieldId
	 * @NeverNull
	 * @param value
	 * @CanBeNull
	 * @throws IllegalArgumentException
	 *             if given writableObject is null
	 */
	public static void setAddressList(@NeverNull XWritableObject writableObject,
			@NeverNull XId fieldId, @CanBeNull List<XAddress> value)
			throws IllegalArgumentException {
		XWritableField f = _getOrCreateField(writableObject, fieldId);
		@CanBeNull
		XAddressListValue v = XV.toAddressListValue(value);
		f.setValue(v);
	}

	/**
	 * Sets the value of given field in given object to desired value. Creates
	 * the field if required.
	 * 
	 * @param writableObject
	 * @NeverNull
	 * @param fieldId
	 * @NeverNull
	 * @param value
	 * @CanBeNull
	 * @throws IllegalArgumentException
	 *             if given writableObject is null
	 */
	public static void setAddressSet(@NeverNull XWritableObject writableObject,
			@NeverNull XId fieldId, @CanBeNull Set<XAddress> value) throws IllegalArgumentException {
		XWritableField f = _getOrCreateField(writableObject, fieldId);
		@CanBeNull
		XAddressSetValue v = XV.toAddressSetValue(value);
		f.setValue(v);
	}

	/**
	 * Sets the value of given field in given object to desired value. Creates
	 * the field if required.
	 * 
	 * @param writableObject
	 * @NeverNull
	 * @param fieldId
	 * @NeverNull
	 * @param value
	 * @CanBeNull
	 * @throws IllegalArgumentException
	 *             if given writableObject is null
	 */
	public static void setAddressSortedSet(@NeverNull XWritableObject writableObject,
			@NeverNull XId fieldId, @CanBeNull SortedSet<XAddress> value)
			throws IllegalArgumentException {
		XWritableField f = _getOrCreateField(writableObject, fieldId);
		@CanBeNull
		XAddressSortedSetValue v = XV.toAddressSortedSetValue(value);
		f.setValue(v);
	}

	/**
	 * Sets the value of given field in given object to desired value. Creates
	 * the field if required.
	 * 
	 * @param writableObject
	 * @NeverNull
	 * @param fieldId
	 * @NeverNull
	 * @param value
	 * @CanBeNull
	 * @throws IllegalArgumentException
	 *             if given writableObject is null
	 */
	public static void setBinary(@NeverNull XWritableObject writableObject, @NeverNull XId fieldId,
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
	 * @param writableObject
	 * @NeverNull
	 * @param fieldId
	 * @NeverNull
	 * @param value
	 * @CanBeNull
	 * @throws IllegalArgumentException
	 *             if given writableObject is null
	 */
	public static void setBoolean(@NeverNull XWritableObject writableObject,
			@NeverNull XId fieldId, @CanBeNull boolean value) throws IllegalArgumentException {
		XWritableField f = _getOrCreateField(writableObject, fieldId);
		@CanBeNull
		XBooleanValue v = XV.toValue(value);
		f.setValue(v);
	}

	public static void setBooleanArray(@NeverNull XWritableObject writableObject,
			@NeverNull XId fieldId, @CanBeNull boolean[] values) {
		XBooleanListValue v = null;
		if (values != null) {
			v = BaseRuntime.getValueFactory().createBooleanListValue(values);
		}
		XWritableField f = _getOrCreateField(writableObject, fieldId);
		f.setValue(v);
	}

	// ============ Generated code follows (via XValueJavaUtils_Dev)

	/**
	 * Sets the value of given field in given object to desired value. Creates
	 * the field if required.
	 * 
	 * @param writableObject
	 * @NeverNull
	 * @param fieldId
	 * @NeverNull
	 * @param value
	 * @CanBeNull
	 * @throws IllegalArgumentException
	 *             if given writableObject is null
	 */
	public static void setBooleanCollection(@NeverNull XWritableObject writableObject,
			@NeverNull XId fieldId, @CanBeNull Collection<Boolean> value)
			throws IllegalArgumentException {
		XWritableField f = _getOrCreateField(writableObject, fieldId);
		@CanBeNull
		XBooleanListValue v = XV.toBooleanListValue(value);
		f.setValue(v);
	}

	/**
	 * Sets the value of given field in given object to desired value. Creates
	 * the field if required.
	 * 
	 * @param writableObject
	 * @NeverNull
	 * @param fieldId
	 * @NeverNull
	 * @param value
	 * @CanBeNull
	 * @throws IllegalArgumentException
	 *             if given writableObject is null
	 */
	public static void setDouble(@NeverNull XWritableObject writableObject, @NeverNull XId fieldId,
			@CanBeNull double value) throws IllegalArgumentException {
		XWritableField f = _getOrCreateField(writableObject, fieldId);
		@CanBeNull
		XDoubleValue v = XV.toValue(value);
		f.setValue(v);
	}

	public static void setDoubleArray(@NeverNull XWritableObject writableObject,
			@NeverNull XId fieldId, @CanBeNull double[] values) {
		XDoubleListValue v = null;
		if (values != null) {
			v = BaseRuntime.getValueFactory().createDoubleListValue(values);
		}
		XWritableField f = _getOrCreateField(writableObject, fieldId);
		f.setValue(v);
	}

	/**
	 * Sets the value of given field in given object to desired value. Creates
	 * the field if required.
	 * 
	 * @param writableObject
	 * @NeverNull
	 * @param fieldId
	 * @NeverNull
	 * @param value
	 * @CanBeNull
	 * @throws IllegalArgumentException
	 *             if given writableObject is null
	 */
	public static void setDoubleCollection(@NeverNull XWritableObject writableObject,
			@NeverNull XId fieldId, @CanBeNull Collection<Double> value)
			throws IllegalArgumentException {
		XWritableField f = _getOrCreateField(writableObject, fieldId);
		@CanBeNull
		XDoubleListValue v = XV.toDoubleListValue(value);
		f.setValue(v);
	}

	/**
	 * Sets the value of given field in given object to desired value. Creates
	 * the field if required.
	 * 
	 * @param writableObject
	 * @NeverNull
	 * @param fieldId
	 * @NeverNull
	 * @param value
	 * @CanBeNull
	 * @throws IllegalArgumentException
	 *             if given writableObject is null
	 */
	public static void setId(@NeverNull XWritableObject writableObject, @NeverNull XId fieldId,
			@CanBeNull XId value) throws IllegalArgumentException {
		XWritableField f = _getOrCreateField(writableObject, fieldId);
		f.setValue(value);
	}

	public static void setIdArray(@NeverNull XWritableObject writableObject,
			@NeverNull XId fieldId, @CanBeNull XId[] values) {
		XIdListValue v = null;
		if (values != null) {
			v = BaseRuntime.getValueFactory().createIdListValue(values);
		}
		XWritableField f = _getOrCreateField(writableObject, fieldId);
		f.setValue(v);
	}

	/**
	 * Sets the value of given field in given object to desired value. Creates
	 * the field if required.
	 * 
	 * @param writableObject
	 * @NeverNull
	 * @param fieldId
	 * @NeverNull
	 * @param value
	 * @CanBeNull
	 * @throws IllegalArgumentException
	 *             if given writableObject is null
	 */
	public static void setIdList(@NeverNull XWritableObject writableObject, @NeverNull XId fieldId,
			@CanBeNull List<XId> value) throws IllegalArgumentException {
		XWritableField f = _getOrCreateField(writableObject, fieldId);
		@CanBeNull
		XIdListValue v = XV.toIdListValue(value);
		f.setValue(v);
	}

	/**
	 * Sets the value of given field in given object to desired value. Creates
	 * the field if required.
	 * 
	 * @param writableObject
	 * @NeverNull
	 * @param fieldId
	 * @NeverNull
	 * @param value
	 * @CanBeNull
	 * @throws IllegalArgumentException
	 *             if given writableObject is null
	 */
	public static void setIdSet(@NeverNull XWritableObject writableObject, @NeverNull XId fieldId,
			@CanBeNull Set<XId> value) throws IllegalArgumentException {
		XWritableField f = _getOrCreateField(writableObject, fieldId);
		@CanBeNull
		XIdSetValue v = XV.toIdSetValue(value);
		f.setValue(v);
	}

	/**
	 * Sets the value of given field in given object to desired value. Creates
	 * the field if required.
	 * 
	 * @param writableObject
	 * @NeverNull
	 * @param fieldId
	 * @NeverNull
	 * @param value
	 * @CanBeNull
	 * @throws IllegalArgumentException
	 *             if given writableObject is null
	 */
	public static void setIdSortedSet(@NeverNull XWritableObject writableObject,
			@NeverNull XId fieldId, @CanBeNull SortedSet<XId> value)
			throws IllegalArgumentException {
		XWritableField f = _getOrCreateField(writableObject, fieldId);
		@CanBeNull
		XIdSortedSetValue v = XV.toIdSortedSetValue(value);
		f.setValue(v);
	}

	/**
	 * Sets the value of given field in given object to desired value. Creates
	 * the field if required.
	 * 
	 * @param writableObject
	 * @NeverNull
	 * @param fieldId
	 * @NeverNull
	 * @param value
	 * @CanBeNull
	 * @throws IllegalArgumentException
	 *             if given writableObject is null
	 */
	public static void setInteger(@NeverNull XWritableObject writableObject,
			@NeverNull XId fieldId, @CanBeNull int value) throws IllegalArgumentException {
		XWritableField f = _getOrCreateField(writableObject, fieldId);
		@CanBeNull
		XIntegerValue v = XV.toValue(value);
		f.setValue(v);
	}

	public static void setIntegerArray(@NeverNull XWritableObject writableObject,
			@NeverNull XId fieldId, @CanBeNull int[] values) {
		XIntegerListValue v = null;
		if (values != null) {
			v = BaseRuntime.getValueFactory().createIntegerListValue(values);
		}
		XWritableField f = _getOrCreateField(writableObject, fieldId);
		f.setValue(v);
	}

	/**
	 * Sets the value of given field in given object to desired value. Creates
	 * the field if required.
	 * 
	 * @param writableObject
	 * @NeverNull
	 * @param fieldId
	 * @NeverNull
	 * @param value
	 * @CanBeNull
	 * @throws IllegalArgumentException
	 *             if given writableObject is null
	 */
	public static void setIntegerCollection(@NeverNull XWritableObject writableObject,
			@NeverNull XId fieldId, @CanBeNull Collection<Integer> value)
			throws IllegalArgumentException {
		XWritableField f = _getOrCreateField(writableObject, fieldId);
		@CanBeNull
		XIntegerListValue v = XV.toIntegerListValue(value);
		f.setValue(v);
	}

	/**
	 * Sets the value of given field in given object to desired value. Creates
	 * the field if required.
	 * 
	 * @param writableObject
	 * @NeverNull
	 * @param fieldId
	 * @NeverNull
	 * @param value
	 * @CanBeNull
	 * @throws IllegalArgumentException
	 *             if given writableObject is null
	 */
	public static void setLong(@NeverNull XWritableObject writableObject, @NeverNull XId fieldId,
			@CanBeNull Long value) throws IllegalArgumentException {
		XWritableField f = _getOrCreateField(writableObject, fieldId);
		@CanBeNull
		XLongValue v = XV.toValue(value);
		f.setValue(v);
	}

	public static void setLongArray(@NeverNull XWritableObject writableObject,
			@NeverNull XId fieldId, @CanBeNull long[] values) {
		XLongListValue v = null;
		if (values != null) {
			v = BaseRuntime.getValueFactory().createLongListValue(values);
		}
		XWritableField f = _getOrCreateField(writableObject, fieldId);
		f.setValue(v);
	}

	/**
	 * Sets the value of given field in given object to desired value. Creates
	 * the field if required.
	 * 
	 * @param writableObject
	 * @NeverNull
	 * @param fieldId
	 * @NeverNull
	 * @param value
	 * @CanBeNull
	 * @throws IllegalArgumentException
	 *             if given writableObject is null
	 */
	public static void setLongCollection(@NeverNull XWritableObject writableObject,
			@NeverNull XId fieldId, @CanBeNull Collection<Long> value)
			throws IllegalArgumentException {
		XWritableField f = _getOrCreateField(writableObject, fieldId);
		@CanBeNull
		XLongListValue v = XV.toLongListValue(value);
		f.setValue(v);
	}

	/**
	 * Sets the value of given field in given object to desired value. Creates
	 * the field if required.
	 * 
	 * @param writableObject
	 * @NeverNull
	 * @param fieldId
	 * @NeverNull
	 * @param value
	 * @CanBeNull
	 * @throws IllegalArgumentException
	 *             if given writableObject is null
	 */
	public static void setString(@NeverNull XWritableObject writableObject, @NeverNull XId fieldId,
			@CanBeNull String value) throws IllegalArgumentException {
		XWritableField f = _getOrCreateField(writableObject, fieldId);
		@CanBeNull
		XStringValue v = XV.toValue(value);
		f.setValue(v);
	}

	public static void setStringArray(@NeverNull XWritableObject writableObject,
			@NeverNull XId fieldId, @CanBeNull String[] values) {
		XStringListValue v = null;
		if (values != null) {
			v = BaseRuntime.getValueFactory().createStringListValue(values);
		}
		XWritableField f = _getOrCreateField(writableObject, fieldId);
		f.setValue(v);
	}

	/**
	 * Sets the value of given field in given object to desired value. Creates
	 * the field if required.
	 * 
	 * @param writableObject
	 * @NeverNull
	 * @param fieldId
	 * @NeverNull
	 * @param value
	 * @CanBeNull
	 * @throws IllegalArgumentException
	 *             if given writableObject is null
	 */
	public static void setStringList(@NeverNull XWritableObject writableObject,
			@NeverNull XId fieldId, @CanBeNull List<String> value) throws IllegalArgumentException {
		XWritableField f = _getOrCreateField(writableObject, fieldId);
		@CanBeNull
		XStringListValue v = XV.toStringListValue(value);
		f.setValue(v);
	}

	/**
	 * Sets the value of given field in given object to desired value. Creates
	 * the field if required.
	 * 
	 * @param writableObject
	 * @NeverNull
	 * @param fieldId
	 * @NeverNull
	 * @param value
	 * @CanBeNull
	 * @throws IllegalArgumentException
	 *             if given writableObject is null
	 */
	public static void setStringSet(@NeverNull XWritableObject writableObject,
			@NeverNull XId fieldId, @CanBeNull Set<String> value) throws IllegalArgumentException {
		XWritableField f = _getOrCreateField(writableObject, fieldId);
		@CanBeNull
		XStringSetValue v = XV.toStringSetValue(value);
		f.setValue(v);
	}

	public static XAddress toAddress(XAddress javaValue) {
		return javaValue;
	}

	public static XAddressListValue toAddressList(List<XAddress> javaValue) {
		if (javaValue == null)
			return null;
		return XV.toAddressListValue(javaValue);
	}

	public static XAddressSetValue toAddressSet(Set<XAddress> javaValue) {
		if (javaValue == null)
			return null;
		return XV.toAddressSetValue(javaValue);
	}

	public static XAddressSortedSetValue toAddressSortedSet(SortedSet<XAddress> javaValue) {
		if (javaValue == null)
			return null;
		return XV.toAddressSortedSetValue(javaValue);
	}

	public static XBinaryValue toBinary(byte[] javaValue) {
		if (javaValue == null)
			return null;
		return XV.toValue(javaValue);
	}

	public static XBooleanValue toboolean(boolean javaValue) {
		return XV.toValue(javaValue);
	}

	public static XBooleanValue toBoolean(Boolean javaValue) {
		if (javaValue == null)
			return null;
		return XV.toValue(javaValue);
	}

	public static XBooleanListValue toBooleanList(List<Boolean> javaValue) {
		if (javaValue == null)
			return null;
		return XV.toBooleanListValue(javaValue);
	}

	public static XDoubleValue todouble(double javaValue) {
		return XV.toValue(javaValue);
	}

	public static XDoubleValue toDouble(Double javaValue) {
		if (javaValue == null)
			return null;
		return XV.toValue(javaValue);
	}

	public static XDoubleListValue toDoubleList(List<Double> javaValue) {
		if (javaValue == null)
			return null;
		return XV.toDoubleListValue(javaValue);
	}

	public static XId toId(XId javaValue) {
		return javaValue;
	}

	public static XIdListValue toIdList(List<XId> javaValue) {
		if (javaValue == null)
			return null;
		return XV.toIdListValue(javaValue);
	}

	public static XIdSetValue toIdSet(Set<XId> javaValue) {
		if (javaValue == null)
			return null;
		return XV.toIdSetValue(javaValue);
	}

	public static XIdSortedSetValue toIdSortedSet(SortedSet<XId> javaValue) {
		if (javaValue == null)
			return null;
		return XV.toIdSortedSetValue(javaValue);
	}

	public static XIntegerValue toint(int javaValue) {
		return XV.toValue(javaValue);
	}

	public static XIntegerValue toInteger(Integer javaValue) {
		if (javaValue == null)
			return null;
		return XV.toValue(javaValue);
	}

	public static XIntegerListValue toIntegerList(List<Integer> javaValue) {
		if (javaValue == null)
			return null;
		return XV.toIntegerListValue(javaValue);
	}

	public static XLongValue tolong(long javaValue) {
		return XV.toValue(javaValue);
	}

	public static XLongValue toLong(Long javaValue) {
		if (javaValue == null)
			return null;
		return XV.toValue(javaValue);
	}

	public static XLongListValue toLongList(List<Long> javaValue) {
		if (javaValue == null)
			return null;
		return XV.toLongListValue(javaValue);
	}

	public static XStringValue toString(String javaValue) {
		if (javaValue == null)
			return null;
		return XV.toValue(javaValue);
	}

	public static XStringListValue toStringList(List<String> javaValue) {
		if (javaValue == null)
			return null;
		return XV.toStringListValue(javaValue);
	}

	public static XStringSetValue toStringSet(Set<String> javaValue) {
		if (javaValue == null)
			return null;
		return XV.toStringSetValue(javaValue);
	}

	public static XValue toValue(String content, ValueType valueType) {
		switch (valueType) {
		case Long:
			return XV.toValue(Long.parseLong(content));
		case String:
			return XV.toValue(content);
		default:
			return null;
		}
	}
}
