package org.xydra.index;

import java.util.Iterator;

import org.xydra.index.query.Constraint;
import org.xydra.index.query.KeyEntryTuple;



/**
 * @author voelkel
 * 
 *         Multiple entries can be indexed for a certain key-combination.
 * 
 * @param <K>
 * @param <E>
 */
public interface IMapIndex<K, E> extends IIndex {
	
	/**
	 * @param name
	 * @return true if the index contains an entry for the key
	 */
	boolean containsKey(K key);
	
	void deIndex(K key1);
	
	void index(K key1, E entry);
	
	/**
	 * @return all entries
	 */
	Iterator<E> iterator();
	
	E lookup(K key);
	
	boolean containsKey(Constraint<K> c1);
	
	Iterator<KeyEntryTuple<K,E>> tupleIterator(Constraint<K> c1);
	
	Iterator<K> keyIterator();
	
}
