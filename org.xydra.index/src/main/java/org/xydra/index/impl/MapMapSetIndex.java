package org.xydra.index.impl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.xydra.index.Factory;
import org.xydra.index.IEntrySet;
import org.xydra.index.IMapMapSetIndex;
import org.xydra.index.IMapSetIndex;
import org.xydra.index.IMapSetIndex.IMapSetDiff;
import org.xydra.index.iterator.AbstractCascadedIterator;
import org.xydra.index.iterator.NoneIterator;
import org.xydra.index.query.Constraint;
import org.xydra.index.query.EqualsConstraint;
import org.xydra.index.query.GenericKeyEntryTupleConstraintFilteringIterator;
import org.xydra.index.query.KeyEntryTuple;
import org.xydra.index.query.KeyKeyEntryTuple;
import org.xydra.index.query.Wildcard;


public class MapMapSetIndex<K, L, E> implements IMapMapSetIndex<K,L,E> {
	
	private static final long serialVersionUID = -1872398601112534222L;
	
	/*
	 * needed for tupleIterator()
	 * 
	 * This iterator knows key1. Together with the values from the tuple
	 * (tuple-key, tuple-entry) it forms the result-tuples (key, tuple-key,
	 * tuple-entry)
	 */
	private static class AdaptMapEntryToTupleIterator<K, L, E> implements
	        Iterator<KeyKeyEntryTuple<K,L,E>> {
		
		private K key1;
		private Iterator<KeyEntryTuple<L,E>> tupleIterator;
		
		public AdaptMapEntryToTupleIterator(K key1, Iterator<KeyEntryTuple<L,E>> tupleIterator) {
			this.key1 = key1;
			this.tupleIterator = tupleIterator;
		}
		
		@Override
		public boolean hasNext() {
			return this.tupleIterator.hasNext();
		}
		
		@Override
		public KeyKeyEntryTuple<K,L,E> next() {
			if(this.tupleIterator.hasNext()) {
				KeyEntryTuple<L,E> x = this.tupleIterator.next();
				return new KeyKeyEntryTuple<K,L,E>(this.key1, x.getKey(), x.getEntry());
			} else
				return null;
		}
		
		@Override
		public void remove() {
			this.tupleIterator.remove();
		}
		
	}
	
	/* needed for tupleIterator() */
	private static class CascadingMapEntry_K_MapSet_Iterator<K, L, E> extends
	        AbstractCascadedIterator<Map.Entry<K,IMapSetIndex<L,E>>,KeyKeyEntryTuple<K,L,E>> {
		
		private Constraint<L> c1;
		private Constraint<E> entryConstraint;
		
		public CascadingMapEntry_K_MapSet_Iterator(Iterator<Map.Entry<K,IMapSetIndex<L,E>>> base,
		        Constraint<L> c1, Constraint<E> entryConstraint) {
			super(base);
			assert c1 != null;
			assert entryConstraint != null;
			this.c1 = c1;
			this.entryConstraint = entryConstraint;
		}
		
		@Override
		protected Iterator<KeyKeyEntryTuple<K,L,E>> toIterator(Entry<K,IMapSetIndex<L,E>> baseEntry) {
			
			return new AdaptMapEntryToTupleIterator<K,L,E>(baseEntry.getKey(), baseEntry.getValue()
			        .tupleIterator(this.c1, this.entryConstraint));
		}
		
	}
	
	/* needed for constraintIterator() */
	private class CascadingMapSetIndexIterator extends
	        AbstractCascadedIterator<IMapSetIndex<L,E>,E> {
		private Constraint<L> c1;
		
		public CascadingMapSetIndexIterator(Iterator<IMapSetIndex<L,E>> base, Constraint<L> c1) {
			super(base);
			this.c1 = c1;
		}
		
		@Override
		protected Iterator<E> toIterator(IMapSetIndex<L,E> baseEntry) {
			return baseEntry.constraintIterator(this.c1);
		}
	}
	
	/* needed for tupleIterator() */
	private class RememberKeyIterator implements Iterator<KeyKeyEntryTuple<K,L,E>> {
		
		private K key;
		private Iterator<KeyEntryTuple<L,E>> tupleIterator;
		
		public RememberKeyIterator(K key, Iterator<KeyEntryTuple<L,E>> tupleIterator) {
			this.key = key;
			this.tupleIterator = tupleIterator;
		}
		
		@Override
		public boolean hasNext() {
			return this.tupleIterator.hasNext();
		}
		
		@Override
		public KeyKeyEntryTuple<K,L,E> next() {
			KeyEntryTuple<L,E> e = this.tupleIterator.next();
			return new KeyKeyEntryTuple<K,L,E>(this.key, e.getKey(), e.getEntry());
		}
		
		@Override
		public void remove() {
			this.tupleIterator.remove();
		}
	}
	
	private Map<K,IMapSetIndex<L,E>> map = new HashMap<K,IMapSetIndex<L,E>>(2);
	
	// experimental extension
	public Iterator<K> key1Iterator() {
		return this.map.keySet().iterator();
	}
	
	// experimental extension
	public IMapSetIndex<L,E> getMapEntry(K k) {
		return this.map.get(k);
	}
	
	private Factory<IEntrySet<E>> entrySetFactory;
	
	/**
	 * @param entrySetFactory Theis factory configures the trade-off between
	 *            time and space. Existing factories you can use are
	 *            {@link FastEntrySetFactory} and {@link SmallEntrySetFactory}.
	 */
	public MapMapSetIndex(Factory<IEntrySet<E>> entrySetFactory) {
		super();
		this.entrySetFactory = entrySetFactory;
	}
	
	@Override
	public void clear() {
		this.map.clear();
	}
	
	@Override
	public Iterator<E> constraintIterator(Constraint<K> c1, Constraint<L> c2) {
		if(c1 instanceof Wildcard<?>) {
			return new CascadingMapSetIndexIterator(this.map.values().iterator(), c2);
		} else if(c1 instanceof EqualsConstraint<?>) {
			EqualsConstraint<K> keyConstraint = (EqualsConstraint<K>)c1;
			K key = keyConstraint.getKey();
			IMapSetIndex<L,E> index1 = this.map.get(key);
			return index1 == null ? new NoneIterator<E>() : index1.constraintIterator(c2);
		} else {
			throw new AssertionError("unknown constraint type " + c1.getClass());
		}
	}
	
	@Override
	public boolean contains(Constraint<K> c1, Constraint<L> c2, Constraint<E> entryConstraint) {
		Iterator<E> c1it = this.constraintIterator(c1, c2);
		while(c1it.hasNext()) {
			E entry = c1it.next();
			if(entryConstraint.matches(entry)) {
				// we found any element, that's good enough
				return true;
			}
		}
		return false;
	}
	
	public static class DiffImpl<K, L, E> implements IMapMapSetDiff<K,L,E> {
		
		protected MapMapSetIndex<K,L,E> added;
		protected MapMapSetIndex<K,L,E> removed;
		
		@Override
		public IMapMapSetIndex<K,L,E> getAdded() {
			return this.added;
		}
		
		@Override
		public IMapMapSetIndex<K,L,E> getRemoved() {
			return this.removed;
		}
		
		public DiffImpl(Factory<IEntrySet<E>> entrySetFactory) {
			this.added = new MapMapSetIndex<K,L,E>(entrySetFactory);
			this.removed = new MapMapSetIndex<K,L,E>(entrySetFactory);
		}
		
	}
	
	@Override
	public IMapMapSetDiff<K,L,E> computeDiff(IMapMapSetIndex<K,L,E> otherFuture) {
		DiffImpl<K,L,E> diff = new DiffImpl<K,L,E>(this.entrySetFactory);
		
		MapMapSetIndex<K,L,E> otherIndex = (MapMapSetIndex<K,L,E>)otherFuture;
		
		for(Entry<K,IMapSetIndex<L,E>> thisEntry : this.map.entrySet()) {
			K key = thisEntry.getKey();
			IMapSetIndex<L,E> otherValue = otherIndex.map.get(key);
			if(otherValue != null) {
				// same (key,*,*) entry, compare sub-maps
				IMapSetDiff<L,E> mapSetDiff = thisEntry.getValue().computeDiff(otherValue);
				if(!mapSetDiff.getAdded().isEmpty()) {
					diff.added.map.put(key, mapSetDiff.getAdded());
				}
				if(!mapSetDiff.getRemoved().isEmpty()) {
					diff.removed.map.put(key, mapSetDiff.getRemoved());
				}
			} else {
				// missing in other => removed a complete sub-tree (key,*,*)
				diff.removed.map.put(thisEntry.getKey(), thisEntry.getValue());
			}
		}
		
		// compare other to this
		for(Entry<K,IMapSetIndex<L,E>> otherEntry : otherIndex.map.entrySet()) {
			K key = otherEntry.getKey();
			if(!this.containsKey(key)) {
				// whole set (key,*,*) missing in this => added
				diff.added.map.put(key, otherEntry.getValue());
			}
		}
		
		return diff;
	}
	
	private boolean containsKey(K key) {
		return this.map.containsKey(key);
	}
	
	@Override
	public void deIndex(K key1, L key2, E entry) {
		IMapSetIndex<L,E> index1 = this.map.get(key1);
		if(index1 != null) {
			if(index1 instanceof SingleEntryMapSetIndex<?,?>) {
				// special remove of single entry map
				this.map.remove(key1);
			} else {
				// normal remove
				index1.deIndex(key2, entry);
				if(index1.isEmpty()) {
					this.map.remove(key1);
				}
			}
		}
	}
	
	@Override
	public void index(K key1, L key2, E entry) {
		IMapSetIndex<L,E> index1 = this.map.get(key1);
		if(index1 == null) {
			index1 = new SingleEntryMapSetIndex<L,E>(key2, entry);
			this.map.put(key1, index1);
		} else {
			// we need to index more than one (*,K,E)
			if(index1 instanceof SingleEntryMapSetIndex<?,?>) {
				// put a flexible map instead
				L k = ((SingleEntryMapSetIndex<L,E>)index1).getKey();
				E e = ((SingleEntryMapSetIndex<L,E>)index1).getEntry();
				index1 = new MapSetIndex<L,E>(this.entrySetFactory);
				this.map.put(key1, index1);
				// add existing single entry
				index1.index(k, e);
			}
			index1.index(key2, entry);
		}
	}
	
	@Override
	public boolean isEmpty() {
		return this.map.isEmpty();
	}
	
	@Override
	public Iterator<E> iterator() {
		return this.constraintIterator(new Wildcard<K>(), new Wildcard<L>());
	}
	
	@Override
	public Iterator<KeyKeyEntryTuple<K,L,E>> tupleIterator(Constraint<K> c1, Constraint<L> c2,
	        Constraint<E> entryConstraint) {
		assert c1 != null;
		assert c2 != null;
		assert entryConstraint != null;
		if(c1 instanceof Wildcard<?>) {
			Iterator<Map.Entry<K,IMapSetIndex<L,E>>> entryIt = this.map.entrySet().iterator();
			// cascade to tuples
			Iterator<KeyKeyEntryTuple<K,L,E>> cascaded = new CascadingMapEntry_K_MapSet_Iterator<K,L,E>(
			        entryIt, c2, entryConstraint);
			// filter entries
			Iterator<KeyKeyEntryTuple<K,L,E>> filtered = new GenericKeyEntryTupleConstraintFilteringIterator<KeyKeyEntryTuple<K,L,E>,E>(
			        cascaded, entryConstraint);
			return filtered;
		} else if(c1 instanceof EqualsConstraint<?>) {
			EqualsConstraint<K> keyConstraint = (EqualsConstraint<K>)c1;
			K key = keyConstraint.getKey();
			IMapSetIndex<L,E> index1 = this.map.get(key);
			if(index1 == null) {
				return new NoneIterator<KeyKeyEntryTuple<K,L,E>>();
			} else {
				return new RememberKeyIterator(key, index1.tupleIterator(c2, entryConstraint));
			}
		} else {
			throw new AssertionError("unknown constraint type " + c1.getClass());
		}
	}
	
	@Override
	public String toString() {
		return this.map.toString();
	}
	
}