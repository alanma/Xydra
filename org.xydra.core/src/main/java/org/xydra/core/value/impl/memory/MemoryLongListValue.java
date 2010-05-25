package org.xydra.core.value.impl.memory;

import java.util.Arrays;
import java.util.Iterator;

import org.xydra.core.value.ArrayIterator;
import org.xydra.core.value.XIDValue;
import org.xydra.core.value.XLongListValue;
import org.xydra.core.value.XStringValue;



/**
 * An implementation of {@link XLongListValue}
 * 
 * @author Kaidel
 * 
 */

public class MemoryLongListValue implements XLongListValue {
	
	private static final long serialVersionUID = -3294191125211048647L;
	
	private Long[] list;
	
	public MemoryLongListValue(Long[] initialContent) {
		this.list = new Long[initialContent.length];
		System.arraycopy(initialContent, 0, this.list, 0, initialContent.length);
	}
	
	public MemoryLongListValue(long[] initialContent) {
		this.list = new Long[initialContent.length];
		
		for(int i = 0; i < initialContent.length; i++) {
			this.list[i] = initialContent[i];
		}
	}
	
	public Long[] contents() {
		Long[] copy = new Long[this.list.length];
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
		if(object instanceof MemoryLongListValue) {
			return Arrays.equals(this.list, ((MemoryLongListValue)object).list);
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
		
		for(Long l : this.list) {
			result += l.hashCode();
		}
		
		return result;
	}
	
	public boolean contains(Object elem) {
		if(elem instanceof Long) {
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
		if(elem instanceof Long) {
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
		if(elem instanceof Long) {
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
	
	public Long get(int index) {
		return this.list[index];
	}
	
	public Iterator<Long> iterator() {
		return new ArrayIterator<Long>(this.list);
	}
	
	@Override
	public String toString() {
		return Arrays.toString(this.list);
	}
	
}
