package org.xydra.base.value.impl.memory;

import java.util.Arrays;
import java.util.Collection;

import org.xydra.base.value.ValueType;
import org.xydra.base.value.XLongListValue;
import org.xydra.index.XI;


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
	
	private MemoryLongListValue(int length) {
		this.list = new long[length];
	}
	
	public MemoryLongListValue(long[] content) {
		this.list = new long[content.length];
		System.arraycopy(content, 0, this.list, 0, content.length);
	}
	
	@Override
    public XLongListValue add(int index, Long entry) {
		int size = this.list.length;
		if(index < 0 || index > size) {
			throw new IndexOutOfBoundsException();
		}
		MemoryLongListValue v = new MemoryLongListValue(size + 1);
		System.arraycopy(this.list, 0, v.list, 0, index);
		v.list[index] = entry;
		System.arraycopy(this.list, index, v.list, index + 1, size - index);
		return v;
	}
	
	@Override
    public XLongListValue add(Long entry) {
		return add(this.list.length, entry);
	}
	
	@Override
    public long[] contents() {
		long[] array = new long[this.list.length];
		System.arraycopy(this.list, 0, array, 0, this.list.length);
		return array;
	}
	
	@Override
	public boolean equals(Object other) {
		return other instanceof XLongListValue
		        && XI.equalsIterator(this.iterator(), ((XLongListValue)other).iterator());
	}
	
	@Override
    public Long get(int index) {
		return this.list[index];
	}
	
	@Override
	public int hashCode() {
		return Arrays.hashCode(this.list);
	}
	
	@Override
    public XLongListValue remove(int index) {
		int size = this.list.length;
		if(index < 0 || index >= size) {
			throw new IndexOutOfBoundsException();
		}
		MemoryLongListValue v = new MemoryLongListValue(size - 1);
		System.arraycopy(this.list, 0, v.list, 0, index);
		System.arraycopy(this.list, index + 1, v.list, index, size - index - 1);
		return v;
	}
	
	@Override
    public XLongListValue remove(Long entry) {
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
    public Long[] toArray() {
		Long[] array = new Long[this.list.length];
		fillArray(array);
		return array;
	}
	
	@Override
    public Number[] toNumberArray() {
		Number[] array = new Number[this.list.length];
		int i = 0;
		for(Number e : this) {
			array[i++] = e;
		}
		return array;
	}
	
	@Override
	public String toString() {
		return Arrays.toString(this.list);
	}
	
	@Override
	public ValueType getType() {
		return ValueType.LongList;
	}
	
	@Override
	public ValueType getComponentType() {
		return ValueType.Long;
	}
	
}
