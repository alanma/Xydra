package org.xydra.core.value.impl.memory;

import java.util.Arrays;
import java.util.Iterator;

import org.xydra.core.value.ArrayIterator;
import org.xydra.core.value.XBooleanListValue;
import org.xydra.core.value.XIDValue;
import org.xydra.core.value.XStringValue;



/**
 * An implementation of {@link XBooleanListValue}
 * 
 * @author Kaidel
 * 
 */

public class MemoryBooleanListValue implements XBooleanListValue {
	
	private static final long serialVersionUID = -2012063819510070665L;
	
	private Boolean[] list;
	
	public MemoryBooleanListValue(Boolean[] initialContent) {
		this.list = new Boolean[initialContent.length];
		System.arraycopy(initialContent, 0, this.list, 0, initialContent.length);
	}
	
	public MemoryBooleanListValue(boolean[] initialContent) {
		this.list = new Boolean[initialContent.length];
		
		for(int i = 0; i < initialContent.length; i++) {
			this.list[i] = initialContent[i];
		}
	}
	
	public Boolean[] contents() {
		Boolean[] copy = new Boolean[this.list.length];
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
	public int hashCode() {
		int result = 0;
		
		if(this.list == null) {
			return 0;
		}
		
		for(Boolean b : this.list) {
			result += b.hashCode();
		}
		
		return result;
	}
	
	@Override
	public boolean equals(Object object) {
		if(object instanceof MemoryBooleanListValue) {
			return Arrays.equals(this.list, ((MemoryBooleanListValue)object).list);
		} else {
			return false;
		}
	}
	
	public boolean contains(Object elem) {
		if(elem instanceof Boolean) {
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
		if(elem instanceof Boolean) {
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
		if(elem instanceof Boolean) {
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
	
	public Boolean get(int index) {
		return this.list[index];
	}
	
	public Iterator<Boolean> iterator() {
		return new ArrayIterator<Boolean>(this.list);
	}
	
	@Override
	public String toString() {
		return Arrays.toString(this.list);
	}
	
}
