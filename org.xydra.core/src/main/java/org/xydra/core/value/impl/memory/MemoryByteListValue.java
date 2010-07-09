package org.xydra.core.value.impl.memory;

import java.util.Arrays;
import java.util.Collection;

import org.xydra.core.XX;
import org.xydra.core.value.XByteListValue;


/**
 * An implementation of {@link XByteListValue}
 * 
 * @author dscharrer
 * 
 */
public class MemoryByteListValue extends MemoryListValue<Byte> implements XByteListValue {
	
	private static final long serialVersionUID = -674503742791516328L;
	
	private final byte[] list;
	
	public MemoryByteListValue(Collection<Byte> content) {
		this.list = new byte[content.size()];
		int i = 0;
		for(byte b : content) {
			this.list[i++] = b;
		}
	}
	
	public MemoryByteListValue(byte[] content) {
		this.list = new byte[content.length];
		System.arraycopy(content, 0, this.list, 0, content.length);
	}
	
	public byte[] contents() {
		byte[] array = new byte[this.list.length];
		System.arraycopy(this.list, 0, array, 0, this.list.length);
		return array;
	}
	
	public Byte[] toArray() {
		Byte[] array = new Byte[this.list.length];
		fillArray(array);
		return array;
	}
	
	@Override
	public boolean equals(Object other) {
		return other instanceof XByteListValue
		        && XX.equalsIterator(this.iterator(), ((XByteListValue)other).iterator());
	}
	
	@Override
	public int hashCode() {
		return Arrays.hashCode(this.list);
	}
	
	@Override
	public String toString() {
		return Arrays.toString(this.list);
	}
	
	public Byte get(int index) {
		return this.list[index];
	}
	
	public int size() {
		return this.list.length;
	}
	
}
