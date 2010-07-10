package org.xydra.core.value.impl.memory;

import org.xydra.core.XX;
import org.xydra.core.model.XID;
import org.xydra.core.value.XIDValue;


/**
 * An implementation of {@link XIDValue}
 * 
 * @author voelkel
 * 
 */
public class MemoryIDValue implements XIDValue {
	
	private static final long serialVersionUID = -7239623174442190402L;
	
	private final XID id;
	
	public MemoryIDValue(XID id) {
		this.id = id;
	}
	
	public XID contents() {
		return this.id;
	}
	
	@Override
	public boolean equals(Object object) {
		if(object instanceof XIDValue) {
			return XX.equals(this.id, ((XIDValue)object).contents());
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
