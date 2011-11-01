package org.xydra.base.value.impl.memory;

import java.io.Serializable;

import org.xydra.base.value.ValueType;
import org.xydra.base.value.XBooleanValue;


/**
 * An implementation of {@link XBooleanValue}
 * 
 * @author Kaidel
 * 
 */
public class MemoryBooleanValue implements XBooleanValue, Serializable {
	
	private static final long serialVersionUID = 4401350466432317386L;
	
	// non-final to be GWT-Serializable
	private Boolean content;
	
	// empty constructor for GWT-Serializable
	protected MemoryBooleanValue() {
	}
	
	public MemoryBooleanValue(boolean content) {
		this.content = content;
	}
	
	@Override
	public boolean contents() {
		return this.content;
	}
	
	@Override
	public boolean equals(Object object) {
		if(object instanceof XBooleanValue) {
			return this.content == ((XBooleanValue)object).contents();
		} else {
			return false;
		}
	}
	
	@Override
	public ValueType getType() {
		return ValueType.Boolean;
	}
	
	@Override
	public Boolean getValue() {
		return this.content;
	}
	
	@Override
	public int hashCode() {
		return this.content.hashCode();
	}
	
	@Override
	public String toString() {
		return Boolean.toString(this.content);
	}
	
}
