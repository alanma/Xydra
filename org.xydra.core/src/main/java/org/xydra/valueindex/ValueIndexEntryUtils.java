package org.xydra.valueindex;

import org.xydra.base.XAddress;
import org.xydra.base.value.XValue;
import org.xydra.core.serialize.SerializedValue;
import org.xydra.core.serialize.XydraElement;
import org.xydra.core.serialize.XydraOut;
import org.xydra.core.serialize.json.JsonParser;
import org.xydra.core.serialize.json.JsonSerializer;


public class ValueIndexEntryUtils {
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
			/*
			 * Null entries of the array will not appear in the string
			 * 
			 * TODO Document this
			 */
		}
		
		return result;
	}
	
	public static String serializeAsString(ValueIndexEntry[] oldEntries, ValueIndexEntry newEntry) {
		String result = serializeAsString(oldEntries);
		
		if(newEntry != null) {
			result += '\n' + "<entry>";
			result += serializeAsString(newEntry);
		}
		/*
		 * Null entries of the array will not appear in the string
		 * 
		 * TODO Document this
		 */

		return result;
	}
	
	public static ValueIndexEntry fromString(String s) {
		// TODO document that this only works with strings returned by the
		// serializeAsString method
		
		XAddress address = null;
		XValue value = null;
		Integer counter = 0;
		
		// TODO does this work?
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
	
	public static ValueIndexEntry[] getArrayFromString(String s) {
		// TODO document that this only works with strings returned by the
		// serializeAsString method
		// Strings entries = s.split(arg0)
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
