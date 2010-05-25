package org.xydra.index.impl;

import java.util.Iterator;

import org.xydra.index.IMapSetIndex;
import org.xydra.index.IPairIndex;
import org.xydra.index.iterator.AbstractTransformingIterator;
import org.xydra.index.query.Constraint;
import org.xydra.index.query.KeyEntryTuple;
import org.xydra.index.query.Pair;
import org.xydra.index.query.Wildcard;



/**
 * An implementation of IPairIndex that uses two separate indices to allow
 * efficient queries on both keys.
 * 
 * @author dscharrer
 */
public class PairIndex<K, L> implements IPairIndex<K,L> {
	
	private static final long serialVersionUID = 1027758476161499843L;
	
	/**
	 * key1 -> set of key2
	 */
	final private IMapSetIndex<K,L> index_k1_k2;
	
	/**
	 * ley2 -> set of key1
	 */
	private transient IMapSetIndex<L,K> index_k2_k1;
	
	public PairIndex() {
		this.index_k1_k2 = new MapSetIndex<K,L>(new FastEntrySetFactory<L>());
		this.index_k2_k1 = new MapSetIndex<L,K>(new FastEntrySetFactory<K>());
	}
	
	public void clear() {
		this.index_k1_k2.clear();
		this.index_k2_k1.clear();
	}
	
	public void index(K k1, L k2) {
		this.index_k1_k2.index(k1, k2);
		this.index_k2_k1.index(k2, k1);
	}
	
	public void deIndex(K k1, L k2) {
		this.index_k1_k2.deIndex(k1, k2);
		this.index_k2_k1.deIndex(k2, k1);
	}
	
	public Iterator<Pair<K,L>> constraintIterator(Constraint<K> c1, Constraint<L> c2) {
		
		if(!c1.isStar() || c2.isStar()) {
			return new AbstractTransformingIterator<KeyEntryTuple<K,L>,Pair<K,L>>(this.index_k1_k2
			        .tupleIterator(c1, c2)) {
				
				@Override
				public Pair<K,L> transform(KeyEntryTuple<K,L> in) {
					return in;
				}
				
			};
		}
		
		return new AbstractTransformingIterator<KeyEntryTuple<L,K>,Pair<K,L>>(this.index_k2_k1
		        .tupleIterator(c2, c1)) {
			
			@Override
			public Pair<K,L> transform(KeyEntryTuple<L,K> in) {
				return in.inverse();
			}
		};
		
	}
	
	public boolean contains(Constraint<K> c1, Constraint<L> c2) {
		
		if(!c1.isStar())
			return this.index_k1_k2.contains(c1, c2);
		
		return this.index_k2_k1.contains(c2, c1);
		
	}
	
	public boolean isEmpty() {
		return this.index_k1_k2.isEmpty();
	}
	
	/**
	 * Called on deserialization, needs to restore transient members.
	 */
	private Object readResolve() {
		if(this.index_k2_k1 == null) {
			this.index_k2_k1 = new MapSetIndex<L,K>(new FastEntrySetFactory<K>());
			Iterator<KeyEntryTuple<K,L>> it = this.index_k1_k2.tupleIterator(new Wildcard<K>(),
			        new Wildcard<L>());
			while(it.hasNext()) {
				KeyEntryTuple<K,L> tuple = it.next();
				this.index_k2_k1.index(tuple.getEntry(), tuple.getKey());
			}
		}
		return this;
	}
	
	@Override
	public String toString() {
		return this.index_k1_k2.toString();
	}
	
	public Iterator<Pair<K,L>> iterator() {
		return constraintIterator(new Wildcard<K>(), new Wildcard<L>());
	}
	
	public Iterator<K> key1Iterator() {
		return this.index_k1_k2.keyIterator();
	}
	
	public Iterator<L> key2Iterator() {
		return this.index_k2_k1.keyIterator();
	}
	
}
