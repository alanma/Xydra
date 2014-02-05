package org.xydra.oo.runtime.shared;

import java.util.Collection;
import java.util.Iterator;
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
    
    /**
     * @param xo
     * @param fieldId
     * @param componentTransformer @NeverNull
     */
    public ListProxy(XWritableObject xo, XId fieldId,
            CollectionProxy.IComponentTransformer<X,T,J,C> componentTransformer) {
        super(xo, fieldId, componentTransformer);
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
        Iterator<? extends C> it = c.iterator();
        boolean changed = false;
        while(it.hasNext()) {
            C cItem = it.next();
            changed |= super.add(cItem);
        }
        return changed;
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
