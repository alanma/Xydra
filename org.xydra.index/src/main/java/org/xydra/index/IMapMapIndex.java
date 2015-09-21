package org.xydra.index;

import java.util.Iterator;

import org.xydra.index.iterator.Iterators;
import org.xydra.index.query.Constraint;
import org.xydra.index.query.KeyEntryTuple;
import org.xydra.index.query.KeyKeyEntryTuple;

/**
 * A map with double keys.
 *
 * Exactly one entry can be indexed for a certain key-combination.
 *
 * @param <K>
 *            key type
 * @param <L>
 *            key2 type
 * @param <E>
 *            entity type
 *
 * @author dscharrer
 */
public interface IMapMapIndex<K,L,E> extends IIndex {

	/**
	 * @param key1
	 *            Depending on implementation, a null-key might be permitted and
	 *            usually maps to null.
	 * @param key2
	 *            Depending on implementation, a null-key might be permitted and
	 *            usually maps to null.
	 * @return the value stored for the given keys or null
	 */
	E lookup(K key1, L key2);

	/**
	 * @param c1
	 * @param c2
	 * @return true if there is a value matching the constraints for the keys
	 */
	boolean containsKey(Constraint<K> c1, Constraint<L> c2);

	/**
	 * @param key1
	 * @param key2
	 */
	void deIndex(K key1, L key2);

	/**
	 * @param key1
	 * @param key2
	 * @param entry
	 */
	void index(K key1, L key2, E entry);

	/**
	 * @param c1
	 * @param c2
	 * @return all tuples matching the constraints
	 */
	Iterator<KeyKeyEntryTuple<K, L, E>> tupleIterator(Constraint<K> c1, Constraint<L> c2);

	/**
	 * @return an iterator over all keys in the first position of the tuples.
	 *         Usually duplciate-free,
	 */
	Iterator<K> key1Iterator();

	/**
	 * @return an iterator over all keys in the second position of the tuples
	 *         (can contain duplicates)
	 *
	 *         Use {@link Iterators#distinct(Iterator)} for distinct values.
	 */
	Iterator<L> key2Iterator();

	/**
	 * @return
	 */
	Iterator<KeyEntryTuple<K, L>> keyKeyIterator();

	/**
	 * @param c1
	 * @param c2
	 * @return
	 */
	Iterator<KeyEntryTuple<K, L>> keyKeyIterator(Constraint<K> c1, Constraint<L> c2);

}
