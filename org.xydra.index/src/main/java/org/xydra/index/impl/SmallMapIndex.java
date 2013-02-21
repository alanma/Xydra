package org.xydra.index.impl;

import java.util.Iterator;

import org.xydra.index.IMapIndex;
import org.xydra.index.XI;
import org.xydra.index.iterator.NoneIterator;
import org.xydra.index.iterator.SingleValueIterator;
import org.xydra.index.query.Constraint;
import org.xydra.index.query.IndexFullException;
import org.xydra.index.query.KeyEntryTuple;


/**
 * An implementation of {@link IMapIndex} that can hold exactly one entry.
 * 
 * @author dscharrer
 * @param <K>
 * @param <E>
 * 
 */
public class SmallMapIndex<K, E> implements IMapIndex<K,E> {
	
	private static final long serialVersionUID = 2037583029777355928L;
	
	KeyEntryTuple<K,E> tuple;
	
	@Override
	public boolean containsKey(K key) {
		return this.tuple != null && XI.equals(key, this.tuple.getKey());
	}
	
	@Override
	public boolean containsKey(Constraint<K> c1) {
		return this.tuple != null && c1.matches(this.tuple.getKey());
	}
	
	@Override
	public void deIndex(K key1) {
		if(containsKey(key1))
			this.tuple = null;
	}
	
	@Override
	public void index(K key1, E entry) {
		if(this.tuple != null && !containsKey(key1))
			throw new IndexFullException();
		this.tuple = new KeyEntryTuple<K,E>(key1, entry);
	}
	
	@Override
	public Iterator<E> iterator() {
		if(this.tuple == null)
			return new NoneIterator<E>();
		return new SingleValueIterator<E>(this.tuple.getEntry()) {
			@Override
			public void remove() {
				clear();
			}
		};
	}
	
	@Override
	public E lookup(K key) {
		if(!containsKey(key))
			return null;
		return this.tuple.getEntry();
	}
	
	@Override
	public Iterator<KeyEntryTuple<K,E>> tupleIterator(Constraint<K> c1) {
		if(!containsKey(c1))
			return new NoneIterator<KeyEntryTuple<K,E>>();
		return new SingleValueIterator<KeyEntryTuple<K,E>>(this.tuple) {
			@Override
			public void remove() {
				clear();
			}
		};
	}
	
	@Override
	public void clear() {
		this.tuple = null;
	}
	
	@Override
	public boolean isEmpty() {
		return this.tuple == null;
	}
	
	@Override
	public String toString() {
		if(this.tuple == null)
			return "{}";
		else
			return "{" + this.tuple.getKey() + "=" + this.tuple.getEntry() + "}";
	}
	
	@Override
	public Iterator<K> keyIterator() {
		if(isEmpty())
			return new NoneIterator<K>();
		return new SingleValueIterator<K>(this.tuple.getKey());
	}
	
}