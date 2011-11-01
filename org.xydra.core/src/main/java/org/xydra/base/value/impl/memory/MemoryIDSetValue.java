package org.xydra.base.value.impl.memory;

import java.io.Serializable;
import java.util.Collection;

import org.xydra.base.XID;
import org.xydra.base.value.ValueType;
import org.xydra.base.value.XIDSetValue;


/**
 * An implementation of {@link XIDSetValue}
 * 
 * @author dscharrer
 * 
 */
public class MemoryIDSetValue extends MemorySetValue<XID> implements XIDSetValue, Serializable {
	
	private static final long serialVersionUID = -83885798275571937L;
	
	// empty constructor for GWT-Serializable
	protected MemoryIDSetValue() {
	}
	
	public MemoryIDSetValue(Collection<XID> contents) {
		super(contents);
	}
	
	public MemoryIDSetValue(XID[] contents) {
		super(contents);
	}
	
	@Override
	public XIDSetValue add(XID entry) {
		MemoryIDSetValue v = new MemoryIDSetValue(this.set);
		v.set.add(entry);
		return v;
	}
	
	@Override
	public XID[] contents() {
		return toArray(new XID[size()]);
	}
	
	@Override
	public boolean equals(Object other) {
		return other instanceof XIDSetValue && checkEquals((XIDSetValue)other);
	}
	
	@Override
	public ValueType getComponentType() {
		return ValueType.Id;
	}
	
	@Override
	public ValueType getType() {
		return ValueType.IdSet;
	}
	
	@Override
	public int hashCode() {
		return getHashCode();
	}
	
	@Override
	public XIDSetValue remove(XID entry) {
		MemoryIDSetValue v = new MemoryIDSetValue(this.set);
		v.set.remove(entry);
		return v;
	}
	
	@Override
	public XID[] toArray() {
		return contents();
	}
	
}
