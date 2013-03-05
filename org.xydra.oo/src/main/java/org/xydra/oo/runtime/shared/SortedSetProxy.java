package org.xydra.oo.runtime.shared;

import java.util.Collection;
import java.util.Comparator;
import java.util.SortedSet;

import org.xydra.annotations.RunsInGWT;
import org.xydra.base.XId;
import org.xydra.base.rmof.XWritableObject;
import org.xydra.base.value.XCollectionValue;


/**
 * A typed, modifiable Xydra-backed SortedSet
 * 
 * @author xamde
 * 
 * @param <X>
 * @param <T>
 * @param <J>
 * @param <C>
 */
@RunsInGWT(true)
public class SortedSetProxy<X extends XCollectionValue<T>, T, J, C> extends
        CollectionProxy<X,T,J,C> implements SortedSet<C> {
    
    public SortedSetProxy(XWritableObject xo, XId fieldId, CollectionProxy.ITransformer<X,T,J,C> t) {
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
    
    @Override
    public Comparator<? super C> comparator() {
        throw new UnsupportedOperationException("Not implemented yet");
    }
    
    @Override
    public SortedSet<C> subSet(C fromElement, C toElement) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
    
    @Override
    public SortedSet<C> headSet(C toElement) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
    
    @Override
    public SortedSet<C> tailSet(C fromElement) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
    
    @Override
    public C first() {
        throw new UnsupportedOperationException("Not implemented yet");
    }
    
    @Override
    public C last() {
        throw new UnsupportedOperationException("Not implemented yet");
    }
    
}