package org.xydra.gwt.editor.value;

import java.util.Iterator;

import org.xydra.base.Base;
import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.value.XAddressListValue;
import org.xydra.base.value.XBinaryValue;
import org.xydra.base.value.XBooleanListValue;
import org.xydra.base.value.XBooleanValue;
import org.xydra.base.value.XDoubleListValue;
import org.xydra.base.value.XDoubleValue;
import org.xydra.base.value.XIdListValue;
import org.xydra.base.value.XIdSetValue;
import org.xydra.base.value.XIntegerListValue;
import org.xydra.base.value.XIntegerValue;
import org.xydra.base.value.XListValue;
import org.xydra.base.value.XLongListValue;
import org.xydra.base.value.XLongValue;
import org.xydra.base.value.XStringListValue;
import org.xydra.base.value.XStringSetValue;
import org.xydra.base.value.XValue;
import org.xydra.index.iterator.AbstractFilteringIterator;
import org.xydra.index.iterator.AbstractTransformingIterator;
import org.xydra.index.iterator.NoneIterator;
import org.xydra.index.iterator.SingleValueIterator;

/**
 * Collection of procedures to convert XValues to different types but keep as
 * much information as possible while doing so.
 */
abstract public class XValueUtils {

	protected static Object listGetFirst(final XListValue<?> v) {
		if (!v.isEmpty()) {
			return v.get(0);
		} else {
			return "";
		}
	}

	static public String asString(final XValue value) {
		if (value == null) {
			return "";
		}
		if (value instanceof XBinaryValue) {
			return byteArrayToString(((XBinaryValue) value).getValue());
		} else if (value instanceof XListValue<?>) {
			return listGetFirst((XListValue<?>) value).toString();
		} else {
			return value.toString();
		}
	}

	private static String byteArrayToString(final byte[] bytes) {
		final char[] chars = new char[bytes.length];
		for (int i = 0; i < bytes.length; i++) {
			chars[i] = (char) bytes[i];
		}
		return new String(chars);
	}

	static public Iterator<String> asStringList(final XValue value) {
		if (value == null) {
			return NoneIterator.<String> create();
		} else if (value instanceof XStringListValue) {
			return ((XStringListValue) value).iterator();
		} else if (value instanceof XStringSetValue) {
			return ((XStringSetValue) value).iterator();
		} else if (value instanceof XBinaryValue) {
			return new SingleValueIterator<String>(
					byteArrayToString(((XBinaryValue) value).getValue()));
		} else if (value instanceof XListValue<?>) {
			return transform((XListValue<?>) value);
		} else {
			return new SingleValueIterator<String>(value.toString());
		}
	}

	static private <E> Iterator<String> transform(final XListValue<E> value) {
		return new AbstractTransformingIterator<E, String>(value.iterator()) {
			@Override
			public String transform(final E in) {
				if (in == null) {
					return null;
				}
				return in.toString();
			}
		};
	}

	static public XId asXID(final XValue value) {

		if (value == null) {
			return Base.createUniqueId();
		}

		if (value instanceof XId) {
			return (XId) value;
		} else if (value instanceof XIdListValue) {
			final XIdListValue lv = (XIdListValue) value;
			if (!lv.isEmpty()) {
				return lv.get(0);
			} else {
				return Base.createUniqueId();
			}
		} else {
			final XId id = generateXid(asString(value));
			if (id == null) {
				return Base.createUniqueId();
			}
			return id;
		}
	}

	static public XAddress asAddress(final XValue value) {

		if (value == null) {
			return Base.toAddress(Base.createUniqueId(), Base.createUniqueId(), Base.createUniqueId(),
					Base.createUniqueId());
		}

		if (value instanceof XAddress) {
			return (XAddress) value;
		} else if (value instanceof XAddressListValue) {
			final XAddressListValue lv = (XAddressListValue) value;
			if (!lv.isEmpty()) {
				return lv.get(0);
			} else {
				return Base.toAddress(Base.createUniqueId(), Base.createUniqueId(), Base.createUniqueId(),
						Base.createUniqueId());
			}
		} else {
			final XAddress id = generateAddress(asString(value));
			if (id == null) {
				return Base.toAddress(Base.createUniqueId(), Base.createUniqueId(), Base.createUniqueId(),
						Base.createUniqueId());
			}
			return id;
		}
	}

	private static final String nameStartChar = "A-Z_a-z\\xC0-\\xD6\\xD8-\\xF6"
			+ "\\u00F8-\\u02FF\\u0370-\\u037D\\u037F-\\u1FFF\\u200C-\\u200D"
			+ "\\u2070-\\u218F\\u2C00-\\u2FEF\\u3001-\\uD7FF\\uF900-\\uFDCF" + "\\uFDF0-\\uFFFD";
	private static final String nameChar = nameStartChar
			+ "\\-\\.0-9\\xB7\\u0300-\u036F\\u203F-\\u2040";
	private static final String startClass = "[" + nameStartChar + "]";

	public static XId generateXid(final String string) {

		String cleaned = string.replaceAll("[^" + nameChar + "]+", "");

		if (cleaned.length() == 0) {
			return null;
		}

		if (!cleaned.substring(0, 1).matches(startClass)) {
			cleaned = "_" + cleaned;
		}

		return Base.toId(cleaned);
	}

	public static XAddress generateAddress(final String text) {

		try {
			return Base.toAddress(text);
		} catch (final Exception e) {
			return Base.toAddress("/dummy");
		}
	}

	static public Iterator<XAddress> asAddressList(final XValue value) {
		if (value == null) {
			return NoneIterator.<XAddress> create();
		} else if (value instanceof XAddressListValue) {
			return ((XAddressListValue) value).iterator();
		}
		if (value instanceof XAddressListValue) {
			return ((XAddressListValue) value).iterator();
		} else if (value instanceof XListValue<?>) {
			final Iterator<XAddress> it = new AbstractTransformingIterator<String, XAddress>(
					transform((XListValue<?>) value)) {
				@Override
				public XAddress transform(final String in) {
					if (in == null) {
						return null;
					}
					return generateAddress(in);
				}
			};
			return new AbstractFilteringIterator<XAddress>(it) {
				@Override
				protected boolean matchesFilter(final XAddress entry) {
					return entry != null;
				}
			};
		} else {
			final XAddress xid = asAddress(value);
			if (xid == null) {
				return NoneIterator.<XAddress> create();
			}
			return new SingleValueIterator<XAddress>(xid);
		}
	}

	static public Iterator<XId> asXIDList(final XValue value) {
		if (value == null) {
			return NoneIterator.<XId> create();
		} else if (value instanceof XIdListValue) {
			return ((XIdListValue) value).iterator();
		}
		if (value instanceof XIdSetValue) {
			return ((XIdSetValue) value).iterator();
		} else if (value instanceof XListValue<?>) {
			final Iterator<XId> it = new AbstractTransformingIterator<String, XId>(
					transform((XListValue<?>) value)) {
				@Override
				public XId transform(final String in) {
					if (in == null) {
						return null;
					}
					return generateXid(in);
				}
			};
			return new AbstractFilteringIterator<XId>(it) {
				@Override
				protected boolean matchesFilter(final XId entry) {
					return entry != null;
				}
			};
		} else {
			final XId xid = asXID(value);
			if (xid == null) {
				return NoneIterator.<XId> create();
			}
			return new SingleValueIterator<XId>(xid);
		}
	}

	static public double asDouble(final XValue value) {

		if (value == null) {
			return 0.;
		}

		if (value instanceof XDoubleValue) {
			return ((XDoubleValue) value).contents();
		}
		if (value instanceof XIntegerValue) {
			return ((XIntegerValue) value).contents();
		}
		if (value instanceof XLongValue) {
			return ((XLongValue) value).contents();
		}
		if (value instanceof XListValue<?>) {
			final Object o = listGetFirst((XListValue<?>) value);
			if (o == null) {
				return 0.;
			} else if (o instanceof Double) {
				return (Double) o;
			} else if (o instanceof Integer) {
				return (Integer) o;
			} else if (o instanceof Long) {
				return (Long) o;
			}
			return generateDouble(o.toString());
		} else {
			return generateDouble(value.toString());
		}

	}

	static public double generateDouble(final Object object) {
		if (object instanceof Number) {
			return ((Number) object).doubleValue();
		}
		final String string = object.toString();
		try {
			return Double.parseDouble(string);
		} catch (final NumberFormatException nfe) {
			final String cleaned = string.replaceAll("[^0-9.]", "");
			try {
				return Double.parseDouble(cleaned);
			} catch (final NumberFormatException nfe2) {
				return 0.0;
			}
		}
	}

	static public <E> Iterator<Double> transformToDoubles(final XListValue<E> value) {
		return new AbstractTransformingIterator<E, Double>(value.iterator()) {
			@Override
			public Double transform(final E in) {
				if (in == null) {
					return 0.;
				}
				return generateDouble(in);
			}
		};
	}

	static public <E> Iterator<Integer> transformToIntegers(final XListValue<E> value) {
		return new AbstractTransformingIterator<E, Integer>(value.iterator()) {
			@Override
			public Integer transform(final E in) {
				if (in == null) {
					return 0;
				}
				return (int) generateLong(in);
			}
		};
	}

	static public <E> Iterator<Long> transformToLongs(final XListValue<E> value) {
		return new AbstractTransformingIterator<E, Long>(value.iterator()) {
			@Override
			public Long transform(final E in) {
				if (in == null) {
					return 0l;
				}
				return generateLong(in);
			}
		};
	}

	static public Iterator<Double> asDoubleList(final XValue value) {
		if (value == null) {
			return NoneIterator.<Double> create();
		} else if (value instanceof XDoubleListValue) {
			return ((XDoubleListValue) value).iterator();
		} else if (value instanceof XListValue<?>) {
			return transformToDoubles((XListValue<?>) value);
		} else {
			return new SingleValueIterator<Double>(asDouble(value));
		}
	}

	static public Iterator<Long> asLongList(final XValue value) {
		if (value == null) {
			return NoneIterator.<Long> create();
		} else if (value instanceof XLongListValue) {
			return ((XLongListValue) value).iterator();
		} else if (value instanceof XListValue<?>) {
			return transformToLongs((XListValue<?>) value);
		} else {
			return new SingleValueIterator<Long>(asLong(value));
		}
	}

	static public Iterator<Integer> asIntegerList(final XValue value) {
		if (value == null) {
			return NoneIterator.<Integer> create();
		} else if (value instanceof XIntegerListValue) {
			return ((XIntegerListValue) value).iterator();
		} else if (value instanceof XListValue<?>) {
			return transformToIntegers((XListValue<?>) value);
		} else {
			return new SingleValueIterator<Integer>(asInteger(value));
		}
	}

	static public boolean asBoolean(final XValue value) {
		if (value == null) {
			return false;
		}
		if (value instanceof XBooleanValue) {
			return ((XBooleanValue) value).contents();
		} else if (value instanceof XBooleanListValue) {
			final XBooleanListValue lv = (XBooleanListValue) value;
			if (!lv.isEmpty()) {
				return lv.get(0);
			} else {
				return false;
			}
		} else {
			return generateBoolean(asString(value));
		}
	}

	public static boolean generateBoolean(final String s) {
		try {
			return Double.parseDouble(s) != 0;
		} catch (final NumberFormatException nfe) {
			return Boolean.parseBoolean(s);
		}
	}

	static public Iterator<Boolean> asBooleanList(final XValue value) {
		if (value == null) {
			return NoneIterator.<Boolean> create();
		} else if (value instanceof XBooleanListValue) {
			return ((XBooleanListValue) value).iterator();
		} else if (value instanceof XListValue<?>) {
			return transformToBoolean((XListValue<?>) value);
		} else {
			return new SingleValueIterator<Boolean>(asBoolean(value));
		}
	}

	static private <E> Iterator<Boolean> transformToBoolean(final XListValue<E> value) {
		return new AbstractTransformingIterator<E, Boolean>(value.iterator()) {
			@Override
			public Boolean transform(final E in) {
				if (in == null) {
					return false;
				}
				return generateBoolean(in.toString());
			}
		};
	}

	static public long asLong(final XValue value) {

		if (value == null) {
			return 0L;
		}

		if (value instanceof XDoubleValue) {
			return (long) ((XDoubleValue) value).contents();
		}
		if (value instanceof XIntegerValue) {
			return ((XIntegerValue) value).contents();
		}
		if (value instanceof XLongValue) {
			return ((XLongValue) value).contents();
		}
		if (value instanceof XListValue<?>) {
			final Object o = listGetFirst((XListValue<?>) value);
			if (o == null) {
				return 0L;
			} else if (o instanceof Double) {
				return ((Double) o).longValue();
			} else if (o instanceof Integer) {
				return (Integer) o;
			} else if (o instanceof Long) {
				return (Long) o;
			}
			return generateLong(o.toString());
		} else {
			return generateLong(value.toString());
		}

	}

	static public long generateLong(final Object object) {
		if (object instanceof Number) {
			return ((Number) object).longValue();
		}
		final String string = object.toString();
		try {
			return Long.parseLong(string);
		} catch (final NumberFormatException nfe) {
			final String cleaned = string.replaceAll("[^0-9]", "");
			try {
				return Long.parseLong(cleaned);
			} catch (final NumberFormatException nfe2) {
				return 0L;
			}
		}
	}

	static public int asInteger(final XValue value) {
		return (int) asLong(value);
	}

	static public byte[] asByteList(final XValue value) {
		if (value == null) {
			return new byte[0];
		}

		if (value instanceof XBinaryValue) {
			return ((XBinaryValue) value).getValue();
		} else {
			final char[] chars = value.toString().toCharArray();
			final byte[] bytes = new byte[chars.length];
			for (int i = 0; i < chars.length; i++) {
				bytes[i] = (byte) chars[i];
			}
			return bytes;
		}

	}

}
