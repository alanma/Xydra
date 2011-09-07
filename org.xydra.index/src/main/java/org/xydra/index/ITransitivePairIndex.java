package org.xydra.index;

import java.util.Iterator;

import org.xydra.index.query.Constraint;
import org.xydra.index.query.Pair;



/**
 * An index for pairs of keys that supports queries on both keys. Additionally
 * queries for pairs that are implied by a transitive property on the pairs are
 * supported. True cycles of pairs (eg. pairs (k1,k2) and (k2,k1) at the same
 * time) not allowed, however there can be multiple paths between to keys (eg.
 * pairs (s,e), (s,k) and (k,e)).
 * 
 * @author dscharrer
 */
public interface ITransitivePairIndex<K> extends IPairIndex<K,K> {
	
	public class CycleException extends IllegalArgumentException {
		private static final long serialVersionUID = -1875597157941776446L;
	}
	
	/**
	 * @param c1 The constraint for the first key.
	 * @param c2 The constraint for the second key.
	 * @return true if there is any chain of pairs p(1) ... p(n) such that
	 *         p(i).getSecond() equals p(i+1).getFirst() (i=1,..,n-1),
	 *         p(1).getFirst() matches c1 and p(n).getSecond() matches c2
	 */
	boolean implies(Constraint<K> c1, Constraint<K> c2);
	
	/**
	 * Iterate over all implied pairs where the keys match the given
	 * constraints.
	 * 
	 * Each pair will be returned only once.
	 * 
	 * Indexing new pairs or deIndexing pairs invalidates the iterator.
	 * 
	 * @param c1 The constraint for the first key.
	 * @param c2 The constraint for the second key.
	 * @return An iterator over all matching pairs.
	 */
	Iterator<Pair<K,K>> transitiveIterator(Constraint<K> c1, Constraint<K> c2);
	
	/**
	 * Add a new pair (k1,k2) to the index. If the pair was already in the
	 * index, nothing is changed.
	 * 
	 * @throws CycleException if adding the pair would close a cycle
	 */
	@Override
    void index(K k1, K k2) throws CycleException;
	
	/**
	 * This iterates over the defined pairs, not the implied ones.
	 * 
	 * @return an Iterator.
	 */
	@Override
    Iterator<Pair<K,K>> iterator();
	
}
