package org.xydra.base.value.impl.memory;

import java.io.Serializable;

import org.xydra.base.value.ValueType;
import org.xydra.base.value.XIntegerValue;


/**
 * An implementation of {@link XIntegerValue}
 * 
 * @author kaidel
 * 
 */
public class MemoryIntegerValue implements XIntegerValue, Serializable {
	
	private static final long serialVersionUID = -7591305944744567132L;
	
	// non-final to be GWT-Serializable
	private int content;
	
	// empty constructor for GWT-Serializable
	protected MemoryIntegerValue() {
	}
	
	public MemoryIntegerValue(int content) {
		this.content = content;
	}
	
	@Override
	public Number asNumber() {
		return contents();
	}
	
	@Override
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
	public ValueType getType() {
		return ValueType.Integer;
	}
	
	@Override
	public Integer getValue() {
		return this.content;
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
