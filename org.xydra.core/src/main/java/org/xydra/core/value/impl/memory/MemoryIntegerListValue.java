package org.xydra.core.value.impl.memory;

import java.util.Arrays;
import java.util.Iterator;

import org.xydra.core.value.ArrayIterator;
import org.xydra.core.value.XIDValue;
import org.xydra.core.value.XIntegerListValue;
import org.xydra.core.value.XStringValue;



/**
 * An implementation of {@link XIntegerListValue}
 * 
 * @author Kaidel
 * 
 */

public class MemoryIntegerListValue implements XIntegerListValue {
	
	private static final long serialVersionUID = -2698183990443882191L;
	
	private Integer[] list;
	
	public MemoryIntegerListValue(Integer[] initialContent) {
		this.list = new Integer[initialContent.length];
		System.arraycopy(initialContent, 0, this.list, 0, initialContent.length);
	}
	
	public MemoryIntegerListValue(int[] initialContent) {
		this.list = new Integer[initialContent.length];
		
		for(int i = 0; i < initialContent.length; i++) {
			this.list[i] = initialContent[i];
		}
	}
	
	public Integer[] contents() {
		Integer[] copy = new Integer[this.list.length];
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
		if(object instanceof MemoryIntegerListValue) {
			return Arrays.equals(this.list, ((MemoryIntegerListValue)object).list);
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
		
		for(Integer i : this.list) {
			result += i.hashCode();
		}
		
		return result;
	}
	
	public boolean contains(Object elem) {
		if(elem instanceof Integer) {
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
		if(elem instanceof Integer) {
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
		if(elem instanceof Integer) {
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
	
	public Integer get(int index) {
		return this.list[index];
	}
	
	public Iterator<Integer> iterator() {
		return new ArrayIterator<Integer>(this.list);
	}
	
	@Override
	public String toString() {
		return Arrays.toString(this.list);
	}
	
}
