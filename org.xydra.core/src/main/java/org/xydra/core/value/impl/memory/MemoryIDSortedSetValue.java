package org.xydra.core.value.impl.memory;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.xydra.core.model.XID;
import org.xydra.core.value.XIDSortedSetValue;


/**
 * An implementation of {@link XIDSortedSetValue}
 * 
 * @author dscharrer, voelkel
 * 
 */
public class MemoryIDSortedSetValue extends MemoryIDListValue implements XIDSortedSetValue {
	
	private static final long serialVersionUID = -83885798275571937L;
	
	public MemoryIDSortedSetValue(XID[] contents) {
		super(contents);
	}
	
	public MemoryIDSortedSetValue(Collection<XID> contents) {
		super(contents);
	}
	
	@Override
	public MemoryIDSortedSetValue add(XID entry) {
		MemoryIDSortedSetValue v = new MemoryIDSortedSetValue(this.contents());
		if(!v.contains(entry)) {
			v.add(entry);
		}
		return v;
	}
	
	@Override
	public MemoryIDSortedSetValue remove(XID entry) {
		MemoryIDSortedSetValue v = new MemoryIDSortedSetValue(this.contents());
		v.remove(entry);
		return v;
	}
	
	public Set<XID> toSet() {
		Set<XID> copy = new HashSet<XID>();
		XID[] list = this.contents();
		for(int i = 0; i < list.length; i++) {
			copy.add(list[i]);
		}
		return copy;
	}
	
}
