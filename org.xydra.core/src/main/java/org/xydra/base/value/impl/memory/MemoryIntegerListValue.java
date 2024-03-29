package org.xydra.base.value.impl.memory;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;

import org.xydra.base.value.ValueType;
import org.xydra.base.value.XIntegerListValue;
import org.xydra.index.XI;


/**
 * An implementation of {@link XIntegerListValue}
 *
 * @author kaidel
 *
 */
public class MemoryIntegerListValue extends MemoryListValue<Integer> implements XIntegerListValue,
        Serializable {

	private static final long serialVersionUID = -2698183990443882191L;

	// non-final to be GWT-Serializable
	private int[] list;

	// empty constructor for GWT-Serializable
	protected MemoryIntegerListValue() {
	}

	public MemoryIntegerListValue(final Collection<Integer> content) {
		this.list = new int[content.size()];
		int i = 0;
		for(final int b : content) {
			this.list[i++] = b;
		}
	}

	private MemoryIntegerListValue(final int length) {
		this.list = new int[length];
	}

	public MemoryIntegerListValue(final int[] content) {
		this.list = new int[content.length];
		System.arraycopy(content, 0, this.list, 0, content.length);
	}

	@Override
	public XIntegerListValue add(final int index, final Integer entry) {
		final int size = this.list.length;
		if(index < 0 || index > size) {
			throw new IndexOutOfBoundsException();
		}
		final MemoryIntegerListValue v = new MemoryIntegerListValue(size + 1);
		System.arraycopy(this.list, 0, v.list, 0, index);
		v.list[index] = entry;
		System.arraycopy(this.list, index, v.list, index + 1, size - index);
		return v;
	}

	@Override
	public XIntegerListValue add(final Integer entry) {
		return add(this.list.length, entry);
	}

	@Override
	public int[] contents() {
		final int[] array = new int[this.list.length];
		System.arraycopy(this.list, 0, array, 0, this.list.length);
		return array;
	}

	@Override
	public boolean equals(final Object other) {
		return other instanceof XIntegerListValue
		        && XI.equalsIterator(iterator(), ((XIntegerListValue)other).iterator());
	}

	@Override
	public Integer get(final int index) {
		return this.list[index];
	}

	@Override
	public ValueType getComponentType() {
		return ValueType.Integer;
	}

	@Override
	public ValueType getType() {
		return ValueType.IntegerList;
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(this.list);
	}

	@Override
	public XIntegerListValue remove(final int index) {
		final int size = this.list.length;
		if(index < 0 || index >= size) {
			throw new IndexOutOfBoundsException();
		}
		final MemoryIntegerListValue v = new MemoryIntegerListValue(size - 1);
		System.arraycopy(this.list, 0, v.list, 0, index);
		System.arraycopy(this.list, index + 1, v.list, index, size - index - 1);
		return v;
	}

	@Override
	public XIntegerListValue remove(final Integer entry) {
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
	public Integer[] toArray() {
		final Integer[] array = new Integer[this.list.length];
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
