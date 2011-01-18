package org.xydra.base.value.impl.memory;

import java.util.Collection;

import org.xydra.base.XID;
import org.xydra.base.value.XIDSetValue;


/**
 * An implementation of {@link XIDSetValue}
 * 
 * @author dscharrer
 * 
 */
public class MemoryIDSetValue extends MemorySetValue<XID> implements XIDSetValue {
	
	private static final long serialVersionUID = -83885798275571937L;
	
	public MemoryIDSetValue(XID[] contents) {
		super(contents);
	}
	
	public MemoryIDSetValue(Collection<XID> contents) {
		super(contents);
	}
	
	public XID[] contents() {
		return toArray(new XID[size()]);
	}
	
	public XID[] toArray() {
		return contents();
	}
	
	@Override
	public boolean equals(Object other) {
		return other instanceof XIDSetValue && checkEquals((XIDSetValue)other);
	}
	
	@Override
	public int hashCode() {
		return getHashCode();
	}
	
	public XIDSetValue add(XID entry) {
		MemoryIDSetValue v = new MemoryIDSetValue(this.set);
		v.set.add(entry);
		return v;
	}
	
	public XIDSetValue remove(XID entry) {
		MemoryIDSetValue v = new MemoryIDSetValue(this.set);
		v.set.remove(entry);
		return v;
	}
	
}
