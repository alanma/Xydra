package org.xydra.index.iterator;

/**
 * Iterator modelling zero results.
 * 
 * @author voelkel
 * 
 *         Ideally this would be a singleton , but that seems impossible with
 *         Generics.
 * 
 * @param <E> entity type
 */
public class NoneIterator<E> implements ClosableIterator<E> {
	
	private static final long serialVersionUID = 8727531049551996167L;
	
	@Override
    public void close() {
		// NoneIterator needs not to be closed
	}
	
	@Override
    public boolean hasNext() {
		return false;
	}
	
	@Override
    public E next() {
		return null;
	}
	
	@Override
    public void remove() {
		// nothing to do
	}
	
}
