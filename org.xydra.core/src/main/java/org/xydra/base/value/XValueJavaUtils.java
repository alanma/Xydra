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
	 * @throws IllegalArgumentException if fieldId or writableObject are null
	 */
	private static XWritableField _getOrCreateField(final XWritableObject writableObject, final XId fieldId)
			throws IllegalArgumentException {
		if (writableObject == null) {
			throw new IllegalArgumentException("writableObject is null");
		}
		if (fieldId == null) {
			throw new IllegalArgumentException("fieldId is null");
		}
		XWritableField f = writableObject.getField(fieldId);
		if (f == null) {
			f = writableObject.createField(fieldId);
		}
		return f;
	}

	/**
	 * @param readableObject
	 * @param fieldId
	 * @return null if field or value does not exist; a {@link XValue} if set
	 * @throws IllegalArgumentException if given readableObject is null
	 */
	private static XValue _getValue(@NeverNull final XReadableObject readableObject,
			@NeverNull final XId fieldId) {
		if (readableObject == null) {
			throw new IllegalArgumentException("readableObject is null");
		}
		if (fieldId == null) {
			throw new IllegalArgumentException("fieldId is null");
		}
		final XReadableField f = readableObject.getField(fieldId);
		if (f == null) {
			return null;
		}
		final XValue v = f.getValue();
		return v;
	}

	public static List<Boolean> asList(final boolean[] contents) {
		final ArrayList<Boolean> list = new ArrayList<Boolean>(contents.length);
		for (int i = 0; i < contents.length; i++) {
			list.add(contents[i]);
		}
		return list;
	}

	public static List<Byte> asList(final byte[] contents) {
		final ArrayList<Byte> list = new ArrayList<Byte>(contents.length);
		for (int i = 0; i < contents.length; i++) {
			list.add(contents[i]);
		}
		return list;
	}

	public static List<Character> asList(final char[] contents) {
		final ArrayList<Character> list = new ArrayList<Character>(contents.length);
		for (int i = 0; i < contents.length; i++) {
			list.add(contents[i]);
		}
		return list;
	}

	public static List<Double> asList(final double[] contents) {
		final ArrayList<Double> list = new ArrayList<Double>(contents.length);
		for (int i = 0; i < contents.length; i++) {
			list.add(contents[i]);
		}
		return list;
	}

	public static List<Float> asList(final float[] contents) {
		final ArrayList<Float> list = new ArrayList<Float>(contents.length);
		for (int i = 0; i < contents.length; i++) {
			list.add(contents[i]);
		}
		return list;
	}

	public static List<Integer> asList(final int[] contents) {
		final ArrayList<Integer> list = new ArrayList<Integer>(contents.length);
		for (int i = 0; i < contents.length; i++) {
			list.add(contents[i]);
		}
		return list;
	}

	public static List<Long> asList(final long[] contents) {
		final ArrayList<Long> list = new ArrayList<Long>(contents.length);
		for (int i = 0; i < contents.length; i++) {
			list.add(contents[i]);
		}
		return list;
	}

	public static List<Short> asList(final short[] contents) {
		final ArrayList<Short> list = new ArrayList<Short>(contents.length);
		for (int i = 0; i < contents.length; i++) {
			list.add(contents[i]);
		}
		return list;
	}

	public static <T> List<T> asList(final T[] contents) {
		return Arrays.asList(contents);
	}

	public static <T> Set<T> asSet(final T[] contents) {
		return new HashSet<T>(asList(contents));
	}

	public static <T> SortedSet<T> asSortedSet(final T[] contents) {
		return new TreeSet<T>(asList(contents));
	}

	// Address
	public static XAddress fromAddress(final XAddress xydraValue) {
		return xydraValue;
	}

	// AddressList
	public static List<XAddress> fromAddressList(final XAddressListValue xydraValue) {
		if (xydraValue == null) {
			return null;
		}
		return asList(xydraValue.contents());
	}

	// AddressSet
	public static Set<XAddress> fromAddressSet(final XAddressSetValue xydraValue) {
		if (xydraValue == null) {
			return null;
		}
		return asSet(xydraValue.contents());
	}

	// AddressSortedSet
	public static SortedSet<XAddress> fromAddressSortedSet(final XAddressSortedSetValue xydraValue) {
		if (xydraValue == null) {
			return null;
		}
		return asSortedSet(xydraValue.contents());
	}

	// Binary
	public static byte[] fromBinary(final XBinaryValue xydraValue) {
		if (xydraValue == null) {
			return null;
		}
		return xydraValue.getValue();
	}

	// boolean
	public static boolean fromboolean(final XBooleanValue xydraValue) {
		if (xydraValue == null) {
			return false;
		}
		return xydraValue.contents();
	}

	// Boolean
	public static Boolean fromBoolean(final XBooleanValue xydraValue) {
		if (xydraValue == null) {
			return null;
		}
		return xydraValue.contents();
	}

	// BooleanList
	public static List<Boolean> fromBooleanList(final XBooleanListValue xydraValue) {
		if (xydraValue == null) {
			return null;
		}
		return asList(xydraValue.contents());
	}

	// double
	public static double fromdouble(final XDoubleValue xydraValue) {
		if (xydraValue == null) {
			return 0d;
		}
		return xydraValue.contents();
	}

	// Double
	public static Double fromDouble(final XDoubleValue xydraValue) {
		if (xydraValue == null) {
			return null;
		}
		return xydraValue.contents();
	}

	// DoubleList
	public static List<Double> fromDoubleList(final XDoubleListValue xydraValue) {
		if (xydraValue == null) {
			return null;
		}
		return asList(xydraValue.contents());
	}

	// Id
	public static XId fromId(final XId xydraValue) {
		return xydraValue;
	}

	// IdList
	public static List<XId> fromIdList(final XIdListValue xydraValue) {
		if (xydraValue == null) {
			return null;
		}
		return asList(xydraValue.contents());
	}

	// IdSet
	public static Set<XId> fromIdSet(final XIdSetValue xydraValue) {
		if (xydraValue == null) {
			return null;
		}
		return asSet(xydraValue.contents());
	}

	// IdSortedSet
	public static SortedSet<XId> fromIdSortedSet(final XIdSortedSetValue xydraValue) {
		if (xydraValue == null) {
			return null;
		}
		return asSortedSet(xydraValue.contents());
	}

	// int
	public static int fromint(final XIntegerValue xydraValue) {
		if (xydraValue == null) {
			return 0;
		}
		return xydraValue.contents();
	}

	// Integer
	public static Integer fromInteger(final XIntegerValue xydraValue) {
		if (xydraValue == null) {
			return null;
		}
		return xydraValue.contents();
	}

	// IntegerList
	public static List<Integer> fromIntegerList(final XIntegerListValue xydraValue) {
		if (xydraValue == null) {
			return null;
		}
		return asList(xydraValue.contents());
	}

	// long
	public static long fromlong(final XLongValue xydraValue) {
		if (xydraValue == null) {
			return 0l;
		}
		return xydraValue.contents();
	}

	// Long
	public static Long fromLong(final XLongValue xydraValue) {
		if (xydraValue == null) {
			return null;
		}
		return xydraValue.contents();
	}

	// LongList
	public static List<Long> fromLongList(final XLongListValue xydraValue) {
		if (xydraValue == null) {
			return null;
		}
		return asList(xydraValue.contents());
	}

	// String
	public static String fromString(final XStringValue xydraValue) {
		if (xydraValue == null) {
			return null;
		}
		return xydraValue.contents();
	}

	// StringList
	public static List<String> fromStringList(final XStringListValue xydraValue) {
		if (xydraValue == null) {
			return null;
		}
		return asList(xydraValue.contents());
	}

	// StringSet
	public static Set<String> fromStringSet(final XStringSetValue xydraValue) {
		if (xydraValue == null) {
			return null;
		}
		return asSet(xydraValue.contents());
	}

	/**
	 * @param readableObject @NeverNull
	 * @param fieldId @NeverNull
	 * @return null if field or value does not exist; a XAddress value if set
	 * @throws ClassCastException if value exists but is not a {@link XAddress}
	 * @throws IllegalArgumentException if given readableObject is null
	 */
	@CanBeNull
	public static XAddress getAddress(@NeverNull final XReadableObject readableObject,
			@NeverNull final XId fieldId) throws ClassCastException, IllegalArgumentException {
		final XValue v = _getValue(readableObject, fieldId);
		if (v == null) {
			return null;
		}
		if (v instanceof XAddress) {
			final XAddress specificV = (XAddress) v;
			return specificV;
		} else {
			throw new ClassCastException("XValue is a " + v.getClass() + " require an XAddress");
		}
	}

	public static XAddress[] getAddressArray(final XWritableObject readableObject, final XId fieldId) {
		final List<XAddress> list = getAddressList(readableObject, fieldId);
		if (list == null) {
			return null;
		}
		return list.toArray(new XAddress[list.size()]);
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
	public static List<XAddress> getAddressList(@NeverNull final XReadableObject readableObject,
			@NeverNull final XId fieldId) throws ClassCastException, IllegalArgumentException {
		final XValue v = _getValue(readableObject, fieldId);
		if (v == null) {
			return null;
		}
		if (v instanceof XAddressListValue) {
			final XAddressListValue specificV = (XAddressListValue) v;
			final XAddress[] array = specificV.contents();
			return Arrays.asList(array);
		} else {
			throw new ClassCastException("XValue is a " + v.getClass()
					+ " require an XAddressListValue");
		}
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
	public static Set<XAddress> getAddressSet(@NeverNull final XReadableObject readableObject,
			@NeverNull final XId fieldId) throws ClassCastException, IllegalArgumentException {
		final XValue v = _getValue(readableObject, fieldId);
		if (v == null) {
			return null;
		}
		if (v instanceof XAddressSetValue) {
			final XAddressSetValue specificV = (XAddressSetValue) v;
			final Set<XAddress> set = specificV.toSet();
			return set;
		} else {
			throw new ClassCastException("XValue is a " + v.getClass()
					+ " require an XAddressSetValue");
		}
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
			@NeverNull final XReadableObject readableObject, @NeverNull final XId fieldId)
			throws ClassCastException, IllegalArgumentException {
		final XValue v = _getValue(readableObject, fieldId);
		if (v == null) {
			return null;
		}
		if (v instanceof XAddressSortedSetValue) {
			final XAddressSortedSetValue specificV = (XAddressSortedSetValue) v;
			final SortedSet<XAddress> set = specificV.toSortedSet();
			return set;
		} else {
			throw new ClassCastException("XValue is a " + v.getClass()
					+ " require an XAddressSortedSetValue");
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
	public static byte[] getBinary(@NeverNull final XReadableObject readableObject, @NeverNull final XId fieldId)
			throws ClassCastException, IllegalArgumentException {
		final XValue v = _getValue(readableObject, fieldId);
		if (v == null) {
			return null;
		}
		if (v instanceof XBinaryValue) {
			final XBinaryValue specificV = (XBinaryValue) v;
			return specificV.getValue();
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
	public static Boolean getBoolean(@NeverNull final XReadableObject readableObject,
			@NeverNull final XId fieldId) throws ClassCastException, IllegalArgumentException {
		final XValue v = _getValue(readableObject, fieldId);
		if (v == null) {
			return null;
		}
		if (v instanceof XBooleanValue) {
			final XBooleanValue specificV = (XBooleanValue) v;
			return specificV.contents();
		} else {
			throw new ClassCastException("XValue is a " + v.getClass()
					+ " require an XBooleanValue");
		}
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
	public static boolean[] getBooleanArray(@NeverNull final XReadableObject readableObject,
			@NeverNull final XId fieldId) throws ClassCastException, IllegalArgumentException {
		final XValue v = _getValue(readableObject, fieldId);
		if (v == null) {
			return null;
		}
		if (v instanceof XBooleanListValue) {
			final XBooleanListValue specificV = (XBooleanListValue) v;
			final boolean[] array = specificV.contents();
			return array;
		} else {
			throw new ClassCastException("XValue is a " + v.getClass()
					+ " require an XBooleanListValue");
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
	public static Double getDouble(@NeverNull final XReadableObject readableObject, @NeverNull final XId fieldId)
			throws ClassCastException, IllegalArgumentException {
		final XValue v = _getValue(readableObject, fieldId);
		if (v == null) {
			return null;
		}
		if (v instanceof XDoubleValue) {
			final XDoubleValue specificV = (XDoubleValue) v;
			return specificV.contents();
		} else {
			throw new ClassCastException("XValue is a " + v.getClass() + " require an XDoubleValue");
		}
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
	public static double[] getDoubleArray(@NeverNull final XReadableObject readableObject,
			@NeverNull final XId fieldId) throws ClassCastException, IllegalArgumentException {
		final XValue v = _getValue(readableObject, fieldId);
		if (v == null) {
			return null;
		}
		if (v instanceof XDoubleListValue) {
			final XDoubleListValue specificV = (XDoubleListValue) v;
			final double[] array = specificV.contents();
			return array;
		} else {
			throw new ClassCastException("XValue is a " + v.getClass()
					+ " require an XDoubleListValue");
		}
	}

	/**
	 * @param readableObject @NeverNull
	 * @param fieldId @NeverNull
	 * @return null if field or value does not exist; a XId value if set
	 * @throws ClassCastException if value exists but is not a {@link XId}
	 * @throws IllegalArgumentException if given readableObject is null
	 */
	@CanBeNull
	public static XId getId(@NeverNull final XReadableObject readableObject, @NeverNull final XId fieldId)
			throws ClassCastException, IllegalArgumentException {
		final XValue v = _getValue(readableObject, fieldId);
		if (v == null) {
			return null;
		}
		if (v instanceof XId) {
			final XId specificV = (XId) v;
			return specificV;
		} else {
			throw new ClassCastException("XValue is a " + v.getClass() + " require an XId");
		}
	}

	public static XId[] getIdArray(final XWritableObject readableObject, final XId fieldId) {
		final List<XId> list = getIdList(readableObject, fieldId);
		if (list == null) {
			return null;
		}
		return list.toArray(new XId[list.size()]);
	}

	/**
	 * @param readableObject @NeverNull
	 * @param fieldId @NeverNull
	 * @return null if field or value does not exist; a ID value if set
	 * @throws ClassCastException if value exists but is not a
	 *             {@link XIdListValue}
	 * @throws IllegalArgumentException if given readableObject is null
	 */
	@CanBeNull
	public static List<XId> getIdList(@NeverNull final XReadableObject readableObject,
			@NeverNull final XId fieldId) throws ClassCastException, IllegalArgumentException {
		final XValue v = _getValue(readableObject, fieldId);
		if (v == null) {
			return null;
		}
		if (v instanceof XIdListValue) {
			final XIdListValue specificV = (XIdListValue) v;
			final XId[] array = specificV.contents();
			return Arrays.asList(array);
		} else {
			throw new ClassCastException("XValue is a " + v.getClass() + " require an XIdListValue");
		}
	}

	/**
	 * @param readableObject @NeverNull
	 * @param fieldId @NeverNull
	 * @return null if field or value does not exist; a ID value if set
	 * @throws ClassCastException if value exists but is not a
	 *             {@link XIdSetValue}
	 * @throws IllegalArgumentException if given readableObject is null
	 */
	@CanBeNull
	public static Set<XId> getIdSet(@NeverNull final XReadableObject readableObject,
			@NeverNull final XId fieldId) throws ClassCastException, IllegalArgumentException {
		final XValue v = _getValue(readableObject, fieldId);
		if (v == null) {
			return null;
		}
		if (v instanceof XIdSetValue) {
			final XIdSetValue specificV = (XIdSetValue) v;
			final Set<XId> set = specificV.toSet();
			return set;
		} else {
			throw new ClassCastException("XValue is a " + v.getClass() + " require an XIdSetValue");
		}
	}

	/**
	 * @param readableObject @NeverNull
	 * @param fieldId @NeverNull
	 * @return null if field or value does not exist; a ID value if set
	 * @throws ClassCastException if value exists but is not a
	 *             {@link XIdSortedSetValue}
	 * @throws IllegalArgumentException if given readableObject is null
	 */
	@CanBeNull
	public static SortedSet<XId> getIdSortedSet(@NeverNull final XReadableObject readableObject,
			@NeverNull final XId fieldId) throws ClassCastException, IllegalArgumentException {
		final XValue v = _getValue(readableObject, fieldId);
		if (v == null) {
			return null;
		}
		if (v instanceof XIdSortedSetValue) {
			final XIdSortedSetValue specificV = (XIdSortedSetValue) v;
			final SortedSet<XId> set = specificV.toSortedSet();
			return set;
		} else {
			throw new ClassCastException("XValue is a " + v.getClass()
					+ " require an XIdSortedSetValue");
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
	public static Integer getInteger(@NeverNull final XReadableObject readableObject,
			@NeverNull final XId fieldId) throws ClassCastException, IllegalArgumentException {
		final XValue v = _getValue(readableObject, fieldId);
		if (v == null) {
			return null;
		}
		if (v instanceof XIntegerValue) {
			final XIntegerValue specificV = (XIntegerValue) v;
			return specificV.contents();
		} else {
			throw new ClassCastException("XValue is a " + v.getClass()
					+ " require an XIntegerValue");
		}
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
	public static int[] getIntegerArray(@NeverNull final XReadableObject readableObject,
			@NeverNull final XId fieldId) throws ClassCastException, IllegalArgumentException {
		final XValue v = _getValue(readableObject, fieldId);
		if (v == null) {
			return null;
		}
		if (v instanceof XIntegerListValue) {
			final XIntegerListValue specificV = (XIntegerListValue) v;
			final int[] array = specificV.contents();
			return array;
		} else {
			throw new ClassCastException("XValue is a " + v.getClass()
					+ " require an XIntegerListValue");
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
	public static Long getLong(@NeverNull final XReadableObject readableObject, @NeverNull final XId fieldId)
			throws ClassCastException, IllegalArgumentException {
		final XValue v = _getValue(readableObject, fieldId);
		if (v == null) {
			return null;
		}
		if (v instanceof XLongValue) {
			final XLongValue specificV = (XLongValue) v;
			return specificV.contents();
		} else {
			throw new ClassCastException("XValue is a " + v.getClass() + " require an XLongValue");
		}
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
	public static long[] getLongArray(@NeverNull final XReadableObject readableObject,
			@NeverNull final XId fieldId) throws ClassCastException, IllegalArgumentException {
		final XValue v = _getValue(readableObject, fieldId);
		if (v == null) {
			return null;
		}
		if (v instanceof XLongListValue) {
			final XLongListValue specificV = (XLongListValue) v;
			final long[] array = specificV.contents();
			return array;
		} else {
			throw new ClassCastException("XValue is a " + v.getClass()
					+ " require an XLongListValue");
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
	public static String getString(@NeverNull final XReadableObject readableObject, @NeverNull final XId fieldId)
			throws ClassCastException, IllegalArgumentException {
		final XValue v = _getValue(readableObject, fieldId);
		if (v == null) {
			return null;
		}
		if (v instanceof XStringValue) {
			final XStringValue specificV = (XStringValue) v;
			return specificV.contents();
		} else {
			throw new ClassCastException("XValue is a " + v.getClass() + " require an XStringValue");
		}
	}

	public static String[] getStringArray(final XWritableObject readableObject, final XId fieldId) {
		final List<String> list = getStringList(readableObject, fieldId);
		if (list == null) {
			return null;
		}
		return list.toArray(new String[list.size()]);
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
	public static List<String> getStringList(@NeverNull final XReadableObject readableObject,
			@NeverNull final XId fieldId) throws ClassCastException, IllegalArgumentException {
		final XValue v = _getValue(readableObject, fieldId);
		if (v == null) {
			return null;
		}
		if (v instanceof XStringListValue) {
			final XStringListValue specificV = (XStringListValue) v;
			final String[] array = specificV.contents();
			return Arrays.asList(array);
		} else {
			throw new ClassCastException("XValue is a " + v.getClass()
					+ " require an XStringListValue");
		}
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
	public static Set<String> getStringSet(@NeverNull final XReadableObject readableObject,
			@NeverNull final XId fieldId) throws ClassCastException, IllegalArgumentException {
		final XValue v = _getValue(readableObject, fieldId);
		if (v == null) {
			return null;
		}
		if (v instanceof XStringSetValue) {
			final XStringSetValue specificV = (XStringSetValue) v;
			final Set<String> set = specificV.toSet();
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
	public static Object getUninitializedValue(final Class<?> type) {
		if (type.equals(boolean.class)) {
			return false;
		}
		if (type.equals(int.class)) {
			return 0;
		}
		if (type.equals(double.class)) {
			return 0d;
		}
		if (type.equals(byte.class)) {
			return 0;
		}
		if (type.equals(float.class)) {
			return 0f;
		}
		if (type.equals(byte.class)) {
			return 0;
		}
		if (type.equals(char.class)) {
			return 0;
		}
		if (type.equals(short.class)) {
			return 0;
		}
		if (type.equals(long.class)) {
			return 0l;
		}
		return null;
	}

	// ========== manually added code

	/**
	 * Sets the value of given field in given object to desired value. Creates
	 * the field if required.
	 *
	 * @param writableObject @NeverNull
	 * @param fieldId @NeverNull
	 * @param value
	 * @CanBeNull
	 * @throws IllegalArgumentException if given writableObject is null
	 */
	public static void setAddress(@NeverNull final XWritableObject writableObject,
			@NeverNull final XId fieldId, @CanBeNull final XAddress value) throws IllegalArgumentException {
		final XWritableField f = _getOrCreateField(writableObject, fieldId);
		f.setValue(value);
	}

	public static void setAddressArray(@NeverNull final XWritableObject writableObject,
			@NeverNull final XId fieldId, @CanBeNull final XAddress[] values) {
		XAddressListValue v = null;
		if (values != null) {
			v = BaseRuntime.getValueFactory().createAddressListValue(values);
		}
		final XWritableField f = _getOrCreateField(writableObject, fieldId);
		f.setValue(v);
	}

	/**
	 * Sets the value of given field in given object to desired value. Creates
	 * the field if required.
	 *
	 * @param writableObject @NeverNull
	 * @param fieldId @NeverNull
	 * @param value
	 * @CanBeNull
	 * @throws IllegalArgumentException if given writableObject is null
	 */
	public static void setAddressList(@NeverNull final XWritableObject writableObject,
			@NeverNull final XId fieldId, @CanBeNull final List<XAddress> value)
			throws IllegalArgumentException {
		final XWritableField f = _getOrCreateField(writableObject, fieldId);
		@CanBeNull
		final
		XAddressListValue v = XV.toAddressListValue(value);
		f.setValue(v);
	}

	/**
	 * Sets the value of given field in given object to desired value. Creates
	 * the field if required.
	 *
	 * @param writableObject @NeverNull
	 * @param fieldId @NeverNull
	 * @param value
	 * @CanBeNull
	 * @throws IllegalArgumentException if given writableObject is null
	 */
	public static void setAddressSet(@NeverNull final XWritableObject writableObject,
			@NeverNull final XId fieldId, @CanBeNull final Set<XAddress> value) throws IllegalArgumentException {
		final XWritableField f = _getOrCreateField(writableObject, fieldId);
		@CanBeNull
		final
		XAddressSetValue v = XV.toAddressSetValue(value);
		f.setValue(v);
	}

	/**
	 * Sets the value of given field in given object to desired value. Creates
	 * the field if required.
	 *
	 * @param writableObject @NeverNull
	 * @param fieldId @NeverNull
	 * @param value
	 * @CanBeNull
	 * @throws IllegalArgumentException if given writableObject is null
	 */
	public static void setAddressSortedSet(@NeverNull final XWritableObject writableObject,
			@NeverNull final XId fieldId, @CanBeNull final SortedSet<XAddress> value)
			throws IllegalArgumentException {
		final XWritableField f = _getOrCreateField(writableObject, fieldId);
		@CanBeNull
		final
		XAddressSortedSetValue v = XV.toAddressSortedSetValue(value);
		f.setValue(v);
	}

	/**
	 * Sets the value of given field in given object to desired value. Creates
	 * the field if required.
	 *
	 * @param writableObject @NeverNull
	 * @param fieldId @NeverNull
	 * @param value
	 * @CanBeNull
	 * @throws IllegalArgumentException if given writableObject is null
	 */
	public static void setBinary(@NeverNull final XWritableObject writableObject, @NeverNull final XId fieldId,
			@CanBeNull final byte[] value) throws IllegalArgumentException {
		final XWritableField f = _getOrCreateField(writableObject, fieldId);
		@CanBeNull
		final
		XBinaryValue v = XV.toValue(value);
		f.setValue(v);
	}

	/**
	 * Sets the value of given field in given object to desired value. Creates
	 * the field if required.
	 *
	 * @param writableObject @NeverNull
	 * @param fieldId @NeverNull
	 * @param value
	 * @CanBeNull
	 * @throws IllegalArgumentException if given writableObject is null
	 */
	public static void setBoolean(@NeverNull final XWritableObject writableObject,
			@NeverNull final XId fieldId, @CanBeNull final boolean value) throws IllegalArgumentException {
		final XWritableField f = _getOrCreateField(writableObject, fieldId);
		@CanBeNull
		final
		XBooleanValue v = XV.toValue(value);
		f.setValue(v);
	}

	public static void setBooleanArray(@NeverNull final XWritableObject writableObject,
			@NeverNull final XId fieldId, @CanBeNull final boolean[] values) {
		XBooleanListValue v = null;
		if (values != null) {
			v = BaseRuntime.getValueFactory().createBooleanListValue(values);
		}
		final XWritableField f = _getOrCreateField(writableObject, fieldId);
		f.setValue(v);
	}

	// ============ Generated code follows (via XValueJavaUtils_Dev)

	/**
	 * Sets the value of given field in given object to desired value. Creates
	 * the field if required.
	 *
	 * @param writableObject @NeverNull
	 * @param fieldId @NeverNull
	 * @param value
	 * @CanBeNull
	 * @throws IllegalArgumentException if given writableObject is null
	 */
	public static void setBooleanCollection(@NeverNull final XWritableObject writableObject,
			@NeverNull final XId fieldId, @CanBeNull final Collection<Boolean> value)
			throws IllegalArgumentException {
		final XWritableField f = _getOrCreateField(writableObject, fieldId);
		@CanBeNull
		final
		XBooleanListValue v = XV.toBooleanListValue(value);
		f.setValue(v);
	}

	/**
	 * Sets the value of given field in given object to desired value. Creates
	 * the field if required.
	 *
	 * @param writableObject @NeverNull
	 * @param fieldId @NeverNull
	 * @param value
	 * @CanBeNull
	 * @throws IllegalArgumentException if given writableObject is null
	 */
	public static void setDouble(@NeverNull final XWritableObject writableObject, @NeverNull final XId fieldId,
			@CanBeNull final double value) throws IllegalArgumentException {
		final XWritableField f = _getOrCreateField(writableObject, fieldId);
		@CanBeNull
		final
		XDoubleValue v = XV.toValue(value);
		f.setValue(v);
	}

	public static void setDoubleArray(@NeverNull final XWritableObject writableObject,
			@NeverNull final XId fieldId, @CanBeNull final double[] values) {
		XDoubleListValue v = null;
		if (values != null) {
			v = BaseRuntime.getValueFactory().createDoubleListValue(values);
		}
		final XWritableField f = _getOrCreateField(writableObject, fieldId);
		f.setValue(v);
	}

	/**
	 * Sets the value of given field in given object to desired value. Creates
	 * the field if required.
	 *
	 * @param writableObject @NeverNull
	 * @param fieldId @NeverNull
	 * @param value
	 * @CanBeNull
	 * @throws IllegalArgumentException if given writableObject is null
	 */
	public static void setDoubleCollection(@NeverNull final XWritableObject writableObject,
			@NeverNull final XId fieldId, @CanBeNull final Collection<Double> value)
			throws IllegalArgumentException {
		final XWritableField f = _getOrCreateField(writableObject, fieldId);
		@CanBeNull
		final
		XDoubleListValue v = XV.toDoubleListValue(value);
		f.setValue(v);
	}

	/**
	 * Sets the value of given field in given object to desired value. Creates
	 * the field if required.
	 *
	 * @param writableObject @NeverNull
	 * @param fieldId @NeverNull
	 * @param value
	 * @CanBeNull
	 * @throws IllegalArgumentException if given writableObject is null
	 */
	public static void setId(@NeverNull final XWritableObject writableObject, @NeverNull final XId fieldId,
			@CanBeNull final XId value) throws IllegalArgumentException {
		final XWritableField f = _getOrCreateField(writableObject, fieldId);
		f.setValue(value);
	}

	public static void setIdArray(@NeverNull final XWritableObject writableObject,
			@NeverNull final XId fieldId, @CanBeNull final XId[] values) {
		XIdListValue v = null;
		if (values != null) {
			v = BaseRuntime.getValueFactory().createIdListValue(values);
		}
		final XWritableField f = _getOrCreateField(writableObject, fieldId);
		f.setValue(v);
	}

	/**
	 * Sets the value of given field in given object to desired value. Creates
	 * the field if required.
	 *
	 * @param writableObject @NeverNull
	 * @param fieldId @NeverNull
	 * @param value
	 * @CanBeNull
	 * @throws IllegalArgumentException if given writableObject is null
	 */
	public static void setIdList(@NeverNull final XWritableObject writableObject, @NeverNull final XId fieldId,
			@CanBeNull final List<XId> value) throws IllegalArgumentException {
		final XWritableField f = _getOrCreateField(writableObject, fieldId);
		@CanBeNull
		final
		XIdListValue v = XV.toIdListValue(value);
		f.setValue(v);
	}

	/**
	 * Sets the value of given field in given object to desired value. Creates
	 * the field if required.
	 *
	 * @param writableObject @NeverNull
	 * @param fieldId @NeverNull
	 * @param value
	 * @CanBeNull
	 * @throws IllegalArgumentException if given writableObject is null
	 */
	public static void setIdSet(@NeverNull final XWritableObject writableObject, @NeverNull final XId fieldId,
			@CanBeNull final Set<XId> value) throws IllegalArgumentException {
		final XWritableField f = _getOrCreateField(writableObject, fieldId);
		@CanBeNull
		final
		XIdSetValue v = XV.toIdSetValue(value);
		f.setValue(v);
	}

	/**
	 * Sets the value of given field in given object to desired value. Creates
	 * the field if required.
	 *
	 * @param writableObject @NeverNull
	 * @param fieldId @NeverNull
	 * @param value
	 * @CanBeNull
	 * @throws IllegalArgumentException if given writableObject is null
	 */
	public static void setIdSortedSet(@NeverNull final XWritableObject writableObject,
			@NeverNull final XId fieldId, @CanBeNull final SortedSet<XId> value)
			throws IllegalArgumentException {
		final XWritableField f = _getOrCreateField(writableObject, fieldId);
		@CanBeNull
		final
		XIdSortedSetValue v = XV.toIdSortedSetValue(value);
		f.setValue(v);
	}

	/**
	 * Sets the value of given field in given object to desired value. Creates
	 * the field if required.
	 *
	 * @param writableObject @NeverNull
	 * @param fieldId @NeverNull
	 * @param value
	 * @CanBeNull
	 * @throws IllegalArgumentException if given writableObject is null
	 */
	public static void setInteger(@NeverNull final XWritableObject writableObject,
			@NeverNull final XId fieldId, @CanBeNull final int value) throws IllegalArgumentException {
		final XWritableField f = _getOrCreateField(writableObject, fieldId);
		@CanBeNull
		final
		XIntegerValue v = XV.toValue(value);
		f.setValue(v);
	}

	public static void setIntegerArray(@NeverNull final XWritableObject writableObject,
			@NeverNull final XId fieldId, @CanBeNull final int[] values) {
		XIntegerListValue v = null;
		if (values != null) {
			v = BaseRuntime.getValueFactory().createIntegerListValue(values);
		}
		final XWritableField f = _getOrCreateField(writableObject, fieldId);
		f.setValue(v);
	}

	/**
	 * Sets the value of given field in given object to desired value. Creates
	 * the field if required.
	 *
	 * @param writableObject @NeverNull
	 * @param fieldId @NeverNull
	 * @param value
	 * @CanBeNull
	 * @throws IllegalArgumentException if given writableObject is null
	 */
	public static void setIntegerCollection(@NeverNull final XWritableObject writableObject,
			@NeverNull final XId fieldId, @CanBeNull final Collection<Integer> value)
			throws IllegalArgumentException {
		final XWritableField f = _getOrCreateField(writableObject, fieldId);
		@CanBeNull
		final
		XIntegerListValue v = XV.toIntegerListValue(value);
		f.setValue(v);
	}

	/**
	 * Sets the value of given field in given object to desired value. Creates
	 * the field if required.
	 *
	 * @param writableObject @NeverNull
	 * @param fieldId @NeverNull
	 * @param value
	 * @CanBeNull
	 * @throws IllegalArgumentException if given writableObject is null
	 */
	public static void setLong(@NeverNull final XWritableObject writableObject, @NeverNull final XId fieldId,
			@CanBeNull final Long value) throws IllegalArgumentException {
		final XWritableField f = _getOrCreateField(writableObject, fieldId);
		@CanBeNull
		final
		XLongValue v = XV.toValue(value);
		f.setValue(v);
	}

	public static void setLongArray(@NeverNull final XWritableObject writableObject,
			@NeverNull final XId fieldId, @CanBeNull final long[] values) {
		XLongListValue v = null;
		if (values != null) {
			v = BaseRuntime.getValueFactory().createLongListValue(values);
		}
		final XWritableField f = _getOrCreateField(writableObject, fieldId);
		f.setValue(v);
	}

	/**
	 * Sets the value of given field in given object to desired value. Creates
	 * the field if required.
	 *
	 * @param writableObject @NeverNull
	 * @param fieldId @NeverNull
	 * @param value
	 * @CanBeNull
	 * @throws IllegalArgumentException if given writableObject is null
	 */
	public static void setLongCollection(@NeverNull final XWritableObject writableObject,
			@NeverNull final XId fieldId, @CanBeNull final Collection<Long> value)
			throws IllegalArgumentException {
		final XWritableField f = _getOrCreateField(writableObject, fieldId);
		@CanBeNull
		final
		XLongListValue v = XV.toLongListValue(value);
		f.setValue(v);
	}

	/**
	 * Sets the value of given field in given object to desired value. Creates
	 * the field if required.
	 *
	 * @param writableObject @NeverNull
	 * @param fieldId @NeverNull
	 * @param value
	 * @CanBeNull
	 * @throws IllegalArgumentException if given writableObject is null
	 */
	public static void setString(@NeverNull final XWritableObject writableObject, @NeverNull final XId fieldId,
			@CanBeNull final String value) throws IllegalArgumentException {
		final XWritableField f = _getOrCreateField(writableObject, fieldId);
		@CanBeNull
		final
		XStringValue v = XV.toValue(value);
		f.setValue(v);
	}

	public static void setStringArray(@NeverNull final XWritableObject writableObject,
			@NeverNull final XId fieldId, @CanBeNull final String[] values) {
		XStringListValue v = null;
		if (values != null) {
			v = BaseRuntime.getValueFactory().createStringListValue(values);
		}
		final XWritableField f = _getOrCreateField(writableObject, fieldId);
		f.setValue(v);
	}

	/**
	 * Sets the value of given field in given object to desired value. Creates
	 * the field if required.
	 *
	 * @param writableObject @NeverNull
	 * @param fieldId @NeverNull
	 * @param value
	 * @CanBeNull
	 * @throws IllegalArgumentException if given writableObject is null
	 */
	public static void setStringList(@NeverNull final XWritableObject writableObject,
			@NeverNull final XId fieldId, @CanBeNull final List<String> value) throws IllegalArgumentException {
		final XWritableField f = _getOrCreateField(writableObject, fieldId);
		@CanBeNull
		final
		XStringListValue v = XV.toStringListValue(value);
		f.setValue(v);
	}

	/**
	 * Sets the value of given field in given object to desired value. Creates
	 * the field if required.
	 *
	 * @param writableObject @NeverNull
	 * @param fieldId @NeverNull
	 * @param value
	 * @CanBeNull
	 * @throws IllegalArgumentException if given writableObject is null
	 */
	public static void setStringSet(@NeverNull final XWritableObject writableObject,
			@NeverNull final XId fieldId, @CanBeNull final Set<String> value) throws IllegalArgumentException {
		final XWritableField f = _getOrCreateField(writableObject, fieldId);
		@CanBeNull
		final
		XStringSetValue v = XV.toStringSetValue(value);
		f.setValue(v);
	}

	public static XAddress toAddress(final XAddress javaValue) {
		return javaValue;
	}

	public static XAddressListValue toAddressList(final List<XAddress> javaValue) {
		if (javaValue == null) {
			return null;
		}
		return XV.toAddressListValue(javaValue);
	}

	public static XAddressSetValue toAddressSet(final Set<XAddress> javaValue) {
		if (javaValue == null) {
			return null;
		}
		return XV.toAddressSetValue(javaValue);
	}

	public static XAddressSortedSetValue toAddressSortedSet(final SortedSet<XAddress> javaValue) {
		if (javaValue == null) {
			return null;
		}
		return XV.toAddressSortedSetValue(javaValue);
	}

	public static XBinaryValue toBinary(final byte[] javaValue) {
		if (javaValue == null) {
			return null;
		}
		return XV.toValue(javaValue);
	}

	public static XBooleanValue toboolean(final boolean javaValue) {
		return XV.toValue(javaValue);
	}

	public static XBooleanValue toBoolean(final Boolean javaValue) {
		if (javaValue == null) {
			return null;
		}
		return XV.toValue(javaValue);
	}

	public static XBooleanListValue toBooleanList(final List<Boolean> javaValue) {
		if (javaValue == null) {
			return null;
		}
		return XV.toBooleanListValue(javaValue);
	}

	public static XDoubleValue todouble(final double javaValue) {
		return XV.toValue(javaValue);
	}

	public static XDoubleValue toDouble(final Double javaValue) {
		if (javaValue == null) {
			return null;
		}
		return XV.toValue(javaValue);
	}

	public static XDoubleListValue toDoubleList(final List<Double> javaValue) {
		if (javaValue == null) {
			return null;
		}
		return XV.toDoubleListValue(javaValue);
	}

	public static XId toId(final XId javaValue) {
		return javaValue;
	}

	public static XIdListValue toIdList(final List<XId> javaValue) {
		if (javaValue == null) {
			return null;
		}
		return XV.toIdListValue(javaValue);
	}

	public static XIdSetValue toIdSet(final Set<XId> javaValue) {
		if (javaValue == null) {
			return null;
		}
		return XV.toIdSetValue(javaValue);
	}

	public static XIdSortedSetValue toIdSortedSet(final SortedSet<XId> javaValue) {
		if (javaValue == null) {
			return null;
		}
		return XV.toIdSortedSetValue(javaValue);
	}

	public static XIntegerValue toint(final int javaValue) {
		return XV.toValue(javaValue);
	}

	public static XIntegerValue toInteger(final Integer javaValue) {
		if (javaValue == null) {
			return null;
		}
		return XV.toValue(javaValue);
	}

	public static XIntegerListValue toIntegerList(final List<Integer> javaValue) {
		if (javaValue == null) {
			return null;
		}
		return XV.toIntegerListValue(javaValue);
	}

	public static XLongValue tolong(final long javaValue) {
		return XV.toValue(javaValue);
	}

	public static XLongValue toLong(final Long javaValue) {
		if (javaValue == null) {
			return null;
		}
		return XV.toValue(javaValue);
	}

	public static XLongListValue toLongList(final List<Long> javaValue) {
		if (javaValue == null) {
			return null;
		}
		return XV.toLongListValue(javaValue);
	}

	public static XStringValue toString(final String javaValue) {
		if (javaValue == null) {
			return null;
		}
		return XV.toValue(javaValue);
	}

	public static XStringListValue toStringList(final List<String> javaValue) {
		if (javaValue == null) {
			return null;
		}
		return XV.toStringListValue(javaValue);
	}

	public static XStringSetValue toStringSet(final Set<String> javaValue) {
		if (javaValue == null) {
			return null;
		}
		return XV.toStringSetValue(javaValue);
	}

	public static XValue toValue(final String content, final ValueType valueType) {
		switch (valueType) {
		case Long:
			return XV.toValue(Long.parseLong(content));
		case String:
			return XV.toValue(content);
		default:
			return null;
		}
	}

	// manual code:

	/**
	 * @param value should not be an empty collection @CanBeNull
	 * @return
	 * @throws IllegalArgumentException if Java type does not map to a Xydra
	 *             type
	 */
	public static XValue toValue(final Object object) throws IllegalArgumentException {
		if (object == null) {
			return null;
		} else if (object instanceof XValue) {
			return (XValue) object;
		} else if (object instanceof List<?>) {
			final List<?> list = (List<?>) object;
			if (list.isEmpty()) {
				// no type information for empty list
				throw new IllegalArgumentException("Cannot handle empty collections");
			}
			@SuppressWarnings("unused")
			final
			Object first = list.get(0);
			// TODO map to xydra type, convert all elements
			throw new IllegalStateException("too lazy too implement this");
		} else if (object instanceof Long) {
			return toLong((Long) object);
		} else if (object instanceof Double) {
			return toDouble((Double) object);
		} else if (object instanceof Integer) {
			return toInteger((Integer) object);
		} else if (object instanceof String) {
			return toString((String) object);
		} else if (object instanceof Boolean) {
			return toBoolean((Boolean) object);
		} else if (object instanceof XId) {
			return toId((XId) object);
		}
		throw new IllegalStateException("too lazy too implement this for type "
				+ object.getClass().getName());
	}
}
