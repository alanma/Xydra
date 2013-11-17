package org.xydra.index.iterator;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


public class Iterators {
    
    /**
     * @param base
     * @param filter
     * @return an iterator returning fewer elements, namely only those matching
     *         the filter
     */
    public static <E> Iterator<E> filter(Iterator<E> base, IFilter<E> filter) {
        return new FilteringIterator<>(base, filter);
    }
    
    /**
     * Lazy evaluated
     * 
     * @param it must have valid implementation of {@link Object#hashCode()} and
     *            {@link Object#equals(Object)}
     * @return unique elements
     */
    public static <E> Iterator<E> distinct(Iterator<E> it) {
        return Iterators.filter(it, new IFilter<E>() {
            
            Set<E> unique = new HashSet<>();
            
            @Override
            public boolean matches(E entry) {
                if(this.unique.contains(entry))
                    return false;
                
                this.unique.add(entry);
                return true;
            }
        });
    }
    
    /**
     * @param base
     * @param transformer
     * @return an iterator returning entries of another type as the input type
     */
    public static <I, O> Iterator<O> transform(Iterator<I> base, ITransformer<I,O> transformer) {
        return new TransformingIterator<I,O>(base, transformer);
    }
    
    /**
     * @param it1
     * @param it2
     * @return a single, continuous iterator; might contain duplicates
     */
    public static <E> Iterator<E> concat(Iterator<E> it1, Iterator<E> it2) {
        return new BagUnionIterator<>(it1, it2);
    }
    
    public static <E> Iterator<E> forOne(E value) {
        return new SingleValueIterator<E>(value);
    }
    
    public static <B, E> Iterator<E> cascade(Iterator<B> base,
            final ITransformer<B,Iterator<E>> transformer) {
        return new AbstractCascadedIterator<B,E>(base) {
            
            @Override
            protected Iterator<? extends E> toIterator(B baseEntry) {
                return transformer.transform(baseEntry);
            }
        };
    }
    
    /**
     * A crude way of debugging, print the contents of the iterator to
     * System.out, one item per line, each via toString().
     * 
     * @param label
     * @param it
     */
    public static <E> void dump(String label, Iterator<E> it) {
        System.out.println("Dump of iterator '" + label + "':");
        while(it.hasNext()) {
            E e = it.next();
            System.out.println("  Item: '" + e.toString() + "'");
        }
        System.out.println(" End of iterator '" + label + "'.");
    }
    
}
