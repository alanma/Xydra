package org.xydra.core.value.impl.memory;

import org.xydra.core.model.XID;
import org.xydra.core.value.XIDValue;
import org.xydra.core.value.XStringValue;


/**
 * An implementation of {@link XIDValue}
 * 
 * @author voelkel
 * 
 */

public class MemoryIDValue implements XIDValue {
	
	private static final long serialVersionUID = -7239623174442190402L;
	
	private XID id;
	
	public MemoryIDValue(XID id) {
		this.id = id;
	}
	
	public XID contents() {
		return this.id;
	}
	
	public XStringValue asStringValue() {
		return null;
	}
	
	public XIDValue asIDValue() {
		return this;
	}
	
	@Override
	public boolean equals(Object object) {
		if(object instanceof XIDValue) {
			return this.id.equals(((XIDValue)object).contents());
		} else {
			return false;
		}
	}
	
	@Override
	public int hashCode() {
		return this.id.hashCode();
	}
	
	@Override
	public String toString() {
		return this.id.toString();
	}
	
}
