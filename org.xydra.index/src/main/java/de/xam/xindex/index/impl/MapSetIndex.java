package de.xam.xindex.index.impl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import de.xam.xindex.Factory;
import de.xam.xindex.index.IEntrySet;
import de.xam.xindex.index.IMapSetIndex;
import de.xam.xindex.index.IEntrySet.IEntrySetDiff;
import de.xam.xindex.iterator.AbstractCascadedIterator;
import de.xam.xindex.iterator.NoneIterator;
import de.xam.xindex.query.Constraint;
import de.xam.xindex.query.EqualsConstraint;
import de.xam.xindex.query.GenericKeyEntryTupleConstraintFilteringIterator;
import de.xam.xindex.query.KeyEntryTuple;


public class MapSetIndex<K, E> implements IMapSetIndex<K,E> {
	
	private static final long serialVersionUID = 8275298314930537914L;
	
	/* needed for tupleIterator() */
	private class AdaptMapEntryToTupleIterator implements Iterator<KeyEntryTuple<K,E>> {
		
		private Entry<K,IEntrySet<E>> base;
		private Iterator<E> it;
		
		public AdaptMapEntryToTupleIterator(Entry<K,IEntrySet<E>> base) {
			this.base = base;
			this.it = base.getValue().iterator();
		}
		
		public boolean hasNext() {
			return this.it.hasNext();
		}
		
		public KeyEntryTuple<K,E> next() {
			return new KeyEntryTuple<K,E>(this.base.getKey(), this.it.next());
		}
		
		public void remove() {
			this.it.remove();
		}
		
	}
	
	/* needed for constraintIterator() */
	private class CascadingEntrySetIterator extends AbstractCascadedIterator<IEntrySet<E>,E> {
		public CascadingEntrySetIterator(Iterator<IEntrySet<E>> base) {
			super(base);
		}
		
		@Override
		protected Iterator<E> toIterator(IEntrySet<E> baseEntry) {
			return baseEntry.iterator();
		}
	}
	
	/* needed for tupleIterator() */
	private class CascadingMapEntry_K_EntrySet_Iterator extends
	        AbstractCascadedIterator<Map.Entry<K,IEntrySet<E>>,KeyEntryTuple<K,E>> {
		
		public CascadingMapEntry_K_EntrySet_Iterator(Iterator<Map.Entry<K,IEntrySet<E>>> base) {
			super(base);
		}
		
		@Override
		protected Iterator<KeyEntryTuple<K,E>> toIterator(Map.Entry<K,IEntrySet<E>> baseEntry) {
			return new AdaptMapEntryToTupleIterator(baseEntry);
		}
		
	}
	
	/* needed for tupleIterator() */
	private class RememberKeyIterator implements Iterator<KeyEntryTuple<K,E>> {
		
		private Iterator<E> base;
		private K key;
		
		public RememberKeyIterator(K key, Iterator<E> base) {
			this.base = base;
			this.key = key;
		}
		
		public boolean hasNext() {
			return this.base.hasNext();
		}
		
		public KeyEntryTuple<K,E> next() {
			E entry = this.base.next();
			return new KeyEntryTuple<K,E>(this.key, entry);
		}
		
		public void remove() {
			this.base.remove();
		}
		
	}
	
	private Map<K,IEntrySet<E>> map;
	private Factory<IEntrySet<E>> entrySetFactory;
	
	public MapSetIndex(Factory<IEntrySet<E>> entrySetFactory) {
		this.map = new HashMap<K,IEntrySet<E>>(4);
		this.entrySetFactory = entrySetFactory;
	}
	
	public void clear() {
		this.map.clear();
	}
	
	public Iterator<E> constraintIterator(Constraint<K> c1) {
		if(c1.isStar()) {
			return new CascadingEntrySetIterator(this.map.values().iterator());
		} else if(c1 instanceof EqualsConstraint<?>) {
			EqualsConstraint<K> keyConstraint = (EqualsConstraint<K>)c1;
			K key = keyConstraint.getKey();
			IEntrySet<E> index0 = this.map.get(key);
			return index0 == null ? new NoneIterator<E>() : index0.iterator();
		} else {
			throw new AssertionError("unknown constraint type " + c1.getClass());
		}
	}
	
	public boolean contains(Constraint<K> c1, Constraint<E> entryConstraint) {
		if(c1.isStar()) {
			if(entryConstraint.isStar()) {
				return !this.map.isEmpty();
			} else {
				assert entryConstraint instanceof EqualsConstraint<?>;
				E entry = ((EqualsConstraint<E>)entryConstraint).getKey();
				for(IEntrySet<E> e : this.map.values()) {
					if(e.contains(entry)) {
						return true;
					}
				}
				return false;
			}
		} else {
			assert c1 instanceof EqualsConstraint<?>;
			K key = ((EqualsConstraint<K>)c1).getKey();
			if(entryConstraint.isStar()) {
				return this.map.containsKey(key);
			} else {
				assert entryConstraint instanceof EqualsConstraint<?>;
				E entry = ((EqualsConstraint<E>)entryConstraint).getKey();
				IEntrySet<E> index0 = this.map.get(key);
				return index0 != null && index0.contains(entry);
			}
		}
	}
	
	public boolean containsKey(K key) {
		return this.map.containsKey(key);
	}
	
	public void deIndex(K key1, E entry) {
		IEntrySet<E> index0 = this.map.get(key1);
		if(index0 != null) {
			index0.deIndex(entry);
			if(index0.isEmpty()) {
				this.map.remove(key1);
			}
		}
	}
	
	public void index(K key1, E entry) {
		IEntrySet<E> index0 = this.map.get(key1);
		if(index0 == null) {
			index0 = this.entrySetFactory.createInstance();
			this.map.put(key1, index0);
		}
		index0.index(entry);
	}
	
	public boolean isEmpty() {
		return this.map.isEmpty();
	}
	
	public Iterator<KeyEntryTuple<K,E>> tupleIterator(Constraint<K> c1,
	        Constraint<E> entryConstraint) {
		assert c1 != null;
		assert entryConstraint != null;
		
		if(c1.isStar()) {
			Iterator<Map.Entry<K,IEntrySet<E>>> entryIt = this.map.entrySet().iterator();
			// cascade to tuples
			Iterator<KeyEntryTuple<K,E>> cascaded = new CascadingMapEntry_K_EntrySet_Iterator(
			        entryIt);
			if(entryConstraint.isStar()) {
				return cascaded;
			} else {
				// filter entries
				Iterator<KeyEntryTuple<K,E>> filtered = new GenericKeyEntryTupleConstraintFilteringIterator<KeyEntryTuple<K,E>,E>(
				        cascaded, entryConstraint);
				return filtered;
			}
		} else if(c1 instanceof EqualsConstraint<?>) {
			EqualsConstraint<K> keyConstraint = (EqualsConstraint<K>)c1;
			K key = keyConstraint.getKey();
			IEntrySet<E> index0 = this.map.get(key);
			if(index0 == null) {
				return new NoneIterator<KeyEntryTuple<K,E>>();
			} else {
				return new RememberKeyIterator(key, index0.constraintIterator(entryConstraint));
			}
		} else {
			throw new AssertionError("unknown constraint type " + c1.getClass());
		}
	}
	
	public static class LocalDiffImpl<K, E> implements IMapSetDiff<K,E> {
		
		protected MapSetIndex<K,E> added;
		protected MapSetIndex<K,E> removed;
		
		public IMapSetIndex<K,E> getAdded() {
			return this.added;
		}
		
		public IMapSetIndex<K,E> getRemoved() {
			return this.removed;
		}
		
		public LocalDiffImpl(Factory<IEntrySet<E>> entrySetFactory) {
			this.added = new MapSetIndex<K,E>(entrySetFactory);
			this.removed = new MapSetIndex<K,E>(entrySetFactory);
		}
		
	}
	
	public static class DiffImpl<K, E> implements IMapSetDiff<K,E> {
		
		protected IMapSetIndex<K,E> added;
		protected IMapSetIndex<K,E> removed;
		
		public IMapSetIndex<K,E> getAdded() {
			return this.added;
		}
		
		public IMapSetIndex<K,E> getRemoved() {
			return this.removed;
		}
		
	}
	
	public IMapSetDiff<K,E> computeDiff(IMapSetIndex<K,E> otherFuture) {
		if(otherFuture instanceof MapSetIndex<?,?>) {
			return computeDiff_MapSetIndex((MapSetIndex<K,E>)otherFuture);
		} // else:
		IMapSetDiff<K,E> twistedDiff = otherFuture.computeDiff(this);
		DiffImpl<K,E> diff = new DiffImpl<K,E>();
		diff.added = twistedDiff.getRemoved();
		diff.removed = twistedDiff.getAdded();
		return diff;
	}
	
	private IMapSetDiff<K,E> computeDiff_MapSetIndex(MapSetIndex<K,E> otherIndex) {
		LocalDiffImpl<K,E> diff = new LocalDiffImpl<K,E>(this.entrySetFactory);
		
		for(Entry<K,IEntrySet<E>> thisEntry : this.map.entrySet()) {
			K key = thisEntry.getKey();
			IEntrySet<E> otherValue = otherIndex.map.get(key);
			if(otherValue != null) {
				// same (key,*) entry, compare sets
				IEntrySetDiff<E> setDiff = thisEntry.getValue().computeDiff(otherValue);
				if(!setDiff.getAdded().isEmpty()) {
					diff.added.map.put(key, setDiff.getAdded());
				}
				if(!setDiff.getRemoved().isEmpty()) {
					diff.removed.map.put(key, setDiff.getRemoved());
				}
			} else {
				// whole set (key,*) missing in other => removed
				diff.removed.map.put(key, thisEntry.getValue());
			}
		}
		
		// compare other to this
		for(Entry<K,IEntrySet<E>> otherEntry : otherIndex.map.entrySet()) {
			K key = otherEntry.getKey();
			if(!this.map.containsKey(key)) {
				// other has it, this does not => added
				diff.added.map.put(key, otherEntry.getValue());
			}
			// we treated the case of same key in the loop above
		}
		
		return diff;
	}
	
	@Override
	public String toString() {
		return this.map.toString();
	}
	
	public Iterator<K> keyIterator() {
		return this.map.keySet().iterator();
	}
	
}
