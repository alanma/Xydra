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
 *            entity type
 */
public class NoneIterator<E> implements ClosableIterator<E> {

	public static final NoneIterator<Object> INSTANCE = new NoneIterator<Object>();

	@SuppressWarnings("unchecked")
	public static <E> NoneIterator<E> create() {
		return (NoneIterator<E>) INSTANCE;
	}

	/**
	 * Use {@link #create()} to avoid excessive object creation
	 */
	private NoneIterator() {
	}

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
