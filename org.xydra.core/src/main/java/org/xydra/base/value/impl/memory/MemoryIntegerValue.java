package org.xydra.base.value.impl.memory;

import org.xydra.base.value.ValueType;
import org.xydra.base.value.XIntegerValue;


/**
 * An implementation of {@link XIntegerValue}
 * 
 * @author Kaidel
 * 
 */
public class MemoryIntegerValue implements XIntegerValue {
	
	private static final long serialVersionUID = -7591305944744567132L;
	
	private final int content;
	
	public MemoryIntegerValue(int content) {
		this.content = content;
	}
	
	public Number asNumber() {
		return contents();
	}
	
	public int contents() {
		return this.content;
	}
	
	@Override
	public boolean equals(Object object) {
		if(object instanceof XIntegerValue) {
			return ((XIntegerValue)object).contents() == this.content;
		} else {
			return false;
		}
	}
	
	@Override
	public int hashCode() {
		return this.content;
	}
	
	@Override
	public String toString() {
		return Integer.toString(this.content);
	}
	
	@Override
	public ValueType getType() {
		return ValueType.Integer;
	}
	
}
