package org.xydra.core.value.impl.memory;

import java.util.Arrays;
import java.util.Collection;

import org.xydra.core.XX;
import org.xydra.core.model.XID;
import org.xydra.core.value.XIDListValue;


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
		        && XX.equalsIterator(this.iterator(), ((XIDListValue)other).iterator());
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
	
}
