package org.xydra.index.impl;

import java.util.Iterator;

import org.xydra.index.IMapMapSetIndex;
import org.xydra.index.IMapSetIndex;
import org.xydra.index.ITripleIndex;
import org.xydra.index.IMapMapSetIndex.IMapMapSetDiff;
import org.xydra.index.query.Constraint;
import org.xydra.index.query.EqualsConstraint;
import org.xydra.index.query.KeyKeyEntryTuple;
import org.xydra.index.query.Wildcard;



/**
 * An implementation that uses several indexes internally and chooses among
 * them.
 * 
 * @author voelkel
 * 
 * @param <K>
 * @param <E>
 */
public class TripleIndex<K, L, M> implements ITripleIndex<K,L,M> {
	
	private static final long serialVersionUID = 4825573034123083085L;
	
	/**
	 * o-URI -> p-URI -> set of IStatement
	 */
	private transient IMapSetIndex<M,K> index_o_s_stmt;
	
	/**
	 * p-URI > o-URI > set of IStatement
	 */
	private transient IMapSetIndex<L,M> index_p_o_stmt;
	
	/**
	 * s-URI > p-URI > o-URI > set of IStatement
	 * 
	 * Note: This index is not transient. This index suffices to reconstruct the
	 * state of this object.
	 * 
	 * FIXME restore transient indices when deserializing
	 */
	protected IMapMapSetIndex<K,L,M> index_s_p_o_stmt;
	
	public TripleIndex() {
		this.index_o_s_stmt = new MapSetIndex<M,K>(new FastEntrySetFactory<K>());
		this.index_p_o_stmt = new MapSetIndex<L,M>(new FastEntrySetFactory<M>());
		this.index_s_p_o_stmt = new MapMapSetIndex<K,L,M>(new SmallEntrySetFactory<M>());
	}
	
	/*
	 * JavaDoc: @see de.xam.xindex.index.ITripleIndex#clear()
	 */
	public void clear() {
		this.index_o_s_stmt.clear();
		this.index_p_o_stmt.clear();
		this.index_s_p_o_stmt.clear();
	}
	
	/*
	 * JavaDoc: @see de.xam.xindex.index.ITripleIndex#contains
	 * (de.xam.xindex.query.Constraint, de.xam.xindex.query.Constraint,
	 * de.xam.xindex.query.Constraint)
	 */
	public boolean contains(Constraint<K> c1, Constraint<L> c2, Constraint<M> c3) {
		// deal with the eight patterns
		if(
		// spo -> s_p_o
		(!c1.isStar() && !c2.isStar() && !c3.isStar()) ||
		// sp* -> s_p_o
		        (!c1.isStar() && !c2.isStar() && c3.isStar()) ||
		        // s** -> s_p_o
		        (!c1.isStar() && c2.isStar() && c3.isStar()) ||
		        // *** -> s_p_o
		        (c1.isStar() && c2.isStar() && c3.isStar())

		) {
			return this.index_s_p_o_stmt.contains(c1, c2, c3);
		} else if(
		// *po -> p_o
		(c1.isStar() && !c2.isStar() && !c3.isStar()) ||
		// *p* -> p_o
		        (c1.isStar() && !c2.isStar() && c3.isStar())

		) {
			return this.index_p_o_stmt.contains(c2, c3);
		} else if(
		// s*o -> o_s
		(!c1.isStar() && c2.isStar() && !c3.isStar()) ||
		// **o -> o_s
		        (c1.isStar() && c2.isStar() && !c3.isStar())

		) {
			return this.index_o_s_stmt.contains(c3, c1);
		}
		
		throw new AssertionError("one of the patterns should have matched");
	}
	
	public boolean contains(K key1, L key2, M key3) {
		Constraint<K> c1 = new EqualsConstraint<K>(key1);
		Constraint<L> c2 = new EqualsConstraint<L>(key2);
		Constraint<M> c3 = new EqualsConstraint<M>(key3);
		return this.contains(c1, c2, c3);
	}
	
	/*
	 * JavaDoc: @see de.xam.xindex.index.ITripleIndex#deIndex(K, K, K)
	 */
	public void deIndex(K s, L p, M o) {
		this.index_s_p_o_stmt.deIndex(s, p, o);
		this.index_o_s_stmt.deIndex(o, s);
		this.index_p_o_stmt.deIndex(p, o);
	}
	
	/*
	 * JavaDoc: @see de.xam.xindex.index.ITripleIndex#dump()
	 */
	public void dump() {
		System.out.println("Dumping s-p-o-index (there are others)");
		Iterator<KeyKeyEntryTuple<K,L,M>> it = this.index_s_p_o_stmt.tupleIterator(
		        new Wildcard<K>(), new Wildcard<L>(), new Wildcard<M>());
		while(it.hasNext()) {
			KeyKeyEntryTuple<K,L,M> t = it.next();
			System.out.println(t.getKey1() + " - " + t.getKey2() + " - " + t.getEntry());
		}
	}
	
	/**
	 * @param c1
	 * @param c2
	 * @param c3
	 * @return
	 */
	public Iterator<KeyKeyEntryTuple<K,L,M>> getTriples(Constraint<K> c1, Constraint<L> c2,
	        Constraint<M> c3) {
		if(c1 == null)
			throw new IllegalArgumentException("c1 was null");
		if(c2 == null)
			throw new IllegalArgumentException("c2 was null");
		if(c3 == null)
			throw new IllegalArgumentException("c3 was null");
		Iterator<KeyKeyEntryTuple<K,L,M>> tupleIterator = this.index_s_p_o_stmt.tupleIterator(c1,
		        c2, c3);
		return tupleIterator;
	}
	
	/*
	 * JavaDoc: @see de.xam.xindex.index.ITripleIndex#index(K, K, K)
	 */
	public void index(K s, L p, M o) {
		this.index_s_p_o_stmt.index(s, p, o);
		this.index_o_s_stmt.index(o, s);
		this.index_p_o_stmt.index(p, o);
	}
	
	public IMapMapSetDiff<K,L,M> computeDiff(ITripleIndex<K,L,M> other) {
		TripleIndex<K,L,M> otherIndex = (TripleIndex<K,L,M>)other;
		IMapMapSetDiff<K,L,M> spoDiff = this.index_s_p_o_stmt
		        .computeDiff(otherIndex.index_s_p_o_stmt);
		return spoDiff;
	}
	
	public boolean isEmpty() {
		return this.index_s_p_o_stmt.isEmpty();
	}
	
}
