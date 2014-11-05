package org.xydra.base.value.impl.memory;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;

import org.xydra.base.XAddress;
import org.xydra.base.value.ValueType;
import org.xydra.base.value.XAddressListValue;
import org.xydra.base.value.XIdListValue;
import org.xydra.index.XI;


/**
 * An implementation of {@link XIdListValue}
 * 
 * @author kaidel
 * @author xamde
 * 
 */
public class MemoryAddressListValue extends MemoryListValue<XAddress> implements XAddressListValue,
        Serializable {
	
	private static final long serialVersionUID = -7641986388917629097L;
	
	public static final XAddress[] createArrayWithEntryInsertedAtPosition(XAddress[] array,
	        int position, XAddress entry) {
		int size = array.length;
		if(position < 0 || position > size) {
			throw new IndexOutOfBoundsException();
		}
		XAddress[] newList = new XAddress[size + 1];
		System.arraycopy(array, 0, newList, 0, position);
		newList[position] = entry;
		System.arraycopy(array, position, newList, position + 1, size - position);
		return newList;
	}
	
	public static final XAddress[] createArrayWithEntryRemovedAtPosition(XAddress[] array,
	        int position) {
		int size = array.length;
		if(position < 0 || position >= size) {
			throw new IndexOutOfBoundsException();
		}
		XAddress[] newList = new XAddress[size - 1];
		System.arraycopy(array, 0, newList, 0, position);
		System.arraycopy(array, position + 1, newList, position, size - position - 1);
		return newList;
	}
	
	// non-final to be GWT-Serializable
	private XAddress[] list;
	
	// empty constructor for GWT-Serializable
	protected MemoryAddressListValue() {
	}
	
	public MemoryAddressListValue(Collection<XAddress> content) {
		this.list = content.toArray(new XAddress[content.size()]);
	}
	
	@SuppressWarnings("unused")
	private MemoryAddressListValue(int length) {
		this.list = new XAddress[length];
	}
	
	public MemoryAddressListValue(XAddress[] content) {
		this.list = new XAddress[content.length];
		System.arraycopy(content, 0, this.list, 0, content.length);
	}
	
	@Override
	public XAddressListValue add(int index, XAddress entry) {
		XAddress[] newList = createArrayWithEntryInsertedAtPosition(this.list, index, entry);
		return new MemoryAddressListValue(newList);
	}
	
	@Override
	public XAddressListValue add(XAddress entry) {
		return add(this.list.length, entry);
	}
	
	@Override
	public XAddress[] contents() {
		XAddress[] array = new XAddress[this.list.length];
		System.arraycopy(this.list, 0, array, 0, this.list.length);
		return array;
	}
	
	@Override
	public boolean equals(Object other) {
		return other instanceof XAddressListValue
		        && XI.equalsIterator(this.iterator(), ((XAddressListValue)other).iterator());
	}
	
	@Override
	public XAddress get(int index) {
		return this.list[index];
	}
	
	@Override
	public ValueType getComponentType() {
		return ValueType.Address;
	}
	
	@Override
	public ValueType getType() {
		return ValueType.AddressList;
	}
	
	@Override
	public int hashCode() {
		return Arrays.hashCode(this.list);
	}
	
	@Override
	public XAddressListValue remove(int index) {
		XAddress[] newList = createArrayWithEntryRemovedAtPosition(this.contents(), index);
		return new MemoryAddressListValue(newList);
	}
	
	@Override
	public XAddressListValue remove(XAddress entry) {
		int index = indexOf(entry);
		if(index < 0) {
			return this;
		}
		return remove(index);
	}
	
	@Override
	public int size() {
		return this.list.length;
	}
	
	@Override
	public XAddress[] toArray() {
		return contents();
	}
	
	@Override
	public String toString() {
		return Arrays.toString(this.list);
	}
	
}
