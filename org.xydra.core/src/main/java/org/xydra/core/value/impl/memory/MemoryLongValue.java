package org.xydra.core.value.impl.memory;

import org.xydra.core.value.XLongValue;


/**
 * An implementation of {@link XLongValue}
 * 
 * @author Kaidel
 * 
 */
public class MemoryLongValue implements XLongValue {
	
	private static final long serialVersionUID = 2488255853315733958L;
	
	private final long content;
	
	public MemoryLongValue(long content) {
		this.content = content;
	}
	
	public long contents() {
		return this.content;
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
		return (int)this.content;
	}
	
	@Override
	public String toString() {
		return Long.toString(this.content);
	}
	
	public Number asNumber() {
		return contents();
	}
	
}
