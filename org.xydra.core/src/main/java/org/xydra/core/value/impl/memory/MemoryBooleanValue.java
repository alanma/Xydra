package org.xydra.core.value.impl.memory;

import org.xydra.core.value.XBooleanValue;
import org.xydra.core.value.XIDValue;
import org.xydra.core.value.XStringValue;


/**
 * An implementation of {@link XBooleanValue}
 * 
 * @author Kaidel
 * 
 */

public class MemoryBooleanValue implements XBooleanValue {
	
	private static final long serialVersionUID = 4401350466432317386L;
	private Boolean content;
	
	public MemoryBooleanValue(boolean content) {
		this.content = content;
	}
	
	public boolean contents() {
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
		if(object instanceof XBooleanValue) {
			return this.content == ((XBooleanValue)object).contents();
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
		return Boolean.toString(this.content);
	}
	
}
