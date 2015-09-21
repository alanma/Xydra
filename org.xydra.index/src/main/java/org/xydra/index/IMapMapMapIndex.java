package org.xydra.index;

import java.util.Iterator;

import org.xydra.index.query.Constraint;
import org.xydra.index.query.KeyKeyEntryTuple;
import org.xydra.index.query.KeyKeyKeyEntryTuple;

/**
 * A map with triple keys.
 *
 * Exactly one entry can be indexed for a certain key-combination.
 *
 * @param <K>
 *            key type
 * @param <L>
 *            key2 type
 * @param <M>
 *            key3 type
 * @param <E>
 *            entity type
 * @author dscharrer
 */
public interface IMapMapMapIndex<K, L, M, E> extends IIndex {

	/**
	 * @param key1
	 * @param key2
	 * @param key3
	 * @return
	 */
	E lookup(K key1, L key2, M key3);

	/**
	 * @param c1
	 * @param c2
	 * @param c3
	 * @return
	 */
	boolean containsKey(Constraint<K> c1, Constraint<L> c2, Constraint<M> c3);

	/**
	 * @param key1
	 * @param key2
	 * @param key3
	 */
	void deIndex(K key1, L key2, M key3);

	/**
	 * Indexes a entry for a key tuple. If there already is an entry for that
	 * key tuple it will be replace.
	 *
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
	 * @return
	 */
	Iterator<KeyKeyKeyEntryTuple<K, L, M, E>> tupleIterator(Constraint<K> c1, Constraint<L> c2,
			Constraint<M> c3);

	/**
	 * @return
	 */
	Iterator<K> key1Iterator();

	/**
	 * @return
	 */
	Iterator<L> key2Iterator();

	/**
	 * @return
	 */
	Iterator<M> key3Iterator();

	/**
	 * @param c1
	 * @param c2
	 * @param c3
	 * @return tuples of matching keys only.
	 */
	Iterator<KeyKeyEntryTuple<K, L, M>> keyKeyKeyIterator(Constraint<K> c1, Constraint<L> c2, Constraint<M> c3);

	void dump();

}
