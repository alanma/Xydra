package de.xam.xindex.index.impl;

import java.util.Iterator;

import de.xam.xindex.index.IMapSetIndex;
import de.xam.xindex.iterator.NoneIterator;
import de.xam.xindex.query.Constraint;
import de.xam.xindex.query.KeyEntryTuple;


/**
 * An empty, read-only {@link IMapSetIndex}.
 * 
 * @author voelkel
 * 
 * @param <E>
 */
public class NoEntryMapSetIndex<K, E> implements IMapSetIndex<K,E> {
	
	private static final long serialVersionUID = -4800590888115736374L;
	
	public de.xam.xindex.index.IMapSetIndex.IMapSetDiff<K,E> computeDiff(
	        IMapSetIndex<K,E> otherFuture) {
		
		// comparing this to other: all content has been added, nothing has been
		// removed
		return new EmptyMapSetDiff(otherFuture);
	}
	
	class EmptyMapSetDiff implements IMapSetDiff<K,E> {
		
		private IMapSetIndex<K,E> added;
		
		public EmptyMapSetDiff(IMapSetIndex<K,E> other) {
			this.added = other;
		}
		
		public IMapSetIndex<K,E> getAdded() {
			return this.added;
		}
		
		public IMapSetIndex<K,E> getRemoved() {
			return NoEntryMapSetIndex.this;
		}
		
	}
	
	public Iterator<E> constraintIterator(Constraint<K> c1) {
		return new NoneIterator<E>();
	}
	
	public boolean contains(Constraint<K> c1, Constraint<E> entryConstraint) {
		return false;
	}
	
	public boolean containsKey(K key) {
		return false;
	}
	
	public void deIndex(K key1, E entry) {
		throw new RuntimeException("this index is not meant to write");
	}
	
	public void index(K key1, E entry) {
		throw new RuntimeException("this index is not meant to write");
	}
	
	public Iterator<KeyEntryTuple<K,E>> tupleIterator(Constraint<K> c1,
	        Constraint<E> entryConstraint) {
		return new NoneIterator<KeyEntryTuple<K,E>>();
	}
	
	public void clear() {
		throw new RuntimeException("this index is not meant to write");
	}
	
	public boolean isEmpty() {
		return true;
	}
	
	public Iterator<K> keyIterator() {
		return new NoneIterator<K>();
	}
	
}
