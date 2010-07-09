package org.xydra.core.value.impl.memory;

import org.xydra.core.value.XIntegerValue;


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
	
}
