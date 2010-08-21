package org.xydra.core.value.impl.memory;

import java.util.Collection;

import org.xydra.core.value.XStringSetValue;


/**
 * An implementation of {@link StringSetValue}
 * 
 * @author dscharrer
 * 
 */
public class MemoryStringSetValue extends MemorySetValue<String> implements XStringSetValue {
	
	private static final long serialVersionUID = -83885798275571937L;
	
	public MemoryStringSetValue(String[] contents) {
		super(contents);
	}
	
	public MemoryStringSetValue(Collection<String> contents) {
		super(contents);
	}
	
	public String[] contents() {
		return toArray(new String[size()]);
	}
	
	public String[] toArray() {
		return contents();
	}
	
	@Override
	public boolean equals(Object other) {
		return other instanceof XStringSetValue && checkEquals((XStringSetValue)other);
	}
	
	@Override
	public int hashCode() {
		return getHashCode();
	}
	
}
