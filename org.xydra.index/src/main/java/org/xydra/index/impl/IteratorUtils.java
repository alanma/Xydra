package org.xydra.index.impl;

import org.xydra.annotations.RunsInGWT;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;


@RunsInGWT(true)
public class IteratorUtils {
    
    /**
     * @param <T> type of both
     * @param <C> a collection type of T
     * @param it never null
     * @param collection to which elements are added
     * @return as a convenience, the supplied collection
     */
    public static <C extends Collection<T>, T> C addAll(Iterator<? extends T> it, C collection) {
        while(it.hasNext()) {
            T t = it.next();
            collection.add(t);
        }
        return collection;
    }
    
    public static <C extends Collection<T>, T> C addFirstN(Iterator<? extends T> it, C collection,
            int n) {
        int i = 0;
        while(it.hasNext() && i < n) {
            T t = it.next();
            collection.add(t);
            i++;
        }
        return collection;
    }
    
    public static <T> boolean isEmpty(Iterable<T> iterable) {
        return isEmpty(iterable.iterator());
    }
    
    public static <T> boolean isEmpty(Iterator<T> it) {
        return !it.hasNext();
    }
    
    /**
     * @param it
     * @return a LinkedList
     */
    public static <T> List<T> toList(Iterator<? extends T> it) {
        LinkedList<T> list = new LinkedList<T>();
        addAll(it, list);
        return list;
    }
    
    public static <T> ArrayList<T> toArrayList(Iterator<T> it) {
        ArrayList<T> list = new ArrayList<T>();
        addAll(it, list);
        return list;
    }
    
    /**
     * @param it
     * @return a HashSet
     */
    public static <T> Set<T> toSet(Iterator<? extends T> it) {
        HashSet<T> set = new HashSet<T>();
        addAll(it, set);
        return set;
    }
    
    public static <T> List<T> firstNtoList(Iterator<? extends T> it, int n) {
        ArrayList<T> list = new ArrayList<T>(n);
        addFirstN(it, list, n);
        return list;
    }
    
    public static <T> String toText(Collection<T> value) {
        StringBuffer buf = new StringBuffer();
        for(T s : value) {
            buf.append(s).append(",");
        }
        return buf.toString();
    }
    
    public static int count(Iterator<?> it) {
        int i = 0;
        while(it.hasNext()) {
            i++;
            it.next();
        }
        return i;
    }
    
    /**
     * @param it
     * @param max
     * @return number of elements in iterator; maximum is max. Reports -1 if
     *         maximum is reached.
     */
    public static int count(Iterator<?> it, int max) {
        int i = 0;
        while(it.hasNext() && i < max) {
            i++;
            it.next();
        }
        return i < max ? i : -1;
    }
    
    /**
     * @param it @NeverNull
     * @return the single value, if present. Or null, otherwise.
     * @throws IllegalStateException if iterator has more than one result
     */
    public static <X> X getSingleValue(Iterator<X> it) {
        assert it != null;
        if(it.hasNext()) {
            X result = it.next();
            if(it.hasNext())
                throw new IllegalStateException("Found more than one result: " + result + " AND "
                        + it.next());
            return result;
        } else {
            return null;
        }
    }
    
    /**
     * For debugging. Output the contents of the iterator to System.out by
     * calling toString on each element.
     * 
     * @param it
     */
    public static <E> void dump(Iterator<E> it) {
        System.out.println("Dumping " + it.getClass().getName());
        while(it.hasNext()) {
            E e = it.next();
            System.out.println(e.toString());
        }
        System.out.println("End of iterator");
    }
    
    public static <E> boolean contains(Iterator<E> it, E element) {
        while(it.hasNext()) {
            E e = it.next();
            if(e.equals(element))
                return true;
        }
        return false;
    }
    
}
