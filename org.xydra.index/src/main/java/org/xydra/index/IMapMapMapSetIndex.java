package org.xydra.index;

import java.io.Serializable;
import java.util.Iterator;

import org.xydra.index.query.Constraint;
import org.xydra.index.query.KeyKeyKeyEntryTuple;

/**
 * All implementations must be {@link Serializable}.
 *
 * @author voelkel
 *
 *         Multiple entries can be indexed for a certain key-combination.
 *
 * @param <K> key1 type
 * @param <L> key2 type
 * @param <M> key3 type
 * @param <E> entity type
 */
public interface IMapMapMapSetIndex<K, L, M, E> extends IIndex, Serializable {

	/**
	 * @param c1
	 * @param c2
	 * @param c3
	 * @return
	 */
	Iterator<E> constraintIterator(Constraint<K> c1, Constraint<L> c2, Constraint<M> c3);

	/**
	 * @param key1
	 * @param key2
	 * @param key3
	 * @param entry
	 */
	void deIndex(K key1, L key2, M key3, E entry);

	/**
	 * @param key1
	 * @param key2
	 * @param key3
	 * @param entry
	 */
	void index(K key1, L key2, M key3, E entry);

	/**
	 * @param c1
	 * @param c2
	 * @param c3
	 * @param entryConstraint
	 * @return
	 */
	Iterator<KeyKeyKeyEntryTuple<K, L, M, E>> tupleIterator(Constraint<K> c1, Constraint<L> c2, Constraint<M> c3,
			Constraint<E> entryConstraint);

}
