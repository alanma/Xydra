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
public class MemoryIdSortedSetValue extends MemoryIdListValue implements XIdSortedSetValue,
        Serializable {
	
	private static final long serialVersionUID = -83885798275571937L;
	
	// empty constructor for GWT-Serializable
	protected MemoryIdSortedSetValue() {
	}
	
	public MemoryIdSortedSetValue(Collection<XId> contents) {
		super(contents);
	}
	
	public MemoryIdSortedSetValue(XId[] contents) {
		super(contents);
	}
	
	@Override
	public MemoryIdSortedSetValue add(XId entry) {
		if(this.contains(entry)) {
			// no need to add it
			return this;
		} else {
			XId[] newList = MemoryIdListValue.createArrayWithEntryInsertedAtPosition(
			        this.contents(), this.contents().length, entry);
			return new MemoryIdSortedSetValue(newList);
		}
	}
	
	@Override
	public ValueType getType() {
		return ValueType.IdSortedSet;
	}
	
	@Override
	public MemoryIdSortedSetValue remove(XId entry) {
		// find it
		int index = this.indexOf(entry);
		if(index == -1) {
			// not possible to remove it
			return this;
		} else {
			XId[] newList = MemoryIdListValue.createArrayWithEntryRemovedAtPosition(
			        this.contents(), index);
			return new MemoryIdSortedSetValue(newList);
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
