package org.xydra.base.value.impl.memory;

import java.util.Arrays;
import java.util.Collection;

import org.xydra.base.value.ValueType;
import org.xydra.base.value.XStringListValue;
import org.xydra.index.XI;


/**
 * An implementation of {@link XStringListValue}
 * 
 * @author Kaidel
 * 
 */
public class MemoryStringListValue extends MemoryListValue<String> implements XStringListValue {
	
	private static final long serialVersionUID = -1175161517909257560L;
	
	private final String[] list;
	
	public MemoryStringListValue(Collection<String> content) {
		this.list = content.toArray(new String[content.size()]);
	}
	
	private MemoryStringListValue(int length) {
		this.list = new String[length];
	}
	
	public MemoryStringListValue(String[] content) {
		this.list = new String[content.length];
		System.arraycopy(content, 0, this.list, 0, content.length);
	}
	
	public XStringListValue add(int index, String entry) {
		int size = this.list.length;
		if(index < 0 || index > size) {
			throw new IndexOutOfBoundsException();
		}
		MemoryStringListValue v = new MemoryStringListValue(size + 1);
		System.arraycopy(this.list, 0, v.list, 0, index);
		v.list[index] = entry;
		System.arraycopy(this.list, index, v.list, index + 1, size - index);
		return v;
	}
	
	public XStringListValue add(String entry) {
		return add(this.list.length, entry);
	}
	
	public String[] contents() {
		String[] array = new String[this.list.length];
		System.arraycopy(this.list, 0, array, 0, this.list.length);
		return array;
	}
	
	@Override
	public boolean equals(Object other) {
		return other instanceof XStringListValue
		        && XI.equalsIterator(this.iterator(), ((XStringListValue)other).iterator());
	}
	
	public String get(int index) {
		return this.list[index];
	}
	
	@Override
	public int hashCode() {
		return Arrays.hashCode(this.list);
	}
	
	public XStringListValue remove(int index) {
		int size = this.list.length;
		if(index < 0 || index >= size) {
			throw new IndexOutOfBoundsException();
		}
		MemoryStringListValue v = new MemoryStringListValue(size - 1);
		System.arraycopy(this.list, 0, v.list, 0, index);
		System.arraycopy(this.list, index + 1, v.list, index, size - index - 1);
		return v;
	}
	
	public XStringListValue remove(String entry) {
		int index = indexOf(entry);
		if(index < 0) {
			return this;
		}
		return remove(index);
	}
	
	public int size() {
		return this.list.length;
	}
	
	public String[] toArray() {
		return contents();
	}
	
	@Override
	public String toString() {
		return Arrays.toString(this.list);
	}
	
	@Override
	public ValueType getType() {
		return ValueType.StringList;
	}
	
	@Override
	public ValueType getComponentType() {
		return ValueType.String;
	}
	
}
