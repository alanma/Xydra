package org.xydra.oo.runtime.shared;

import java.util.Collection;
import java.util.Set;

import org.xydra.base.XID;
import org.xydra.base.rmof.XWritableObject;
import org.xydra.base.value.XCollectionValue;
import org.xydra.base.value.XValue;


public class SetProxy<X extends XCollectionValue<T>, T extends XValue, J, C> extends
        CollectionProxy<X,T,J,C> implements Set<C> {
    
    public SetProxy(XWritableObject xo, XID fieldId, CollectionProxy.ITransformer<X,T,J,C> t) {
        super(xo, fieldId, t);
    }
    
    @Override
    public boolean containsAll(Collection<?> c) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
    
    @Override
    public boolean addAll(Collection<? extends C> c) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
    
    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
    
    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
    
    @Override
    public void clear() {
        throw new UnsupportedOperationException("Not implemented yet");
    }
    
    @Override
    public Object[] toArray() {
        throw new UnsupportedOperationException("Not implemented yet");
    }
    
    @Override
    public <R> R[] toArray(R[] a) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
    
}
