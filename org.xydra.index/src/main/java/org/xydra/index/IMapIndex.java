package org.xydra.index;

import java.util.Iterator;

import org.xydra.index.query.Constraint;
import org.xydra.index.query.KeyEntryTuple;

/**
 * Similar to a normal {@link java.util.Map}, just using Xydra index method conventions.
 *
 * @author voelkel
 * @param <K>
 *            key type
 * @param <E>
 *            entity type
 */
public interface IMapIndex<K, E> extends IIndex, Iterable<E> {

	/**
	 * @param key
	 * @param name
	 * @return true if the index contains an entry for the key
	 */
	boolean containsKey(K key);

	/**
	 * @param key
	 */
	void deIndex(K key);

	/**
	 * @param key
	 * @param entry
	 */
	void index(K key, E entry);

	/**
	 * @return all entries
	 */
	@Override
	Iterator<E> iterator();

	/**
	 * @param key
	 *            Depending on implementation, a null-key might be permitted and
	 *            usually maps to null.
	 * @return the value stored for the given key or null
	 */
	E lookup(K key);

	/**
	 * @param c1
	 * @return true iff this index contains at least one key matching the
	 *         constraint
	 */
	boolean containsKey(Constraint<K> c1);

	/**
	 * @param keyConstraint
	 * @return all tuples matching the key-constraint
	 */
	Iterator<KeyEntryTuple<K, E>> tupleIterator(Constraint<K> keyConstraint);

	/**
	 * @return
	 */
	Iterator<KeyEntryTuple<K, E>> tupleIterator();

	/**
	 * @return an iterator over all keys
	 */
	Iterator<K> keyIterator();


}
