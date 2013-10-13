package org.xydra.index;

import java.util.Iterator;

import org.xydra.index.query.Constraint;


/**
 * An implementation that uses several indexes internally and chooses among
 * them.
 * 
 * @author voelkel
 * @param <K>
 * @param <L>
 * @param <M>
 * @param <E>
 */
public interface TripleSetIndex<K, L, M, E> {
	
	void clear();
	
	boolean contains(Constraint<K> c1, Constraint<L> c2, Constraint<M> c3);
	
	void deIndex(K s, L p, M o, E entry);
	
	void index(K s, L p, M o, E entry);
	
	Iterator<E> iterator();
	
	/**
	 * @param c1
	 * @param c2
	 * @param c3
	 * @return all matching entities for (c1,c2,c3)
	 */
	Iterator<E> lookup(Constraint<K> c1, Constraint<L> c2, Constraint<M> c3);
	
}
