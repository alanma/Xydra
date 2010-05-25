package org.xydra.core.value.impl.memory;

import org.xydra.core.value.XDoubleValue;
import org.xydra.core.value.XIDValue;
import org.xydra.core.value.XStringValue;


/**
 * An implementation of {@link XDoubleValue}
 * 
 * @author Kaidel
 * 
 */

public class MemoryDoubleValue implements XDoubleValue {
	
	private Double content;
	private static final long serialVersionUID = -8067526366632112607L;
	
	public MemoryDoubleValue(double content) {
		this.content = content;
	}
	
	public double contents() {
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
