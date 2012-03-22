package org.xydra.index;

import java.util.Iterator;

import org.xydra.index.query.Constraint;
import org.xydra.index.query.Pair;


/**
 * An index for pairs of keys that supports queries on both keys.
 * 
 * @author dscharrer
 * @param <K>
 * @param <L>
 */
public interface IPairIndex<K, L> extends IIndex, Iterable<Pair<K,L>> {
	
	/**
	 * @param c1 The constraint for the first key.
	 * @param c2 The constraint for the second key.
	 * @return true if there is any pair in the index matching the constraints.
	 */
	boolean contains(Constraint<K> c1, Constraint<L> c2);
	
	/**
	 * Add a new pair (k1,k2) to the index. If the pair was already in the
	 * index, nothing is changed.
	 * 
	 * @param k1
	 * @param k2
	 */
	void index(K k1, L k2);
	
	/**
	 * Remove the pair (k1,k2) from the index.
	 * 
	 * @param k1
	 * @param k2
	 */
	void deIndex(K k1, L k2);
	
	/**
	 * Iterate over all pairs where the keys match the given constraints.
	 * 
	 * Each pair will be returned only once.
	 * 
	 * Indexing new pairs or deIndexing pairs invalidates the iterator.
	 * 
	 * @param c1 The constraint for the first key.
	 * @param c2 The constraint for the second key.
	 * @return An iterator over all matching pairs.
	 */
	Iterator<Pair<K,L>> constraintIterator(Constraint<K> c1, Constraint<L> c2);
	
	/**
	 * @return an iterator over all first keys.
	 */
	Iterator<K> key1Iterator();
	
	/**
	 * @return an iterator over all second keys.
	 */
	Iterator<L> key2Iterator();
	
}
