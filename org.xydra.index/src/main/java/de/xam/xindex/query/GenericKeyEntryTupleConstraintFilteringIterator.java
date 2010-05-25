package de.xam.xindex.query;

import java.util.Iterator;

import de.xam.xindex.iterator.AbstractFilteringIterator;


/**
 * A filtering iterator that returns all elements of a base iterator matching a
 * given criteria, expressed as a function.
 * 
 * @author voelkel
 * 
 * @param <T>
 * @param <E>
 */
public class GenericKeyEntryTupleConstraintFilteringIterator<T extends HasEntry<E>, E> extends
        AbstractFilteringIterator<T> {
	
	private Constraint<E> entryConstraint;
	
	public GenericKeyEntryTupleConstraintFilteringIterator(Iterator<T> base,
	        Constraint<E> entryConstraint) {
		super(base);
		this.entryConstraint = entryConstraint;
	}
	
	@Override
	protected boolean matchesFilter(T tuple) {
		return this.entryConstraint.matches(tuple.getEntry());
	}
	
}
