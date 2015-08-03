package org.xydra.base.value.impl.memory;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;

import org.xydra.base.value.ValueType;
import org.xydra.base.value.XLongListValue;
import org.xydra.index.XI;


/**
 * An implementation of {@link XLongListValue}
 *
 * @author kaidel
 *
 */
public class MemoryLongListValue extends MemoryListValue<Long> implements XLongListValue,
        Serializable {

	private static final long serialVersionUID = -3294191125211048647L;

	// non-final to be GWT-Serializable
	private long[] list;

	// empty constructor for GWT-Serializable
	protected MemoryLongListValue() {
	}

	public MemoryLongListValue(final Collection<Long> content) {
		this.list = new long[content.size()];
		int i = 0;
		for(final long b : content) {
			this.list[i++] = b;
		}
	}

	private MemoryLongListValue(final int length) {
		this.list = new long[length];
	}

	public MemoryLongListValue(final long[] content) {
		this.list = new long[content.length];
		System.arraycopy(content, 0, this.list, 0, content.length);
	}

	@Override
	public XLongListValue add(final int index, final Long entry) {
		final int size = this.list.length;
		if(index < 0 || index > size) {
			throw new IndexOutOfBoundsException();
		}
		final MemoryLongListValue v = new MemoryLongListValue(size + 1);
		System.arraycopy(this.list, 0, v.list, 0, index);
		v.list[index] = entry;
		System.arraycopy(this.list, index, v.list, index + 1, size - index);
		return v;
	}

	@Override
	public XLongListValue add(final Long entry) {
		return add(this.list.length, entry);
	}

	@Override
	public long[] contents() {
		final long[] array = new long[this.list.length];
		System.arraycopy(this.list, 0, array, 0, this.list.length);
		return array;
	}

	@Override
	public boolean equals(final Object other) {
		return other instanceof XLongListValue
		        && XI.equalsIterator(iterator(), ((XLongListValue)other).iterator());
	}

	@Override
	public Long get(final int index) {
		return this.list[index];
	}

	@Override
	public ValueType getComponentType() {
		return ValueType.Long;
	}

	@Override
	public ValueType getType() {
		return ValueType.LongList;
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(this.list);
	}

	@Override
	public XLongListValue remove(final int index) {
		final int size = this.list.length;
		if(index < 0 || index >= size) {
			throw new IndexOutOfBoundsException();
		}
		final MemoryLongListValue v = new MemoryLongListValue(size - 1);
		System.arraycopy(this.list, 0, v.list, 0, index);
		System.arraycopy(this.list, index + 1, v.list, index, size - index - 1);
		return v;
	}

	@Override
	public XLongListValue remove(final Long entry) {
		final int index = indexOf(entry);
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
		final Long[] array = new Long[this.list.length];
		fillArray(array);
		return array;
	}

	@Override
	public Number[] toNumberArray() {
		final Number[] array = new Number[this.list.length];
		int i = 0;
		for(final Number e : this) {
			array[i++] = e;
		}
		return array;
	}

	@Override
	public String toString() {
		return Arrays.toString(this.list);
	}

}
