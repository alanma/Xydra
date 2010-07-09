package org.xydra.core.value.impl.memory;

import java.util.Arrays;
import java.util.Collection;

import org.xydra.core.XX;
import org.xydra.core.value.XStringListValue;


/**
 * An implementation of {@link XStringListValue}
 * 
 * @author Kaidel
 * 
 */
public class MemoryStringListValue extends MemoryListValue<String> implements XStringListValue {
	
	private static final long serialVersionUID = -1175161517909257560L;
	
	private final String[] list;
	
	public MemoryStringListValue(Collection<String> content) {
		this.list = content.toArray(new String[content.size()]);
	}
	
	public MemoryStringListValue(String[] content) {
		this.list = new String[content.length];
		System.arraycopy(content, 0, this.list, 0, content.length);
	}
	
	public String[] contents() {
		String[] array = new String[this.list.length];
		System.arraycopy(this.list, 0, array, 0, this.list.length);
		return array;
	}
	
	public String[] toArray() {
		return contents();
	}
	
	@Override
	public boolean equals(Object other) {
		return other instanceof XStringListValue
		        && XX.equalsIterator(this.iterator(), ((XStringListValue)other).iterator());
	}
	
	@Override
	public int hashCode() {
		return Arrays.hashCode(this.list);
	}
	
	@Override
	public String toString() {
		return Arrays.toString(this.list);
	}
	
	public String get(int index) {
		return this.list[index];
	}
	
	public int size() {
		return this.list.length;
	}
	
}
