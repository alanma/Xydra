package org.xydra.core.value.impl.memory;

import java.util.Arrays;
import java.util.Iterator;

import org.xydra.core.value.ArrayIterator;
import org.xydra.core.value.XIDValue;
import org.xydra.core.value.XStringListValue;
import org.xydra.core.value.XStringValue;



/**
 * An implementation of {@link XStringListValue}
 * 
 * @author Kaidel
 * 
 */

public class MemoryStringListValue implements XStringListValue {
	
	private static final long serialVersionUID = -1175161517909257560L;
	
	private String[] list;
	
	public MemoryStringListValue(String[] initialContent) {
		this.list = new String[initialContent.length];
		System.arraycopy(initialContent, 0, this.list, 0, initialContent.length);
	}
	
	public String[] contents() {
		String[] copy = new String[this.list.length];
		System.arraycopy(this.list, 0, copy, 0, this.list.length);
		return copy;
	}
	
	public XIDValue asIDValue() {
		return null;
	}
	
	public XStringValue asStringValue() {
		return null;
	}
	
	@Override
	public boolean equals(Object object) {
		if(object instanceof MemoryStringListValue) {
			return Arrays.equals(this.list, ((MemoryStringListValue)object).list);
		} else {
			return false;
		}
	}
	
	@Override
	public int hashCode() {
		int result = 0;
		
		if(this.list == null) {
			return 0;
		}
		
		for(String s : this.list) {
			result += s.hashCode();
		}
		
		return result;
	}
	
	public boolean contains(Object elem) {
		if(elem instanceof String) {
			for(int i = 0; i < this.list.length; i++) {
				if(this.list[i].equals(elem)) {
					return true;
				}
			}
			
			// no element equals the given element
			return false;
		} else {
			return false;
		}
	}
	
	public int indexOf(Object elem) {
		if(elem instanceof String) {
			for(int i = 0; i < this.list.length; i++) {
				if(this.list[i].equals(elem)) {
					return i;
				}
			}
			
			// no element equals the given element
			return -1;
		} else {
			return -1;
		}
	}
	
	public boolean isEmpty() {
		return this.list.length == 0;
	}
	
	public int lastIndexOf(Object elem) {
		if(elem instanceof String) {
			for(int i = this.list.length - 1; i >= 0; i--) {
				if(this.list[i].equals(elem)) {
					return i;
				}
			}
			
			// no element equals the given element
			return -1;
		} else {
			return -1;
		}
	}
	
	public int size() {
		return this.list.length;
	}
	
	public String get(int index) {
		return this.list[index];
	}
	
	public Iterator<String> iterator() {
		return new ArrayIterator<String>(this.list);
	}
	
	@Override
	public String toString() {
		return Arrays.toString(this.list);
	}
	
}
