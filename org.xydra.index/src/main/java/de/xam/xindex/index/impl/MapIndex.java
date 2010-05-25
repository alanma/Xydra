package de.xam.xindex.index.impl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import de.xam.xindex.index.IMapIndex;
import de.xam.xindex.iterator.AbstractTransformingIterator;
import de.xam.xindex.iterator.NoneIterator;
import de.xam.xindex.iterator.SingleValueIterator;
import de.xam.xindex.query.Constraint;
import de.xam.xindex.query.EqualsConstraint;
import de.xam.xindex.query.KeyEntryTuple;


/**
 * An implementation of {@link IMapIndex} using a HashMap.
 * 
 * @author dscharrer
 * 
 * @param <K>
 * @param <E>
 */
public class MapIndex<K, E> implements IMapIndex<K,E> {
	
	private static final long serialVersionUID = -156688788520337376L;
	
	private final Map<K,E> index;
	
	public MapIndex() {
		this.index = new HashMap<K,E>();
	}
	
	public boolean containsKey(K key) {
		return this.index.containsKey(key);
	}
	
	public void deIndex(K key1) {
		this.index.remove(key1);
	}
	
	public void index(K key1, E entry) {
		this.index.put(key1, entry);
	}
	
	public Iterator<E> iterator() {
		return this.index.values().iterator();
	}
	
	public E lookup(K key) {
		return this.index.get(key);
	}
	
	public boolean containsKey(Constraint<K> c1) {
		if(c1.isStar())
			return isEmpty();
		else {
			K key = ((EqualsConstraint<K>)c1).getKey();
			return this.index.containsKey(key);
		}
	}
	
	public Iterator<KeyEntryTuple<K,E>> tupleIterator(Constraint<K> c1) {
		if(c1.isStar()) {
			return new AbstractTransformingIterator<Map.Entry<K,E>,KeyEntryTuple<K,E>>(this.index
			        .entrySet().iterator()) {
				
				@Override
				public KeyEntryTuple<K,E> transform(Entry<K,E> in) {
					return new KeyEntryTuple<K,E>(in.getKey(), in.getValue());
				}
				
			};
		}
		final K key = ((EqualsConstraint<K>)c1).getKey();
		if(this.index.containsKey(key)) {
			return new SingleValueIterator<KeyEntryTuple<K,E>>(new KeyEntryTuple<K,E>(key,
			        this.index.get(key))) {
				@Override
				public void remove() {
					MapIndex.this.deIndex(key);
				}
			};
		} else
			return new NoneIterator<KeyEntryTuple<K,E>>();
		
	}
	
	public void clear() {
		this.index.clear();
	}
	
	public boolean isEmpty() {
		return this.index.isEmpty();
	}
	
	@Override
	public String toString() {
		return this.index.toString();
	}
	
	public Iterator<K> keyIterator() {
		return this.index.keySet().iterator();
	}
	
}
