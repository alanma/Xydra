package org.xydra.index.iterator;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import org.xydra.index.impl.IteratorUtils;

import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;


/**
 * @author xamde
 * 
 */
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
    
    @SuppressWarnings("unchecked")
    public static <E> Iterator<E> nonNull(Iterator<E> base) {
        return filter(base, FILTER_NON_NULL);
    }
    
    @SuppressWarnings("rawtypes")
    public static final IFilter FILTER_NON_NULL = new IFilter() {
        
        @Override
        public boolean matches(Object entry) {
            return entry != null;
        }
    };
    
    /**
     * Uses an internal, unbounded HashSet to return only unique elements.
     * 
     * Lazy evaluated
     * 
     * @param base elements must have valid implementation of
     *            {@link Object#hashCode()} and {@link Object#equals(Object)}
     * @NeverNull
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
    
    /*
     * Copyright (C) 2007 The Guava Authors
     * 
     * Licensed under the Apache License, Version 2.0 (the "License"); you may
     * not use this file except in compliance with the License. You may obtain a
     * copy of the License at
     * 
     * http://www.apache.org/licenses/LICENSE-2.0
     * 
     * Unless required by applicable law or agreed to in writing, software
     * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
     * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
     * License for the specific language governing permissions and limitations
     * under the License.
     */
    /**
     * Creates an iterator returning the first {@code max} elements of the given
     * iterator. If the original iterator does not contain that many elements,
     * the returned iterator will have the same behaviour as the original
     * iterator. The returned iterator supports {@code remove()} if the original
     * iterator does.
     * 
     * @param iterator the iterator to limit
     * @param max the maximum number of elements in the returned iterator
     * @return ...
     * @throws IllegalArgumentException if {@code limitSize} is negative
     * @since 3.0
     */
    public static <T> Iterator<T> limit(final Iterator<T> iterator, final int max) {
        checkNotNull(iterator);
        checkArgument(max >= 0, "limit is negative");
        return new Iterator<T>() {
            private int count;
            
            @Override
            public boolean hasNext() {
                return this.count < max && iterator.hasNext();
            }
            
            @Override
            public T next() {
                if(!hasNext()) {
                    throw new NoSuchElementException();
                }
                this.count++;
                return iterator.next();
            }
            
            @Override
            public void remove() {
                iterator.remove();
            }
        };
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
    
    /**
     * @param partIterators @NeverNull each may return an element only once, no
     *            duplicates
     * @return an iterator representing the set-intersection of the set implied
     *         by the partial iterators
     */
    public static <E> Iterator<E> setIntersect(Iterator<Iterator<E>> partIterators) {
        // none
        if(!partIterators.hasNext())
            return NoneIterator.create();
        
        // just one?
        Set<E> result = new HashSet<E>();
        IteratorUtils.addAll(partIterators.next(), result);
        if(!partIterators.hasNext())
            return result.iterator();
        
        // more
        while(partIterators.hasNext()) {
            Iterator<E> otherIt = partIterators.next();
            Set<E> deleteMe = new HashSet<E>();
            Set<E> other = new HashSet<E>();
            IteratorUtils.addAll(otherIt, other);
            for(E e : result) {
                if(!other.contains(e)) {
                    deleteMe.add(e);
                }
            }
            result.removeAll(deleteMe);
        }
        return result.iterator();
    }
    
    public static <E> Iterator<E> none() {
        return NoneIterator.create();
    }
    
}
