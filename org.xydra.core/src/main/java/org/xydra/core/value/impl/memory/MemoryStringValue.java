package org.xydra.core.value.impl.memory;

import org.xydra.core.value.XIDValue;
import org.xydra.core.value.XStringValue;


/**
 * An implementation of {@link XStringValue}
 * 
 * @author voelkel
 * 
 */

public class MemoryStringValue implements XStringValue {
	
	private static final long serialVersionUID = -7239623174442190402L;
	
	private String string;
	
	public MemoryStringValue(String string) {
		this.string = string;
	}
	
	public String contents() {
		return this.string;
	}
	
	public XStringValue asStringValue() {
		return this;
	}
	
	public XIDValue asIDValue() {
		return null;
	}
	
	@Override
	public boolean equals(Object object) {
		if(object instanceof MemoryStringValue) {
			return this.string.equals(((MemoryStringValue)object).contents());
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
	
}
