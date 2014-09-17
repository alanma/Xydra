package org.xydra.index.impl;

import org.xydra.index.Factory;
import org.xydra.index.IEntrySet;
import org.xydra.index.IEntrySet.IEntrySetDiff;
import org.xydra.index.IMapSetIndex;
import org.xydra.index.iterator.AbstractCascadedIterator;
import org.xydra.index.iterator.Iterators;
import org.xydra.index.iterator.NoneIterator;
import org.xydra.index.query.Constraint;
import org.xydra.index.query.EqualsConstraint;
import org.xydra.index.query.GenericKeyEntryTupleConstraintFilteringIterator;
import org.xydra.index.query.KeyEntryTuple;
import org.xydra.index.query.Wildcard;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


public class MapSetIndex<K, E> implements IMapSetIndex<K,E> {
    
    private static Logger log;
    
    private static void ensureLogger() {
        if(log == null) {
            log = LoggerFactory.getLogger(MapSetIndex.class);
        }
    }
    
    /* needed for tupleIterator() */
    private class AdaptMapEntryToTupleIterator implements Iterator<KeyEntryTuple<K,E>> {
        
        private Entry<K,IEntrySet<E>> base;
        private Iterator<E> it;
        
        public AdaptMapEntryToTupleIterator(Entry<K,IEntrySet<E>> base) {
            this.base = base;
            this.it = base.getValue().iterator();
        }
        
        @Override
        public boolean hasNext() {
            return this.it.hasNext();
        }
        
        @Override
        public KeyEntryTuple<K,E> next() {
            return new KeyEntryTuple<K,E>(this.base.getKey(), this.it.next());
        }
        
        @Override
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
    
    public static class DiffImpl<K, E> implements IMapSetDiff<K,E> {
        
        protected IMapSetIndex<K,E> added;
        protected IMapSetIndex<K,E> removed;
        
        @Override
        public IMapSetIndex<K,E> getAdded() {
            return this.added;
        }
        
        @Override
        public IMapSetIndex<K,E> getRemoved() {
            return this.removed;
        }
        
    }
    
    public static class LocalDiffImpl<K, E> implements IMapSetDiff<K,E> {
        
        protected MapSetIndex<K,E> added;
        protected MapSetIndex<K,E> removed;
        
        public LocalDiffImpl(Factory<IEntrySet<E>> entrySetFactory) {
            this.added = new MapSetIndex<K,E>(entrySetFactory);
            this.removed = new MapSetIndex<K,E>(entrySetFactory);
        }
        
        @Override
        public IMapSetIndex<K,E> getAdded() {
            return this.added;
        }
        
        @Override
        public IMapSetIndex<K,E> getRemoved() {
            return this.removed;
        }
        
    }
    
    /* needed for tupleIterator() */
    private class AddKeyIterator implements Iterator<KeyEntryTuple<K,E>> {
        
        private Iterator<E> base;
        private K key;
        
        public AddKeyIterator(K key, Iterator<E> base) {
            this.base = base;
            this.key = key;
        }
        
        @Override
        public boolean hasNext() {
            return this.base.hasNext();
        }
        
        @Override
        public KeyEntryTuple<K,E> next() {
            E entry = this.base.next();
            return new KeyEntryTuple<K,E>(this.key, entry);
        }
        
        @Override
        public void remove() {
            this.base.remove();
        }
        
    }
    
    private static final long serialVersionUID = 8275298314930537914L;
    
    /**
     * @return an impl that uses more memory and is fast
     */
    public static <K, E> MapSetIndex<K,E> createWithFastEntrySets() {
        return new MapSetIndex<K,E>(new FastEntrySetFactory<E>());
    }
    
    /**
     * @return an impl that uses less memory and is slower
     */
    public static <K, E> MapSetIndex<K,E> createWithSmallEntrySets() {
        return new MapSetIndex<K,E>(new SmallEntrySetFactory<E>());
    }
    
    private Factory<IEntrySet<E>> entrySetFactory;
    
    private Map<K,IEntrySet<E>> map;
    
    public MapSetIndex(Factory<IEntrySet<E>> entrySetFactory) {
        this(entrySetFactory, false);
    }
    
    /**
     * @param entrySetFactory
     * @param concurrent
     */
    public MapSetIndex(Factory<IEntrySet<E>> entrySetFactory, boolean concurrent) {
        if(concurrent) {
            this.map = new ConcurrentHashMap<K,IEntrySet<E>>(4);
        } else {
            this.map = new HashMap<K,IEntrySet<E>>(4);
        }
        this.entrySetFactory = entrySetFactory;
    }
    
    @Override
    public void clear() {
        this.map.clear();
    }
    
    @Override
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
    public Iterator<E> constraintIterator(Constraint<K> c1) {
        if(c1.isStar()) {
            return new CascadingEntrySetIterator(this.map.values().iterator());
        } else if(c1 instanceof EqualsConstraint<?>) {
            EqualsConstraint<K> keyConstraint = (EqualsConstraint<K>)c1;
            K key = keyConstraint.getKey();
            return valueIterator(key);
        } else {
            throw new AssertionError("unknown constraint type " + c1.getClass());
        }
    }
    
    public Iterator<E> valueIterator(K key) {
        IEntrySet<E> index0 = this.map.get(key);
        return index0 == null ? NoneIterator.<E>create() : index0.iterator();
    }
    
    @Override
    public boolean contains(Constraint<K> c1, Constraint<E> entryConstraint) {
        // IMPROVE can this be sped up?
        
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
                return contains(key, entry);
            }
        }
    }
    
    @Override
    public boolean contains(K key, E entry) {
        IEntrySet<E> index0 = this.map.get(key);
        return index0 != null && index0.contains(entry);
    }
    
    @Override
    public boolean containsKey(K key) {
        return this.map.containsKey(key);
    }
    
    @Override
    public void deIndex(K key) {
        this.map.remove(key);
    }
    
    @Override
    public boolean deIndex(K key1, E entry) {
        IEntrySet<E> index0 = this.map.get(key1);
        if(index0 == null) {
            return false;
        } else {
            boolean removed = index0.deIndex(entry);
            if(index0.isEmpty()) {
                this.map.remove(key1);
            }
            return removed;
        }
    }
    
    /**
     * Dump the contents to Xydra Logging as log.info(...)
     */
    public void dump() {
        ensureLogger();
        Iterator<KeyEntryTuple<K,E>> it = tupleIterator(new Wildcard<K>(), new Wildcard<E>());
        
        List<KeyEntryTuple<K,E>> list = Iterators.firstNtoList(it, 1000);
        if(it.hasNext()) {
            // iterator has over 1000 elements, ignore sort order
            for(KeyEntryTuple<K,E> t : list) {
                dumpTuple(t);
            }
            while(it.hasNext()) {
                KeyEntryTuple<K,E> t = it.next();
                dumpTuple(t);
            }
        } else {
            Collections.sort(list, new Comparator<KeyEntryTuple<K,E>>() {
                
                @SuppressWarnings({ "unchecked", "rawtypes" })
                @Override
                public int compare(KeyEntryTuple<K,E> o1, KeyEntryTuple<K,E> o2) {
                    
                    K k1 = o1.getKey();
                    K k2 = o2.getKey();
                    
                    if(k1 instanceof Comparable) {
                        return ((Comparable)k1).compareTo(k2);
                    } else {
                        return k1.toString().compareTo(k2.toString());
                    }
                }
            });
            for(KeyEntryTuple<K,E> t : list) {
                dumpTuple(t);
            }
        }
        
    }
    
    private void dumpTuple(KeyEntryTuple<K,E> t) {
        log.info("(" + t.getFirst() + ", " + t.getSecond() + ")");
    }
    
    public Set<Entry<K,IEntrySet<E>>> getEntries() {
        return this.map.entrySet();
    }
    
    @Override
    public boolean index(K key1, E entry) {
        IEntrySet<E> index0 = this.map.get(key1);
        if(index0 == null) {
            index0 = this.entrySetFactory.createInstance();
            this.map.put(key1, index0);
        }
        return index0.index(entry);
    }
    
    @Override
    public boolean isEmpty() {
        return this.map.isEmpty();
    }
    
    @Override
    public Iterator<K> keyIterator() {
        return this.map.keySet().iterator();
    }
    
    public Set<K> keySet() {
        return this.map.keySet();
    }
    
    @Override
    public IEntrySet<E> lookup(K key) {
        return this.map.get(key);
    }
    
    @Override
    public String toString() {
        return this.map.toString();
    }
    
    @Override
    public Iterator<KeyEntryTuple<K,E>> tupleIterator(Constraint<K> c1,
            Constraint<E> entryConstraint) {
        assert c1 != null;
        assert entryConstraint != null;
        
        if(c1.isStar()) {
            Iterator<Map.Entry<K,IEntrySet<E>>> entryIt = this.map.entrySet().iterator();
            if(!entryIt.hasNext()) {
                return NoneIterator.create();
            }
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
                return NoneIterator.<KeyEntryTuple<K,E>>create();
            } else {
                return new AddKeyIterator(key, index0.constraintIterator(entryConstraint));
            }
        } else {
            throw new AssertionError("unknown constraint type " + c1.getClass());
        }
    }
    
}
