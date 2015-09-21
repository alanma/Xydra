package org.xydra.index;

import java.io.Serializable;
import java.util.Iterator;

import org.xydra.index.iterator.ClosableIterator;
import org.xydra.index.query.Constraint;
import org.xydra.index.query.KeyEntryTuple;

/**
 * Some implementations might used {@link ClosableIterator} as return types to
 * handle concurrency. Remember to close them to release read-locks.
 *
 * Note: All implementations need to be {@link Serializable}.
 *
 * @author voelkel
 *
 *         Multiple entries can be indexed for a certain key-combination.
 *
 * @param <K> key type
 * @param <E> entity type
 */
public interface IMapSetIndex<K, E> extends IIndex, Serializable {

	/**
	 * @param c1
	 * @NeverNull
	 * @return an iterator that ranges over all entries indexed by keys, where
	 *         the keys match c1
	 */
	Iterator<E> constraintIterator(Constraint<K> c1);

	/**
	 * @param c1
	 * @NeverNull
	 * @param entryConstraint
	 * @return true iff this index contains a tuple matching the given
	 *         constraints.
	 */
	boolean contains(Constraint<K> c1, Constraint<E> entryConstraint);

	/**
	 * @param k
	 * @param e
	 * @return true iff the index contains exactly the tuple
	 */
	boolean contains(K k, E e);

	/**
	 * @param key
	 * @NeverNull
	 * @return true if this index contains any tuple (key,*)
	 */
	boolean containsKey(K key);

	/**
	 * Removed a tuple from the index.
	 *
	 * @param key1
	 * @NeverNull
	 * @param entry
	 * @NeverNull
	 * @return true iff set K contained entry
	 */
	boolean deIndex(K key1, E entry);

	/**
	 * De-index all current entries with (key1, *).
	 *
	 * @param key1
	 * @return
	 * @NeverNull
	 */
	boolean deIndex(K key1);

	/**
	 * Add a tuple to the index
	 *
	 * @param key1
	 * @NeverNull
	 * @param entry
	 * @NeverNull
	 * @return true iff set K did not contain entry yet
	 */
	boolean index(K key1, E entry);

	/**
	 * @param c1 constraint on the key @NeverNull
	 * @param entryConstraint constraint on the value @NeverNull
	 * @return an iterator over all result tuples matching the constraints @NeverNull
	 */
	Iterator<KeyEntryTuple<K, E>> tupleIterator(Constraint<K> c1, Constraint<E> entryConstraint);

	/**
	 * @return an iterator over all keys ?x in tuples (?x,?y) without
	 *         duplicates.
	 */
	Iterator<K> keyIterator();


	/**
	 * @param otherFuture the other map index is the future. What is found here
	 *            and not present in this, has been added. @NeverNull
	 * @return an {@link IMapSetDiff}
	 */
	IMapSetDiff<K, E> computeDiff(IMapSetIndex<K, E> otherFuture);

	/**
	 * A diff of two tuple indexes
	 *
	 * @param <K>
	 * @param <E>
	 */
	public static interface IMapSetDiff<K, E> {
		/**
		 * @return all added tuples; writes to this data have no effect.
		 */
		IMapSetIndex<K, E> getAdded();

		/**
		 * @return all removed tuples; writes to this data have no effect.
		 */
		IMapSetIndex<K, E> getRemoved();
	}

	/**
	 * @param key
	 * @NeverNull
	 * @return an {@link IEntrySet} containing all ?y from tuples (key,?y), @CanBeNull
	 *         if no entries
	 */
	IEntrySet<E> lookup(K key);

	String toString(String indent);

}
