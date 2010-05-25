package de.xam.xindex.index.impl;

import java.util.Iterator;

import de.xam.xindex.index.IMapSetIndex;
import de.xam.xindex.index.XI;
import de.xam.xindex.iterator.NoneIterator;
import de.xam.xindex.iterator.SingleValueIterator;
import de.xam.xindex.query.Constraint;
import de.xam.xindex.query.EqualsConstraint;
import de.xam.xindex.query.KeyEntryTuple;


/**
 * An {@link IMapSetIndex} that can store exactly one key-entry mapping. Small.
 * 
 * @author voelkel
 * 
 * @param <K>
 * @param <V>
 */
public class SingleEntryMapSetIndex<K, E> extends KeyEntryTuple<K,E> implements IMapSetIndex<K,E> {
	
	private static final long serialVersionUID = 3040641314902060159L;
	
	boolean empty = false;
	
	public SingleEntryMapSetIndex(K key, E value) {
		super(key, value);
	}
	
	public void clear() {
		this.empty = true;
	}
	
	public boolean containsKey(K key) {
		return !this.empty && XI.equals(key, getKey());
	}
	
	public boolean containsValue(E value) {
		return !this.empty && XI.equals(value, getEntry());
	}
	
	public E get(K key) {
		if(containsKey(key)) {
			return getEntry();
		} else
			return null;
	}
	
	public boolean isEmpty() {
		return this.empty;
	}
	
	public int size() {
		return isEmpty() ? 0 : 1;
	}
	
	public Iterator<E> constraintIterator(Constraint<K> c1) {
		if(!isEmpty() && c1.matches(getKey())) {
			return new SingleValueIterator<E>(getEntry());
		} else {
			return new NoneIterator<E>();
		}
	}
	
	public boolean contains(Constraint<K> c1, Constraint<E> entryConstraint) {
		
		if(isEmpty()) {
			return false;
		}
		
		// 1. component
		if(!c1.matches(getKey())) {
			return false;
		}
		
		// 2nd component
		return entryConstraint.matches(getEntry());
	}
	
	public void deIndex(K key1, E entry) {
		this.clear();
	}
	
	public void index(K key1, E entry) {
		throw new UnsupportedOperationException();
	}
	
	public Iterator<KeyEntryTuple<K,E>> tupleIterator(Constraint<K> c1,
	        Constraint<E> entryConstraint) {
		if(contains(c1, entryConstraint)) {
			return new SingleValueIterator<KeyEntryTuple<K,E>>(this);
		} else {
			return new NoneIterator<KeyEntryTuple<K,E>>();
		}
	}
	
	public IMapSetDiff<K,E> computeDiff(IMapSetIndex<K,E> otherFuture) {
		SingleEntryMapSetDiff<K,E> diff = new SingleEntryMapSetDiff<K,E>();
		
		if(this.getKey() == null) {
			diff.added = otherFuture;
			diff.removed = new NoEntryMapSetIndex<K,E>();
		} else {
			// if not null, the Key-Value is either
			
			// a) still present => no removes
			// b) no longer present => one remove
			
			// c) many other values might have been added
			diff.added = otherFuture;
			
			if(otherFuture.contains(new EqualsConstraint<K>(getKey()), new EqualsConstraint<E>(
			        getEntry()))) {
				// still present => no adds, no removes
				diff.removed = new NoEntryMapSetIndex<K,E>();
				diff.added.deIndex(getKey(), getEntry());
			} else {
				// missing? so everything besides this has been added
				diff.removed = this;
			}
		}
		
		return diff;
	}
	
	public static class SingleEntryMapSetDiff<K, E> implements IMapSetDiff<K,E> {
		
		protected IMapSetIndex<K,E> added, removed;
		
		public IMapSetIndex<K,E> getAdded() {
			return this.added;
		}
		
		public IMapSetIndex<K,E> getRemoved() {
			return this.removed;
		}
		
	}
	
	public Iterator<K> keyIterator() {
		return new SingleValueIterator<K>(getKey());
	}
	
}
