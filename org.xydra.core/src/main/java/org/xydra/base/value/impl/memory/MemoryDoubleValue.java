package org.xydra.base.value.impl.memory;

import java.io.Serializable;

import org.xydra.base.value.ValueType;
import org.xydra.base.value.XDoubleValue;


/**
 * An implementation of {@link XDoubleValue}
 * 
 * @author kaidel
 * 
 */
public class MemoryDoubleValue implements XDoubleValue, Serializable {
	
	private static final long serialVersionUID = -8067526366632112607L;
	
	// non-final to be GWT-Serializable
	private Double content;
	
	// empty constructor for GWT-Serializable
	protected MemoryDoubleValue() {
	}
	
	public MemoryDoubleValue(double content) {
		this.content = content;
	}
	
	@Override
	public Number asNumber() {
		return contents();
	}
	
	@Override
	public double contents() {
		return this.content;
	}
	
	@Override
	public boolean equals(Object object) {
		if(object instanceof XDoubleValue) {
			return ((XDoubleValue)object).contents() == this.content;
		} else {
			return false;
		}
	}
	
	@Override
	public ValueType getType() {
		return ValueType.Double;
	}
	
	@Override
	public Double getValue() {
		return this.content;
	}
	
	@Override
	public int hashCode() {
		return this.content.hashCode();
	}
	
	@Override
	public String toString() {
		return Double.toString(this.content);
	}
	
}
