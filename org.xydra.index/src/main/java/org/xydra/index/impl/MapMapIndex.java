package org.xydra.index.impl;

import java.util.Iterator;

import org.xydra.index.IMapIndex;
import org.xydra.index.IMapMapIndex;
import org.xydra.index.iterator.AbstractCascadedIterator;
import org.xydra.index.iterator.NoneIterator;
import org.xydra.index.query.Constraint;
import org.xydra.index.query.EqualsConstraint;
import org.xydra.index.query.IndexFullException;
import org.xydra.index.query.KeyEntryTuple;
import org.xydra.index.query.KeyKeyEntryTuple;
import org.xydra.index.query.Wildcard;


/**
 * An implementation of {@link IMapMapIndex} using a IMapIndex of an IMapIndex.
 * The IMapIndex is automatically switched between a SmallMapIndex and a
 * MapIndex as needed.
 * 
 * The remove() method of iterators always works if c1 != * or c2 == *.
 * Otherwise it might throw UnsupportedOperationException.
 * 
 * @author dscharrer
 * 
 * @param <K> key1 type
 * @param <L> key2 type
 * @param <E> entity type
 */
public class MapMapIndex<K, L, E> implements IMapMapIndex<K,L,E> {
	
	private static final long serialVersionUID = 4902240800466019367L;
	
	protected IMapIndex<K,IMapIndex<L,E>> index;
	
	public MapMapIndex() {
		this.index = new SmallMapIndex<K,IMapIndex<L,E>>();
	}
	
	public boolean containsKey(Constraint<K> c1, Constraint<L> c2) {
		if(c1.isStar()) {
			if(c2.isStar())
				return !isEmpty();
			else {
				L key2 = ((EqualsConstraint<L>)c2).getKey();
				Iterator<IMapIndex<L,E>> it = this.index.iterator();
				while(it.hasNext())
					if(it.next().containsKey(key2))
						return true;
				return false;
			}
		}
		K key1 = ((EqualsConstraint<K>)c1).getKey();
		IMapIndex<L,E> map = this.index.lookup(key1);
		if(map == null)
			return false;
		
		if(c2.isStar()) {
			return true;
		} else {
			return map.containsKey(c2);
		}
	}
	
	public void deIndex(K key1, L key2) {
		IMapIndex<L,E> map = this.index.lookup(key1);
		if(map == null)
			return;
		map.deIndex(key2);
		if(map.isEmpty())
			this.index.deIndex(key1);
	}
	
	public void index(K key1, L key2, E entry) {
		IMapIndex<L,E> map = this.index.lookup(key1);
		if(map == null) {
			map = new SmallMapIndex<L,E>();
			try {
				this.index.index(key1, map);
			} catch(IndexFullException e) {
				IMapIndex<K,IMapIndex<L,E>> newMap = new MapIndex<K,IMapIndex<L,E>>();
				Iterator<KeyEntryTuple<K,IMapIndex<L,E>>> it = this.index
				        .tupleIterator(new Wildcard<K>());
				while(it.hasNext()) {
					KeyEntryTuple<K,IMapIndex<L,E>> tuple = it.next();
					newMap.index(tuple.getKey(), tuple.getEntry());
				}
				newMap.index(key1, map);
				this.index = newMap;
			}
		}
		try {
			map.index(key2, entry);
		} catch(IndexFullException e) {
			IMapIndex<L,E> newMap = new MapIndex<L,E>();
			Iterator<KeyEntryTuple<L,E>> it = map.tupleIterator(new Wildcard<L>());
			while(it.hasNext()) {
				KeyEntryTuple<L,E> tuple = it.next();
				newMap.index(tuple.getKey(), tuple.getEntry());
			}
			newMap.index(key2, entry);
			this.index.index(key1, newMap);
		}
	}
	
	public E lookup(K key1, L key2) {
		IMapIndex<L,E> map = this.index.lookup(key1);
		if(map == null)
			return null;
		return map.lookup(key2);
	}
	
	public Iterator<KeyKeyEntryTuple<K,L,E>> tupleIterator(Constraint<K> c1, Constraint<L> c2) {
		if(c1.isStar())
			return new CascadingIterator<K,L,E>(this.index.tupleIterator(c1), c2);
		K key1 = ((EqualsConstraint<K>)c1).getKey();
		IMapIndex<L,E> map = this.index.lookup(key1);
		if(map == null)
			return new NoneIterator<KeyKeyEntryTuple<K,L,E>>();
		return new FixedFirstKeyIterator(key1, map, c2);
	}
	
	static private class CascadingIterator<K, L, E> implements Iterator<KeyKeyEntryTuple<K,L,E>> {
		
		Iterator<KeyEntryTuple<K,IMapIndex<L,E>>> outer;
		K key1;
		IMapIndex<L,E> map;
		Iterator<KeyEntryTuple<L,E>> inner;
		Iterator<KeyEntryTuple<L,E>> last;
		Constraint<L> c;
		
		public CascadingIterator(Iterator<KeyEntryTuple<K,IMapIndex<L,E>>> it, Constraint<L> c) {
			this.outer = it;
			this.c = c;
		}
		
		public boolean hasNext() {
			
			// if the inner constraint is * we can assume that inner maps
			// always have at least one element
			// this allows the remove() method to work in that case
			if(this.c.isStar())
				return this.outer.hasNext() || (this.inner != null && this.inner.hasNext());
			
			while(this.inner == null || !this.inner.hasNext()) {
				if(!this.outer.hasNext())
					return false;
				nextInner();
			}
			
			return true;
			
		}
		
		private void nextInner() {
			KeyEntryTuple<K,IMapIndex<L,E>> tuple = this.outer.next();
			this.key1 = tuple.getKey();
			this.map = tuple.getEntry();
			this.inner = this.map.tupleIterator(this.c);
		}
		
		public KeyKeyEntryTuple<K,L,E> next() {
			
			while(this.inner == null || !this.inner.hasNext()) {
				if(!this.outer.hasNext())
					return null;
				nextInner();
			}
			
			this.last = this.inner;
			
			KeyEntryTuple<L,E> tuple = this.inner.next();
			if(tuple == null)
				return null;
			return new KeyKeyEntryTuple<K,L,E>(this.key1, tuple.getKey(), tuple.getEntry());
		}
		
		public void remove() {
			
			// should also remove when last != inner, but can't as outer already
			// is at the next inner map and modifying the map outside of the
			// iterator can cause undefined behavior
			if(this.last != this.inner)
				throw new UnsupportedOperationException();
			
			this.last.remove();
			
			if(this.map.isEmpty()) {
				this.last = this.inner = null;
				this.outer.remove();
			}
		}
		
	}
	
	private class FixedFirstKeyIterator implements Iterator<KeyKeyEntryTuple<K,L,E>> {
		
		private K key1;
		private IMapIndex<L,E> map;
		private Iterator<KeyEntryTuple<L,E>> base;
		
		public FixedFirstKeyIterator(K key1, IMapIndex<L,E> map, Constraint<L> c) {
			this.key1 = key1;
			this.map = map;
			this.base = map.tupleIterator(c);
		}
		
		public void remove() {
			this.base.remove();
			if(this.map.isEmpty())
				MapMapIndex.this.index.deIndex(this.key1);
		}
		
		public boolean hasNext() {
			return this.base.hasNext();
		}
		
		public KeyKeyEntryTuple<K,L,E> next() {
			KeyEntryTuple<L,E> in = this.base.next();
			if(in == null)
				return null;
			return new KeyKeyEntryTuple<K,L,E>(this.key1, in.getKey(), in.getEntry());
		}
		
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
	
	public Iterator<K> key1Iterator() {
		return this.index.keyIterator();
	}
	
	public Iterator<L> key2Iterator() {
		return new AbstractCascadedIterator<IMapIndex<L,E>,L>(this.index.iterator()) {
			
			@Override
			protected Iterator<? extends L> toIterator(IMapIndex<L,E> baseEntry) {
				return baseEntry.keyIterator();
			}
			
		};
	}
}
