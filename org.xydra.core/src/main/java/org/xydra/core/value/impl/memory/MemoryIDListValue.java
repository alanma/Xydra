package org.xydra.core.value.impl.memory;

import java.util.Arrays;
import java.util.Iterator;

import org.xydra.core.model.XID;
import org.xydra.core.value.ArrayIterator;
import org.xydra.core.value.XIDListValue;
import org.xydra.core.value.XIDValue;
import org.xydra.core.value.XStringValue;



/**
 * An implementation of {@link XIDListValue}
 * 
 * @author Kaidel
 * @author voelkel
 * 
 */

public class MemoryIDListValue implements XIDListValue {
	
	private static final long serialVersionUID = -7641986388917629097L;
	
	private XID[] list;
	
	public MemoryIDListValue(XID[] initialContent) {
		this.list = new XID[initialContent.length];
		System.arraycopy(initialContent, 0, this.list, 0, initialContent.length);
	}
	
	public XID[] contents() {
		XID[] copy = new XID[this.list.length];
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
		if(object instanceof MemoryIDListValue) {
			return Arrays.equals(this.list, ((MemoryIDListValue)object).list);
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
		
		for(XID xid : this.list) {
			result += xid.hashCode();
		}
		
		return result;
	}
	
	public boolean contains(Object elem) {
		if(elem instanceof XID) {
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
		if(elem instanceof XID) {
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
		if(elem instanceof XID) {
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
	
	public XID get(int index) {
		return this.list[index];
	}
	
	public Iterator<XID> iterator() {
		return new ArrayIterator<XID>(this.list);
	}
	
	@Override
	public String toString() {
		return Arrays.toString(this.list);
	}
	
	// public boolean add(XID arg0) {
	// return this.list.add(arg0);
	// }
	//
	// public void add(int arg0, XID arg1) {
	// this.list.add(arg0, arg1);
	// }
	//
	// public boolean addAll(Collection<? extends XID> arg0) {
	// return this.list.addAll(arg0);
	// }
	//
	// public boolean addAll(int arg0, Collection<? extends XID> arg1) {
	// return this.list.addAll(arg0, arg1);
	// }
	//
	// public void clear() {
	// this.list.clear();
	// }
	//
	// public boolean contains(Object arg0) {
	// return this.list.contains(arg0);
	// }
	//
	// public boolean containsAll(Collection<?> arg0) {
	// return this.list.containsAll(arg0);
	// }
	//
	// public XID get(int arg0) {
	// return this.list.get(arg0);
	// }
	//
	// public int indexOf(Object arg0) {
	// return this.list.indexOf(arg0);
	// }
	//
	// public boolean isEmpty() {
	// return this.list.isEmpty();
	// }
	//
	// public Iterator<XID> iterator() {
	// return this.list.iterator();
	// }
	//
	// public int lastIndexOf(Object arg0) {
	// return this.list.lastIndexOf(arg0);
	// }
	//
	// public ListIterator<XID> listIterator() {
	// return this.list.listIterator();
	// }
	//
	// public ListIterator<XID> listIterator(int arg0) {
	// return this.list.listIterator(arg0);
	// }
	//
	// public boolean remove(Object arg0) {
	// return this.list.remove(arg0);
	// }
	//
	// public XID remove(int arg0) {
	// return this.list.remove(arg0);
	// }
	//
	// public boolean removeAll(Collection<?> arg0) {
	// return this.list.removeAll(arg0);
	// }
	//
	// public boolean retainAll(Collection<?> arg0) {
	// return this.list.retainAll(arg0);
	// }
	//
	// public XID set(int arg0, XID arg1) {
	// return this.list.set(arg0, arg1);
	// }
	//
	// public int size() {
	// return this.list.size();
	// }
	//
	// public List<XID> subList(int arg0, int arg1) {
	// return this.list.subList(arg0, arg1);
	// }
	//
	// public Object[] toArray() {
	// return this.list.toArray();
	// }
	//
	// public <T> T[] toArray(T[] arg0) {
	// return this.list.toArray(arg0);
	// }
	//
	// public List<XID> getList() {
	// return this.list;
	// }
	
}
