package org.xydra.base.value;

import java.util.Iterator;
import java.util.NoSuchElementException;


/**
 * An iterator for {@link XListValue XListValues}.
 * 
 * @author Kaidel
 * 
 * @param <E> Content Type of the underlying {@link XListValue} type.
 */
public class XListValueIterator<E> implements Iterator<E> {
	
	private final XListValue<E> list;
	private int index = -1;
	private int size;
	
	public XListValueIterator(XListValue<E> list) {
		this.list = list;
		this.size = list.size();
	}
	
	public boolean hasNext() {
		return ((this.index + 1) < this.size);
	}
	
	public E next() {
		if(hasNext()) {
			return this.list.get(++this.index);
		} else {
			throw new NoSuchElementException();
		}
	}
	
	public void remove() {
		throw new UnsupportedOperationException(
		        "XListValueIterators do not support the remove() method");
	}
	
}
