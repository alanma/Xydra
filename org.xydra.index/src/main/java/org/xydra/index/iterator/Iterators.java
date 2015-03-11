package org.xydra.index.iterator;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import org.xydra.annotations.LicenseApache;
import org.xydra.annotations.RunsInGWT;
import org.xydra.index.query.Constraint;

/**
 * @author xamde
 * 
 */
@RunsInGWT(true)
public class Iterators {

	/**
	 * @param base
	 * @NeverNull
	 * @param filter
	 * @return an iterator returning fewer elements, namely only those matching
	 *         the filter
	 */
	public static <E> ClosableIterator<E> filter(Iterator<E> base, IFilter<E> filter) {
		assert base != null;
		if (base == NoneIterator.INSTANCE)
			return (ClosableIterator<E>) base;

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
	public static <E> ClosableIterator<E> distinct(Iterator<E> base) {
		assert base != null;
		if (base == NoneIterator.INSTANCE)
			return (ClosableIterator<E>) base;

		return Iterators.filter(base, new IFilter<E>() {

			Set<E> unique = new HashSet<E>();

			@Override
			public boolean matches(E entry) {
				if (this.unique.contains(entry))
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
	@LicenseApache(copyright = "Copyright (C) 2007 The Guava Authors", project = "Guava")
	public static <T> ClosableIterator<T> limit(final Iterator<T> iterator, final int max) {
		checkNotNull(iterator);
		checkArgument(max >= 0, "limit is negative");
		return new LimitingIterator<T>(max, iterator);
	}

	@LicenseApache(copyright = "Copyright (C) 2007 The Guava Authors", project = "Guava")
	private static class LimitingIterator<T> implements ClosableIterator<T> {

		private int count = 0;

		public LimitingIterator(int max, Iterator<T> limited) {
			super();
			this.max = max;
			this.limited = limited;
		}

		private int max;
		private Iterator<T> limited;

		@Override
		public boolean hasNext() {
			return this.count < this.max && this.limited.hasNext();
		}

		@Override
		public T next() {
			if (!hasNext()) {
				throw new NoSuchElementException();
			}
			this.count++;
			return this.limited.next();
		}

		@Override
		public void remove() {
			this.limited.remove();
		}

		@SuppressWarnings("rawtypes")
		@Override
		public void close() {
			if (this.limited instanceof ClosableIterator) {
				((ClosableIterator) this.limited).close();
			}
		}

	}

	/**
	 * @param iterator
	 * @param max use -1 for no limit
	 * @return a potentially limited iterator, depending on max
	 */
	public static <T> Iterator<T> maybeLimit(final Iterator<T> iterator, final int max) {
		if (max == -1)
			return iterator;
		return limit(iterator, max);
	}

	/**
	 * @param base
	 * @NeverNull
	 * @param transformer
	 * @return an iterator returning entries of another type as the input type
	 */
	@SuppressWarnings("unchecked")
	public static <I, O> ClosableIterator<O> transform(Iterator<? extends I> base,
			ITransformer<I, O> transformer) {
		assert base != null;

		if (base == NoneIterator.INSTANCE)
			return (ClosableIterator<O>) base;

		return new TransformingIterator<I, O>(base, transformer);
	}

	/**
	 * An optimized version of {@link #transform(Iterator, ITransformer)}, which
	 * implicitly filters out all items for which the transformer returns null
	 * 
	 * @param base
	 * @NeverNull
	 * @param transformer returns null for unwanted elements @NeverNull
	 * @return an iterator returning entries of another type as the input type @NeverNull
	 */
	@SuppressWarnings("unchecked")
	public static <I, O> Iterator<O> transformSkipNulls(Iterator<? extends I> base,
			ITransformer<I, O> transformer) {
		assert base != null;
		assert transformer != null;

		if (base == NoneIterator.INSTANCE)
			return (Iterator<O>) base;

		return Iterators.filter(Iterators.transform(base, transformer), FILTER_NON_NULL);
	}

	/**
	 * @param it1
	 * @NeverNull
	 * @param it2
	 * @NeverNull
	 * @return a single, continuous iterator; might contain duplicates
	 */
	public static <E> ClosableIterator<E> concat(Iterator<? extends E> it1,
			Iterator<? extends E> it2) {
		assert it1 != null;
		assert it2 != null;

		return new BagUnionIterator<E>(it1, it2);
	}

	/**
	 * @param iterators must be of generic type <E>
	 * @return a single, continuous iterator; might contain duplicates
	 */
	@SafeVarargs
	public static <E> Iterator<E> concat(final Iterator<E>... iterators) {
		return new BagUnionIterator<E>(iterators);
	}

	public static <E> Iterator<E> forOne(E value) {
		return new SingleValueIterator<E>(value);
	}

	/**
	 * @param base
	 * @NeverNull
	 * @param transformer
	 * @return a uniform iterator in which each element of base was turned into
	 *         a part of the resulting sequence
	 */
	@SuppressWarnings("unchecked")
	public static <B, E> Iterator<E> cascade(Iterator<B> base,
			final ITransformer<B, Iterator<E>> transformer) {
		assert base != null;
		if (base == NoneIterator.INSTANCE)
			return (Iterator<E>) base;

		return new CascadedIterator<B, E>(base, transformer);
	}

	private static class CascadedIterator<B, E> extends AbstractCascadedIterator<B, E> implements
			Iterator<E> {

		private ITransformer<B, Iterator<E>> transformer;

		public CascadedIterator(Iterator<B> base, final ITransformer<B, Iterator<E>> transformer) {
			super(base);
			this.transformer = transformer;
		}

		@Override
		protected Iterator<? extends E> toIterator(B baseEntry) {
			return this.transformer.transform(baseEntry);
		}
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
		while (it.hasNext()) {
			E e = it.next();
			System.out.println("  Item: '" + e.toString() + "'");
		}
		System.out.println(" End of iterator '" + label + "'.");
	}

	/**
	 * @param partIterators
	 * @NeverNull each may return an element only once, no duplicates
	 * @return an iterator representing the set-intersection of the set implied
	 *         by the partial iterators
	 */
	public static <E> Iterator<E> setIntersect(Iterator<Iterator<E>> partIterators) {
		// none
		if (!partIterators.hasNext())
			return NoneIterator.create();

		// just one?
		Set<E> result = new HashSet<E>();
		Iterators.addAll(partIterators.next(), result);
		if (!partIterators.hasNext())
			return result.iterator();

		// more
		while (partIterators.hasNext()) {
			Iterator<E> otherIt = partIterators.next();
			Set<E> deleteMe = new HashSet<E>();
			Set<E> other = new HashSet<E>();
			Iterators.addAll(otherIt, other);
			for (E e : result) {
				if (!other.contains(e)) {
					deleteMe.add(e);
				}
			}
			result.removeAll(deleteMe);
		}
		return result.iterator();
	}

	/**
	 * Lazily evaluated union
	 * 
	 * @param partIterators smallest iterators should come first, each iterator
	 *            must be duplicate-free
	 * @return an iterator representing the set-union of the set implied by the
	 *         partial iterators
	 */
	public static <E> Iterator<E> setUnion(Iterator<E> smallIterator, Iterator<E> largeIterator) {
		// none
		if (!smallIterator.hasNext())
			return largeIterator;

		if (!largeIterator.hasNext())
			return NoneIterator.create();

		final Set<E> smallSet = new HashSet<E>();
		return Iterators.concat(Iterators.filter(smallSet.iterator(), new IFilter<E>() {

			@Override
			public boolean matches(E entry) {
				smallSet.add(entry);
				return true;
			}
		}), Iterators.filter(largeIterator, new IFilter<E>() {

			@Override
			public boolean matches(E entry) {
				return !smallSet.contains(entry);
			}
		}));
	}

	public static <E> ClosableIterator<E> none() {
		return NoneIterator.create();
	}

	/**
	 * @param <T> type of both
	 * @param <C> a collection type of T
	 * @param it @NeverNull, is closed if instance of {@link ClosableIterator}
	 * @param collection to which elements are added
	 * @return as a convenience, the supplied collection
	 */
	public static <C extends Collection<T>, T> C addAll(Iterator<? extends T> it, C collection) {
		while (it.hasNext()) {
			T t = it.next();
			collection.add(t);
		}
		return collection;
	}

	public static <C extends Collection<T>, T> C addFirstN(Iterator<? extends T> it, C collection,
			int n) {
		int i = 0;
		while (it.hasNext() && i < n) {
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
	 * @param it will be closed if instance of ClosableIterator
	 * @return a LinkedList
	 */
	@SuppressWarnings("rawtypes")
	public static <T> List<T> toList(Iterator<? extends T> it) {
		LinkedList<T> list = new LinkedList<T>();
		addAll(it, list);
		if (it instanceof ClosableIterator) {
			((ClosableIterator) it).close();
		}
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

	/**
	 * @param collection
	 * @return the collections as a string, elements separated by ','
	 */
	public static <T> String toText(Collection<T> collection) {
		StringBuffer buf = new StringBuffer();
		for (T s : collection) {
			buf.append(s).append(",");
		}
		return buf.toString();
	}

	public static <T> String toText(Iterator<T> it) {
		StringBuffer buf = new StringBuffer();
		while (it.hasNext()) {
			T t = it.next();
			buf.append(t);
			if (it.hasNext()) {
				buf.append(",");
			}
		}
		return buf.toString();
	}

	/**
	 * @param it is closed if instance of {@link ClosableIterator}
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public static int count(Iterator<?> it) {
		int i = 0;
		while (it.hasNext()) {
			i++;
			it.next();
		}
		if (it instanceof ClosableIterator) {
			((ClosableIterator) it).close();
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
		while (it.hasNext() && i < max) {
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
		if (it.hasNext()) {
			X result = it.next();
			if (it.hasNext())
				throw new IllegalStateException("Found more than one result: " + result + " AND "
						+ it.next());
			return result;
		} else {
			return null;
		}
	}

	/**
	 * @param coll @NeverNull
	 * @return the single value, if present. Or null, otherwise.
	 * @throws IllegalStateException if iterator has more than one result
	 */
	public static <X> X getSingleValue(Collection<X> coll) {
		assert coll != null;
		return getSingleValue(coll.iterator());
	}

	/**
	 * For debugging. Output the contents of the iterator to System.out by
	 * calling toString on each element.
	 * 
	 * @param it
	 */
	public static <E> void dump(Iterator<E> it) {
		System.out.println("Dumping " + it.getClass().getName());
		while (it.hasNext()) {
			E e = it.next();
			System.out.println(e.toString());
		}
		System.out.println("End of iterator");
	}

	public static <E> boolean contains(Iterator<E> it, E element) {
		while (it.hasNext()) {
			E e = it.next();
			if (e.equals(element))
				return true;
		}
		return false;
	}

	/**
	 * Makes constraints (a very narrow concept) compatible with filters (a very
	 * generic concept)
	 * 
	 * @param base
	 * @param constraint
	 * @return an iterator which returns only those elements of base that match
	 *         the constraint
	 */
	public static <E> Iterator<E> filterWithConstraint(Iterator<E> base,
			final Constraint<E> constraint) {
		return Iterators.filter(base, new IFilter<E>() {

			@Override
			public boolean matches(E entry) {
				return constraint.matches(entry);
			}
		});
	}

	/**
	 * Applies the natural sorting via an internal list. Don't do this on huge
	 * iterators :-)
	 * 
	 * @param it
	 * @return
	 */
	public static <E extends Comparable<E>> Iterator<E> sort(Iterator<E> it) {
		List<E> list = Iterators.toArrayList(it);
		Collections.sort(list);
		return list.iterator();
	}

	/**
	 * Converts a collection of Strings into an array of String
	 * 
	 * @param collection of strings
	 * @return @NeverNull
	 */
	public static String[] toArray(Collection<String> collection) {
		if (collection == null)
			return new String[0];
		return new ArrayList<String>(collection).toArray(new String[collection.size()]);
	}

}
