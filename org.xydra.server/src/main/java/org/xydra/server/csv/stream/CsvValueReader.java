package org.xydra.server.csv.stream;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.xydra.base.Base;
import org.xydra.base.BaseRuntime;
import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.value.ValueType;
import org.xydra.base.value.XBinaryValue;
import org.xydra.base.value.XCollectionValue;
import org.xydra.base.value.XV;
import org.xydra.base.value.XValue;
import org.xydra.core.X;
import org.xydra.core.XX;
import org.xydra.core.serialize.SerializedValue;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;

/**
 * The dual class to {@link AbstractValueStream}
 *
 * @author xamde
 *
 */
public class CsvValueReader {

	private static final Logger log = LoggerFactory.getLogger(CsvValueReader.class);

	/**
	 * @param valueString
	 *            may not contain whitespace
	 * @param type
	 * @return the parsed XValue
	 */
	public static XValue parseValue(final String valueString, final ValueType type) {
		if (type.isCollection()) {
			assert valueString.startsWith("[") : valueString;
			assert valueString.endsWith("]");
			return parseCollectionValue(valueString.substring(1, valueString.length() - 1), type);
		} else {
			return parsePlainValue(valueString, type);
		}
	}

	public static XValue parsePlainValue(final String valueString, final ValueType type) {
		if (valueString.equals("null")) {
			return null;
		}
		switch (type) {
		case Address:
			return BaseRuntime.getIDProvider().fromAddress(valueString.trim());
		case Boolean:
			return XV.toValue(Boolean.parseBoolean(valueString.trim()));
		case Binary: {
			final byte[] bytes = SerializedValue.deserializeBinaryContent(valueString.trim());
			return XV.toValue(bytes);
		}
		case Double:
			return BaseRuntime.getValueFactory().createDoubleValue(Double.parseDouble(valueString.trim()));
		case Integer:
			return BaseRuntime.getValueFactory().createIntegerValue(Integer.parseInt(valueString.trim()));
		case Long:
			return BaseRuntime.getValueFactory().createLongValue(Long.parseLong(valueString.trim()));
		case String:
			return BaseRuntime.getValueFactory().createStringValue(valueString.trim());
		case Id:
			return Base.toId(valueString.trim());
		default:
			throw new AssertionError("Not a plain type? " + type);
		}
	}

	/**
	 * @param valueSequenceString
	 *            is comma-separated
	 * @param type
	 *            must be a collection-type
	 * @return the parsed XValue
	 */
	public static XValue parseCollectionValue(final String valueSequenceString, final ValueType type) {
		assert type.isCollection();
		if (type == ValueType.StringList || type == ValueType.StringSet) {
			// careful escaping
			final List<String> list = toStringList(valueSequenceString);
			if (type == ValueType.StringList) {
				return XV.toStringListValue(list);
			} else {
				return XV.toStringSetValue(list);
			}
		} else {
			final String[] parts = valueSequenceString.split(",");
			switch (type) {
			case AddressList:
			case AddressSet:
			case AddressSortedSet: {
				final ArrayList<XAddress> list = new ArrayList<XAddress>();
				for (final String s : parts) {
					list.add((XAddress) parsePlainValue(s.trim(), ValueType.Address));
				}
				switch (type) {
				case AddressList:
					return XV.toAddressListValue(list);
				case AddressSet:
					return XV.toAddressSetValue(list);
				case AddressSortedSet:
					return XV.toAddressSortedSetValue(list);
				default:
					throw new AssertionError();
				}
			}
			case IdList:
			case IdSet:
			case IdSortedSet: {
				final ArrayList<XId> list = new ArrayList<XId>();
				for (final String s : parts) {
					list.add((XId) parsePlainValue(s.trim(), ValueType.Id));
				}
				switch (type) {
				case IdList:
					return XV.toIdListValue(list);
				case IdSet:
					return XV.toIdSetValue(list);
				case IdSortedSet:
					return XV.toIdSortedSetValue(list);
				default:
					throw new AssertionError();
				}

			}
			case BooleanList: {
				final ArrayList<Boolean> list = new ArrayList<Boolean>();
				for (final String s : parts) {
					list.add(Boolean.parseBoolean(s.trim()));
				}
				return XV.toBooleanListValue(list);
			}
			case DoubleList: {
				final ArrayList<Double> list = new ArrayList<Double>();
				for (final String s : parts) {
					list.add(Double.parseDouble(s.trim()));
				}
				return XV.toDoubleListValue(list);
			}
			case LongList: {
				final ArrayList<Long> list = new ArrayList<Long>();
				for (final String s : parts) {
					list.add(Long.parseLong(s.trim()));
				}
				return XV.toLongListValue(list);
			}
			case IntegerList: {
				final ArrayList<Integer> list = new ArrayList<Integer>();
				for (final String s : parts) {
					list.add(Integer.parseInt(s.trim()));
				}
				return XV.toIntegerListValue(list);
			}
			default:
				throw new AssertionError("Could not handle " + type);
			}
		}
	}

	public static String toCollectionString(final XCollectionValue<?> value) {
		final StringBuffer buf = new StringBuffer();
		buf.append("[");
		boolean first = true;

		switch (value.getType()) {
		case AddressList:
		case AddressSet:
		case AddressSortedSet: {
			for (final Object o : value) {
				if (!first) {
					buf.append(COMMA);
				}
				buf.append(o == null ? "null" : ((XAddress) o).toString());
				first = false;
			}
			break;
		}
		case BooleanList:
		case DoubleList:
		case IntegerList:
		case LongList: {
			for (final Object o : value) {
				if (!first) {
					buf.append(COMMA);
				}
				buf.append(o == null ? "null" : o.toString());
				first = false;
			}
			break;
		}
		case StringList:
		case StringSet: {
			for (final Object o : value) {
				if (!first) {
					buf.append(COMMA);
				}
				buf.append(o == null ? "null" : aposEncode((String) o));
				first = false;
			}
			break;
		}
		case IdList:
		case IdSet:
		case IdSortedSet: {
			for (final Object o : value) {
				if (!first) {
					buf.append(COMMA);
				}
				buf.append(o == null ? "null" : o.toString());
				first = false;
			}
			break;
		}
		default:
			throw new IllegalArgumentException("Type " + value.getType()
					+ " is not a known collection type");
		}
		buf.append("]");
		return buf.toString();
	}

	private static String aposEncode(final String s) {
		return "'" + s.replace("'", "''") + "'";
	}

	private static final char APOS = '\'';
	private static final char COMMA = ',';

	private static boolean isNextChar(final String s, final int pos, final char c) {
		return pos + 1 < s.length() && s.charAt(pos + 1) == c;
	}

	/**
	 * @param in
	 *            has format
	 *            'foo','bar','','stu,ff','es''cap''ed','maybeeven'',''withcomma
	 *            '
	 *
	 *            which is to be interpreted as "foo", "bar", "", "stu,ff",
	 *            "es'cap'ed", "maybeeben','withcomma"
	 *
	 * @return the parsed strings
	 */
	public static List<String> toStringList(final String in) {
		final List<String> result = new LinkedList<String>();
		if (in == null) {
			return result;
		}

		int pos = 0;
		boolean inString = false;
		StringBuffer token = new StringBuffer();
		while (pos < in.length()) {
			// eval current char
			final char c = in.charAt(pos);
			log.trace("Parsing __" + c + "__ inString?" + inString + " token=" + token);
			if (c == APOS) {
				// lookahead to find if this is an encoded '
				if (isNextChar(in, pos, APOS)) {
					// lookahead to find if we look at an empty string
					if (isNextChar(in, pos + 1, COMMA) && !inString) {
						log.trace("EmptyString");
						pos += 2;
						result.add("");
						token = new StringBuffer();
					} else {
						log.trace("APOS");
						token.append(APOS);
						pos += 1;
					}
				} else {
					// single quote symbol
					if (inString) {
						// string ended
						log.trace("TokenEnd");
						inString = false;
						result.add(token.toString());
						token.setLength(0);
					} else {
						// string started
						log.trace("TokenStart");
						inString = true;
						token = new StringBuffer();
					}
				}
			} else {
				if (inString) {
					log.trace("InToken");
					// string grows
					token.append(c);
				} else {
					log.trace("OutToken");
					// must be a comma or whitespace
					assert c == ',' || c == ' ' : "c is " + c + " in " + in;
				}
			}
			pos++;
		}
		assert in.equals("") || in.charAt(pos - 1) == APOS;
		return result;
	}

	public static void main(final String[] args) {
		final List<String> list = toStringList("'fo''o','ba,r''','','stu,ff','es''cap''ed','maybeeven'',''withcomma'");
		final String[] result = list.toArray(new String[list.size()]);
		final String[] expected = new String[] { "fo'o", "ba,r'", "", "stu,ff", "es'cap'ed",
				"maybeeven','withcomma'" };

		log.trace(Arrays.toString(result));
		log.trace(Arrays.toString(expected));
		assert expected.length == result.length;
	}

	public static String toString(final XValue value) {
		if (value.getType().isCollection()) {
			return toCollectionString((XCollectionValue<?>) value);
		} else {
			return toPlainString(value);
		}
	}

	public static String toPlainString(final XValue value) {
		switch (value.getType()) {
		case Address:
		case Boolean:
		case Double:
		case Integer:
		case Long:
		case Id:
			return value.toString();
		case Binary: {
			final byte[] bytes = ((XBinaryValue) value).getValue();
			try {
				final String s = new String(bytes, "utf-8");
				return s;
			} catch (final UnsupportedEncodingException e) {
				throw new RuntimeException("No utf-8 on this platform?");
			}
		}
		case String:
			return aposEncode(value.toString());
		default:
			throw new IllegalArgumentException("No known plain type " + value.getType());
		}
	}

}
