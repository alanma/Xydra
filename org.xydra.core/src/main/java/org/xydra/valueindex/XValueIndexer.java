package org.xydra.valueindex;

import java.util.ArrayList;
import java.util.List;

import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.XType;
import org.xydra.base.rmof.XReadableField;
import org.xydra.base.value.XAddressListValue;
import org.xydra.base.value.XAddressSetValue;
import org.xydra.base.value.XAddressSortedSetValue;
import org.xydra.base.value.XBinaryValue;
import org.xydra.base.value.XBooleanListValue;
import org.xydra.base.value.XBooleanValue;
import org.xydra.base.value.XDoubleListValue;
import org.xydra.base.value.XDoubleValue;
import org.xydra.base.value.XIdListValue;
import org.xydra.base.value.XIdSetValue;
import org.xydra.base.value.XIdSortedSetValue;
import org.xydra.base.value.XIntegerListValue;
import org.xydra.base.value.XIntegerValue;
import org.xydra.base.value.XLongListValue;
import org.xydra.base.value.XLongValue;
import org.xydra.base.value.XStringListValue;
import org.xydra.base.value.XStringSetValue;
import org.xydra.base.value.XStringValue;
import org.xydra.base.value.XValue;

/**
 * Abstract class which determines the Strings which are used to index
 * {@link XValue XValues} in an {@link ValueIndex}.
 *
 * @author kaidel
 *
 */

public abstract class XValueIndexer {
	private final ValueIndex index;

	/**
	 * Creates a new XValueIndexer using the given {@link ValueIndex} for
	 * indexing.
	 *
	 * @param index
	 *            the {@link ValueIndex} which will be used for indexing.
	 */
	public XValueIndexer(final ValueIndex index) {
		this.index = index;
	}

	/**
	 * Returns the {@link ValueIndex} used for indexing by this XValueIndexer.
	 *
	 * @return the {@link ValueIndex} used for indexing by this XValueIndexer.
	 */
	public ValueIndex getIndex() {
		return this.index;
	}

	/**
	 * Indexes the given pair of {@link XAddress} and {@link XValue} according
	 * to the rules of this XValueIndexer and the used {@link ValueIndex}.
	 *
	 * @param fieldAddress
	 *            the {@link XAddress} of the {@link XReadableField}, which
	 *            holds the given value.
	 * @param value
	 *            the {@link XValue} which is to be indexed.
	 * @throws RuntimeException
	 *             if the given {@link XAddress} is no fieldAddress.
	 */
	public void indexValue(final XAddress fieldAddress, final XValue value) {
		checkAddress(fieldAddress);

		if (value == null) {
			indexString(fieldAddress, value, getIndexStringForNull());
		} else {
			switch (value.getType()) {
			case Address:
				indexAddress(fieldAddress, value, (XAddress) value);
				break;
			case AddressList:
				indexAddressArray(fieldAddress, value, ((XAddressListValue) value).contents());
				break;

			case AddressSet:
				indexAddressArray(fieldAddress, value, ((XAddressSetValue) value).contents());
				break;
			case AddressSortedSet:
				indexAddressArray(fieldAddress, value, ((XAddressSortedSetValue) value).contents());
				break;
			case Boolean:
				indexBoolean(fieldAddress, value, ((XBooleanValue) value).contents());
				break;
			case BooleanList:
				indexBooleanArray(fieldAddress, value, ((XBooleanListValue) value).contents());
				break;
			case Binary:
				indexByteArray(fieldAddress, value, ((XBinaryValue) value).getValue());
				break;
			case Double:
				indexDouble(fieldAddress, value, ((XDoubleValue) value).contents());
				break;
			case DoubleList:
				indexDoubleArray(fieldAddress, value, ((XDoubleListValue) value).contents());
				break;
			case Id:
				indexId(fieldAddress, value, (XId) value);
				break;
			case IdList:
				indexIdArray(fieldAddress, value, ((XIdListValue) value).contents());
				break;
			case IdSet:
				indexIdArray(fieldAddress, value, ((XIdSetValue) value).contents());
				break;
			case IdSortedSet:
				indexIdArray(fieldAddress, value, ((XIdSortedSetValue) value).contents());
				break;
			case Integer:
				indexInteger(fieldAddress, value, ((XIntegerValue) value).contents());
				break;
			case IntegerList:
				indexIntegerArray(fieldAddress, value, ((XIntegerListValue) value).contents());
				break;
			case Null:
				indexNull(fieldAddress, value);
				break;
			case Long:
				indexLong(fieldAddress, value, ((XLongValue) value).contents());
				break;
			case LongList:
				indexLongArray(fieldAddress, value, ((XLongListValue) value).contents());
				break;
			case String:
				indexString(fieldAddress, value, ((XStringValue) value).contents());
				break;
			case StringList:
				indexStringArray(fieldAddress, value, ((XStringListValue) value).contents());
				break;
			case StringSet:
				indexStringArray(fieldAddress, value, ((XStringSetValue) value).contents());
				break;
			}
		}
	}

	/**
	 * Returns the String which would be used for indexing the given
	 * {@link XValue}.
	 *
	 * @param value
	 *            the {@link XValue} for which its index string is to be
	 *            returned.
	 * @return the String which would be used for indexing the given
	 *         {@link XValue}.
	 */
	public List<String> getIndexStrings(final XValue value) {
		final ArrayList<String> list = new ArrayList<String>();

		if (value == null) {
			list.add("null");
			return list;
		}

		switch (value.getType()) {
		case Address:
			list.add(getAddressIndexString((XAddress) value));
			break;

		case AddressList:
			for (final XAddress addr : ((XAddressListValue) value).contents()) {
				list.add(getAddressIndexString(addr));
			}
			break;

		case AddressSet:
			for (final XAddress addr : ((XAddressSetValue) value).contents()) {
				list.add(getAddressIndexString(addr));
			}
			break;

		case AddressSortedSet:
			for (final XAddress addr : ((XAddressSortedSetValue) value).contents()) {
				list.add(getAddressIndexString(addr));
			}
			break;

		case Boolean:
			list.add(getBooleanIndexString(((XBooleanValue) value).contents()));
			break;

		case BooleanList:
			for (final Boolean bool : ((XBooleanListValue) value).contents()) {
				list.add(getBooleanIndexString(bool));
			}
			break;

		case Binary:
			for (final Byte b : ((XBinaryValue) value).getValue()) {
				list.add(getByteIndexString(b));
			}
			break;

		case Double:
			list.add(getDoubleIndexString(((XDoubleValue) value).contents()));
			break;

		case DoubleList:
			for (final Double d : ((XDoubleListValue) value).contents()) {
				list.add(getDoubleIndexString(d));
			}
			break;

		case Id:
			list.add(getIdIndexString((XId) value));
			break;

		case IdList:
			for (final XId id : ((XIdListValue) value).contents()) {
				list.add(getIdIndexString(id));
			}
			break;
		case IdSet:
			for (final XId id : ((XIdSetValue) value).contents()) {
				list.add(getIdIndexString(id));
			}
			break;

		case IdSortedSet:
			for (final XId id : ((XIdSortedSetValue) value).contents()) {
				list.add(getIdIndexString(id));
			}
			break;

		case Integer:
			list.add(getIntegerIndexString(((XIntegerValue) value).contents()));
			break;

		case IntegerList:
			for (final Integer i : ((XIntegerListValue) value).contents()) {
				list.add(getIntegerIndexString(i));
			}
			break;

		case Null:
			list.add(null);
			break;

		case Long:
			list.add(getLongIndexString(((XLongValue) value).contents()));
			break;

		case LongList:
			for (final Long l : ((XLongListValue) value).contents()) {
				list.add(getLongIndexString(l));
			}
			break;

		case String:
			for (final String s : getStringIndexStrings(((XStringValue) value).contents())) {
				list.add(s);
			}
			break;

		case StringList:
			for (final String s1 : ((XStringListValue) value).contents()) {
				for (final String s2 : getStringIndexStrings(s1)) {
					list.add(s2);
				}
			}
			break;

		case StringSet:
			for (final String s1 : ((XStringSetValue) value).contents()) {
				for (final String s2 : getStringIndexStrings(s1)) {
					list.add(s2);
				}
			}
			break;

		}

		return list;
	}

	/**
	 * Deindexes the given pair of {@link XAddress} and {@link XValue} according
	 * to the rules of this XValueIndexer and the used {@link ValueIndex}.
	 *
	 * @param fieldAddress
	 *            the {@link XAddress} of the {@link XReadableField}, which
	 *            holds the given value.
	 * @param value
	 *            the {@link XValue} which is to be deindexed.
	 * @throws RuntimeException
	 *             if the given {@link XAddress} is no fieldAddress.
	 */
	public void deIndexValue(final XAddress fieldAddress, final XValue value) {
		checkAddress(fieldAddress);

		if (value == null) {
			deIndexString(fieldAddress, value, getIndexStringForNull());
		} else {
			switch (value.getType()) {
			case Address:
				deIndexAddress(fieldAddress, value, (XAddress) value);
				break;
			case AddressList:
				deIndexAddressArray(fieldAddress, value, ((XAddressListValue) value).contents());
				break;
			case AddressSet:
				deIndexAddressArray(fieldAddress, value, ((XAddressSetValue) value).contents());
				break;
			case AddressSortedSet:
				deIndexAddressArray(fieldAddress, value,
						((XAddressSortedSetValue) value).contents());
				break;
			case Boolean:
				deIndexBoolean(fieldAddress, value, ((XBooleanValue) value).contents());
				break;
			case BooleanList:
				deIndexBooleanArray(fieldAddress, value, ((XBooleanListValue) value).contents());
				break;
			case Binary:
				deIndexByteArray(fieldAddress, value, ((XBinaryValue) value).getValue());
				break;
			case Double:
				deIndexDouble(fieldAddress, value, ((XDoubleValue) value).contents());
				break;
			case DoubleList:
				deIndexDoubleArray(fieldAddress, value, ((XDoubleListValue) value).contents());
				break;
			case Id:
				deIndexId(fieldAddress, value, (XId) value);
				break;
			case IdList:
				deIndexIdArray(fieldAddress, value, ((XIdListValue) value).contents());
				break;
			case IdSet:
				deIndexIdArray(fieldAddress, value, ((XIdSetValue) value).contents());
				break;
			case IdSortedSet:
				deIndexIdArray(fieldAddress, value, ((XIdSortedSetValue) value).contents());
				break;
			case Integer:
				deIndexInteger(fieldAddress, value, ((XIntegerValue) value).contents());
				break;
			case IntegerList:
				deIndexIntegerArray(fieldAddress, value, ((XIntegerListValue) value).contents());
				break;
			case Long:
				deIndexLong(fieldAddress, value, ((XLongValue) value).contents());
				break;
			case LongList:
				deIndexLongArray(fieldAddress, value, ((XLongListValue) value).contents());
				break;
			case Null:
				deIndexId(fieldAddress, value, null);
				break;
			case String:
				deIndexString(fieldAddress, value, ((XStringValue) value).contents());
				break;
			case StringList:
				deIndexStringArray(fieldAddress, value, ((XStringListValue) value).contents());
				break;
			case StringSet:
				deIndexStringArray(fieldAddress, value, ((XStringSetValue) value).contents());
				break;
			}
		}
	}

	/**
	 * Checks if the given {@link XAddress} is a field address an throws a
	 * RuntimeException if this is not the case.
	 *
	 * @param fieldAddress
	 *            The {@link XAddress} which is to be checked.
	 */
	private static void checkAddress(final XAddress fieldAddress) {
		if (fieldAddress.getAddressedType() != XType.XFIELD) {
			throw new RuntimeException("The given address was no field address");
		}
	}

	/**
	 * Indexes the given tuple of {@link XAddress}, {@link XValue} and array of
	 * Strings according to the rules of this XValueIndexer and the used
	 * {@link ValueIndex}.
	 *
	 * This method assumes that the Strings in the given array are part of the
	 * contents of the given {@link XValue}. If this is not the case, the used
	 * {@link ValueIndex} will be in an inconsistent state after calling this
	 * method.
	 *
	 * @param fieldAddress
	 *            the {@link XAddress} of the {@link XReadableField}, which
	 *            holds the given value.
	 * @param value
	 *            the {@link XValue} which is to be indexed.
	 * @param strings
	 *            an array of Strings which are part of the given {@link XValue}
	 *            .
	 * @throws RuntimeException
	 *             if the given {@link XAddress} is no fieldAddress.
	 */
	public void indexStringArray(final XAddress fieldAddress, final XValue value, final String[] strings) {
		checkAddress(fieldAddress);

		for (final String str : strings) {
			indexString(fieldAddress, value, str);
		}
	}

	/**
	 * Deindexes the given tuple of {@link XAddress}, {@link XValue} and array
	 * of Strings according to the rules of this XValueIndexer and the used
	 * {@link ValueIndex}.
	 *
	 * This method assumes that the Strings in the given array are part of the
	 * contents of the given {@link XValue}. If this is not the case, the used
	 * {@link ValueIndex} will be in an inconsistent state after calling this
	 * method.
	 *
	 * @param fieldAddress
	 *            the {@link XAddress} of the {@link XReadableField}, which
	 *            holds the given value.
	 * @param value
	 *            the {@link XValue} which is to be indexed.
	 * @param strings
	 *            an array of Strings which are part of the given {@link XValue}
	 *            .
	 * @throws RuntimeException
	 *             if the given {@link XAddress} is no fieldAddress.
	 */
	public void deIndexStringArray(final XAddress fieldAddress, final XValue value, final String[] strings) {
		checkAddress(fieldAddress);

		for (final String str : strings) {
			deIndexString(fieldAddress, value, str);
		}
	}

	/**
	 * Indexes the given tuple of {@link XAddress}, {@link XValue} and array of
	 * Longs according to the rules of this XValueIndexer and the used
	 * {@link ValueIndex}.
	 *
	 * This method assumes that the Longs in the given array are part of the
	 * contents of the given {@link XValue}. If this is not the case, the used
	 * {@link ValueIndex} will be in an inconsistent state after calling this
	 * method.
	 *
	 * @param fieldAddress
	 *            the {@link XAddress} of the {@link XReadableField}, which
	 *            holds the given value.
	 * @param value
	 *            the {@link XValue} which is to be indexed.
	 * @param longs
	 *            an array of Longs which are part of the given {@link XValue} .
	 * @throws RuntimeException
	 *             if the given {@link XAddress} is no fieldAddress.
	 */
	public void indexLongArray(final XAddress fieldAddress, final XValue value, final long[] longs) {
		checkAddress(fieldAddress);

		for (final long l : longs) {
			indexLong(fieldAddress, value, l);
		}
	}

	/**
	 * Deindexes the given tuple of {@link XAddress}, {@link XValue} and array
	 * of Longs according to the rules of this XValueIndexer and the used
	 * {@link ValueIndex}.
	 *
	 * This method assumes that the Longs in the given array are part of the
	 * contents of the given {@link XValue}. If this is not the case, the used
	 * {@link ValueIndex} will be in an inconsistent state after calling this
	 * method.
	 *
	 * @param fieldAddress
	 *            the {@link XAddress} of the {@link XReadableField}, which
	 *            holds the given value.
	 * @param value
	 *            the {@link XValue} which is to be indexed.
	 * @param longs
	 *            an array of Longs which are part of the given {@link XValue} .
	 * @throws RuntimeException
	 *             if the given {@link XAddress} is no fieldAddress.
	 */
	public void deIndexLongArray(final XAddress fieldAddress, final XValue value, final long[] longs) {
		checkAddress(fieldAddress);

		for (final long l : longs) {
			deIndexLong(fieldAddress, value, l);
		}
	}

	/**
	 * Indexes the given tuple of {@link XAddress}, {@link XValue} and Long
	 * according to the rules of this XValueIndexer and the used
	 * {@link ValueIndex}.
	 *
	 * This method assumes that the given Long is part of the contents of the
	 * given {@link XValue}. If this is not the case, the used
	 * {@link ValueIndex} will be in an inconsistent state after calling this
	 * method.
	 *
	 * @param fieldAddress
	 *            the {@link XAddress} of the {@link XReadableField}, which
	 *            holds the given value.
	 * @param value
	 *            the {@link XValue} which is to be indexed.
	 * @param l
	 *            a Long which is part of the given {@link XValue}.
	 * @throws RuntimeException
	 *             if the given {@link XAddress} is no fieldAddress.
	 */
	public void indexLong(final XAddress fieldAddress, final XValue value, final Long l) {
		checkAddress(fieldAddress);

		final String key = getLongIndexString(l);
		this.index.index(key, fieldAddress, value);
	}

	public void indexNull(final XAddress fieldAddress, final XValue value) {
		checkAddress(fieldAddress);

		final String key = getIndexStringForNull();
		this.index.index(key, fieldAddress, value);
	}

	/**
	 * Deindexes the given tuple of {@link XAddress}, {@link XValue} and Long
	 * according to the rules of this XValueIndexer and the used
	 * {@link ValueIndex}.
	 *
	 * This method assumes that the given Long is part of the contents of the
	 * given {@link XValue}. If this is not the case, the used
	 * {@link ValueIndex} will be in an inconsistent state after calling this
	 * method.
	 *
	 * @param fieldAddress
	 *            the {@link XAddress} of the {@link XReadableField}, which
	 *            holds the given value.
	 * @param value
	 *            the {@link XValue} which is to be deindexed.
	 * @param l
	 *            a Long which is part of the given {@link XValue}.
	 * @throws RuntimeException
	 *             if the given {@link XAddress} is no fieldAddress.
	 */
	public void deIndexLong(final XAddress fieldAddress, final XValue value, final Long l) {
		checkAddress(fieldAddress);

		final String key = getLongIndexString(l);
		this.index.deIndex(key, fieldAddress, value);
	}

	/**
	 * Indexes the given tuple of {@link XAddress}, {@link XValue} and array of
	 * integers according to the rules of this XValueIndexer and the used
	 * {@link ValueIndex}.
	 *
	 * This method assumes that the integers in the given array are part of the
	 * contents of the given {@link XValue}. If this is not the case, the used
	 * {@link ValueIndex} will be in an inconsistent state after calling this
	 * method.
	 *
	 * @param fieldAddress
	 *            the {@link XAddress} of the {@link XReadableField}, which
	 *            holds the given value.
	 * @param value
	 *            the {@link XValue} which is to be indexed.
	 * @param integers
	 *            an array of integers which are part of the given
	 *            {@link XValue}.
	 * @throws RuntimeException
	 *             if the given {@link XAddress} is no fieldAddress.
	 */
	public void indexIntegerArray(final XAddress fieldAddress, final XValue value, final int[] integers) {
		checkAddress(fieldAddress);

		for (final Integer i : integers) {
			indexInteger(fieldAddress, value, i);
		}
	}

	/**
	 * Deindexes the given tuple of {@link XAddress}, {@link XValue} and array
	 * of integers according to the rules of this XValueIndexer and the used
	 * {@link ValueIndex}.
	 *
	 * This method assumes that the integers in the given array are part of the
	 * contents of the given {@link XValue}. If this is not the case, the used
	 * {@link ValueIndex} will be in an inconsistent state after calling this
	 * method.
	 *
	 * @param fieldAddress
	 *            the {@link XAddress} of the {@link XReadableField}, which
	 *            holds the given value.
	 * @param value
	 *            the {@link XValue} which is to be deindexed.
	 * @param integers
	 *            an array of integers which are part of the given
	 *            {@link XValue}.
	 * @throws RuntimeException
	 *             if the given {@link XAddress} is no fieldAddress.
	 */
	public void deIndexIntegerArray(final XAddress fieldAddress, final XValue value, final int[] integers) {
		checkAddress(fieldAddress);

		for (final Integer i : integers) {
			deIndexInteger(fieldAddress, value, i);
		}
	}

	/**
	 * Indexes the given tuple of {@link XAddress}, {@link XValue} and integer
	 * according to the rules of this XValueIndexer and the used
	 * {@link ValueIndex}.
	 *
	 * This method assumes that the given integer is part of the contents of the
	 * given {@link XValue}. If this is not the case, the used
	 * {@link ValueIndex} will be in an inconsistent state after calling this
	 * method.
	 *
	 * @param fieldAddress
	 *            the {@link XAddress} of the {@link XReadableField}, which
	 *            holds the given value.
	 * @param value
	 *            the {@link XValue} which is to be indexed.
	 * @param integer
	 *            an integer which is part of the given {@link XValue}.
	 * @throws RuntimeException
	 *             if the given {@link XAddress} is no fieldAddress.
	 */
	public void indexInteger(final XAddress fieldAddress, final XValue value, final int integer) {
		checkAddress(fieldAddress);

		final String key = getIntegerIndexString(integer);
		this.index.index(key, fieldAddress, value);
	}

	/**
	 * Deindexes the given tuple of {@link XAddress}, {@link XValue} and integer
	 * according to the rules of this XValueIndexer and the used
	 * {@link ValueIndex}.
	 *
	 * This method assumes that the given integer is part of the contents of the
	 * given {@link XValue}. If this is not the case, the used
	 * {@link ValueIndex} will be in an inconsistent state after calling this
	 * method.
	 *
	 * @param fieldAddress
	 *            the {@link XAddress} of the {@link XReadableField}, which
	 *            holds the given value.
	 * @param value
	 *            the {@link XValue} which is to be deindexed.
	 * @param integer
	 *            an integer which is part of the given {@link XValue}.
	 * @throws RuntimeException
	 *             if the given {@link XAddress} is no fieldAddress.
	 */
	public void deIndexInteger(final XAddress fieldAddress, final XValue value, final int integer) {
		checkAddress(fieldAddress);

		final String key = getIntegerIndexString(integer);
		this.index.deIndex(key, fieldAddress, value);
	}

	/**
	 * Indexes the given tuple of {@link XAddress}, {@link XValue} and array of
	 * {@link XId XIds} according to the rules of this XValueIndexer and the
	 * used {@link ValueIndex}.
	 *
	 * This method assumes that the {@link XId XIds} in the given array are part
	 * of the contents of the given {@link XValue}. If this is not the case, the
	 * used {@link ValueIndex} will be in an inconsistent state after calling
	 * this method.
	 *
	 * @param fieldAddress
	 *            the {@link XAddress} of the {@link XReadableField}, which
	 *            holds the given value.
	 * @param value
	 *            the {@link XValue} which is to be indexed.
	 * @param ids
	 *            an array of {@link XId XIds} which are part of the given
	 *            {@link XValue}.
	 * @throws RuntimeException
	 *             if the given {@link XAddress} is no fieldAddress.
	 */
	public void indexIdArray(final XAddress fieldAddress, final XValue value, final XId[] ids) {
		checkAddress(fieldAddress);

		for (final XId id : ids) {
			indexId(fieldAddress, value, id);
		}
	}

	/**
	 * Deindexes the given tuple of {@link XAddress}, {@link XValue} and array
	 * of {@link XId XIds} according to the rules of this XValueIndexer and the
	 * used {@link ValueIndex}.
	 *
	 * This method assumes that the {@link XId XIds} in the given array are part
	 * of the contents of the given {@link XValue}. If this is not the case, the
	 * used {@link ValueIndex} will be in an inconsistent state after calling
	 * this method.
	 *
	 * @param fieldAddress
	 *            the {@link XAddress} of the {@link XReadableField}, which
	 *            holds the given value.
	 * @param value
	 *            the {@link XValue} which is to be deindexed.
	 * @param ids
	 *            an array of {@link XId XIds} which are part of the given
	 *            {@link XValue}.
	 * @throws RuntimeException
	 *             if the given {@link XAddress} is no fieldAddress.
	 */
	public void deIndexIdArray(final XAddress fieldAddress, final XValue value, final XId[] ids) {
		checkAddress(fieldAddress);

		for (final XId id : ids) {
			deIndexId(fieldAddress, value, id);
		}
	}

	/**
	 * Indexes the given tuple of {@link XAddress}, {@link XValue} and array of
	 * doubles according to the rules of this XValueIndexer and the used
	 * {@link ValueIndex}.
	 *
	 * This method assumes that the doubles in the given array are part of the
	 * contents of the given {@link XValue}. If this is not the case, the used
	 * {@link ValueIndex} will be in an inconsistent state after calling this
	 * method.
	 *
	 * @param fieldAddress
	 *            the {@link XAddress} of the {@link XReadableField}, which
	 *            holds the given value.
	 * @param value
	 *            the {@link XValue} which is to be indexed.
	 * @param doubles
	 *            an array of doubles which are part of the given {@link XValue}
	 *            .
	 * @throws RuntimeException
	 *             if the given {@link XAddress} is no fieldAddress.
	 */
	public void indexDoubleArray(final XAddress fieldAddress, final XValue value, final double[] doubles) {
		checkAddress(fieldAddress);

		for (final Double d : doubles) {
			indexDouble(fieldAddress, value, d);
		}
	}

	/**
	 * Deindexes the given tuple of {@link XAddress}, {@link XValue} and array
	 * of doubles according to the rules of this XValueIndexer and the used
	 * {@link ValueIndex}.
	 *
	 * This method assumes that the doubles in the given array are part of the
	 * contents of the given {@link XValue}. If this is not the case, the used
	 * {@link ValueIndex} will be in an inconsistent state after calling this
	 * method.
	 *
	 * @param fieldAddress
	 *            the {@link XAddress} of the {@link XReadableField}, which
	 *            holds the given value.
	 * @param value
	 *            the {@link XValue} which is to be deindexed.
	 * @param doubles
	 *            an array of doubles which are part of the given {@link XValue}
	 *            .
	 * @throws RuntimeException
	 *             if the given {@link XAddress} is no fieldAddress.
	 */
	public void deIndexDoubleArray(final XAddress fieldAddress, final XValue value, final double[] doubles) {
		checkAddress(fieldAddress);

		for (final Double d : doubles) {
			deIndexDouble(fieldAddress, value, d);
		}
	}

	/**
	 * Indexes the given tuple of {@link XAddress}, {@link XValue} and double
	 * according to the rules of this XValueIndexer and the used
	 * {@link ValueIndex}.
	 *
	 * This method assumes that the given double is part of the contents of the
	 * given {@link XValue}. If this is not the case, the used
	 * {@link ValueIndex} will be in an inconsistent state after calling this
	 * method.
	 *
	 * @param fieldAddress
	 *            the {@link XAddress} of the {@link XReadableField}, which
	 *            holds the given value.
	 * @param value
	 *            the {@link XValue} which is to be indexed.
	 * @param d
	 *            a double which is part of the given {@link XValue}.
	 * @throws RuntimeException
	 *             if the given {@link XAddress} is no fieldAddress.
	 */
	public void indexDouble(final XAddress fieldAddress, final XValue value, final double d) {
		checkAddress(fieldAddress);

		final String key = getDoubleIndexString(d);
		this.index.index(key, fieldAddress, value);
	}

	/**
	 * Deindexes the given tuple of {@link XAddress}, {@link XValue} and double
	 * according to the rules of this XValueIndexer and the used
	 * {@link ValueIndex}.
	 *
	 * This method assumes that the given double is part of the contents of the
	 * given {@link XValue}. If this is not the case, the used
	 * {@link ValueIndex} will be in an inconsistent state after calling this
	 * method.
	 *
	 * @param fieldAddress
	 *            the {@link XAddress} of the {@link XReadableField}, which
	 *            holds the given value.
	 * @param value
	 *            the {@link XValue} which is to be deindexed.
	 * @param d
	 *            a double which is part of the given {@link XValue}.
	 * @throws RuntimeException
	 *             if the given {@link XAddress} is no fieldAddress.
	 */
	public void deIndexDouble(final XAddress fieldAddress, final XValue value, final double d) {
		checkAddress(fieldAddress);

		final String key = getDoubleIndexString(d);
		this.index.deIndex(key, fieldAddress, value);
	}

	/**
	 * Indexes the given tuple of {@link XAddress}, {@link XValue} and array of
	 * bytes according to the rules of this XValueIndexer and the used
	 * {@link ValueIndex}.
	 *
	 * This method assumes that the bytes in the given array are part of the
	 * contents of the given {@link XValue}. If this is not the case, the used
	 * {@link ValueIndex} will be in an inconsistent state after calling this
	 * method.
	 *
	 * @param fieldAddress
	 *            the {@link XAddress} of the {@link XReadableField}, which
	 *            holds the given value.
	 * @param value
	 *            the {@link XValue} which is to be indexed.
	 * @param bytes
	 *            an array of bytes which are part of the given {@link XValue} .
	 * @throws RuntimeException
	 *             if the given {@link XAddress} is no fieldAddress.
	 */
	public void indexByteArray(final XAddress fieldAddress, final XValue value, final byte[] bytes) {
		checkAddress(fieldAddress);

		for (final Byte b : bytes) {
			indexByte(fieldAddress, value, b);
		}
	}

	/**
	 * Deindexes the given tuple of {@link XAddress}, {@link XValue} and array
	 * of bytes according to the rules of this XValueIndexer and the used
	 * {@link ValueIndex}.
	 *
	 * This method assumes that the bytes in the given array are part of the
	 * contents of the given {@link XValue}. If this is not the case, the used
	 * {@link ValueIndex} will be in an inconsistent state after calling this
	 * method.
	 *
	 * @param fieldAddress
	 *            the {@link XAddress} of the {@link XReadableField}, which
	 *            holds the given value.
	 * @param value
	 *            the {@link XValue} which is to be deindexed.
	 * @param bytes
	 *            an array of bytes which are part of the given {@link XValue} .
	 * @throws RuntimeException
	 *             if the given {@link XAddress} is no fieldAddress.
	 */
	public void deIndexByteArray(final XAddress fieldAddress, final XValue value, final byte[] bytes) {
		checkAddress(fieldAddress);

		for (final Byte b : bytes) {
			deIndexByte(fieldAddress, value, b);
		}
	}

	/**
	 * Indexes the given tuple of {@link XAddress}, {@link XValue} and byte
	 * according to the rules of this XValueIndexer and the used
	 * {@link ValueIndex}.
	 *
	 * This method assumes that the given byte is part of the contents of the
	 * given {@link XValue}. If this is not the case, the used
	 * {@link ValueIndex} will be in an inconsistent state after calling this
	 * method.
	 *
	 * @param fieldAddress
	 *            the {@link XAddress} of the {@link XReadableField}, which
	 *            holds the given value.
	 * @param value
	 *            the {@link XValue} which is to be indexed.
	 * @param b
	 *            a byte which is part of the given {@link XValue}.
	 * @throws RuntimeException
	 *             if the given {@link XAddress} is no fieldAddress.
	 */
	public void indexByte(final XAddress fieldAddress, final XValue value, final byte b) {
		checkAddress(fieldAddress);

		final String key = getByteIndexString(b);
		this.index.index(key, fieldAddress, value);
	}

	/**
	 * Deindexes the given tuple of {@link XAddress}, {@link XValue} and byte
	 * according to the rules of this XValueIndexer and the used
	 * {@link ValueIndex}.
	 *
	 * This method assumes that the given byte is part of the contents of the
	 * given {@link XValue}. If this is not the case, the used
	 * {@link ValueIndex} will be in an inconsistent state after calling this
	 * method.
	 *
	 * @param fieldAddress
	 *            the {@link XAddress} of the {@link XReadableField}, which
	 *            holds the given value.
	 * @param value
	 *            the {@link XValue} which is to be deindexed.
	 * @param b
	 *            a byte which is part of the given {@link XValue}.
	 * @throws RuntimeException
	 *             if the given {@link XAddress} is no fieldAddress.
	 */
	public void deIndexByte(final XAddress fieldAddress, final XValue value, final byte b) {
		checkAddress(fieldAddress);

		final String key = getByteIndexString(b);
		this.index.deIndex(key, fieldAddress, value);
	}

	/**
	 * Indexes the given tuple of {@link XAddress}, {@link XValue} and array of
	 * booleans according to the rules of this XValueIndexer and the used
	 * {@link ValueIndex}.
	 *
	 * This method assumes that the booleans in the given array are part of the
	 * contents of the given {@link XValue}. If this is not the case, the used
	 * {@link ValueIndex} will be in an inconsistent state after calling this
	 * method.
	 *
	 * @param fieldAddress
	 *            the {@link XAddress} of the {@link XReadableField}, which
	 *            holds the given value.
	 * @param value
	 *            the {@link XValue} which is to be indexed.
	 * @param bools
	 *            an array of booleans which are part of the given
	 *            {@link XValue} .
	 * @throws RuntimeException
	 *             if the given {@link XAddress} is no fieldAddress.
	 */
	public void indexBooleanArray(final XAddress fieldAddress, final XValue value, final boolean[] bools) {
		checkAddress(fieldAddress);

		for (final Boolean b : bools) {
			indexBoolean(fieldAddress, value, b);
		}
	}

	/**
	 * Deindexes the given tuple of {@link XAddress}, {@link XValue} and array
	 * of booleans according to the rules of this XValueIndexer and the used
	 * {@link ValueIndex}.
	 *
	 * This method assumes that the booleans in the given array are part of the
	 * contents of the given {@link XValue}. If this is not the case, the used
	 * {@link ValueIndex} will be in an inconsistent state after calling this
	 * method.
	 *
	 * @param fieldAddress
	 *            the {@link XAddress} of the {@link XReadableField}, which
	 *            holds the given value.
	 * @param value
	 *            the {@link XValue} which is to be deindexed.
	 * @param bools
	 *            an array of booleans which are part of the given
	 *            {@link XValue} .
	 * @throws RuntimeException
	 *             if the given {@link XAddress} is no fieldAddress.
	 */
	public void deIndexBooleanArray(final XAddress fieldAddress, final XValue value, final boolean[] bools) {
		checkAddress(fieldAddress);

		for (final Boolean b : bools) {
			deIndexBoolean(fieldAddress, value, b);
		}
	}

	/**
	 * Indexes the given tuple of {@link XAddress}, {@link XValue} and boolean
	 * according to the rules of this XValueIndexer and the used
	 * {@link ValueIndex}.
	 *
	 * This method assumes that the given boolean is part of the contents of the
	 * given {@link XValue}. If this is not the case, the used
	 * {@link ValueIndex} will be in an inconsistent state after calling this
	 * method.
	 *
	 * @param fieldAddress
	 *            the {@link XAddress} of the {@link XReadableField}, which
	 *            holds the given value.
	 * @param value
	 *            the {@link XValue} which is to be indexed.
	 * @param bool
	 *            a boolean which is part of the given {@link XValue}.
	 * @throws RuntimeException
	 *             if the given {@link XAddress} is no fieldAddress.
	 */
	public void indexBoolean(final XAddress fieldAddress, final XValue value, final boolean bool) {
		checkAddress(fieldAddress);

		final String key = getBooleanIndexString(bool);
		this.index.index(key, fieldAddress, value);
	}

	/**
	 * Deindexes the given tuple of {@link XAddress}, {@link XValue} and boolean
	 * according to the rules of this XValueIndexer and the used
	 * {@link ValueIndex}.
	 *
	 * This method assumes that the given boolean is part of the contents of the
	 * given {@link XValue}. If this is not the case, the used
	 * {@link ValueIndex} will be in an inconsistent state after calling this
	 * method.
	 *
	 * @param fieldAddress
	 *            the {@link XAddress} of the {@link XReadableField}, which
	 *            holds the given value.
	 * @param value
	 *            the {@link XValue} which is to be deindexed.
	 * @param bool
	 *            a boolean which is part of the given {@link XValue}.
	 * @throws RuntimeException
	 *             if the given {@link XAddress} is no fieldAddress.
	 */
	public void deIndexBoolean(final XAddress fieldAddress, final XValue value, final boolean bool) {
		checkAddress(fieldAddress);

		final String key = getBooleanIndexString(bool);
		this.index.deIndex(key, fieldAddress, value);
	}

	/**
	 * Indexes the given tuple of {@link XAddress}, {@link XValue} and String
	 * according to the rules of this XValueIndexer and the used
	 * {@link ValueIndex}.
	 *
	 * This method assumes that the given String is part of the contents of the
	 * given {@link XValue}. If this is not the case, the used
	 * {@link ValueIndex} will be in an inconsistent state after calling this
	 * method.
	 *
	 * @param fieldAddress
	 *            the {@link XAddress} of the {@link XReadableField}, which
	 *            holds the given value.
	 * @param value
	 *            the {@link XValue} which is to be indexed.
	 * @param string
	 *            a String which is part of the given {@link XValue}.
	 * @throws RuntimeException
	 *             if the given {@link XAddress} is no fieldAddress.
	 */
	public void indexString(final XAddress fieldAddress, final XValue value, final String string) {
		checkAddress(fieldAddress);

		for (final String key : getStringIndexStrings(string)) {
			this.index.index(key, fieldAddress, value);
		}
	}

	/**
	 * Deindexes the given tuple of {@link XAddress}, {@link XValue} and String
	 * according to the rules of this XValueIndexer and the used
	 * {@link ValueIndex}.
	 *
	 * This method assumes that the given String is part of the contents of the
	 * given {@link XValue}. If this is not the case, the used
	 * {@link ValueIndex} will be in an inconsistent state after calling this
	 * method.
	 *
	 * @param fieldAddress
	 *            the {@link XAddress} of the {@link XReadableField}, which
	 *            holds the given value.
	 * @param value
	 *            the {@link XValue} which is to be deindexed.
	 * @param string
	 *            a String which is part of the given {@link XValue}.
	 * @throws RuntimeException
	 *             if the given {@link XAddress} is no fieldAddress.
	 */
	public void deIndexString(final XAddress fieldAddress, final XValue value, final String string) {
		checkAddress(fieldAddress);

		for (final String key : getStringIndexStrings(string)) {
			this.index.deIndex(key, fieldAddress, value);
		}
	}

	/**
	 * Indexes the given tuple of {@link XAddress}, {@link XValue} and
	 * {@link XId} according to the rules of this XValueIndexer and the used
	 * {@link ValueIndex}.
	 *
	 * This method assumes that the given {@link XId} is part of the contents of
	 * the given {@link XValue}. If this is not the case, the used
	 * {@link ValueIndex} will be in an inconsistent state after calling this
	 * method.
	 *
	 * @param fieldAddress
	 *            the {@link XAddress} of the {@link XReadableField}, which
	 *            holds the given value.
	 * @param value
	 *            the {@link XValue} which is to be indexed.
	 * @param id
	 *            an {@link XId} which is part of the given {@link XValue}.
	 * @throws RuntimeException
	 *             if the given {@link XAddress} is no fieldAddress.
	 */
	public void indexId(final XAddress fieldAddress, final XValue value, final XId id) {
		checkAddress(fieldAddress);

		final String key = getIdIndexString(id);
		this.index.index(key, fieldAddress, value);
	}

	/**
	 * Deindexes the given tuple of {@link XAddress}, {@link XValue} and
	 * {@link XId} according to the rules of this XValueIndexer and the used
	 * {@link ValueIndex}.
	 *
	 * This method assumes that the given {@link XId} is part of the contents of
	 * the given {@link XValue}. If this is not the case, the used
	 * {@link ValueIndex} will be in an inconsistent state after calling this
	 * method.
	 *
	 * @param fieldAddress
	 *            the {@link XAddress} of the {@link XReadableField}, which
	 *            holds the given value.
	 * @param value
	 *            the {@link XValue} which is to be deindexed.
	 * @param id
	 *            an {@link XId} which is part of the given {@link XValue}.
	 * @throws RuntimeException
	 *             if the given {@link XAddress} is no fieldAddress.
	 */
	public void deIndexId(final XAddress fieldAddress, final XValue value, final XId id) {
		checkAddress(fieldAddress);

		final String key = getIdIndexString(id);
		this.index.deIndex(key, fieldAddress, value);
	}

	/**
	 * Indexes the given tuple of {@link XAddress}, {@link XValue} and array of
	 * {@link XAddress XAddresses} according to the rules of this XValueIndexer
	 * and the used {@link ValueIndex}.
	 *
	 * This method assumes that the {@link XAddress XAddresses} in the given
	 * array are part of the contents of the given {@link XValue}. If this is
	 * not the case, the used {@link ValueIndex} will be in an inconsistent
	 * state after calling this method.
	 *
	 * @param fieldAddress
	 *            the {@link XAddress} of the {@link XReadableField}, which
	 *            holds the given value.
	 * @param value
	 *            the {@link XValue} which is to be indexed.
	 * @param addresses
	 *            an array of {@link XAddress XAddresses} which are part of the
	 *            given {@link XValue} .
	 * @throws RuntimeException
	 *             if the given {@link XAddress} is no fieldAddress.
	 */
	public void indexAddressArray(final XAddress fieldAddress, final XValue value, final XAddress[] addresses) {
		checkAddress(fieldAddress);

		for (final XAddress adr : addresses) {
			indexAddress(fieldAddress, value, adr);
		}
	}

	/**
	 * Deindexes the given tuple of {@link XAddress}, {@link XValue} and array
	 * of {@link XAddress XAddresses} according to the rules of this
	 * XValueIndexer and the used {@link ValueIndex}.
	 *
	 * This method assumes that the {@link XAddress XAddresses} in the given
	 * array are part of the contents of the given {@link XValue}. If this is
	 * not the case, the used {@link ValueIndex} will be in an inconsistent
	 * state after calling this method.
	 *
	 * @param fieldAddress
	 *            the {@link XAddress} of the {@link XReadableField}, which
	 *            holds the given value.
	 * @param value
	 *            the {@link XValue} which is to be deindexed.
	 * @param addresses
	 *            an array of {@link XAddress XAddresses} which are part of the
	 *            given {@link XValue} .
	 * @throws RuntimeException
	 *             if the given {@link XAddress} is no fieldAddress.
	 */
	public void deIndexAddressArray(final XAddress fieldAddress, final XValue value, final XAddress[] addresses) {
		checkAddress(fieldAddress);

		for (final XAddress adr : addresses) {
			deIndexAddress(fieldAddress, value, adr);
		}
	}

	/**
	 * Indexes the given tuple of {@link XAddress}, {@link XValue} and
	 * {@link XAddress} according to the rules of this XValueIndexer and the
	 * used {@link ValueIndex}.
	 *
	 * This method assumes that the given {@link XAddress} is part of the
	 * contents of the given {@link XValue}. If this is not the case, the used
	 * {@link ValueIndex} will be in an inconsistent state after calling this
	 * method.
	 *
	 * @param fieldAddress
	 *            the {@link XAddress} of the {@link XReadableField}, which
	 *            holds the given value.
	 * @param value
	 *            the {@link XValue} which is to be indexed.
	 * @param address
	 *            an {@link XAddress} which is part of the given {@link XValue}.
	 * @throws RuntimeException
	 *             if the given {@link XAddress} is no fieldAddress.
	 */
	public void indexAddress(final XAddress fieldAddress, final XValue value, final XAddress address) {
		checkAddress(fieldAddress);

		final String key = getAddressIndexString(address);
		this.index.index(key, fieldAddress, value);
	}

	/**
	 * Deindexes the given tuple of {@link XAddress}, {@link XValue} and
	 * {@link XAddress} according to the rules of this XValueIndexer and the
	 * used {@link ValueIndex}.
	 *
	 * This method assumes that the given {@link XAddress} is part of the
	 * contents of the given {@link XValue}. If this is not the case, the used
	 * {@link ValueIndex} will be in an inconsistent state after calling this
	 * method.
	 *
	 * @param fieldAddress
	 *            the {@link XAddress} of the {@link XReadableField}, which
	 *            holds the given value.
	 * @param value
	 *            the {@link XValue} which is to be deindexed.
	 * @param address
	 *            an {@link XAddress} which is part of the given {@link XValue}.
	 * @throws RuntimeException
	 *             if the given {@link XAddress} is no fieldAddress.
	 */
	public void deIndexAddress(final XAddress fieldAddress, final XValue value, final XAddress address) {
		checkAddress(fieldAddress);

		final String key = getAddressIndexString(address);
		this.index.deIndex(key, fieldAddress, value);
	}

	// ---- Methods returning the index strings ----

	/**
	 * Returns the String which will be used for indexing the given Long.
	 *
	 * @param value
	 *            the Long which index string is to be returned
	 * @return the String which will be used for indexing the given Long.
	 */
	public abstract String getLongIndexString(Long value);

	/**
	 * Returns the String which will be used for indexing the given Integer.
	 *
	 * @param value
	 *            the Integer which index string is to be returned
	 * @return the String which will be used for indexing the given Integer.
	 */
	public abstract String getIntegerIndexString(Integer value);

	/**
	 * Returns the String which will be used for indexing the given Double.
	 *
	 * @param value
	 *            the Double which index string is to be returned
	 * @return the String which will be used for indexing the given Double.
	 */
	public abstract String getDoubleIndexString(Double value);

	/**
	 * Returns the String which will be used for indexing the given Byte.
	 *
	 * @param value
	 *            the Byte which index string is to be returned
	 * @return the String which will be used for indexing the given Byte.
	 */
	public abstract String getByteIndexString(Byte value);

	/**
	 * Returns the String which will be used for indexing the given Boolean.
	 *
	 * @param value
	 *            the Long which index string is to be returned
	 * @return the String which will be used for indexing the given Boolean.
	 */
	public abstract String getBooleanIndexString(Boolean value);

	/**
	 * Returns the array of Strings which will be used for indexing the given
	 * String.
	 *
	 * @param value
	 *            the String which index string is to be returned
	 * @return the array of Strings which will be used for indexing the given
	 *         String.
	 */
	public abstract String[] getStringIndexStrings(String value);

	/**
	 * Returns the String which will be used for indexing the given {@link XId}.
	 *
	 * @param value
	 *            the {@link XId} which index string is to be returned
	 * @return the String which will be used for indexing the given {@link XId}.
	 */
	public abstract String getIdIndexString(XId value);

	/**
	 * Returns the String which will be used for indexing the given
	 * {@link XAddress}.
	 *
	 * @param value
	 *            the {@link XAddress} which index string is to be returned
	 * @return the String which will be used for indexing the given
	 *         {@link XAddress}.
	 */
	public abstract String getAddressIndexString(XAddress value);

	/**
	 * Returns the String which will be used for indexing null.
	 *
	 * @return the String which will be used for indexing null.
	 */
	public abstract String getIndexStringForNull();
}
