package org.xydra.core.value.impl.memory;

import org.xydra.core.value.XIDValue;
import org.xydra.core.value.XIntegerValue;
import org.xydra.core.value.XStringValue;


/**
 * An implementation of {@link XIntegerValue}
 * 
 * @author Kaidel
 * 
 */

public class MemoryIntegerValue implements XIntegerValue {
	
	private int content;
	private static final long serialVersionUID = -7591305944744567132L;
	
	public MemoryIntegerValue(int content) {
		this.content = content;
	}
	
	public int contents() {
		return this.content;
	}
	
	public XIDValue asIDValue() {
		return null;
	}
	
	public XStringValue asStringValue() {
		return null;
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
