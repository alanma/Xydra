package org.xydra.valueindex;

import org.xydra.base.XAddress;
import org.xydra.base.value.XValue;


// TODO Document
// TODO Find better name?

public class AddressValueCounterTriple {
	private XAddress address;
	private XValue value;
	private Integer counter;
	
	public AddressValueCounterTriple(XAddress address, XValue value, Integer counter) {
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
		return this.address.equals(address) && this.value.equals(value);
	}
}
