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
		/*
		 * TODO The implementation assumes that the JSON strings contain no new
		 * line commands - is this okay?
		 */
		XValue value = entry.getValue();
		XAddress address = entry.getAddress();
		Integer counter = entry.getCounter();
		
		// use existing JSON serializers
		JsonSerializer serializer = new JsonSerializer();
		
		XydraOut adrOut = serializer.create();
		adrOut.enableWhitespace(false, false);
		SerializedValue.serialize(address, adrOut);
		
		String adrString = adrOut.getData();
		
		String valueString = null;
		
		if(value != null) {
			XydraOut valOut = serializer.create();
			valOut.enableWhitespace(false, false);
			SerializedValue.serialize(value, valOut);
			
			valueString = valOut.getData();
		}
		
		String result = "\"" + adrString + "\"" + '\n' + "\"" + valueString + "\"" + '\n' + counter;
		
		return result;
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
		/*
		 * TODO The implementation assumes that the JSON strings contain no new
		 * line commands - is this okay?
		 */
		String result = "";
		
		for(ValueIndexEntry entry : entries) {
			if(entry != null) {
				result += '\n' + "<entry>";
				result += serializeAsString(entry);
			}
		}
		
		return result;
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
			result += '\n' + "<entry>";
			result += serializeAsString(newEntry);
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
		Integer counter = 0;
		
		String[] strings = s.split("" + '\n');
		
		assert strings.length == 3;
		
		JsonParser parser = new JsonParser();
		
		// parse address (use substrings to get rid of beginning/trailing ")
		XydraElement addressElement = parser
		        .parse(strings[0].substring(1, strings[0].length() - 1));
		
		XValue addressVal = SerializedValue.toValue(addressElement);
		assert addressVal instanceof XAddress;
		
		address = (XAddress)addressVal;
		
		// parse value
		if(!strings[1].equals("null")) {
			XydraElement valueElement = parser.parse(strings[1].substring(1,
			        strings[1].length() - 1));
			
			value = SerializedValue.toValue(valueElement);
		}
		
		// parser counter
		
		counter = Integer.parseInt(strings[2]);
		
		ValueIndexEntry entry = new ValueIndexEntry(address, value, counter);
		
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
			String[] strings = s.split('\n' + "(<entry>)");
			
			// strings[0] will be the empty string, so don't use it!
			
			ValueIndexEntry[] entries = new ValueIndexEntry[strings.length - 1];
			
			for(int i = 1; i < strings.length; i++) {
				entries[i - 1] = fromString(strings[i]);
			}
			
			return entries;
		} else {
			ValueIndexEntry[] emptyArray = new ValueIndexEntry[0];
			return emptyArray;
		}
	}
}
