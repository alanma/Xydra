package org.xydra.oo.runtime.shared;

import java.util.Iterator;

import org.xydra.annotations.RunsInGWT;
import org.xydra.base.XId;
import org.xydra.base.rmof.XWritableField;
import org.xydra.base.rmof.XWritableObject;
import org.xydra.base.value.XCollectionValue;
import org.xydra.index.iterator.NoneIterator;
import org.xydra.index.iterator.TransformingIterator;


/**
 * A runtime proxy object to represent a typed live collection that can be
 * modified
 * 
 * @author xamde
 * 
 * @param <X> xydra type
 * @param <T> any component type
 * @param <J> java type
 * @param <C> java component type
 */
@RunsInGWT(true)
public class CollectionProxy<X extends XCollectionValue<T>, T, J, C> {
    
    /**
     * Transforms Java component types to Xydra component types and vice versa.
     * Can create empty Xydra collections.
     * 
     * @param <X> xydra base type, extends XCollectionValue<T>
     * @param <T> any component type
     * @param <J> java base type
     * @param <C> java component type
     */
    public interface IComponentTransformer<X, T, J, C> {
        
        /**
         * @param anyType
         * @return Java component type
         */
        C toJavaComponent(T anyType);
        
        /**
         * @param javaType
         * @return Xydra component type
         */
        T toXydraComponent(C javaType);
        
        /**
         * @return an empty Xydra collection
         */
        X createCollection();
    }
    
    protected CollectionProxy.IComponentTransformer<X,T,J,C> componentTransformer;
    protected XWritableObject xo;
    protected XId fieldId;
    
    public CollectionProxy(XWritableObject xo, XId fieldId,
            CollectionProxy.IComponentTransformer<X,T,J,C> componentTransformer) {
        this.xo = xo;
        this.fieldId = fieldId;
        this.componentTransformer = componentTransformer;
    }
    
    public int size() {
        XWritableField f = this.xo.getField(this.fieldId);
        if(f == null)
            return 0;
        @SuppressWarnings("unchecked")
		XCollectionValue<T> v = (XCollectionValue<T>)f.getValue();
        if(v == null)
            return 0;
        return v.size();
    }
    
    public boolean isEmpty() {
        XWritableField f = this.xo.getField(this.fieldId);
        if(f == null)
            return true;
        @SuppressWarnings("unchecked")
        XCollectionValue<T> v = (XCollectionValue<T>)f.getValue();
        if(v == null)
            return true;
        return v.isEmpty();
    }
    
    public boolean contains(Object o) {
        XWritableField f = this.xo.getField(this.fieldId);
        if(f == null)
            return false;
        @SuppressWarnings("unchecked")
        XCollectionValue<T> v = (XCollectionValue<T>)f.getValue();
        if(v == null)
            return false;
        @SuppressWarnings("unchecked")
        C j = (C)o;
        T t = this.componentTransformer.toXydraComponent(j);
        return v.contains(t);
    }
    
    public Iterator<C> iterator() {
        XWritableField f = this.xo.getField(this.fieldId);
        if(f == null)
            return NoneIterator.<C>create();
        @SuppressWarnings("unchecked")
        XCollectionValue<T> v = (XCollectionValue<T>)f.getValue();
        if(v == null)
            return NoneIterator.<C>create();
        
        Iterator<T> base = v.iterator();
        return new TransformingIterator<T,C>(base,
                new org.xydra.index.iterator.ITransformer<T,C>() {
                    
                    @Override
                    public C transform(T in) {
                        return CollectionProxy.this.componentTransformer.toJavaComponent(in);
                    }
                    
                });
    }
    
    @SuppressWarnings("unchecked")
    public boolean add(C j) {
        boolean changes = false;
        XWritableField f = this.xo.getField(this.fieldId);
        XCollectionValue<T> xydraCollectionValue;
        if(f == null) {
            changes = true;
            f = this.xo.createField(this.fieldId);
            xydraCollectionValue = this.componentTransformer.createCollection();
        } else {
            xydraCollectionValue = (XCollectionValue<T>)f.getValue();
            if(xydraCollectionValue == null) {
                changes = true;
                xydraCollectionValue = this.componentTransformer.createCollection();
            }
        }
        assert xydraCollectionValue != null;
        T x = this.componentTransformer.toXydraComponent(j);
        xydraCollectionValue = xydraCollectionValue.add(x);
        changes = f.setValue(xydraCollectionValue);
        return changes;
    }
    
    @SuppressWarnings("unchecked")
    public boolean remove(Object o) {
        XWritableField f = this.xo.getField(this.fieldId);
        XCollectionValue<T> v;
        if(f == null) {
            return false;
        } else {
            v = (XCollectionValue<T>)f.getValue();
            if(v == null) {
                return false;
            }
        }
        assert v != null;
        C j = (C)o;
        T x = this.componentTransformer.toXydraComponent(j);
        boolean b = v.contains(x);
        v.remove(x);
        return b;
    }
    
}
