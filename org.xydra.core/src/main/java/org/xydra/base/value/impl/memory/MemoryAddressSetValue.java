package org.xydra.base.value.impl.memory;

import java.util.Collection;

import org.xydra.base.XAddress;
import org.xydra.base.value.ValueType;
import org.xydra.base.value.XAddressSetValue;


/**
 * An implementation of {@link XAddressSetValue}
 * 
 * @author dscharrer
 * 
 */
public class MemoryAddressSetValue extends MemorySetValue<XAddress> implements XAddressSetValue {
	
	private static final long serialVersionUID = -83885798275571937L;
	
	public MemoryAddressSetValue(Collection<XAddress> contents) {
		super(contents);
	}
	
	public MemoryAddressSetValue(XAddress[] contents) {
		super(contents);
	}
	
	public XAddressSetValue add(XAddress entry) {
		MemoryAddressSetValue v = new MemoryAddressSetValue(this.set);
		v.set.add(entry);
		return v;
	}
	
	public XAddress[] contents() {
		return toArray(new XAddress[size()]);
	}
	
	@Override
	public boolean equals(Object other) {
		return other instanceof XAddressSetValue && checkEquals((XAddressSetValue)other);
	}
	
	@Override
	public int hashCode() {
		return getHashCode();
	}
	
	public XAddressSetValue remove(XAddress entry) {
		MemoryAddressSetValue v = new MemoryAddressSetValue(this.set);
		v.set.remove(entry);
		return v;
	}
	
	public XAddress[] toArray() {
		return contents();
	}
	
	@Override
	public ValueType getType() {
		return ValueType.AddressSet;
	}
	
	@Override
	public ValueType getComponentType() {
		return ValueType.Address;
	}
	
}
