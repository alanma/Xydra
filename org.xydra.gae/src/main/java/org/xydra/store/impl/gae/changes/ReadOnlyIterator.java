package org.xydra.store.impl.gae.changes;

import java.util.Iterator;

/**
 * Read only iterator wrapper that forbids {@link #remove()}.
 * 
 * TODO move to xydra.index
 * 
 * @author dscharrer
 * 
 * @param <T> The type being iterated.
 */
public class ReadOnlyIterator<T> implements Iterator<T> {
	
	private final Iterator<T> base;
	
	public ReadOnlyIterator(Iterator<T> base) {
		this.base = base;
	}
	
	/**
	 * Convenience constructor.
	 */
	public ReadOnlyIterator(Iterable<T> base) {
		this(base.iterator());
	}
	
	@Override
	public boolean hasNext() {
		return this.base.hasNext();
	}
	
	@Override
	public T next() {
		return this.base.next();
	}
	
	@Override
	public void remove() {
		throw new UnsupportedOperationException("read only iterator");
	}
	
}