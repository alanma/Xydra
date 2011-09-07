package org.xydra.base.value.impl.memory;

import java.util.Collection;

import org.xydra.base.value.ValueType;
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
	
	@Override
    public MemoryStringSetValue add(String entry) {
		MemoryStringSetValue v = new MemoryStringSetValue(this.set);
		v.set.add(entry);
		return v;
	}
	
	@Override
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
	
	@Override
    public MemoryStringSetValue remove(String entry) {
		MemoryStringSetValue v = new MemoryStringSetValue(this.set);
		v.set.remove(entry);
		return v;
	}
	
	@Override
    public String[] toArray() {
		return contents();
	}
	
	@Override
	public ValueType getType() {
		return ValueType.StringSet;
	}
	
	@Override
	public ValueType getComponentType() {
		return ValueType.String;
	}
	
}
