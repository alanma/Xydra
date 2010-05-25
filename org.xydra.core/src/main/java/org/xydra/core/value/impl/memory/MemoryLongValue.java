package org.xydra.core.value.impl.memory;

import org.xydra.core.value.XIDValue;
import org.xydra.core.value.XLongValue;
import org.xydra.core.value.XStringValue;


/**
 * An implementation of {@link XLongValue}
 * 
 * @author Kaidel
 * 
 */

public class MemoryLongValue implements XLongValue {
	
	private Long content;
	private static final long serialVersionUID = 2488255853315733958L;
	
	public MemoryLongValue(long content) {
		this.content = content;
	}
	
	public long contents() {
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
		if(object instanceof XLongValue) {
			return ((XLongValue)object).contents() == this.content;
		} else {
			return false;
		}
	}
	
	@Override
	public int hashCode() {
		return this.content.hashCode();
	}
	
	@Override
	public String toString() {
		return Long.toString(this.content);
	}
	
}
