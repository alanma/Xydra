package org.xydra.base.value.impl.memory;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;

import org.xydra.base.XId;
import org.xydra.base.value.ValueType;
import org.xydra.base.value.XIdListValue;
import org.xydra.index.XI;


/**
 * An implementation of {@link XIdListValue}
 * 
 * @author Kaidel
 * @author voelkel
 * 
 */
public class MemoryIdListValue extends MemoryListValue<XId> implements XIdListValue, Serializable {
	
	private static final long serialVersionUID = -7641986388917629097L;
	
	public static final XId[] createArrayWithEntryInsertedAtPosition(XId[] array, int position,
	        XId entry) {
		int size = array.length;
		if(position < 0 || position > size) {
			throw new IndexOutOfBoundsException();
		}
		XId[] newList = new XId[size + 1];
		System.arraycopy(array, 0, newList, 0, position);
		newList[position] = entry;
		System.arraycopy(array, position, newList, position + 1, size - position);
		return newList;
	}
	
	public static final XId[] createArrayWithEntryRemovedAtPosition(XId[] array, int position) {
		int size = array.length;
		if(position < 0 || position >= size) {
			throw new IndexOutOfBoundsException();
		}
		XId[] newList = new XId[size - 1];
		System.arraycopy(array, 0, newList, 0, position);
		System.arraycopy(array, position + 1, newList, position, size - position - 1);
		return newList;
	}
	
	// non-final to be GWT-Serializable
	private XId[] list;
	
	// empty constructor for GWT-Serializable
	protected MemoryIdListValue() {
	}
	
	public MemoryIdListValue(Collection<XId> content) {
		this.list = content.toArray(new XId[content.size()]);
	}
	
	@SuppressWarnings("unused")
	private MemoryIdListValue(int length) {
		this.list = new XId[length];
	}
	
	public MemoryIdListValue(XId[] content) {
		this.list = new XId[content.length];
		System.arraycopy(content, 0, this.list, 0, content.length);
	}
	
	@Override
	public XIdListValue add(int index, XId entry) {
		XId[] newList = createArrayWithEntryInsertedAtPosition(this.list, index, entry);
		return new MemoryIdListValue(newList);
	}
	
	@Override
	public XIdListValue add(XId entry) {
		return add(this.list.length, entry);
	}
	
	@Override
	public XId[] contents() {
		XId[] array = new XId[this.list.length];
		System.arraycopy(this.list, 0, array, 0, this.list.length);
		return array;
	}
	
	@Override
	public boolean equals(Object other) {
		return other instanceof XIdListValue
		        && XI.equalsIterator(this.iterator(), ((XIdListValue)other).iterator());
	}
	
	@Override
	public XId get(int index) {
		return this.list[index];
	}
	
	@Override
	public ValueType getComponentType() {
		return ValueType.Id;
	}
	
	@Override
	public ValueType getType() {
		return ValueType.IdList;
	}
	
	@Override
	public int hashCode() {
		return Arrays.hashCode(this.list);
	}
	
	@Override
	public XIdListValue remove(int index) {
		XId[] newList = createArrayWithEntryRemovedAtPosition(this.contents(), index);
		return new MemoryIdListValue(newList);
	}
	
	@Override
	public XIdListValue remove(XId entry) {
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
	public XId[] toArray() {
		return contents();
	}
	
	@Override
	public String toString() {
		return Arrays.toString(this.list);
	}
	
}
