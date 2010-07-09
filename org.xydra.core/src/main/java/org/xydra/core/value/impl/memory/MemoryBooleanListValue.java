package org.xydra.core.value.impl.memory;

import java.util.Arrays;
import java.util.Collection;

import org.xydra.core.XX;
import org.xydra.core.value.XBooleanListValue;


/**
 * An implementation of {@link XBooleanListValue}
 * 
 * @author Kaidel
 * @author dscharrer
 * 
 */
public class MemoryBooleanListValue extends MemoryListValue<Boolean> implements XBooleanListValue {
	
	private static final long serialVersionUID = -2012063819510070665L;
	
	private final boolean[] list;
	
	public MemoryBooleanListValue(Collection<Boolean> content) {
		this.list = new boolean[content.size()];
		int i = 0;
		for(boolean b : content) {
			this.list[i++] = b;
		}
	}
	
	public MemoryBooleanListValue(boolean[] content) {
		this.list = new boolean[content.length];
		System.arraycopy(content, 0, this.list, 0, content.length);
	}
	
	public boolean[] contents() {
		boolean[] array = new boolean[this.list.length];
		System.arraycopy(this.list, 0, array, 0, this.list.length);
		return array;
	}
	
	public Boolean[] toArray() {
		Boolean[] array = new Boolean[this.list.length];
		fillArray(array);
		return array;
	}
	
	@Override
	public boolean equals(Object other) {
		return other instanceof XBooleanListValue
		        && XX.equalsIterator(this.iterator(), ((XBooleanListValue)other).iterator());
	}
	
	@Override
	public int hashCode() {
		return Arrays.hashCode(this.list);
	}
	
	@Override
	public String toString() {
		return Arrays.toString(this.list);
	}
	
	public Boolean get(int index) {
		return this.list[index];
	}
	
	public int size() {
		return this.list.length;
	}
	
}
