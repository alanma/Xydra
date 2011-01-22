package org.xydra.base.value.impl.memory;

import java.util.Arrays;
import java.util.Collection;

import org.xydra.base.value.XByteListValue;
import org.xydra.index.XI;


/**
 * An implementation of {@link XByteListValue}
 * 
 * @author dscharrer
 * 
 */
public class MemoryByteListValue extends MemoryListValue<Byte> implements XByteListValue {
	
	private static final long serialVersionUID = -674503742791516328L;
	
	private final byte[] list;
	
	public MemoryByteListValue(byte[] content) {
		this.list = new byte[content.length];
		System.arraycopy(content, 0, this.list, 0, content.length);
	}
	
	public MemoryByteListValue(Collection<Byte> content) {
		this.list = new byte[content.size()];
		int i = 0;
		for(byte b : content) {
			this.list[i++] = b;
		}
	}
	
	private MemoryByteListValue(int length) {
		this.list = new byte[length];
	}
	
	public XByteListValue add(Byte entry) {
		return add(this.list.length, entry);
	}
	
	public XByteListValue add(int index, Byte entry) {
		int size = this.list.length;
		if(index < 0 || index > size) {
			throw new IndexOutOfBoundsException();
		}
		MemoryByteListValue v = new MemoryByteListValue(size + 1);
		System.arraycopy(this.list, 0, v.list, 0, index);
		v.list[index] = entry;
		System.arraycopy(this.list, index, v.list, index + 1, size - index);
		return v;
	}
	
	public byte[] contents() {
		byte[] array = new byte[this.list.length];
		System.arraycopy(this.list, 0, array, 0, this.list.length);
		return array;
	}
	
	@Override
	public boolean equals(Object other) {
		return other instanceof XByteListValue
		        && XI.equalsIterator(this.iterator(), ((XByteListValue)other).iterator());
	}
	
	public Byte get(int index) {
		return this.list[index];
	}
	
	@Override
	public int hashCode() {
		return Arrays.hashCode(this.list);
	}
	
	public XByteListValue remove(Byte entry) {
		int index = indexOf(entry);
		if(index < 0) {
			return this;
		}
		return remove(index);
	}
	
	public XByteListValue remove(int index) {
		int size = this.list.length;
		if(index < 0 || index >= size) {
			throw new IndexOutOfBoundsException();
		}
		MemoryByteListValue v = new MemoryByteListValue(size - 1);
		System.arraycopy(this.list, 0, v.list, 0, index);
		System.arraycopy(this.list, index + 1, v.list, index, size - index - 1);
		return v;
	}
	
	public int size() {
		return this.list.length;
	}
	
	public Byte[] toArray() {
		Byte[] array = new Byte[this.list.length];
		fillArray(array);
		return array;
	}
	
	@Override
	public String toString() {
		return Arrays.toString(this.list);
	}
	
}
