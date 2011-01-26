package org.xydra.index;

import java.util.Iterator;

import org.xydra.index.query.Constraint;
import org.xydra.index.query.KeyKeyEntryTuple;


/**
 * A map with double keys.
 * 
 * Exactly one entry can be indexed for a certain key-combination.
 * 
 * @param <K> key type
 * @param <E> entity type
 * 
 * @author dscharrer
 */
public interface IMapMapIndex<K, L, E> extends IIndex {
	
	E lookup(K key1, L key2);
	
	boolean containsKey(Constraint<K> c1, Constraint<L> c2);
	
	void deIndex(K key1, L key2);
	
	void index(K key1, L key2, E entry);
	
	Iterator<KeyKeyEntryTuple<K,L,E>> tupleIterator(Constraint<K> c1, Constraint<L> c2);
	
	Iterator<K> key1Iterator();
	
	Iterator<L> key2Iterator();
	
}
