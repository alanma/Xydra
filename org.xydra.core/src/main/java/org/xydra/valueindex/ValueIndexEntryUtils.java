package org.xydra.valueindex;

import org.xydra.base.XAddress;
import org.xydra.base.value.XValue;
import org.xydra.core.serialize.SerializedValue;
import org.xydra.core.serialize.XydraElement;
import org.xydra.core.serialize.XydraOut;
import org.xydra.core.serialize.json.JsonParser;
import org.xydra.core.serialize.json.JsonSerializer;
import org.xydra.sharedutils.XyAssert;


/**
 * Provides methods for serializing {@link ValueIndexEntry ValueIndexEntries} as
 * Strings and for deserializing Strings to {@link ValueIndexEntry
 * ValueIndexEntries}.
 *
 * @author kaidel
 *
 */

/*
 * This class serializes ValueIndexEntries as strings, here is a documentation
 * of the simple Serializing-Syntax:
 *
 * 1) A single the ValueIndexEntry is serialized like this:
 *
 * - the XAddress is serialized by using the {@link JsonSerizalizer} and {@link
 * XydraOut} to get the JSON representation of the address. If this string
 * contains \ they will be escaped as \\ and occurences of " will be escaped as
 * \" .
 *
 * - the XValue is serialized like the XAddress (e.g. using the JsonSerializer
 * and escaping likek described).
 *
 * Finally, the string representing the ValueIndexEntry is made up of both these
 * strings like this:
 *
 * adrString""valueString
 *
 * "" is used as a marker to tell where the address ends and the value starts.
 *
 * 2) An array of ValueIndexEntries is serialized like this:
 *
 * - first, we'll create the strings representing the ValueIndexEntries as
 * described in point 1).
 *
 * - all these strings need to be escaped a second time, \ will be escaped with
 * \\ and occurences of the String <entry> will be escaped with <\entry> .
 *
 * Afterwards all these strings will be connected into a single string and
 * <entry> will be used to mark where one string ends and another one starts.
 * For example if we have an array containing 3 different ValueIndexEntries, the
 * resulting string will look like this:
 *
 * value1String<entry>value2String<entry>value3String
 */

public class ValueIndexEntryUtils {

	/**
	 * Serializes the given {@link ValueIndexEntry} as a String.
	 *
	 * @param entry the {@link ValueIndexEntry} which is to be serialized
	 * @return a String representation of the given {@link ValueIndexEntry}.
	 * @throws NullPointerException if the given {@link ValueIndexEntry} was
	 *             null
	 */
	public static String serializeAsString(final ValueIndexEntry entry) {
		final XValue value = entry.getValue();
		final XAddress address = entry.getAddress();

		// use existing JSON serializers
		final JsonSerializer serializer = new JsonSerializer();

		final XydraOut adrOut = serializer.create();
		adrOut.enableWhitespace(false, false);
		SerializedValue.serialize(address, adrOut);

		final String serializedAddress = adrOut.getData();
		final String adrString = escapeSingleString(serializedAddress);

		String valueString = null;

		if(value != null) {
			final XydraOut valOut = serializer.create();
			valOut.enableWhitespace(false, false);
			SerializedValue.serialize(value, valOut);

			final String serializedValue = valOut.getData();
			valueString = escapeSingleString(serializedValue);
		}

		// "" is used as the marker for splitting
		final String result = adrString + "\"\"" + valueString;

		XyAssert.xyAssert(!result.equals(""));

		return result;
	}

	private static String escapeSingleString(final String s) {
		// escape \ with \\
		final String replace1 = s.replace("\\", "\\\\");
		// escape " with \"
		final String replace2 = replace1.replace("\"", "\\\"");

		return replace2;
	}

	private static String deescapeSingleString(final String s) {
		// deescape \" with "
		final String replace1 = s.replace("\\\"", "\"");

		// deescape \\ with \
		final String replace2 = replace1.replace("\\\\", "\\");

		return replace2;
	}

	/**
	 * Serializes the given {@link ValueIndexEntry} array as a single String.
	 * Null entries in the array will not be represented in the String.
	 *
	 * @param entries the {@link ValueIndexEntry} array which is to be
	 *            serialized
	 * @return a String representation of the given {@link ValueIndexEntry}
	 *         array.
	 */
	public static String serializeAsString(final ValueIndexEntry[] entries) {
		String result = "";

		for(int i = 0; i < entries.length; i++) {
			final ValueIndexEntry entry = entries[i];

			if(entry != null) {
				final String resultString = serializeAsString(entry);

				if(result.equals("")) {
					/*
					 * don't add <entry> if this is the first string
					 */
					result += escapeListString(resultString);
				} else {
					result += "<entry>" + escapeListString(resultString);
				}
			}
		}

		/**
		 * "<entry>" should only occur as a marker between the different strings
		 * which are encoding ValueIndexEntries
		 */
		XyAssert.xyAssert(!result.startsWith("<entry>") && !result.endsWith("<entry>"));
		return result;
	}

	private static String escapeListString(final String s) {
		// escape / with //
		final String replace1 = s.replace("\\", "\\\\");

		// escape <entry> with <\entry>
		final String replace2 = replace1.replace("<entry>", "<\\entry>");

		XyAssert.xyAssert(!replace2.equals(""));

		return replace2;
	}

	private static String deescapeListString(final String s) {
		final String replace1 = s.replace("<\\entry>", "<entry>");
		final String replace2 = replace1.replace("\\\\", "\\");

		XyAssert.xyAssert(!replace2.equals(""));

		return replace2;
	}

	/**
	 * Serializes the given {@link ValueIndexEntry} array as a single String,
	 * together with the given new entry appended to the array. Null entries in
	 * the array will not be represented in the String.
	 *
	 * @param oldEntries the {@link ValueIndexEntry} array which is to be
	 *            serialized
	 * @param newEntry another {@link ValueIndexEntry} which also is to be
	 *            serialized, essentially "adds" the entry to the given array in
	 *            the String representation.
	 * @return a String representation of the given {@link ValueIndexEntry}
	 *         array with "added" newElement.
	 */
	public static String serializeAsString(final ValueIndexEntry[] oldEntries, final ValueIndexEntry newEntry) {
		String result = serializeAsString(oldEntries);

		if(newEntry != null) {
			if(result.equals("")) {
				result = escapeListString(serializeAsString(newEntry));
			} else {
				result += "<entry>" + escapeListString(serializeAsString(newEntry));
			}
		}
		return result;
	}

	/**
	 * Parses the given String as a {@link ValueIndexEntry}.
	 *
	 * Warning: Only Strings returned by
	 * {@link ValueIndexEntryUtils#serializeAsString(ValueIndexEntry)} can be
	 * parsed by this method. The behavior of this method is undefined for other
	 * types of Strings.
	 *
	 * @param s The String which is to be parsed as an {@link ValueIndexEntry}.
	 * @return The {@link ValueIndexEntry} which was encoded in the given
	 *         String.
	 */
	public static ValueIndexEntry fromString(final String s) {
		XAddress address = null;
		XValue value = null;

		final String[] strings = s.split("\"\"");

		XyAssert.xyAssert(strings.length == 2);

		final String adrString = deescapeSingleString(strings[0]);
		final String valString = deescapeSingleString(strings[1]);

		final JsonParser parser = new JsonParser();

		// parse address
		final XydraElement addressElement = parser.parse(adrString);

		final XValue addressVal = SerializedValue.toValue(addressElement);
		XyAssert.xyAssert(addressVal instanceof XAddress);

		address = (XAddress)addressVal;

		// parse value
		if(!strings[1].equals("null")) {
			final XydraElement valueElement = parser.parse(valString);

			value = SerializedValue.toValue(valueElement);
		}

		final ValueIndexEntry entry = new ValueIndexEntry(address, value);

		return entry;
	}

	/**
	 * Parses the given String as a an array of {@link ValueIndexEntry
	 * ValueIndexEntries}.
	 *
	 * Warning: Only Strings returned by
	 * {@link ValueIndexEntryUtils#serializeAsString(ValueIndexEntry[])} can be
	 * parsed by this method. The behavior of this method is undefined for other
	 * types of Strings.
	 *
	 * @param s The String which is to be parsed as an {@link ValueIndexEntry}.
	 * @return The {@link ValueIndexEntry} which was encoded in the given
	 *         String.
	 */
	public static ValueIndexEntry[] getArrayFromString(final String s) {
		if(s != null) {
			final String[] strings = s.split("<entry>");

			final ValueIndexEntry[] entries = new ValueIndexEntry[strings.length];

			for(int i = 0; i < strings.length; i++) {
				entries[i] = fromString(deescapeListString(strings[i]));
			}

			return entries;
		} else {
			final ValueIndexEntry[] emptyArray = new ValueIndexEntry[0];
			return emptyArray;
		}
	}

	/**
	 * Adds the given {@link ValueIndexEntry} to the given serialized list of
	 * {@link ValueIndexEntry ValueIndexEntries}. If the given list already
	 * contains a string which represents the given entry, nothing will be
	 * changed.
	 *
	 * This method will only work with strings returned by the serializing
	 * methods of {@link ValueIndexEntryUtils}.
	 *
	 * @param s A string representation of a list of {@link ValueIndexEntry
	 *            ValueIndexEntries}.
	 * @param entry The {@link ValueIndexEntry} which is to be added to the
	 *            given list.
	 * @return A string representing a list of {@link ValueIndexEntry
	 *         ValueIndexEntries} containing the entries of the given list and
	 *         the single given {@link ValueIndexEntry}. Returns null if the
	 *         given string is null or the empty string.
	 */
	public static String addEntryToArrayString(final String s, final ValueIndexEntry entry) {
		final String valueString = ValueIndexEntryUtils.serializeAsString(entry);
		final String escapedString = ValueIndexEntryUtils.escapeListString(valueString);

		if(s != null && !s.equals("")) {
			if(!s.contains(escapedString)) {
				return s + "<entry>" + escapedString;
			} else {
				return s;
			}
		} else {
			return null;
		}
	}

	/**
	 * Removes the given {@link ValueIndexEntry} from the given serialized list
	 * of {@link ValueIndexEntry ValueIndexEntries}. If the given list does not
	 * contain a string which represents the given entry, nothing will be
	 * changed.
	 *
	 * This method will only work with strings returned by the serializing
	 * methods of {@link ValueIndexEntryUtils}.
	 *
	 * @param s A string representation of a list of {@link ValueIndexEntry
	 *            ValueIndexEntries}.
	 * @param entry The {@link ValueIndexEntry} which is to be removed from the
	 *            given list.
	 * @return A string representing a list of {@link ValueIndexEntry
	 *         ValueIndexEntries} containing the entries of the given list, but
	 *         not containing a string representing the single given
	 *         {@link ValueIndexEntry}. May return the empty string if the given
	 *         list only contained the given {@link ValueIndexEntry} and no
	 *         other entries. Returns null if the given string is null or the
	 *         empty string.
	 */
	public static String removeEntryFromArrayString(final String s, final ValueIndexEntry entry) {
		final String valueString = ValueIndexEntryUtils.serializeAsString(entry);
		final String escapedString = ValueIndexEntryUtils.escapeListString(valueString);

		if(s != null && !s.equals("")) {

			if(s.equals(escapedString)) {
				/*
				 * the given entry is the only entry in the array encoded in the
				 * string, so return the empty String.
				 */

				return "";

			} else if(s.contains(escapedString + "<entry>")) {
				/*
				 * the given entry is at the beginning of the encoded array,
				 * remove it and return the resulting string
				 */

				return s.replace(escapedString + "<entry>", "");

			} else if(s.contains("<entry>" + escapedString)) {
				/*
				 * the given entry is somewhere in the middle of the encoded
				 * array, remove it and return the resulting string
				 */

				return s.replace("<entry>" + escapedString, "");

			}

			/*
			 * the encoded array does not contain the given entry
			 */

			return s;

		} else {
			/*
			 * the encoded array is empty or null, so it doesn't contain the
			 * entry
			 */
			return null;
		}
	}
}
