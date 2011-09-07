package org.xydra.base.value.impl.memory;

import org.xydra.base.value.ValueType;
import org.xydra.base.value.XStringValue;
import org.xydra.index.XI;


/**
 * An implementation of {@link XStringValue}
 * 
 * @author voelkel
 * 
 */
public class MemoryStringValue implements XStringValue {
	
	private static final long serialVersionUID = -7239623174442190402L;
	
	private final String string;
	
	public MemoryStringValue(String string) {
		this.string = string;
	}
	
	@Override
    public String contents() {
		return this.string;
	}
	
	@Override
	public boolean equals(Object object) {
		if(object instanceof MemoryStringValue) {
			return XI.equals(this.string, ((MemoryStringValue)object).contents());
		} else {
			return false;
		}
	}
	
	@Override
	public int hashCode() {
		return this.string.hashCode();
	}
	
	@Override
	public String toString() {
		return this.contents();
	}
	
	@Override
	public ValueType getType() {
		return ValueType.String;
	}
	
	@Override
	public String getValue() {
		return this.string;
	}
	
}
