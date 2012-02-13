package org.xydra.valueindex;

import org.xydra.base.XAddress;
import org.xydra.base.value.XValue;
import org.xydra.core.serialize.SerializedValue;
import org.xydra.core.serialize.XydraElement;
import org.xydra.core.serialize.XydraOut;
import org.xydra.core.serialize.json.JsonParser;
import org.xydra.core.serialize.json.JsonSerializer;


// TODO Document

public class ValueIndexEntry {
	private XAddress address;
	private XValue value;
	private Integer counter;
	
	public ValueIndexEntry(XAddress address, XValue value, Integer counter) {
		if(address == null || counter == null) {
			throw new RuntimeException("address or counter must not be null");
		}
		
		this.address = address;
		this.value = value;
		this.counter = counter;
	}
	
	public XAddress getAddress() {
		return this.address;
	}
	
	public XValue getValue() {
		return this.value;
	}
	
	public Integer getCounter() {
		return this.counter;
	}
	
	public void incrementCounter() {
		this.counter++;
	}
	
	public void decrementCounter() {
		this.counter--;
	}
	
	public boolean equalAddressAndValue(XAddress address, XValue value) {
		if(address == null) {
			return false;
		}
		
		if(this.value == null) {
			if(value != null) {
				return false;
			} else {
				return this.address.equals(address);
			}
			
		}
		
		return this.address.equals(address) && this.value.equals(value);
	}
	
	public String serializeAsString(boolean checkSize) {
		/*
		 * TODO The implementation assumes that the JSON strings contain no new
		 * line commands - is this okay?
		 * 
		 * TODO implement checkSize -> if the string representing the value gets
		 * to big, don't add it (or should this be done earlier, i.e. during the
		 * construction of the entry?)
		 */

		// use existing JSON serializers
		JsonSerializer serializer = new JsonSerializer();
		
		XydraOut adrOut = serializer.create();
		adrOut.enableWhitespace(false, false);
		SerializedValue.serialize(this.address, adrOut);
		
		String adrString = adrOut.getData();
		
		String valueString = null;
		
		if(this.value != null) {
			XydraOut valOut = serializer.create();
			valOut.enableWhitespace(false, false);
			SerializedValue.serialize(this.value, valOut);
			
			valueString = valOut.getData();
		}
		
		String result = adrString + '\n' + valueString + '\n' + this.counter;
		
		return result;
	}
	
	public static ValueIndexEntry fromString(String s) {
		// TODO document that this only works with strings returned by the
		// toString method
		
		XAddress address = null;
		XValue value = null;
		Integer counter = 0;
		
		// TODO does this work?
		String[] strings = s.split("" + '\n');
		
		assert strings.length == 3;
		
		JsonParser parser = new JsonParser();
		
		// parse address
		XydraElement addressElement = parser.parse(strings[0]);
		
		XValue addressVal = SerializedValue.toValue(addressElement);
		assert addressVal instanceof XAddress;
		
		address = (XAddress)addressVal;
		
		// parse value
		if(!strings[1].equals("null")) {
			XydraElement valueElement = parser.parse(strings[1]);
			
			value = SerializedValue.toValue(valueElement);
		}
		
		// parser counter
		
		counter = Integer.parseInt(strings[2]);
		
		ValueIndexEntry entry = new ValueIndexEntry(address, value, counter);
		
		return entry;
	}
}
