package org.xydra.core.value;

import java.util.Iterator;
import java.util.NoSuchElementException;


/**
 * We've got to write our own ArrayIterator, since there is no standard iterator
 * for simple arrays
 * 
 * @author Kaidel
 * 
 * @param <E> Content Type of the underlying array
 * 
 *            TODO consider using <code>Arrays.asList(array).iterator()</code>
 *            instead ~~max
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
		throw new UnsupportedOperationException();
	}
	
}
