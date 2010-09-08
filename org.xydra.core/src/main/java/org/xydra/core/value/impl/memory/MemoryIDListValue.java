package org.xydra.core.value.impl.memory;

import java.util.Arrays;
import java.util.Collection;

import org.xydra.core.model.XID;
import org.xydra.core.value.XIDListValue;
import org.xydra.index.XI;


/**
 * An implementation of {@link XIDListValue}
 * 
 * @author Kaidel
 * @author voelkel
 * 
 */
public class MemoryIDListValue extends MemoryListValue<XID> implements XIDListValue {
	
	private static final long serialVersionUID = -7641986388917629097L;
	
	private final XID[] list;
	
	public MemoryIDListValue(Collection<XID> content) {
		this.list = content.toArray(new XID[content.size()]);
	}
	
	public MemoryIDListValue(XID[] content) {
		this.list = new XID[content.length];
		System.arraycopy(content, 0, this.list, 0, content.length);
	}
	
	@SuppressWarnings("unused")
	private MemoryIDListValue(int length) {
		this.list = new XID[length];
	}
	
	public XID[] contents() {
		XID[] array = new XID[this.list.length];
		System.arraycopy(this.list, 0, array, 0, this.list.length);
		return array;
	}
	
	public XID[] toArray() {
		return contents();
	}
	
	@Override
	public boolean equals(Object other) {
		return other instanceof XIDListValue
		        && XI.equalsIterator(this.iterator(), ((XIDListValue)other).iterator());
	}
	
	@Override
	public int hashCode() {
		return Arrays.hashCode(this.list);
	}
	
	@Override
	public String toString() {
		return Arrays.toString(this.list);
	}
	
	public XID get(int index) {
		return this.list[index];
	}
	
	public int size() {
		return this.list.length;
	}
	
	public XIDListValue add(XID entry) {
		return add(this.list.length, entry);
	}
	
	public static final XID[] createArrayWithEntryInsertedAtPosition(XID[] array, int position,
	        XID entry) {
		int size = array.length;
		if(position < 0 || position > size) {
			throw new IndexOutOfBoundsException();
		}
		XID[] newList = new XID[size + 1];
		System.arraycopy(array, 0, newList, 0, position);
		newList[position] = entry;
		System.arraycopy(array, position, newList, position + 1, size - position);
		return newList;
	}
	
	public XIDListValue add(int index, XID entry) {
		XID[] newList = createArrayWithEntryInsertedAtPosition(this.list, index, entry);
		return new MemoryIDListValue(newList);
	}
	
	public XIDListValue remove(XID entry) {
		int index = indexOf(entry);
		if(index < 0) {
			return this;
		}
		return remove(index);
	}
	
	public XIDListValue remove(int index) {
		XID[] newList = createArrayWithEntryRemovedAtPosition(this.contents(), index);
		return new MemoryIDListValue(newList);
	}
	
	public static final XID[] createArrayWithEntryRemovedAtPosition(XID[] array, int position) {
		int size = array.length;
		if(position < 0 || position >= size) {
			throw new IndexOutOfBoundsException();
		}
		XID[] newList = new XID[size - 1];
		System.arraycopy(array, 0, newList, 0, position);
		System.arraycopy(array, position + 1, newList, position, size - position - 1);
		return newList;
	}
	
}
