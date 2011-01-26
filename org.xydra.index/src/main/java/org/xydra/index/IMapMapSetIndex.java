package org.xydra.index;

import java.util.Iterator;

import org.xydra.index.query.Constraint;
import org.xydra.index.query.KeyKeyEntryTuple;


/**
 * @author voelkel
 * 
 *         Multiple entries can be indexed for a certain key-combination.
 * 
 * @param <K> key type
 * @param <E> entity type
 */
public interface IMapMapSetIndex<K, L, E> extends IIndex {
	
	Iterator<E> constraintIterator(Constraint<K> c1, Constraint<L> c2);
	
	boolean contains(Constraint<K> c1, Constraint<L> c2, Constraint<E> entryConstraint);
	
	void deIndex(K key1, L key2, E entry);
	
	void index(K key1, L key2, E entry);
	
	/**
	 * @return an iterator over all entries
	 */
	Iterator<E> iterator();
	
	Iterator<KeyKeyEntryTuple<K,L,E>> tupleIterator(Constraint<K> c1, Constraint<L> c2,
	        Constraint<E> entryConstraint);
	
	/**
	 * @param otherFuture the other is the future: What is present here but not
	 *            in this, is added. Otherwise it is considered removed.
	 * @return an {@link IMapMapSetDiff}
	 */
	IMapMapSetDiff<K,L,E> computeDiff(IMapMapSetIndex<K,L,E> otherFuture);
	
	public static interface IMapMapSetDiff<K, L, E> {
		IMapMapSetIndex<K,L,E> getAdded();
		
		IMapMapSetIndex<K,L,E> getRemoved();
	}
	
}
