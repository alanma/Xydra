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
 * @author kaidel
 * @author xamde
 *
 */
public class MemoryIdListValue extends MemoryListValue<XId> implements XIdListValue, Serializable {

	private static final long serialVersionUID = -7641986388917629097L;

	public static final XId[] createArrayWithEntryInsertedAtPosition(final XId[] array, final int position,
	        final XId entry) {
		final int size = array.length;
		if(position < 0 || position > size) {
			throw new IndexOutOfBoundsException();
		}
		final XId[] newList = new XId[size + 1];
		System.arraycopy(array, 0, newList, 0, position);
		newList[position] = entry;
		System.arraycopy(array, position, newList, position + 1, size - position);
		return newList;
	}

	public static final XId[] createArrayWithEntryRemovedAtPosition(final XId[] array, final int position) {
		final int size = array.length;
		if(position < 0 || position >= size) {
			throw new IndexOutOfBoundsException();
		}
		final XId[] newList = new XId[size - 1];
		System.arraycopy(array, 0, newList, 0, position);
		System.arraycopy(array, position + 1, newList, position, size - position - 1);
		return newList;
	}

	// non-final to be GWT-Serializable
	private XId[] list;

	// empty constructor for GWT-Serializable
	protected MemoryIdListValue() {
	}

	public MemoryIdListValue(final Collection<XId> content) {
		this.list = content.toArray(new XId[content.size()]);
	}

	@SuppressWarnings("unused")
	private MemoryIdListValue(final int length) {
		this.list = new XId[length];
	}

	public MemoryIdListValue(final XId[] content) {
		this.list = new XId[content.length];
		System.arraycopy(content, 0, this.list, 0, content.length);
	}

	@Override
	public XIdListValue add(final int index, final XId entry) {
		final XId[] newList = createArrayWithEntryInsertedAtPosition(this.list, index, entry);
		return new MemoryIdListValue(newList);
	}

	@Override
	public XIdListValue add(final XId entry) {
		return add(this.list.length, entry);
	}

	@Override
	public XId[] contents() {
		final XId[] array = new XId[this.list.length];
		System.arraycopy(this.list, 0, array, 0, this.list.length);
		return array;
	}

	@Override
	public boolean equals(final Object other) {
		return other instanceof XIdListValue
		        && XI.equalsIterator(iterator(), ((XIdListValue)other).iterator());
	}

	@Override
	public XId get(final int index) {
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
	public XIdListValue remove(final int index) {
		final XId[] newList = createArrayWithEntryRemovedAtPosition(contents(), index);
		return new MemoryIdListValue(newList);
	}

	@Override
	public XIdListValue remove(final XId entry) {
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
	public XId[] toArray() {
		return contents();
	}

	@Override
	public String toString() {
		return Arrays.toString(this.list);
	}

}
