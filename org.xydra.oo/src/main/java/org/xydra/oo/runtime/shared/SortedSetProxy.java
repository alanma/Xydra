package org.xydra.oo.runtime.shared;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.SortedSet;

import org.xydra.annotations.RunsInGWT;
import org.xydra.base.XId;
import org.xydra.base.rmof.XWritableField;
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
    
    public SortedSetProxy(XWritableObject xo, XId fieldId,
            CollectionProxy.IComponentTransformer<X,T,J,C> t) {
        super(xo, fieldId, t);
    }
    
    @Override
    public boolean add(C j) {
        boolean changes = super.add(j);
        if(changes && this.size() > 1) {
            /* IMPROVE more efficient */
            XWritableField f = this.xo.getField(this.fieldId);
            
            XCollectionValue<T> col = (XCollectionValue<T>)f.getValue();
            System.out.println(col);
            List<T> list = new ArrayList<T>();
            list.addAll(Arrays.asList(col.toArray()));
            Collections.sort(list, new Comparator<T>() {
                
                @Override
                public int compare(T o1, T o2) {
                    return o1.toString().compareTo(o2.toString());
                }
            });
            col = this.componentTransformer.createCollection();
            
            for(T element : list) {
                col = col.add(element);
            }
            f.setValue(col);
        }
        
        return changes;
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
