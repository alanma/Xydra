package org.xydra.concurrent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.xydra.annotations.RunsInGWT;


@RunsInGWT(true)
public class ConcurrentHashSet<E> implements Set<E> {
    
    private final Set<E> set = new HashSet<E>();
    
    public ConcurrentHashSet() {
    }
    
    public synchronized boolean add(E e) {
        return this.set.add(e);
    }
    
    public synchronized boolean addAll(Collection<? extends E> c) {
        return this.set.addAll(c);
    }
    
    public synchronized void clear() {
        this.set.clear();
    }
    
    public synchronized boolean contains(Object o) {
        return this.set.contains(o);
    }
    
    public synchronized boolean containsAll(Collection<?> c) {
        return this.set.containsAll(c);
    }
    
    public synchronized boolean equals(Object o) {
        return this.set.equals(o);
    }
    
    public synchronized int hashCode() {
        return this.set.hashCode();
    }
    
    public synchronized boolean isEmpty() {
        return this.set.isEmpty();
    }
    
    /**
     * @return an iterator that is based on a copy of this set at creation time.
     */
    public synchronized Iterator<E> iterator() {
        ArrayList<E> list = new ArrayList<E>(this.set);
        return list.iterator();
    }
    
    public synchronized boolean remove(Object o) {
        return this.set.remove(o);
    }
    
    public synchronized boolean removeAll(Collection<?> c) {
        return this.set.removeAll(c);
    }
    
    public synchronized boolean retainAll(Collection<?> c) {
        return this.set.retainAll(c);
    }
    
    public synchronized int size() {
        return this.set.size();
    }
    
    public synchronized Object[] toArray() {
        return this.set.toArray();
    }
    
    public synchronized <T> T[] toArray(T[] a) {
        return this.set.toArray(a);
    }
    
}
