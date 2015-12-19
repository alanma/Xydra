package org.xydra.index.iterator;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
	 * @param base @NeverNull
	 * @param filter
	 * @return an iterator returning fewer elements, namely only those matching the filter
	 */
	public static <E> ClosableIterator<E> filter(final Iterator<E> base, final IFilter<E> filter) {
		assert base != null;
		if (base == NoneIterator.INSTANCE) {
			return (ClosableIterator<E>) base;
		}

		return new FilteringIterator<E>(base, filter);
	}

	@SuppressWarnings("unchecked")
	public static <E> Iterator<E> nonNull(final Iterator<E> base) {
		return filter(base, FILTER_NON_NULL);
	}

	@SuppressWarnings("rawtypes")
	public static final IFilter FILTER_NON_NULL = new IFilter() {

		@Override
		public boolean matches(final Object entry) {
			return entry != null;
		}
	};

	/**
	 * Uses an internal, unbounded HashSet to return only unique elements.
	 *
	 * Lazy evaluated
	 *
	 * @param base elements must have valid implementation of {@link Object#hashCode()} and
	 *        {@link Object#equals(Object)} @NeverNull
	 * @return unique elements
	 */
	public static <E> ClosableIterator<E> distinct(final Iterator<E> base) {
		assert base != null;
		if (base == NoneIterator.INSTANCE) {
			return (ClosableIterator<E>) base;
		}

		return Iterators.filter(base, new IFilter<E>() {

			Set<E> unique = new HashSet<E>();

			@Override
			public boolean matches(final E entry) {
				if (this.unique.contains(entry)) {
					return false;
				}

				this.unique.add(entry);
				return true;
			}
		});
	}

	/* Copyright (C) 2007 The Guava Authors
	 *
	 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance
	 * with the License. You may obtain a copy of the License at
	 *
	 * http://www.apache.org/licenses/LICENSE-2.0
	 *
	 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
	 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for
	 * the specific language governing permissions and limitations under the License. */
	/**
	 * Creates an iterator returning the first {@code max} elements of the given iterator. If the original iterator does
	 * not contain that many elements, the returned iterator will have the same behaviour as the original iterator. The
	 * returned iterator supports {@code remove()} if the original iterator does.
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

		public LimitingIterator(final int max, final Iterator<T> limited) {
			super();
			this.max = max;
			this.limited = limited;
		}

		private final int max;
		private final Iterator<T> limited;

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
		if (max == -1) {
			return iterator;
		}
		return limit(iterator, max);
	}

	/**
	 * @param base @NeverNull
	 * @param transformer
	 * @return an iterator returning entries of another type as the input type
	 */
	@SuppressWarnings("unchecked")
	public static <I, O> ClosableIterator<O> transform(final Iterator<? extends I> base,
			final ITransformer<I, O> transformer) {
		assert base != null;

		if (base == NoneIterator.INSTANCE) {
			return (ClosableIterator<O>) base;
		}

		return new TransformingIterator<I, O>(base, transformer);
	}

	/**
	 * An optimized version of {@link #transform(Iterator, ITransformer)}, which implicitly filters out all items for
	 * which the transformer returns null
	 *
	 * @param base @NeverNull
	 * @param transformer returns null for unwanted elements @NeverNull
	 * @return an iterator returning entries of another type as the input type @NeverNull
	 */
	@SuppressWarnings("unchecked")
	public static <I, O> Iterator<O> transformSkipNulls(final Iterator<? extends I> base,
			final ITransformer<I, O> transformer) {
		assert base != null;
		assert transformer != null;

		if (base == NoneIterator.INSTANCE) {
			return (Iterator<O>) base;
		}

		return Iterators.filter(Iterators.transform(base, transformer), FILTER_NON_NULL);
	}

	/**
	 * @param it1 @NeverNull
	 * @param it2 @NeverNull
	 * @return a single, continuous iterator; might contain duplicates
	 */
	public static <E> ClosableIterator<E> concat(final Iterator<? extends E> it1, final Iterator<? extends E> it2) {
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

	public static <E> Iterator<E> forOne(final E value) {
		return new SingleValueIterator<E>(value);
	}

	/**
	 * @param base @NeverNull
	 * @param transformer
	 * @return a uniform iterator in which each element of base was turned into a part of the resulting sequence
	 */
	@SuppressWarnings("unchecked")
	public static <B, E> ClosableIterator<E> cascade(final Iterator<B> base,
			final ITransformer<B, Iterator<E>> transformer) {
		assert base != null;
		if (base == NoneIterator.INSTANCE) {
			return (ClosableIterator<E>) base;
		}

		return new CascadedIterator<B, E>(base, transformer);
	}

	private static class CascadedIterator<B, E> extends AbstractCascadedIterator<B, E>implements ClosableIterator<E> {

		private final ITransformer<B, Iterator<E>> transformer;

		public CascadedIterator(final Iterator<B> base, final ITransformer<B, Iterator<E>> transformer) {
			super(base);
			this.transformer = transformer;
		}

		@Override
		protected Iterator<? extends E> toIterator(final B baseEntry) {
			return this.transformer.transform(baseEntry);
		}
	}

	/**
	 * A crude way of debugging, print the contents of the iterator to System.out, one item per line, each via
	 * toString().
	 *
	 * @param label
	 * @param it
	 */
	public static <E> void dump(final String label, final Iterator<E> it) {
		System.out.println("Dump of iterator '" + label + "':");
		while (it.hasNext()) {
			final E e = it.next();
			System.out.println("  Item: '" + e.toString() + "'");
		}
		System.out.println(" End of iterator '" + label + "'.");
	}

	/**
	 * @param partIterators @NeverNull each may return an element only once, no duplicates
	 * @return an iterator representing the set-intersection of the set implied by the partial iterators
	 */
	public static <E> Iterator<E> setIntersect(final Iterator<Iterator<E>> partIterators) {
		// none
		if (!partIterators.hasNext()) {
			return NoneIterator.create();
		}

		// just one?
		final Set<E> result = new HashSet<E>();
		Iterators.addAll(partIterators.next(), result);
		if (!partIterators.hasNext()) {
			return result.iterator();
		}

		// more
		while (partIterators.hasNext()) {
			final Iterator<E> otherIt = partIterators.next();
			final Set<E> deleteMe = new HashSet<E>();
			final Set<E> other = new HashSet<E>();
			Iterators.addAll(otherIt, other);
			for (final E e : result) {
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
	 * @param partIterators smallest iterators should come first, each iterator must be duplicate-free
	 * @return an iterator representing the set-union of the set implied by the partial iterators
	 */
	public static <E> Iterator<E> setUnion(final Iterator<E> smallIterator, final Iterator<E> largeIterator) {
		// none
		if (!smallIterator.hasNext()) {
			return largeIterator;
		}

		if (!largeIterator.hasNext()) {
			return NoneIterator.create();
		}

		final Set<E> smallSet = new HashSet<E>();
		return Iterators.concat(Iterators.filter(smallSet.iterator(), new IFilter<E>() {

			@Override
			public boolean matches(final E entry) {
				smallSet.add(entry);
				return true;
			}
		}), Iterators.filter(largeIterator, new IFilter<E>() {

			@Override
			public boolean matches(final E entry) {
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
	public static <C extends Collection<T>, T> C addAll(final Iterator<? extends T> it, final C collection) {
		while (it.hasNext()) {
			final T t = it.next();
			collection.add(t);
		}
		return collection;
	}

	/**
	 * @param it
	 * @param collection
	 * @param n
	 * @return collection for fluent API
	 */
	public static <C extends Collection<T>, T> C addFirstN(final Iterator<? extends T> it, final C collection,
			final int n) {
		int i = 0;
		while (it.hasNext() && i < n) {
			final T t = it.next();
			collection.add(t);
			i++;
		}
		return collection;
	}

	public static <T> boolean isEmpty(final Iterable<T> iterable) {
		return isEmpty(iterable.iterator());
	}

	public static <T> boolean isEmpty(final Iterator<T> it) {
		return !it.hasNext();
	}

	/**
	 * @param it will be closed if instance of ClosableIterator
	 * @return a LinkedList
	 */
	@SuppressWarnings("rawtypes")
	public static <T> List<T> toList(final Iterator<? extends T> it) {
		final LinkedList<T> list = new LinkedList<T>();
		addAll(it, list);
		if (it instanceof ClosableIterator) {
			((ClosableIterator) it).close();
		}
		return list;
	}

	public static <T> ArrayList<T> toArrayList(final Iterator<T> it) {
		final ArrayList<T> list = new ArrayList<T>();
		addAll(it, list);
		return list;
	}

	/**
	 * Does not close the iterator
	 *
	 * @param it
	 * @return a HashSet
	 */
	public static <T> Set<T> toSet(final Iterator<? extends T> it) {
		final HashSet<T> set = new HashSet<T>();
		addAll(it, set);
		return set;
	}

	public static <T> List<T> firstNtoList(final Iterator<? extends T> it, final int n) {
		final ArrayList<T> list = new ArrayList<T>(n);
		addFirstN(it, list, n);
		return list;
	}

	/**
	 * @param collection
	 * @return the collections as a string, elements separated by ','
	 */
	public static <T> String toText(final Collection<T> collection) {
		final StringBuffer buf = new StringBuffer();
		for (final T s : collection) {
			buf.append(s).append(",");
		}
		return buf.toString();
	}

	public static <T> String toText(final Iterator<T> it) {
		final StringBuffer buf = new StringBuffer();
		while (it.hasNext()) {
			final T t = it.next();
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
	public static int count(final Iterator<?> it) {
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
	 * @return number of elements in iterator; maximum is max. Reports -1 if maximum is reached.
	 */
	public static int count(final Iterator<?> it, final int max) {
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
	public static <X> X getSingleValue(final Iterator<X> it) {
		assert it != null;
		if (it.hasNext()) {
			final X result = it.next();
			if (it.hasNext()) {
				throw new IllegalStateException("Found more than one result: " + result + " AND " + it.next());
			}
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
	public static <X> X getSingleValue(final Collection<X> coll) {
		assert coll != null;
		return getSingleValue(coll.iterator());
	}

	/**
	 * For debugging. Output the contents of the iterator to System.out by calling toString on each element.
	 *
	 * @param it
	 */
	public static <E> void dump(final Iterator<E> it) {
		System.out.println("Dumping " + it.getClass().getName());
		while (it.hasNext()) {
			final E e = it.next();
			System.out.println(e.toString());
		}
		System.out.println("End of iterator");
	}

	public static <E> boolean contains(final Iterator<E> it, final E element) {
		while (it.hasNext()) {
			final E e = it.next();
			if (e.equals(element)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Makes constraints (a very narrow concept) compatible with filters (a very generic concept)
	 *
	 * @param base
	 * @param constraint
	 * @return an iterator which returns only those elements of base that match the constraint
	 */
	public static <E> Iterator<E> filterWithConstraint(final Iterator<E> base, final Constraint<E> constraint) {
		if(constraint.isStar()) {
			return base;
		}

		// else
		return Iterators.filter(base, new IFilter<E>() {

			@Override
			public boolean matches(final E entry) {
				return constraint.matches(entry);
			}
		});
	}

	/**
	 * Applies the natural sorting via an internal list. Don't do this on huge iterators :-)
	 *
	 * @param it
	 * @return
	 */
	public static <E extends Comparable<E>> Iterator<E> sort(final Iterator<E> it) {
		final List<E> list = Iterators.toArrayList(it);
		Collections.sort(list);
		return list.iterator();
	}

	/**
	 * Converts a collection of Strings into an array of String
	 *
	 * @param collection of strings
	 * @return @NeverNull
	 */
	public static String[] toArray(final Collection<String> collection) {
		if (collection == null) {
			return new String[0];
		}
		return new ArrayList<String>(collection).toArray(new String[collection.size()]);
	}

	/**
	 * Memory warning: Creates internally a complete copy
	 *
	 * @param map
	 * @param descending if false, sort order is ascending
	 * @return an iterator over map entries in a sorted order
	 */
	public static <K, V extends Comparable<V>> Iterator<Map.Entry<K, V>> sortByValue(final Map<K, V> map,
			final boolean descending) {

		final List<Map.Entry<K, V>> list = new ArrayList<>();
		list.addAll(map.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<K, V>>() {

			@Override
			public int compare(final Entry<K, V> o1, final Entry<K, V> o2) {
				final int i = o1.getValue().compareTo(o2.getValue());

				if (i == 0 && o1.getKey() instanceof Comparable) {
					@SuppressWarnings("unchecked") final Comparable<K> key1 = (Comparable<K>) o1.getKey();
					final K key2 = o2.getKey();
					return key1.compareTo(key2);
				}

				if (descending) {
					return -i;
				}
				return i;
			}
		});
		return list.iterator();
	}

	/**
	 * Type-cast an iterator with generics
	 *
	 * @param symbols
	 * @return
	 * @param <T> type
	 * @param <T> base type
	 */
	public static <T extends B, B> Iterator<B> typeCast(final Iterator<T> typedIt) {
		return Iterators.transform(typedIt, new ITransformer<T, B>() {

			@Override
			public B transform(final T in) {
				return in;
			}
		});
	}

	/**
	 * @param iterator
	 * @return a pseudo-iterable which is only iterable once
	 */
	public static <E> Iterable<E> from(final Iterator<E> iterator) {
		return new Iterable<E>() {

			private boolean done = false;

			@Override
			public Iterator<E> iterator() {
				if (this.done) {
					throw new IllegalStateException("Iterator can be used only once");
				}
				this.done = true;
				return iterator;
			}
		};
	}

	public static <E> Iterator<E> removable(final Iterator<E> iterator, final IRemove remove) {

		return new Iterator<E>() {

			@Override
			public boolean hasNext() {
				return iterator.hasNext();
			}

			@Override
			public E next() {
				return iterator.next();
			}

			@Override
			public void remove() {
				remove.remove();
			}

		};
	}

	/**
	 * @param it
	 * @param comparator
	 * @return the element that gets sorted as the maximum
	 */
	public static <T> T getElementWithHighestValue(final Iterator<T> it, final Comparator<T> comparator) {
		T best = null;
	
		while (it.hasNext()) {
			final T node = it.next();
			if (best == null) {
				best = node;
			} else {
				if (comparator.compare(best, node) > 0) {
					best = node;
				}
			}
		}
		return best;
	}

}
