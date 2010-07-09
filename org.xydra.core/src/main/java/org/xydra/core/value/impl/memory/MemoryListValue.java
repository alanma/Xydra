package org.xydra.core.value.impl.memory;

import java.util.Iterator;

import org.xydra.core.XX;
import org.xydra.core.value.XListValue;
import org.xydra.core.value.XListValueIterator;


/**
 * A generic implementation for most parts of a {@link XListValue}.
 * 
 * @author dscharrer
 * 
 * @param <E>
 */
public abstract class MemoryListValue<E> implements XListValue<E> {
	
	private static final long serialVersionUID = 7285839520276137162L;
	
	public int indexOf(Object elem) {
		int s = size();
		for(int i = 0; i < s; i++) {
			if(XX.equals(get(i), elem)) {
				return i;
			}
		}
		return -1;
	}
	
	public int lastIndexOf(Object elem) {
		for(int i = size(); i >= 0; i--) {
			if(XX.equals(get(i), elem)) {
				return i;
			}
		}
		return -1;
	}
	
	public boolean contains(E elem) {
		int s = size();
		for(int i = 0; i < s; i++) {
			if(XX.equals(get(i), elem)) {
				return true;
			}
		}
		return false;
	}
	
	public boolean isEmpty() {
		return (size() == 0);
	}
	
	protected void fillArray(E[] array) {
		assert array.length == size();
		int i = 0;
		for(E e : this) {
			array[i++] = e;
		}
	}
	
	public Iterator<E> iterator() {
		return new XListValueIterator<E>(this);
	}
	
}
