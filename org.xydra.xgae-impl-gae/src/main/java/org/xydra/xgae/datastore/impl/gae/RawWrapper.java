package org.xydra.xgae.datastore.impl.gae;

import java.util.Iterator;

import org.xydra.index.iterator.ITransformer;
import org.xydra.index.iterator.TransformingIterator;
import org.xydra.xgae.datastore.api.SWrapper;


/**
 * Design decision for nulls: Don't wrap nulls
 * 
 * @author xamde
 * 
 * @param <R>
 * @param <S>
 */
public class RawWrapper<R, S extends SWrapper> {
    
    private R raw;
    
    public RawWrapper(R raw) {
        if(raw == null)
            throw new IllegalArgumentException();
        
        this.raw = raw;
    }
    
    public R raw() {
        return this.raw;
    }
    
    private static class UnwrappedIterable<R, S extends SWrapper> implements Iterable<R> {
        
        public UnwrappedIterable(Iterable<S> it) {
            super();
            this.iterable = it;
        }
        
        private Iterable<S> iterable;
        
        @Override
        public Iterator<R> iterator() {
            return new TransformingIterator<S,R>(this.iterable.iterator(), new ITransformer<S,R>() {
                
                @Override
                public R transform(S in) {
                    return (R)in.raw();
                }
            });
        }
        
    }
    
    protected static <R, S extends SWrapper> Iterable<R> _unwrap(Iterable<S> it) {
        return new UnwrappedIterable<R,S>(it);
    }
    
}
