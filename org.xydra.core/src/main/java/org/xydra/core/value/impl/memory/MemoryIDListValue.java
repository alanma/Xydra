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
	
	public XIDListValue add(int index, XID entry) {
		int size = this.list.length;
		if(index < 0 || index > size) {
			throw new IndexOutOfBoundsException();
		}
		MemoryIDListValue v = new MemoryIDListValue(size + 1);
		System.arraycopy(this.list, 0, v.list, 0, index);
		v.list[index] = entry;
		System.arraycopy(this.list, index, v.list, index + 1, size - index);
		return v;
	}
	
	public XIDListValue remove(XID entry) {
		int index = indexOf(entry);
		if(index < 0) {
			return this;
		}
		return remove(index);
	}
	
	public XIDListValue remove(int index) {
		int size = this.list.length;
		if(index < 0 || index >= size) {
			throw new IndexOutOfBoundsException();
		}
		MemoryIDListValue v = new MemoryIDListValue(size - 1);
		System.arraycopy(this.list, 0, v.list, 0, index);
		System.arraycopy(this.list, index + 1, v.list, index, size - index - 1);
		return v;
	}
	
}
