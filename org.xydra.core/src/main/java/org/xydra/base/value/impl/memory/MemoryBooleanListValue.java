package org.xydra.base.value.impl.memory;

import java.util.Arrays;
import java.util.Collection;

import org.xydra.base.value.ValueType;
import org.xydra.base.value.XBooleanListValue;
import org.xydra.index.XI;


/**
 * An implementation of {@link XBooleanListValue}
 * 
 * @author Kaidel
 * @author dscharrer
 * 
 */
public class MemoryBooleanListValue extends MemoryListValue<Boolean> implements XBooleanListValue {
	
	private static final long serialVersionUID = -2012063819510070665L;
	
	private final boolean[] list;
	
	public MemoryBooleanListValue(boolean[] content) {
		this.list = new boolean[content.length];
		System.arraycopy(content, 0, this.list, 0, content.length);
	}
	
	public MemoryBooleanListValue(Collection<Boolean> content) {
		this.list = new boolean[content.size()];
		int i = 0;
		for(boolean b : content) {
			this.list[i++] = b;
		}
	}
	
	private MemoryBooleanListValue(int length) {
		this.list = new boolean[length];
	}
	
	public XBooleanListValue add(Boolean entry) {
		return add(this.list.length, entry);
	}
	
	public XBooleanListValue add(int index, Boolean entry) {
		int size = this.list.length;
		if(index < 0 || index > size) {
			throw new IndexOutOfBoundsException();
		}
		MemoryBooleanListValue v = new MemoryBooleanListValue(size + 1);
		System.arraycopy(this.list, 0, v.list, 0, index);
		v.list[index] = entry;
		System.arraycopy(this.list, index, v.list, index + 1, size - index);
		return v;
	}
	
	public boolean[] contents() {
		boolean[] array = new boolean[this.list.length];
		System.arraycopy(this.list, 0, array, 0, this.list.length);
		return array;
	}
	
	@Override
	public boolean equals(Object other) {
		return other instanceof XBooleanListValue
		        && XI.equalsIterator(this.iterator(), ((XBooleanListValue)other).iterator());
	}
	
	public Boolean get(int index) {
		return this.list[index];
	}
	
	@Override
	public int hashCode() {
		return Arrays.hashCode(this.list);
	}
	
	public XBooleanListValue remove(Boolean entry) {
		int index = indexOf(entry);
		if(index < 0) {
			return this;
		}
		return remove(index);
	}
	
	public XBooleanListValue remove(int index) {
		int size = this.list.length;
		if(index < 0 || index >= size) {
			throw new IndexOutOfBoundsException();
		}
		MemoryBooleanListValue v = new MemoryBooleanListValue(size - 1);
		System.arraycopy(this.list, 0, v.list, 0, index);
		System.arraycopy(this.list, index + 1, v.list, index, size - index - 1);
		return v;
	}
	
	public int size() {
		return this.list.length;
	}
	
	public Boolean[] toArray() {
		Boolean[] array = new Boolean[this.list.length];
		fillArray(array);
		return array;
	}
	
	@Override
	public String toString() {
		return Arrays.toString(this.list);
	}
	
	@Override
	public ValueType getType() {
		return ValueType.BooleanList;
	}
	
	@Override
	public ValueType getComponentType() {
		return ValueType.Boolean;
	}
	
}
