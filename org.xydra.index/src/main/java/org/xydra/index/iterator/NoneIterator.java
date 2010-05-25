package org.xydra.index.iterator;

/**
 * Iterator modelling zero results.
 * 
 * @author voelkel
 * 
 *         Ideally this would be a singleton , but that seems impossible with
 *         Generics.
 * 
 * @param <E>
 */
public class NoneIterator<E> implements ClosableIterator<E> {
	
	/**
     * 
     */
    private static final long serialVersionUID = 8727531049551996167L;

	public void close() {
		// NoneIterator needs not to be closed
	}
	
	public boolean hasNext() {
		return false;
	}
	
	public E next() {
		return null;
	}
	
	public void remove() {
		// nothing to do
	}
	
}
