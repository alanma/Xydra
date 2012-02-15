package org.xydra.valueindex;

import java.util.Iterator;


// TODO write docu
public class SimpleArrayIterator<E> implements Iterator<E> {
	private E[] array;
	private int index;
	
	public SimpleArrayIterator(E[] array) {
		this.array = array;
	}
	
	@Override
	public boolean hasNext() {
		return this.index < this.array.length;
	}
	
	@Override
	public E next() {
		E element = this.array[this.index];
		this.index++;
		return element;
	}
	
	@Override
	public void remove() {
		throw new UnsupportedOperationException("remove() is not supported");
	}
	
}
