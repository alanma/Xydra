package org.xydra.index.impl;

import java.util.Iterator;

import org.xydra.index.Factory;
import org.xydra.index.IEntrySet;
import org.xydra.index.IMapMapMapSetIndex;
import org.xydra.index.IMapMapSetIndex;
import org.xydra.index.query.Constraint;



/**
 * Indexes sets of entries by three keys.
 * 
 * Note how this is different from a QuadrupleIndex (which should support
 * queries such as (*,*,*,x)).
 * 
 * @author voelkel
 * 
 * @param <K> first part of triple
 * @param <L> second part of triple
 * @param <M> thrid part of triples
 * @param <E> type of entries in the indexed set
 */
public class TripleSetIndexImpl<K, L, M, E> implements TripleSetIndex<K,L,M,E> {
	
	/**
	 * o-URI -> s-URI -> set of IStatement
	 */
	private transient IMapMapSetIndex<M,K,E> index_o_s_stmt;
	
	/**
	 * p-URI > o-URI > set of IStatement
	 */
	private transient IMapMapSetIndex<L,M,E> index_p_o_stmt;
	
	/**
	 * s-URI > p-URI > o-URI > set of IStatement
	 */
	private transient IMapMapMapSetIndex<K,L,M,E> index_s_p_o_stmt;
	
	public TripleSetIndexImpl() {
		Factory<IEntrySet<E>> entrySetFactory = new SmallEntrySetFactory<E>();
		this.index_o_s_stmt = new MapMapSetIndex<M,K,E>(entrySetFactory);
		this.index_p_o_stmt = new MapMapSetIndex<L,M,E>(entrySetFactory);
		this.index_s_p_o_stmt = new MapMapMapSetIndex<K,L,M,E>(entrySetFactory);
	}
	
	public void clear() {
		this.index_o_s_stmt.clear();
		this.index_p_o_stmt.clear();
		this.index_s_p_o_stmt.clear();
	}
	
	public boolean contains(Constraint<K> c1, Constraint<L> c2, Constraint<M> c3) {
		Iterator<E> it = this.lookup(c1, c2, c3);
		boolean result = it.hasNext();
		return result;
	}
	
	public void deIndex(K s, L p, M o, E entry) {
		this.index_s_p_o_stmt.deIndex(s, p, o, entry);
		this.index_o_s_stmt.deIndex(o, s, entry);
		this.index_p_o_stmt.deIndex(p, o, entry);
	}
	
	public void index(K s, L p, M o, E entry) {
		this.index_s_p_o_stmt.index(s, p, o, entry);
		this.index_o_s_stmt.index(o, s, entry);
		this.index_p_o_stmt.index(p, o, entry);
	}
	
	public Iterator<E> iterator() {
		return this.index_o_s_stmt.iterator();
	}
	
	/**
	 * @param c1
	 * @param c2
	 * @param c3
	 * @return all matching entities for (c1,c2,c3)
	 */
	public Iterator<E> lookup(Constraint<K> c1, Constraint<L> c2, Constraint<M> c3) {
		// deal with the eight patterns
		
		if(
		// spo -> s_p_o
		(!c1.isStar() && !c2.isStar() && !c2.isStar()) ||
		// sp* -> s_p_o
		        (!c1.isStar() && !c2.isStar() && c3.isStar()) ||
		        // s** -> s_p_o
		        (!c1.isStar() && c2.isStar() && c3.isStar()) ||
		        // *** -> s_p_o
		        (c1.isStar() && c2.isStar() && c3.isStar())

		) {
			return this.index_s_p_o_stmt.constraintIterator(c1, c2, c3);
		}
		
		if(
		// *po -> p_o
		(c1.isStar() && !c2.isStar() && !c3.isStar()) ||
		// *p* -> p_o
		        (c1.isStar() && !c2.isStar() && c3.isStar())

		) {
			return this.index_p_o_stmt.constraintIterator(c2, c3);
		}
		
		if(
		// s*o -> o_s
		(!c1.isStar() && c2.isStar() && !c3.isStar()) ||
		// **o -> o_s
		        (c1.isStar() && c2.isStar() && !c3.isStar())

		) {
			return this.index_o_s_stmt.constraintIterator(c3, c1);
		}
		
		throw new AssertionError("one of the patterns should have matched");
	}
	
}
