package de.xam.xindex.index.impl;

import java.util.Iterator;

import de.xam.xindex.index.IMapSetIndex;
import de.xam.xindex.index.IPairIndex;
import de.xam.xindex.iterator.AbstractTransformingIterator;
import de.xam.xindex.query.Constraint;
import de.xam.xindex.query.KeyEntryTuple;
import de.xam.xindex.query.Pair;
import de.xam.xindex.query.Wildcard;


/**
 * An implementation of IPairIndex that uses a map to allow efficient queries on
 * the first key.
 * 
 * @author dscharrer
 */
public class MapPairIndex<K, L> implements IPairIndex<K,L> {
	
	private static final long serialVersionUID = 1806686664089019135L;
	
	/**
	 * key1 -> set of key2
	 */
	private IMapSetIndex<K,L> index;
	
	public MapPairIndex() {
		this.index = new MapSetIndex<K,L>(new FastEntrySetFactory<L>());
	}
	
	public void clear() {
		this.index.clear();
	}
	
	public void index(K k1, L k2) {
		this.index.index(k1, k2);
	}
	
	public void deIndex(K k1, L k2) {
		this.index.deIndex(k1, k2);
	}
	
	public Iterator<Pair<K,L>> constraintIterator(Constraint<K> c1, Constraint<L> c2) {
		
		return new AbstractTransformingIterator<KeyEntryTuple<K,L>,Pair<K,L>>(this.index
		        .tupleIterator(c1, c2)) {
			
			@Override
			public Pair<K,L> transform(KeyEntryTuple<K,L> in) {
				return in;
			}
			
		};
		
	}
	
	public boolean contains(Constraint<K> c1, Constraint<L> c2) {
		return this.index.contains(c1, c2);
	}
	
	public boolean isEmpty() {
		return this.index.isEmpty();
	}
	
	@Override
	public String toString() {
		return this.index.toString();
	}
	
	public Iterator<Pair<K,L>> iterator() {
		return constraintIterator(new Wildcard<K>(), new Wildcard<L>());
	}
	
	public Iterator<K> key1Iterator() {
		return this.index.keyIterator();
	}
	
	public Iterator<L> key2Iterator() {
		return new AbstractTransformingIterator<Pair<K,L>,L>(iterator()) {
			
			@Override
			public L transform(Pair<K,L> in) {
				return in.getSecond();
			}
			
		};
	}
	
}
