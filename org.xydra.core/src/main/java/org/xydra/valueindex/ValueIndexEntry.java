package org.xydra.valueindex;

import org.xydra.base.XAddress;
import org.xydra.base.value.XValue;


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
}
