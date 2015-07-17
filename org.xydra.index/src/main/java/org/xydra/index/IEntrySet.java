package org.xydra.index;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Set;

import org.xydra.index.query.Constraint;

/**
 * A set in Xydra Index API shape.
 *
 * All implementations must be {@link Serializable}.
 *
 * @author voelkel
 * @param <E> entity type
 */
public interface IEntrySet<E> extends IIndex, Iterable<E>, Serializable {

	@Override
	public Iterator<E> iterator();

	/**
	 * @param entry
	 * @return true if entry was in the set
	 */
	boolean deIndex(E entry);

	/**
	 * @param entry
	 * @return true if entry was not in the set yet
	 */
	boolean index(E entry);

	/**
	 * @param other
	 * @return the diff (added, removed) of this set with another set
	 */
	IEntrySetDiff<E> computeDiff(IEntrySet<E> other);

	/**
	 * @author xamde
	 *
	 * @param <E>
	 */
	static interface IEntrySetDiff<E> {
		IEntrySet<E> getAdded();

		IEntrySet<E> getRemoved();
	}

	/**
	 * @param entry
	 * @return true iff the set contains the entry
	 */
	boolean contains(E entry);

	/**
	 * @param entryConstraint
	 * @return all elements of the set matching the given entryConstraint
	 */
	Iterator<E> constraintIterator(Constraint<E> entryConstraint);

	/**
	 * @return the number of elements in the set
	 */
	int size();

	/**
	 * @return a read-only {@link java.util.Set}
	 */
	Set<E> toSet();

}
