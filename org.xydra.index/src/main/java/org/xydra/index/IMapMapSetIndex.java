package org.xydra.index;

import java.util.Iterator;

import org.xydra.index.query.Constraint;
import org.xydra.index.query.EqualsConstraint;
import org.xydra.index.query.ITriple;
import org.xydra.index.query.KeyKeyEntryTuple;
import org.xydra.index.query.Wildcard;

/**
 * @author voelkel
 * 
 *         Multiple entries can be indexed for a certain key-combination.
 * 
 * @param <K>
 *            key type
 * @param <L>
 *            key type
 * @param <E>
 *            entity type
 */
public interface IMapMapSetIndex<K, L, E> extends IIndex {

	Iterator<E> constraintIterator(Constraint<K> c1, Constraint<L> c2);

	/**
	 * @param c1
	 * @param c2
	 * @param entryConstraint
	 * @return true if this index contains an entry matching the query pattern
	 */
	boolean contains(Constraint<K> c1, Constraint<L> c2, Constraint<E> entryConstraint);

	/**
	 * @param c1
	 *            if null == {@link Wildcard}; else {@link EqualsConstraint}
	 * @param c2
	 *            if null == {@link Wildcard}; else {@link EqualsConstraint}
	 * @param entryConstraint
	 *            if null == {@link Wildcard}; else {@link EqualsConstraint}
	 * @return true if this index contains an entry matching the query pattern
	 */
	boolean contains(K c1, L c2, E entryConstraint);

	/**
	 * @param key1
	 * @param key2
	 * @param entry
	 * @return true iff entry was in the set (K,L)
	 */
	boolean deIndex(K key1, L key2, E entry);

	/**
	 * @param key1
	 * @param key2
	 * @param entry
	 * @return true iff entry was not yet in the set (K,L)
	 */
	boolean index(K key1, L key2, E entry);

	/**
	 * @return an iterator over all entries
	 */
	Iterator<E> iterator();

	/**
	 * @param c1
	 *            @NeverNull
	 * @param c2
	 *            @NeverNull
	 * @param entryConstraint
	 *            @NeverNull
	 * @return an iterator over all matching {@link KeyKeyEntryTuple}
	 */
	Iterator<ITriple<K, L, E>> tupleIterator(Constraint<K> c1, Constraint<L> c2,
			Constraint<E> entryConstraint);

	/**
	 * @param c1
	 *            if null == {@link Wildcard}; else {@link EqualsConstraint}
	 * @param c2
	 *            if null == {@link Wildcard}; else {@link EqualsConstraint}
	 * @param entryConstraint
	 *            if null == {@link Wildcard}; else {@link EqualsConstraint}
	 * @return an iterator over all matching {@link KeyKeyEntryTuple}
	 */
	Iterator<ITriple<K, L, E>> tupleIterator(K c1, L c2, E entryConstraint);

	/**
	 * @param otherFuture
	 *            the other is the future: What is present here but not in this,
	 *            is added. Otherwise it is considered removed.
	 * @return an {@link IMapMapSetDiff}
	 */
	IMapMapSetDiff<K, L, E> computeDiff(IMapMapSetIndex<K, L, E> otherFuture);

	public static interface IMapMapSetDiff<K, L, E> {
		IMapMapSetIndex<K, L, E> getAdded();

		IMapMapSetIndex<K, L, E> getRemoved();
	}

	Iterator<K> keyIterator();

}
