package org.xydra.oo.runtime.shared;

import java.util.Iterator;

import org.xydra.base.XID;
import org.xydra.base.rmof.XWritableField;
import org.xydra.base.rmof.XWritableObject;
import org.xydra.base.value.XCollectionValue;
import org.xydra.base.value.XValue;
import org.xydra.index.iterator.NoneIterator;
import org.xydra.index.iterator.TransformingIterator;
import org.xydra.index.iterator.TransformingIterator.Transformer;


/**
 * @author xamde
 * 
 * @param <X> xydra type
 * @param <T> xydra component type
 * @param <J> java type
 * @param <C> java component type
 */
public class CollectionProxy<X extends XCollectionValue<T>, T extends XValue, J, C> {
    
    public interface ITransformer<X extends XCollectionValue<T>, T extends XValue, J, C> {
        C toJavaComponent(T xydraType);
        
        T toXydraComponent(C javaType);
        
        X createCollection();
    }
    
    protected CollectionProxy.ITransformer<X,T,J,C> t;
    protected XWritableObject xo;
    protected XID fieldId;
    
    public CollectionProxy(XWritableObject xo, XID fieldId, CollectionProxy.ITransformer<X,T,J,C> t) {
        this.xo = xo;
        this.fieldId = fieldId;
        this.t = t;
    }
    
    public int size() {
        XWritableField f = this.xo.getField(this.fieldId);
        if(f == null)
            return 0;
        XCollectionValue<T> v = (XCollectionValue<T>)f.getValue();
        if(v == null)
            return 0;
        return v.size();
    }
    
    public boolean isEmpty() {
        XWritableField f = this.xo.getField(this.fieldId);
        if(f == null)
            return true;
        XCollectionValue<T> v = (XCollectionValue<T>)f.getValue();
        if(v == null)
            return true;
        return v.isEmpty();
    }
    
    public boolean contains(Object o) {
        XWritableField f = this.xo.getField(this.fieldId);
        if(f == null)
            return false;
        XCollectionValue<T> v = (XCollectionValue<T>)f.getValue();
        if(v == null)
            return false;
        @SuppressWarnings("unchecked")
        C j = (C)o;
        T t = this.t.toXydraComponent(j);
        return v.contains(t);
    }
    
    public Iterator<C> iterator() {
        XWritableField f = this.xo.getField(this.fieldId);
        if(f == null)
            return new NoneIterator<C>();
        XCollectionValue<T> v = (XCollectionValue<T>)f.getValue();
        if(v == null)
            return new NoneIterator<C>();
        
        Iterator<T> base = v.iterator();
        return new TransformingIterator<T,C>(base, new Transformer<T,C>() {
            
            @Override
            public C transform(T in) {
                return CollectionProxy.this.t.toJavaComponent(in);
            }
            
        });
    }
    
    public boolean add(C j) {
        boolean changes = false;
        XWritableField f = this.xo.getField(this.fieldId);
        XCollectionValue<T> xydraCollectionValue;
        if(f == null) {
            changes = true;
            f = this.xo.createField(this.fieldId);
            xydraCollectionValue = this.t.createCollection();
        } else {
            xydraCollectionValue = (XCollectionValue<T>)f.getValue();
            if(xydraCollectionValue == null) {
                changes = true;
                xydraCollectionValue = this.t.createCollection();
            }
        }
        assert xydraCollectionValue != null;
        T x = this.t.toXydraComponent(j);
        xydraCollectionValue = xydraCollectionValue.add(x);
        f.setValue(xydraCollectionValue);
        return changes;
    }
    
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
        @SuppressWarnings("unchecked")
        C j = (C)o;
        T x = this.t.toXydraComponent(j);
        boolean b = v.contains(x);
        v.remove(x);
        return b;
    }
    
}
