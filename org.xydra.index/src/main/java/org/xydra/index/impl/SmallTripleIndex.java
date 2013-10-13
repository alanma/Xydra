package org.xydra.index.impl;

import java.util.Iterator;

import org.xydra.index.IMapMapSetIndex.IMapMapSetDiff;
import org.xydra.index.ITripleIndex;
import org.xydra.index.query.Constraint;
import org.xydra.index.query.EqualsConstraint;
import org.xydra.index.query.KeyKeyEntryTuple;
import org.xydra.index.query.Wildcard;


/**
 * An implementation that uses no indexes. Slow, but small memory footprint.
 * 
 * @author voelkel
 * 
 * @param <K> key type 1
 * @param <L> key type 2
 * @param <M> key type 3
 */
public class SmallTripleIndex<K, L, M> implements ITripleIndex<K,L,M> {
    
    private static final long serialVersionUID = 4825573034123083085L;
    
    /**
     * s > p > o
     */
    protected MapMapSetIndex<K,L,M> index_s_p_o;
    
    public SmallTripleIndex() {
        this.index_s_p_o = new MapMapSetIndex<K,L,M>(new FastEntrySetFactory<M>());
    }
    
    @Override
    public void clear() {
        this.index_s_p_o.clear();
    }
    
    @Override
    public boolean contains(Constraint<K> c1, Constraint<L> c2, Constraint<M> c3) {
        return getTriples(c1, c2, c3).hasNext();
    }
    
    /**
     * @param key1 @NeverNull
     * @param key2 @NeverNull
     * @param key3 @NeverNull
     * @return true if triple index contains the given triple
     */
    public boolean contains(K key1, L key2, M key3) {
        Constraint<K> c1 = new EqualsConstraint<K>(key1);
        Constraint<L> c2 = new EqualsConstraint<L>(key2);
        Constraint<M> c3 = new EqualsConstraint<M>(key3);
        return this.contains(c1, c2, c3);
    }
    
    @Override
    public void deIndex(K s, L p, M o) {
        this.index_s_p_o.deIndex(s, p, o);
    }
    
    @Override
    public void dump() {
        System.out.println("Dumping s-p-o-index (there are others)");
        Iterator<KeyKeyEntryTuple<K,L,M>> it = this.index_s_p_o.tupleIterator(new Wildcard<K>(),
                new Wildcard<L>(), new Wildcard<M>());
        while(it.hasNext()) {
            KeyKeyEntryTuple<K,L,M> t = it.next();
            System.out.println(t.getKey1() + " - " + t.getKey2() + " - " + t.getEntry());
        }
    }
    
    /**
     * @param c1 constraint for component 1 of triple (subject)
     * @param c2 constraint for component 2 of triple (property)
     * @param c3 constraint for component 3 of triple (object)
     * @return an {@link Iterator} over all {@link KeyKeyEntryTuple} that match
     *         the given constraints
     */
    @Override
    public Iterator<KeyKeyEntryTuple<K,L,M>> getTriples(Constraint<K> c1, Constraint<L> c2,
            Constraint<M> c3) {
        if(c1 == null)
            throw new IllegalArgumentException("c1 was null");
        if(c2 == null)
            throw new IllegalArgumentException("c2 was null");
        if(c3 == null)
            throw new IllegalArgumentException("c3 was null");
        Iterator<KeyKeyEntryTuple<K,L,M>> tupleIterator = this.index_s_p_o
                .tupleIterator(c1, c2, c3);
        return tupleIterator;
    }
    
    @Override
    public void index(K s, L p, M o) {
        this.index_s_p_o.index(s, p, o);
    }
    
    @Override
    public IMapMapSetDiff<K,L,M> computeDiff(ITripleIndex<K,L,M> other) {
        SmallTripleIndex<K,L,M> otherIndex = (SmallTripleIndex<K,L,M>)other;
        IMapMapSetDiff<K,L,M> spoDiff = this.index_s_p_o.computeDiff(otherIndex.index_s_p_o);
        return spoDiff;
    }
    
    @Override
    public boolean isEmpty() {
        return this.index_s_p_o.isEmpty();
    }
    
    public Iterator<K> keyIterator() {
        return this.index_s_p_o.keyIterator();
    }
    
}
