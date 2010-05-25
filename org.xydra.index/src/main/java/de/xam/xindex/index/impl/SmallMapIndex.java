package de.xam.xindex.index.impl;

import java.util.Iterator;

import de.xam.xindex.index.IMapIndex;
import de.xam.xindex.index.XI;
import de.xam.xindex.iterator.NoneIterator;
import de.xam.xindex.iterator.SingleValueIterator;
import de.xam.xindex.query.Constraint;
import de.xam.xindex.query.IndexFullException;
import de.xam.xindex.query.KeyEntryTuple;


/**
 * An implementation of {@link IMapIndex} that can hold exactly one entry.
 * 
 * @author dscharrer
 * 
 */
public class SmallMapIndex<K, E> implements IMapIndex<K,E> {
	
	private static final long serialVersionUID = 2037583029777355928L;
	
	KeyEntryTuple<K,E> tuple;
	
	public boolean containsKey(K key) {
		return this.tuple != null && XI.equals(key, this.tuple.getKey());
	}
	
	public boolean containsKey(Constraint<K> c1) {
		return this.tuple != null && c1.matches(this.tuple.getKey());
	}
	
	public void deIndex(K key1) {
		if(containsKey(key1))
			this.tuple = null;
	}
	
	public void index(K key1, E entry) {
		if(this.tuple != null && !containsKey(key1))
			throw new IndexFullException();
		this.tuple = new KeyEntryTuple<K,E>(key1, entry);
	}
	
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
	
	public E lookup(K key) {
		if(!containsKey(key))
			return null;
		return this.tuple.getEntry();
	}
	
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
	
	public void clear() {
		this.tuple = null;
	}
	
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
	
	public Iterator<K> keyIterator() {
		if(isEmpty())
			return new NoneIterator<K>();
		return new SingleValueIterator<K>(this.tuple.getKey());
	}
	
}
