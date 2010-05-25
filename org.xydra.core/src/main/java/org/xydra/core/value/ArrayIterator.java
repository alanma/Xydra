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

public class ArrayIterator<E> implements Iterator<E> {
	private E[] array;
	private int index = -1;
	
	public ArrayIterator(E[] array) {
		this.array = array;
	}
	
	public boolean hasNext() {
		return ((this.index + 1) < this.array.length);
	}
	
	public E next() {
		if(hasNext()) {
			return this.array[++this.index];
		} else {
			throw new NoSuchElementException();
		}
	}
	
	public void remove() {
		throw new UnsupportedOperationException();
	}
}
