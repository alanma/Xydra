package org.xydra.index;

import java.util.Iterator;

import org.xydra.index.IMapMapSetIndex.IMapMapSetDiff;
import org.xydra.index.query.Constraint;
import org.xydra.index.query.KeyKeyEntryTuple;


/**
 * An index for triples of keys that supports queries on all three keys. Indexes
 * three keys (to boolean, either a key combination is there or not).
 * 
 * 
 * @param <K> key type
 * @param <L> key type
 * @param <M> key type
 */
public interface ITripleIndex<K, L, M> extends IIndex {
	
	/**
	 * @param c1
	 * @param c2
	 * @param c3
	 * @return true if there is a triple in the index where s matches c1, p
	 *         matches c2 and o matches c2
	 */
	public abstract boolean contains(Constraint<K> c1, Constraint<L> c2, Constraint<M> c3);
	
	public abstract void deIndex(K s, L p, M o);
	
	public abstract void dump();
	
	public abstract void index(K s, L p, M o);
	
	/**
	 * @param c1
	 * @param c2
	 * @param c3
	 * @return an iterator with all triples matching the given constraints
	 */
	Iterator<KeyKeyEntryTuple<K,L,M>> getTriples(Constraint<K> c1, Constraint<L> c2,
	        Constraint<M> c3);
	
	IMapMapSetDiff<K,L,M> computeDiff(ITripleIndex<K,L,M> other);
	
}
