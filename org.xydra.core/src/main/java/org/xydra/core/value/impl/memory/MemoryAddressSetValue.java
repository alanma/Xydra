package org.xydra.core.value.impl.memory;

import java.util.Collection;

import org.xydra.core.model.XAddress;
import org.xydra.core.value.XAddressSetValue;


/**
 * An implementation of {@link XAddressSetValue}
 * 
 * @author dscharrer
 * 
 */
public class MemoryAddressSetValue extends MemorySetValue<XAddress> implements XAddressSetValue {
	
	private static final long serialVersionUID = -83885798275571937L;
	
	public MemoryAddressSetValue(XAddress[] contents) {
		super(contents);
	}
	
	public MemoryAddressSetValue(Collection<XAddress> contents) {
		super(contents);
	}
	
	public XAddress[] contents() {
		return toArray(new XAddress[size()]);
	}
	
	public XAddress[] toArray() {
		return contents();
	}
	
	@Override
	public boolean equals(Object other) {
		return other instanceof XAddressSetValue && checkEquals((XAddressSetValue)other);
	}
	
	@Override
	public int hashCode() {
		return getHashCode();
	}
	
	public XAddressSetValue add(XAddress entry) {
		MemoryAddressSetValue v = new MemoryAddressSetValue(this.set);
		v.set.add(entry);
		return v;
	}
	
	public XAddressSetValue remove(XAddress entry) {
		MemoryAddressSetValue v = new MemoryAddressSetValue(this.set);
		v.set.remove(entry);
		return v;
	}
	
}
