package org.xydra.base.value.impl.memory;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;

import org.xydra.base.value.ValueType;
import org.xydra.base.value.XStringListValue;
import org.xydra.index.XI;


/**
 * An implementation of {@link XStringListValue}
 *
 * @author kaidel
 *
 */
public class MemoryStringListValue extends MemoryListValue<String> implements XStringListValue,
        Serializable {

	private static final long serialVersionUID = -1175161517909257560L;

	// non-final to be GWT-Serializable
	private String[] list;

	// empty constructor for GWT-Serializable
	protected MemoryStringListValue() {
	}

	public MemoryStringListValue(final Collection<String> content) {
		this.list = content.toArray(new String[content.size()]);
	}

	private MemoryStringListValue(final int length) {
		this.list = new String[length];
	}

	public MemoryStringListValue(final String[] content) {
		this.list = new String[content.length];
		System.arraycopy(content, 0, this.list, 0, content.length);
	}

	@Override
	public XStringListValue add(final int index, final String entry) {
		final int size = this.list.length;
		if(index < 0 || index > size) {
			throw new IndexOutOfBoundsException();
		}
		final MemoryStringListValue v = new MemoryStringListValue(size + 1);
		System.arraycopy(this.list, 0, v.list, 0, index);
		v.list[index] = entry;
		System.arraycopy(this.list, index, v.list, index + 1, size - index);
		return v;
	}

	@Override
	public XStringListValue add(final String entry) {
		return add(this.list.length, entry);
	}

	@Override
	public String[] contents() {
		final String[] array = new String[this.list.length];
		System.arraycopy(this.list, 0, array, 0, this.list.length);
		return array;
	}

	@Override
	public boolean equals(final Object other) {
		return other instanceof XStringListValue
		        && XI.equalsIterator(iterator(), ((XStringListValue)other).iterator());
	}

	@Override
	public String get(final int index) {
		return this.list[index];
	}

	@Override
	public ValueType getComponentType() {
		return ValueType.String;
	}

	@Override
	public ValueType getType() {
		return ValueType.StringList;
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(this.list);
	}

	@Override
	public XStringListValue remove(final int index) {
		final int size = this.list.length;
		if(index < 0 || index >= size) {
			throw new IndexOutOfBoundsException();
		}
		final MemoryStringListValue v = new MemoryStringListValue(size - 1);
		System.arraycopy(this.list, 0, v.list, 0, index);
		System.arraycopy(this.list, index + 1, v.list, index, size - index - 1);
		return v;
	}

	@Override
	public XStringListValue remove(final String entry) {
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
	public String[] toArray() {
		return contents();
	}

	@Override
	public String toString() {
		return Arrays.toString(this.list);
	}

}
