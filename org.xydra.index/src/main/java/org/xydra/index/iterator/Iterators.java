package org.xydra.index.iterator;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


public class Iterators {
    
    /**
     * @param base @NeverNull
     * @param filter
     * @return an iterator returning fewer elements, namely only those matching
     *         the filter
     */
    public static <E> Iterator<E> filter(Iterator<E> base, IFilter<E> filter) {
        assert base != null;
        if(base == NoneIterator.INSTANCE)
            return base;
        
        return new FilteringIterator<E>(base, filter);
    }
    
    /**
     * Lazy evaluated
     * 
     * @param base must have valid implementation of {@link Object#hashCode()}
     *            and {@link Object#equals(Object)} @NeverNull
     * @return unique elements
     */
    public static <E> Iterator<E> distinct(Iterator<E> base) {
        assert base != null;
        if(base == NoneIterator.INSTANCE)
            return base;
        
        return Iterators.filter(base, new IFilter<E>() {
            
            Set<E> unique = new HashSet<E>();
            
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
     * @param base @NeverNull
     * @param transformer
     * @return an iterator returning entries of another type as the input type
     */
    @SuppressWarnings("unchecked")
    public static <I, O> Iterator<O> transform(Iterator<? extends I> base,
            ITransformer<I,O> transformer) {
        assert base != null;
        
        if(base == NoneIterator.INSTANCE)
            return (Iterator<O>)base;
        
        return new TransformingIterator<I,O>(base, transformer);
    }
    
    /**
     * @param it1 @NeverNull
     * @param it2 @NeverNull
     * @return a single, continuous iterator; might contain duplicates
     */
    public static <E> Iterator<E> concat(Iterator<E> it1, Iterator<E> it2) {
        assert it1 != null;
        assert it2 != null;
        
        return new BagUnionIterator<E>(it1, it2);
    }
    
    /**
     * @param iterators must be of generic type <E>
     * @return a single, continuous iterator; might contain duplicates
     */
    @SuppressWarnings({ "unchecked" })
    public static <E> Iterator<E> concat(final Iterator<E> ... iterators) {
        return new BagUnionIterator<E>(iterators);
    }
    
    public static <E> Iterator<E> forOne(E value) {
        return new SingleValueIterator<E>(value);
    }
    
    /**
     * @param base @NeverNull
     * @param transformer
     * @return a uniform iterator in which each element of base was turned into
     *         a part of the resulting sequence
     */
    @SuppressWarnings("unchecked")
    public static <B, E> Iterator<E> cascade(Iterator<B> base,
            final ITransformer<B,Iterator<E>> transformer) {
        assert base != null;
        if(base == NoneIterator.INSTANCE)
            return (Iterator<E>)base;
        
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