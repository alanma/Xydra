package org.xydra.base.value.impl.memory;

import java.util.Collection;

import org.xydra.base.value.XStringSetValue;


/**
 * An implementation of {@link XStringSetValue}
 * 
 * @author dscharrer
 * 
 */
public class MemoryStringSetValue extends MemorySetValue<String> implements XStringSetValue {
	
	private static final long serialVersionUID = -83885798275571937L;
	
	public MemoryStringSetValue(Collection<String> contents) {
		super(contents);
	}
	
	public MemoryStringSetValue(String[] contents) {
		super(contents);
	}
	
	public MemoryStringSetValue add(String entry) {
		MemoryStringSetValue v = new MemoryStringSetValue(this.set);
		v.set.add(entry);
		return v;
	}
	
	public String[] contents() {
		return toArray(new String[size()]);
	}
	
	@Override
	public boolean equals(Object other) {
		return other instanceof XStringSetValue && checkEquals((XStringSetValue)other);
	}
	
	@Override
	public int hashCode() {
		return getHashCode();
	}
	
	public MemoryStringSetValue remove(String entry) {
		MemoryStringSetValue v = new MemoryStringSetValue(this.set);
		v.set.remove(entry);
		return v;
	}
	
	public String[] toArray() {
		return contents();
	}
	
}
