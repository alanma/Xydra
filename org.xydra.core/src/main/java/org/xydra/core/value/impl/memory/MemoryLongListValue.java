package org.xydra.core.value.impl.memory;

import java.util.Arrays;
import java.util.Collection;

import org.xydra.core.XX;
import org.xydra.core.value.XLongListValue;


/**
 * An implementation of {@link XLongListValue}
 * 
 * @author Kaidel
 * 
 */
public class MemoryLongListValue extends MemoryListValue<Long> implements XLongListValue {
	
	private static final long serialVersionUID = -3294191125211048647L;
	
	private final long[] list;
	
	public MemoryLongListValue(Collection<Long> content) {
		this.list = new long[content.size()];
		int i = 0;
		for(long b : content) {
			this.list[i++] = b;
		}
	}
	
	public MemoryLongListValue(long[] content) {
		this.list = new long[content.length];
		System.arraycopy(content, 0, this.list, 0, content.length);
	}
	
	public long[] contents() {
		long[] array = new long[this.list.length];
		System.arraycopy(this.list, 0, array, 0, this.list.length);
		return array;
	}
	
	public Long[] toArray() {
		Long[] array = new Long[this.list.length];
		fillArray(array);
		return array;
	}
	
	@Override
	public boolean equals(Object other) {
		return other instanceof XLongListValue
		        && XX.equalsIterator(this.iterator(), ((XLongListValue)other).iterator());
	}
	
	@Override
	public int hashCode() {
		return Arrays.hashCode(this.list);
	}
	
	@Override
	public String toString() {
		return Arrays.toString(this.list);
	}
	
	public Long get(int index) {
		return this.list[index];
	}
	
	public int size() {
		return this.list.length;
	}
	
	public Number[] toNumberArray() {
		Number[] array = new Number[this.list.length];
		int i = 0;
		for(Number e : this) {
			array[i++] = e;
		}
		return array;
	}
	
}
