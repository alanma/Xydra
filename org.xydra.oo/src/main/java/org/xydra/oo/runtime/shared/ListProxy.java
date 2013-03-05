package org.xydra.oo.runtime.shared;

import java.util.Collection;
import java.util.List;
import java.util.ListIterator;

import org.xydra.annotations.RunsInGWT;
import org.xydra.base.XId;
import org.xydra.base.rmof.XWritableObject;
import org.xydra.base.value.XCollectionValue;


/**
 * A typed, modifiable Xydra-backed List
 * 
 * @author xamde
 * 
 * @param <X>
 * @param <T>
 * @param <J>
 * @param <C>
 */
@RunsInGWT(true)
public class ListProxy<X extends XCollectionValue<T>, T, J, C> extends CollectionProxy<X,T,J,C>
        implements List<C> {
    
    public ListProxy(XWritableObject xo, XId fieldId, CollectionProxy.ITransformer<X,T,J,C> t) {
        super(xo, fieldId, t);
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
    public boolean containsAll(Collection<?> c) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
    
    @Override
    public boolean addAll(Collection<? extends C> c) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
    
    @Override
    public boolean addAll(int index, Collection<? extends C> c) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
    
    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
    
    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
    
    @Override
    public void clear() {
        throw new UnsupportedOperationException("Not implemented yet");
    }
    
    @Override
    public C get(int index) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
    
    @Override
    public C set(int index, C element) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
    
    @Override
    public void add(int index, C element) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
    
    @Override
    public C remove(int index) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
    
    @Override
    public int indexOf(Object o) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
    
    @Override
    public int lastIndexOf(Object o) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
    
    @Override
    public ListIterator<C> listIterator() {
        throw new UnsupportedOperationException("Not implemented yet");
    }
    
    @Override
    public ListIterator<C> listIterator(int index) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
    
    @Override
    public List<C> subList(int fromIndex, int toIndex) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
    
}