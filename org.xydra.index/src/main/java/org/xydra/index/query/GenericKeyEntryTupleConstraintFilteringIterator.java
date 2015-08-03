package org.xydra.index.query;

import java.util.Iterator;

import org.xydra.index.iterator.AbstractFilteringIterator;

/**
 * A filtering iterator that returns all elements of a base iterator matching a
 * given criteria, expressed as a function.
 *
 * @author voelkel
 *
 * @param <T>
 *            tuple type
 * @param <E>
 *            entity type
 */
public class GenericKeyEntryTupleConstraintFilteringIterator<T extends HasEntry<E>, E> extends
		AbstractFilteringIterator<T> {

	private final Constraint<E> entryConstraint;

	public GenericKeyEntryTupleConstraintFilteringIterator(final Iterator<T> base,
			final Constraint<E> entryConstraint) {
		super(base);
		this.entryConstraint = entryConstraint;
	}

	@Override
	protected boolean matchesFilter(final T tuple) {
		return this.entryConstraint.matches(tuple.getEntry());
	}

}
