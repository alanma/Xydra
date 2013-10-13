package org.xydra.index.impl;

import java.util.Collection;
import java.util.Iterator;


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
    
    public static <T> boolean isEmpty(Iterable<T> iterable) {
        return isEmpty(iterable.iterator());
    }
    
    public static <T> boolean isEmpty(Iterator<T> it) {
        return !it.hasNext();
    }
    
    public static String toText(Collection<String> value) {
        StringBuffer buf = new StringBuffer();
        for(String s : value) {
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
    
}
