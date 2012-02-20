package org.xydra.valueindex;

import org.xydra.base.XAddress;
import org.xydra.base.value.XValue;
import org.xydra.core.model.XField;
import org.xydra.core.model.XObject;


/**
 * Entries used in {@link ValueIndex} to store an {@link XValue} together with
 * the {@link XAddress} of the {@link XObject} which contains the {@link XField}
 * holding the value. Also contains a counter variable to keep track of how
 * often the specific value exists in the {@link XObject}, since multiple
 * {@link XField XFields} might contain the same values.
 * 
 * @author Kaidel
 * 
 */

public class ValueIndexEntry {
	private XAddress address;
	private XValue value;
	private Integer counter;
	
	/**
	 * Creates a new ValueIndexEntry.
	 * 
	 * @param address The {@link XAddress} of the {@link XObject} containing an
	 *            {@link XField}, which holds the given value. Must not be null.
	 * @param value The {@link XValue} which is to be indexed.
	 * @param counter The number of occurrences of the given value in the
	 *            {@link XObject} with the given address. Must not be null.
	 * @throws RuntimeException if address or counter were null
	 */
	public ValueIndexEntry(XAddress address, XValue value, Integer counter) {
		if(address == null || counter == null) {
			throw new RuntimeException("address or counter must not be null");
		}
		
		this.address = address;
		this.value = value;
		this.counter = counter;
	}
	
	/**
	 * Returns the stored {@link XAddress}.
	 * 
	 * @return the stored {@link XAddress}.
	 */
	public XAddress getAddress() {
		return this.address;
	}
	
	/**
	 * Returns the stored {@link XValue}.
	 * 
	 * @return the stored {@link XValue}.
	 */
	public XValue getValue() {
		return this.value;
	}
	
	/**
	 * Returns the current value of the counter.
	 * 
	 * @return the current value of the counter.
	 */
	public Integer getCounter() {
		return this.counter;
	}
	
	/**
	 * Increments the counter.
	 * 
	 * @return the counter after the incrementation.
	 */
	public Integer incrementCounter() {
		this.counter++;
		
		return this.counter;
	}
	
	/**
	 * Decrements the counter.
	 * 
	 * @return the counter after the decrementation.
	 */
	public Integer decrementCounter() {
		this.counter--;
		
		return this.counter;
	}
	
	/**
	 * Checks whether the stored {@link XAddress} and {@link XValue} are equal
	 * to the given address and value.
	 * 
	 * @param address The {@link XAddress} which is to be compared to the stored
	 *            address.
	 * @param value The {@link XValue} which is to be compared to the stored
	 *            value.
	 * @return true, if and only if the given address is equal to the stored
	 *         address and the given value is equal to the stored value
	 */
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
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.address == null) ? 0 : this.address.hashCode());
		result = prime * result + ((this.counter == null) ? 0 : this.counter.hashCode());
		result = prime * result + ((this.value == null) ? 0 : this.value.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj == null) {
			return false;
		}
		if(!(obj instanceof ValueIndexEntry)) {
			return false;
		}
		
		ValueIndexEntry other = (ValueIndexEntry)obj;
		
		if(this.address == null) {
			if(other.address != null)
				return false;
		} else if(!this.address.equals(other.address))
			return false;
		if(this.counter == null) {
			if(other.counter != null)
				return false;
		} else if(!this.counter.equals(other.counter))
			return false;
		if(this.value == null) {
			if(other.value != null)
				return false;
		} else if(!this.value.equals(other.value))
			return false;
		return true;
	}
}
