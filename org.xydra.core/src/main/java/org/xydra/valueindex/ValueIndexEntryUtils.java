package org.xydra.valueindex;

import org.xydra.base.XAddress;
import org.xydra.base.value.XValue;
import org.xydra.core.serialize.SerializedValue;
import org.xydra.core.serialize.XydraElement;
import org.xydra.core.serialize.XydraOut;
import org.xydra.core.serialize.json.JsonParser;
import org.xydra.core.serialize.json.JsonSerializer;


/**
 * Provides methods for serializing {@link ValueIndexEntry ValueIndexEntries} as
 * Strings and for deserializing Strings to {@link ValueIndexEntry
 * ValueIndexEntries}.
 * 
 * @author Kaidel
 * 
 */

/*
 * TODO document espacing/parsing syntax!
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
	public static String serializeAsString(ValueIndexEntry entry) {
		XValue value = entry.getValue();
		XAddress address = entry.getAddress();
		
		// use existing JSON serializers
		JsonSerializer serializer = new JsonSerializer();
		
		XydraOut adrOut = serializer.create();
		adrOut.enableWhitespace(false, false);
		SerializedValue.serialize(address, adrOut);
		
		String serializedAddress = adrOut.getData();
		String adrString = escapeSingleString(serializedAddress);
		
		String valueString = null;
		
		if(value != null) {
			XydraOut valOut = serializer.create();
			valOut.enableWhitespace(false, false);
			SerializedValue.serialize(value, valOut);
			
			String serializedValue = valOut.getData();
			valueString = escapeSingleString(serializedValue);
		}
		
		// "" is used as the marker for splitting
		String result = adrString + "\"\"" + valueString;
		
		assert !result.equals("");
		
		return result;
	}
	
	private static String escapeSingleString(String s) {
		// escape \ with \\
		String replace1 = s.replace("\\", "\\\\");
		// escape " with \"
		String replace2 = replace1.replace("\"", "\\\"");
		
		return replace2;
	}
	
	private static String deescapeSingleString(String s) {
		// deescape \" with "
		String replace1 = s.replace("\\\"", "\"");
		
		// deescape \\ with \
		String replace2 = replace1.replace("\\\\", "\\");
		
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
	public static String serializeAsString(ValueIndexEntry[] entries) {
		String result = "";
		
		for(int i = 0; i < entries.length; i++) {
			ValueIndexEntry entry = entries[i];
			
			if(entry != null) {
				String resultString = serializeAsString(entry);
				
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
		assert !result.startsWith("<entry>") && !result.endsWith("<entry>");
		return result;
	}
	
	private static String escapeListString(String s) {
		String replace1 = s.replace("\\", "\\\\");
		
		String replace2 = replace1.replace("<entry>", "<\\entry>");
		
		assert !replace2.equals("");
		
		return replace2;
	}
	
	private static String deescapeListString(String s) {
		String replace1 = s.replace("<\\entry>", "<entry>");
		String replace2 = replace1.replace("\\\\", "\\");
		
		assert !replace2.equals("");
		
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
	public static String serializeAsString(ValueIndexEntry[] oldEntries, ValueIndexEntry newEntry) {
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
	public static ValueIndexEntry fromString(String s) {
		XAddress address = null;
		XValue value = null;
		
		String[] strings = s.split("\"\"");
		
		assert strings.length == 2;
		
		String adrString = deescapeSingleString(strings[0]);
		String valString = deescapeSingleString(strings[1]);
		
		JsonParser parser = new JsonParser();
		
		// parse address
		XydraElement addressElement = parser.parse(adrString);
		
		XValue addressVal = SerializedValue.toValue(addressElement);
		assert addressVal instanceof XAddress;
		
		address = (XAddress)addressVal;
		
		// parse value
		if(!strings[1].equals("null")) {
			XydraElement valueElement = parser.parse(valString);
			
			value = SerializedValue.toValue(valueElement);
		}
		
		ValueIndexEntry entry = new ValueIndexEntry(address, value);
		
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
	public static ValueIndexEntry[] getArrayFromString(String s) {
		if(s != null) {
			String[] strings = s.split("<entry>");
			
			ValueIndexEntry[] entries = new ValueIndexEntry[strings.length];
			
			for(int i = 0; i < strings.length; i++) {
				entries[i] = fromString(deescapeListString(strings[i]));
			}
			
			return entries;
		} else {
			ValueIndexEntry[] emptyArray = new ValueIndexEntry[0];
			return emptyArray;
		}
	}
}
