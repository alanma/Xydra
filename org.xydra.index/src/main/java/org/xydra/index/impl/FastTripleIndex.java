package org.xydra.index.impl;

import java.util.Iterator;

import org.xydra.index.IEntrySet;
import org.xydra.index.IMapSetIndex;
import org.xydra.index.ITripleIndex;
import org.xydra.index.iterator.NoneIterator;
import org.xydra.index.iterator.TransformingIterator;
import org.xydra.index.iterator.TransformingIterator.Transformer;
import org.xydra.index.query.Constraint;
import org.xydra.index.query.KeyEntryTuple;
import org.xydra.index.query.KeyKeyEntryTuple;
import org.xydra.index.query.Wildcard;


/**
 * An implementation that uses several indexes internally and chooses among
 * them.
 * 
 * Fast implementation for {@link #contains(Constraint, Constraint, Constraint)}
 * and fast implementation for
 * {@link #getTriples(Constraint, Constraint, Constraint)}.
 * 
 * @author voelkel
 * 
 * @param <K> key type 1 (s)
 * @param <L> key type 2 (p)
 * @param <M> key type 3 (o)
 */
public class FastTripleIndex<K, L, M> extends SmallTripleIndex<K,L,M> implements
        ITripleIndex<K,L,M> {
    
    private static final long serialVersionUID = 4825573034123083085L;
    
    private final Constraint<K> STAR_S = new Wildcard<K>();
    private final Constraint<L> STAR_P = new Wildcard<L>();
    private final Constraint<M> STAR_O = new Wildcard<M>();
    
    /**
     * o -> s -> p
     */
    private transient MapMapSetIndex<M,K,L> index_o_s_p;
    
    /**
     * p -> o -> s
     */
    private transient MapMapSetIndex<L,M,K> index_p_o_s;
    
    private Transformer<KeyKeyEntryTuple<L,M,K>,KeyKeyEntryTuple<K,L,M>> transformerPOS = new Transformer<KeyKeyEntryTuple<L,M,K>,KeyKeyEntryTuple<K,L,M>>() {
        
        @Override
        public KeyKeyEntryTuple<K,L,M> transform(KeyKeyEntryTuple<L,M,K> in) {
            return new KeyKeyEntryTuple<K,L,M>(in.getEntry(), in.getKey1(), in.getKey2());
        }
    };
    
    private Transformer<KeyKeyEntryTuple<M,K,L>,KeyKeyEntryTuple<K,L,M>> transformerOSP = new Transformer<KeyKeyEntryTuple<M,K,L>,KeyKeyEntryTuple<K,L,M>>() {
        
        @Override
        public KeyKeyEntryTuple<K,L,M> transform(KeyKeyEntryTuple<M,K,L> in) {
            return new KeyKeyEntryTuple<K,L,M>(in.getKey2(), in.getEntry(), in.getKey1());
        }
    };
    
    public FastTripleIndex() {
        super();
        this.index_o_s_p = new MapMapSetIndex<M,K,L>(new FastEntrySetFactory<L>());
        this.index_p_o_s = new MapMapSetIndex<L,M,K>(new FastEntrySetFactory<K>());
    }
    
    @Override
    public void clear() {
        super.clear();
        this.index_o_s_p.clear();
        this.index_p_o_s.clear();
    }
    
    @Override
    public boolean contains(Constraint<K> c1, Constraint<L> c2, Constraint<M> c3) {
        // deal with the eight patterns
        if(
        // spo -> s_p_o
        (!c1.isStar() && !c2.isStar() && !c3.isStar()) ||
        // sp* -> s_p_o
                (!c1.isStar() && !c2.isStar() && c3.isStar()) ||
                // s** -> s_p_o
                (!c1.isStar() && c2.isStar() && c3.isStar()) ||
                // *** -> s_p_o
                (c1.isStar() && c2.isStar() && c3.isStar())
        
        ) {
            return this.index_s_p_o.contains(c1, c2, c3);
        } else if(
        // *po -> p_o
        (c1.isStar() && !c2.isStar() && !c3.isStar()) ||
        // *p* -> p_o
                (c1.isStar() && !c2.isStar() && c3.isStar())
        
        ) {
            return this.index_p_o_s.contains(c2, c3, this.STAR_S);
        } else if(
        // s*o -> o_s
        (!c1.isStar() && c2.isStar() && !c3.isStar()) ||
        // **o -> o_s
                (c1.isStar() && c2.isStar() && !c3.isStar())
        
        ) {
            return this.index_o_s_p.contains(c3, c1, this.STAR_P);
        }
        
        throw new AssertionError("one of the patterns should have matched");
    }
    
    /**
     * @param c1 @CanBeNull to denote wildcard
     * @param c2 @CanBeNull to denote wildcard
     * @param c3 @CanBeNull to denote wildcard
     */
    @Override
    public boolean contains(K c1, L c2, M c3) {
        /* avoid creating Constraint and Wildcard objects */
        
        // deal with the eight patterns
        if(
        // spo -> s_p_o
        (c1 != null && c2 != null && c3 != null) ||
        // sp* -> s_p_o
                (c1 != null && c2 != null && c3 == null) ||
                // s** -> s_p_o
                (c1 != null && c2 == null && c3 == null) ||
                // *** -> s_p_o
                (c1 == null && c2 == null && c3 == null)
        
        ) {
            return this.index_s_p_o.contains(c1, c2, c3);
        } else if(
        // *po -> p_o
        (c1 == null && c2 != null && c3 != null) ||
        // *p* -> p_o
                (c1 == null && c2 != null && c3 == null)
        
        ) {
            return this.index_p_o_s.contains(c2, c3, null);
        } else if(
        // s*o -> o_s
        (c1 != null && c2 == null && c3 != null) ||
        // **o -> o_s
                (c1 == null && c2 == null && c3 != null)
        
        ) {
            return this.index_o_s_p.contains(c3, c1, null);
        }
        
        throw new AssertionError("one of the patterns should have matched");
    }
    
    /**
     * @param c1 @CanBeNull to denote wildcard
     * @param c2 @CanBeNull to denote wildcard
     * @param c3 @CanBeNull to denote wildcard
     * @return an iterator over all matching triples
     */
    public Iterator<KeyKeyEntryTuple<K,L,M>> getTriples(K c1, L c2, M c3) {
        /* avoid creating Constraint and Wildcard objects */
        
        // deal with the eight patterns
        if(
        // spo -> s_p_o
        (c1 != null && c2 != null && c3 != null) ||
        // sp* -> s_p_o
                (c1 != null && c2 != null && c3 == null) ||
                // s** -> s_p_o
                (c1 != null && c2 == null && c3 == null) ||
                // *** -> s_p_o
                (c1 == null && c2 == null && c3 == null)
        
        ) {
            return this.index_s_p_o.tupleIterator(c1, c2, c3);
        } else if(
        // *po -> p_o
        (c1 == null && c2 != null && c3 != null) ||
        // *p* -> p_o
                (c1 == null && c2 != null && c3 == null)
        
        ) {
            Iterator<KeyKeyEntryTuple<L,M,K>> baseIt = this.index_p_o_s.tupleIterator(c2, c3, null);
            return new TransformingIterator<KeyKeyEntryTuple<L,M,K>,KeyKeyEntryTuple<K,L,M>>(
                    baseIt, this.transformerPOS);
            
        } else if(
        // s*o -> o_s
        (c1 != null && c2 == null && c3 != null) ||
        // **o -> o_s
                (c1 == null && c2 == null && c3 != null)
        
        ) {
            Iterator<KeyKeyEntryTuple<M,K,L>> baseIt = this.index_o_s_p.tupleIterator(c3, c1, null);
            return new TransformingIterator<KeyKeyEntryTuple<M,K,L>,KeyKeyEntryTuple<K,L,M>>(
                    baseIt, this.transformerOSP);
        }
        
        throw new AssertionError("one of the patterns should have matched");
    }
    
    /**
     * @param s @NeverNull
     * @param p @NeverNull
     * @return an iterator over all objects matching (s,p,*)
     */
    public Iterator<M> getObjects_SPX(K s, L p) {
        assert s != null;
        assert p != null;
        IMapSetIndex<L,M> index_s_Px_Ox = this.index_s_p_o.getMapEntry(s);
        if(index_s_Px_Ox == null)
            return new NoneIterator<M>();
        IEntrySet<M> index_s_p_Ox = index_s_Px_Ox.lookup(p);
        if(index_s_p_Ox == null)
            return new NoneIterator<M>();
        return index_s_p_Ox.iterator();
    }
    
    /**
     * @param s @NeverNull
     * @return an iterator over (p,o)-tuples for the given s
     */
    public Iterator<KeyEntryTuple<L,M>> getTupels_SXX(K s) {
        assert s != null;
        IMapSetIndex<L,M> index_s_Px_Ox = this.index_s_p_o.getMapEntry(s);
        if(index_s_Px_Ox == null)
            return new NoneIterator<KeyEntryTuple<L,M>>();
        return index_s_Px_Ox.tupleIterator(this.STAR_P, this.STAR_O);
    }
    
    /**
     * @param p @NeverNull
     * @param o @NeverNull
     * @return an iterator over all subjects matching (*,p,o)
     */
    public Iterator<K> getSubjects_XPO(L p, M o) {
        assert p != null;
        assert o != null;
        IMapSetIndex<M,K> index_p_Ox_Sx = this.index_p_o_s.getMapEntry(p);
        if(index_p_Ox_Sx == null)
            return new NoneIterator<K>();
        IEntrySet<K> index_p_o_Sx = index_p_Ox_Sx.lookup(o);
        if(index_p_o_Sx == null)
            return new NoneIterator<K>();
        return index_p_o_Sx.iterator();
    }
    
    /**
     * @param s @NeverNull
     * @param o @NeverNull
     * @return an iterator over all predicates occuring in triples (s,*,o)
     */
    public Iterator<L> getPredicates_SXO(K s, M o) {
        assert s != null;
        assert o != null;
        IMapSetIndex<K,L> index_o_Sx_Px = this.index_o_s_p.getMapEntry(o);
        if(index_o_Sx_Px == null)
            return new NoneIterator<L>();
        IEntrySet<L> index_o_s_Px = index_o_Sx_Px.lookup(s);
        if(index_o_s_Px == null)
            return new NoneIterator<L>();
        return index_o_s_Px.iterator();
    }
    
    /**
     * @param o @NeverNull
     * @return an iterator over (s,p)-tuples for the given o
     */
    public Iterator<KeyEntryTuple<K,L>> getTupels_XXO(M o) {
        assert o != null;
        IMapSetIndex<K,L> index_o_Sx_Px = this.index_o_s_p.getMapEntry(o);
        if(index_o_Sx_Px == null)
            return new NoneIterator<KeyEntryTuple<K,L>>();
        return index_o_Sx_Px.tupleIterator(this.STAR_S, this.STAR_P);
    }
    
    /**
     * @param p @NeverNull
     * @return an iterator over (s,o)-tuples for the given p
     */
    public Iterator<KeyEntryTuple<M,K>> getTupels_XPX(L p) {
        assert p != null;
        IMapSetIndex<M,K> index_p_Ox_Sx = this.index_p_o_s.getMapEntry(p);
        if(index_p_Ox_Sx == null)
            return new NoneIterator<KeyEntryTuple<M,K>>();
        return index_p_Ox_Sx.tupleIterator(this.STAR_O, this.STAR_S);
    }
    
    @Override
    public void deIndex(K s, L p, M o) {
        super.deIndex(s, p, o);
        this.index_o_s_p.deIndex(o, s, p);
        this.index_p_o_s.deIndex(p, o, s);
    }
    
    @Override
    public void index(K s, L p, M o) {
        super.index(s, p, o);
        transientIndex(s, p, o);
    }
    
    private void transientIndex(K s, L p, M o) {
        this.index_o_s_p.index(o, s, p);
        this.index_p_o_s.index(p, o, s);
    }
    
    /**
     * This method should be called after deserialisation to rebuild the
     * transient indexes. Deserialisation in GWT and plain Java is quite
     * different and incompatible, so this is not called automatically. For
     * plain Java see
     * http://java.sun.com/developer/technicalArticles/Programming
     * /serialization/
     */
    public void rebuildAfterDeserialize() {
        Iterator<KeyKeyEntryTuple<K,L,M>> it = this.index_s_p_o.tupleIterator(new Wildcard<K>(),
                new Wildcard<L>(), new Wildcard<M>());
        while(it.hasNext()) {
            KeyKeyEntryTuple<K,L,M> t = it.next();
            transientIndex(t.getKey1(), t.getKey2(), t.getEntry());
        }
    }
    
}
