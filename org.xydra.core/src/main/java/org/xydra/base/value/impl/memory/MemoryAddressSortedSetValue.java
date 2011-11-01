package org.xydra.base.value.impl.memory;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.xydra.base.XAddress;
import org.xydra.base.value.ValueType;
import org.xydra.base.value.XAddressSortedSetValue;
import org.xydra.base.value.XIDSortedSetValue;


/**
 * An implementation of {@link XIDSortedSetValue}
 * 
 * @author dscharrer, voelkel
 * 
 */
public class MemoryAddressSortedSetValue extends MemoryAddressListValue implements
        XAddressSortedSetValue, Serializable {
	
	private static final long serialVersionUID = -83885798275571937L;
	
	// empty constructor for GWT-Serializable
	protected MemoryAddressSortedSetValue() {
	}
	
	public MemoryAddressSortedSetValue(Collection<XAddress> contents) {
		super(contents);
	}
	
	public MemoryAddressSortedSetValue(XAddress[] contents) {
		super(contents);
	}
	
	@Override
	public MemoryAddressSortedSetValue add(XAddress entry) {
		if(this.contains(entry)) {
			// no need to add it
			return this;
		} else {
			XAddress[] newList = MemoryAddressListValue.createArrayWithEntryInsertedAtPosition(
			        this.contents(), this.contents().length, entry);
			return new MemoryAddressSortedSetValue(newList);
		}
	}
	
	@Override
	public ValueType getType() {
		return ValueType.AddressSortedSet;
	}
	
	@Override
	public MemoryAddressSortedSetValue remove(XAddress entry) {
		// find it
		int index = this.indexOf(entry);
		if(index == -1) {
			// not possible to remove it
			return this;
		} else {
			XAddress[] newList = MemoryAddressListValue.createArrayWithEntryRemovedAtPosition(
			        this.contents(), index);
			return new MemoryAddressSortedSetValue(newList);
		}
	}
	
	@Override
	public Set<XAddress> toSet() {
		Set<XAddress> copy = new HashSet<XAddress>();
		XAddress[] list = this.contents();
		for(int i = 0; i < list.length; i++) {
			copy.add(list[i]);
		}
		return copy;
	}
	
	@Override
	public SortedSet<XAddress> toSortedSet() {
		SortedSet<XAddress> copy = new TreeSet<XAddress>();
		XAddress[] list = this.contents();
		for(int i = 0; i < list.length; i++) {
			copy.add(list[i]);
		}
		return copy;
	}
}
