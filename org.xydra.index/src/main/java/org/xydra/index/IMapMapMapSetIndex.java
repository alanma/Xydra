package org.xydra.index;

import java.util.Iterator;

import org.xydra.index.query.Constraint;
import org.xydra.index.query.KeyKeyKeyEntryTuple;



/**
 * @author voelkel
 * 
 *         Multiple entries can be indexed for a certain key-combination.
 * 
 * @param <K>
 * @param <E>
 */
public interface IMapMapMapSetIndex<K, L, M, E> extends IIndex {
	
	Iterator<E> constraintIterator(Constraint<K> c1, Constraint<L> c2, Constraint<M> c3);
	
	void deIndex(K key1, L key2, M key3, E entry);
	
	void index(K key1, L key2, M key3, E entry);
	
	Iterator<KeyKeyKeyEntryTuple<K,L,M,E>> tupleIterator(Constraint<K> c1, Constraint<L> c2,
	        Constraint<M> c3, Constraint<E> entryConstraint);
	
}
