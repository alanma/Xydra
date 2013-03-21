package org.xydra.base.value.impl.memory;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.xydra.base.XId;
import org.xydra.base.value.ValueType;
import org.xydra.base.value.XIdSortedSetValue;


/**
 * An implementation of {@link XIdSortedSetValue}
 * 
 * @author dscharrer, voelkel
 * 
 */
public class MemoryIDSortedSetValue extends MemoryIDListValue implements XIdSortedSetValue,
        Serializable {
	
	private static final long serialVersionUID = -83885798275571937L;
	
	// empty constructor for GWT-Serializable
	protected MemoryIDSortedSetValue() {
	}
	
	public MemoryIDSortedSetValue(Collection<XId> contents) {
		super(contents);
	}
	
	public MemoryIDSortedSetValue(XId[] contents) {
		super(contents);
	}
	
	@Override
	public MemoryIDSortedSetValue add(XId entry) {
		if(this.contains(entry)) {
			// no need to add it
			return this;
		} else {
			XId[] newList = MemoryIDListValue.createArrayWithEntryInsertedAtPosition(
			        this.contents(), this.contents().length, entry);
			return new MemoryIDSortedSetValue(newList);
		}
	}
	
	@Override
	public ValueType getType() {
		return ValueType.IdSortedSet;
	}
	
	@Override
	public MemoryIDSortedSetValue remove(XId entry) {
		// find it
		int index = this.indexOf(entry);
		if(index == -1) {
			// not possible to remove it
			return this;
		} else {
			XId[] newList = MemoryIDListValue.createArrayWithEntryRemovedAtPosition(
			        this.contents(), index);
			return new MemoryIDSortedSetValue(newList);
		}
	}
	
	@Override
	public Set<XId> toSet() {
		Set<XId> copy = new HashSet<XId>();
		XId[] list = this.contents();
		for(int i = 0; i < list.length; i++) {
			copy.add(list[i]);
		}
		return copy;
	}
	
	@Override
	public SortedSet<XId> toSortedSet() {
		SortedSet<XId> copy = new TreeSet<XId>();
		XId[] list = this.contents();
		for(int i = 0; i < list.length; i++) {
			copy.add(list[i]);
		}
		return copy;
	}
	
}
