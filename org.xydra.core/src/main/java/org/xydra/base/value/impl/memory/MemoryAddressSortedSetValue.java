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
import org.xydra.base.value.XIdSortedSetValue;


/**
 * An implementation of {@link XIdSortedSetValue}
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

	public MemoryAddressSortedSetValue(final Collection<XAddress> contents) {
		super(contents);
	}

	public MemoryAddressSortedSetValue(final XAddress[] contents) {
		super(contents);
	}

	@Override
	public MemoryAddressSortedSetValue add(final XAddress entry) {
		if(contains(entry)) {
			// no need to add it
			return this;
		} else {
			final XAddress[] newList = MemoryAddressListValue.createArrayWithEntryInsertedAtPosition(
			        contents(), contents().length, entry);
			return new MemoryAddressSortedSetValue(newList);
		}
	}

	@Override
	public ValueType getType() {
		return ValueType.AddressSortedSet;
	}

	@Override
	public MemoryAddressSortedSetValue remove(final XAddress entry) {
		// find it
		final int index = indexOf(entry);
		if(index == -1) {
			// not possible to remove it
			return this;
		} else {
			final XAddress[] newList = MemoryAddressListValue.createArrayWithEntryRemovedAtPosition(
			        contents(), index);
			return new MemoryAddressSortedSetValue(newList);
		}
	}

	@Override
	public Set<XAddress> toSet() {
		final Set<XAddress> copy = new HashSet<XAddress>();
		final XAddress[] list = contents();
		for(int i = 0; i < list.length; i++) {
			copy.add(list[i]);
		}
		return copy;
	}

	@Override
	public SortedSet<XAddress> toSortedSet() {
		final SortedSet<XAddress> copy = new TreeSet<XAddress>();
		final XAddress[] list = contents();
		for(int i = 0; i < list.length; i++) {
			copy.add(list[i]);
		}
		return copy;
	}
}
