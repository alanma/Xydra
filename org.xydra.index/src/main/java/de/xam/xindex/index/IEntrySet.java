package de.xam.xindex.index;

import java.util.Iterator;

import de.xam.xindex.query.Constraint;


/**
 * @author voelkel
 * 
 *         Multiple entries can be indexed for a certain key-combination.
 * 
 * @param <E>
 */
public interface IEntrySet<E> extends IIndex, Iterable<E> {
	
	public Iterator<E> iterator();
	
	void deIndex(E entry);
	
	void index(E entry);
	
	IEntrySetDiff<E> computeDiff(IEntrySet<E> other);
	
	static interface IEntrySetDiff<E> {
		IEntrySet<E> getAdded();
		
		IEntrySet<E> getRemoved();
	}
	
	boolean contains(E entry);
	
	Iterator<E> constraintIterator(Constraint<E> entryConstraint);
	
}
