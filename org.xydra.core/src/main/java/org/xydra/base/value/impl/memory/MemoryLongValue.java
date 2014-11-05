package org.xydra.base.value.impl.memory;

import java.io.Serializable;

import org.xydra.base.value.ValueType;
import org.xydra.base.value.XLongValue;


/**
 * An implementation of {@link XLongValue}
 * 
 * @author kaidel
 * 
 */
public class MemoryLongValue implements XLongValue, Serializable {
	
	private static final long serialVersionUID = 2488255853315733958L;
	
	// non-final to be GWT-Serializable
	private long content;
	
	// empty constructor for GWT-Serializable
	protected MemoryLongValue() {
	}
	
	public MemoryLongValue(long content) {
		this.content = content;
	}
	
	@Override
	public Number asNumber() {
		return contents();
	}
	
	@Override
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
	public ValueType getType() {
		return ValueType.Long;
	}
	
	@Override
	public Long getValue() {
		return this.content;
	}
	
	@Override
	public int hashCode() {
		return (int)this.content;
	}
	
	@Override
	public String toString() {
		return Long.toString(this.content);
	}
	
}
