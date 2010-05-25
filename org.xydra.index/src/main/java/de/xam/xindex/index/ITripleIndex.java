package de.xam.xindex.index;

import java.util.Iterator;

import de.xam.xindex.index.IMapMapSetIndex.IMapMapSetDiff;
import de.xam.xindex.query.Constraint;
import de.xam.xindex.query.KeyKeyEntryTuple;


/**
 * An index for triples of keys that supports queries on all three keys. Indexes
 * three keys (to boolean, either a key combination is there or not).
 * 
 * 
 * @param <K>
 */
public interface ITripleIndex<K, L, M> extends IIndex {
	
	/**
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
