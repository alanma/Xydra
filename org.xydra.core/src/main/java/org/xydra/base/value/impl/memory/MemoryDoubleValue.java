package org.xydra.base.value.impl.memory;

import org.xydra.base.value.XDoubleValue;


/**
 * An implementation of {@link XDoubleValue}
 * 
 * @author Kaidel
 * 
 */
public class MemoryDoubleValue implements XDoubleValue {
	
	private static final long serialVersionUID = -8067526366632112607L;
	
	private final Double content;
	
	public MemoryDoubleValue(double content) {
		this.content = content;
	}
	
	public Number asNumber() {
		return contents();
	}
	
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
	public int hashCode() {
		return this.content.hashCode();
	}
	
	@Override
	public String toString() {
		return Double.toString(this.content);
	}
	
}
