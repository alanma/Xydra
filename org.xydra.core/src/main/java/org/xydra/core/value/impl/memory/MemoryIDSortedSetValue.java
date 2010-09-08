package org.xydra.core.value.impl.memory;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

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
		if(this.contains(entry)) {
			// no need to add it
			return this;
		} else {
			XID[] newList = MemoryIDListValue.createArrayWithEntryInsertedAtPosition(this
			        .contents(), this.contents().length, entry);
			return new MemoryIDSortedSetValue(newList);
		}
	}
	
	@Override
	public MemoryIDSortedSetValue remove(XID entry) {
		// find it
		int index = this.indexOf(entry);
		if(index == -1) {
			// not possible to remove it
			return this;
		} else {
			XID[] newList = MemoryIDListValue.createArrayWithEntryRemovedAtPosition(
			        this.contents(), index);
			return new MemoryIDSortedSetValue(newList);
		}
	}
	
	public Set<XID> toSet() {
		Set<XID> copy = new HashSet<XID>();
		XID[] list = this.contents();
		for(int i = 0; i < list.length; i++) {
			copy.add(list[i]);
		}
		return copy;
	}
	
	public SortedSet<XID> toSortedSet() {
		SortedSet<XID> copy = new TreeSet<XID>();
		XID[] list = this.contents();
		for(int i = 0; i < list.length; i++) {
			copy.add(list[i]);
		}
		return copy;
	}
	
}
