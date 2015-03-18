package org.xydra.index.impl.trie;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;

import org.xydra.index.IMapIndex;
import org.xydra.index.iterator.IFilter;
import org.xydra.index.iterator.ITransformer;
import org.xydra.index.iterator.Iterators;
import org.xydra.index.query.Constraint;
import org.xydra.index.query.KeyEntryTuple;
import org.xydra.index.query.Pair;

/**
 * A {@link SortedArrayMap} put under an {@link IMapIndex}<String,E> interface.
 * 
 * @author xamde
 * 
 * @param <E>
 */
public class SortedStringMap<E> implements IMapIndex<String, E> {

	private static final long serialVersionUID = 1L;

	/**
	 * org.apache.commons.collections.FastTreeMap = 26 seconds
	 * 
	 * java.util.TreeMap = 19-20 seconds
	 */
	SortedArrayMap<String, E> map = new SortedArrayMap<String, E>();

	@Override
	public void index(final String key, final E value) {
		this.map.put(key.intern(), value);
	}

	@Override
	public void clear() {
		this.map.clear();
	}

	@Override
	public boolean isEmpty() {
		return this.map.isEmpty();
	}

	@Override
	public boolean containsKey(final String key) {
		return this.map.containsKey(key);
	}

	@Override
	public void deIndex(String key) {
		this.map.remove(key);
	}

	@Override
	public Iterator<E> iterator() {
		return this.map.values().iterator();
	}

	@Override
	public E lookup(final String key) {
		return this.map.get(key);
	}

	@Override
	public boolean containsKey(final Constraint<String> c1) {
		if (c1.isStar()) {
			return !this.map.isEmpty();
		} else {
			return this.map.containsKey(c1.getExpected());
		}
	}

	@Override
	public Iterator<KeyEntryTuple<String, E>> tupleIterator(final Constraint<String> keyConstraint) {
		Iterator<Entry<String, E>> mapentryIt = this.map.entrySet().iterator();
		mapentryIt = Iterators.filter(mapentryIt, new IFilter<Entry<String, E>>() {

			@Override
			public boolean matches(Entry<String, E> mapentry) {
				return keyConstraint.matches(mapentry.getKey());
			}
		});
		return Iterators.transform(mapentryIt,
				new ITransformer<Map.Entry<String, E>, KeyEntryTuple<String, E>>() {

					@Override
					public KeyEntryTuple<String, E> transform(Entry<String, E> mapentry) {
						return new KeyEntryTuple<String, E>(mapentry.getKey(), mapentry.getValue());
					}
				});
	}

	public Iterator<E> entryIterator(final Constraint<String> keyConstraint) {
		Iterator<Entry<String, E>> mapentryIt = this.map.entrySet().iterator();
		mapentryIt = Iterators.filter(mapentryIt, new IFilter<Entry<String, E>>() {

			@Override
			public boolean matches(Entry<String, E> mapentry) {
				return keyConstraint.matches(mapentry.getKey());
			}
		});
		return Iterators.transform(mapentryIt, new ITransformer<Map.Entry<String, E>, E>() {

			@Override
			public E transform(Entry<String, E> mapentry) {
				return mapentry.getValue();
			}
		});
	}

	@Override
	public Iterator<String> keyIterator() {
		return this.map.keySet().iterator();
	}

	/**
	 * Handy for constructing range-queries
	 * 
	 * IMPROVE deal with unicode outside of BMP
	 */
	public static final String LAST_UNICODE_CHAR = "\uFFFF";

	/**
	 * Special function of Sorted...
	 * 
	 * @param keyPrefix
	 * @return true iff at least one key has been indexed which starts with the
	 *         given keyPrefix
	 */
	public boolean containsKeysStartingWith(final String keyPrefix) {
		return lookupFirstPrefix(keyPrefix) != null;
	}

	/**
	 * @param keyPrefix
	 * @return all entries which have been indexed at a key starting with the
	 *         given prefix. Collects the results of potentially many such keys.
	 */
	public Iterator<E> lookupStartingWith(final String keyPrefix) {
		SortedMap<String, E> subMap = this.map.subMap(keyPrefix, keyPrefix + LAST_UNICODE_CHAR);
		return subMap.values().iterator();
	}

	/**
	 * This method is required for a trie, such as {@link SmallStringSetTrie}
	 * 
	 * @param keyPrefix
	 * @return the first (lowest) complete key starting with the given prefix @CanBeNull
	 *         if no such key exists.
	 */
	public String lookupFirstPrefix(final String keyPrefix) {
		// fast path hack
		if (this.map instanceof SortedArrayMap) {
			return this.map.tailMapFirstKey(keyPrefix, keyPrefix + LAST_UNICODE_CHAR);
		}

		SortedMap<String, E> subMap = this.map.subMap(keyPrefix, keyPrefix + LAST_UNICODE_CHAR);
		return subMap.isEmpty() ? null : subMap.firstKey();
	}

	public E lookupFirstValue(final String keyPrefix) {
		// fast path hack
		if (this.map instanceof SortedArrayMap) {
			return this.map.tailMapFirstEntry(keyPrefix, keyPrefix + LAST_UNICODE_CHAR);
		}

		SortedMap<String, E> subMap = this.map.subMap(keyPrefix, keyPrefix + LAST_UNICODE_CHAR);
		return subMap.isEmpty() ? null : subMap.get(subMap.firstKey());
	}

	public int size() {
		return this.map.size();
	}

	@Override
	public String toString() {
		return this.map.toString();
	}

	/**
	 * @param keyPrefix
	 * @return @CanBeNull if no entry starts with the given keyPrefix. If not
	 *         null, a Pair is returned. Pair.first: @CanBeNull, is the found
	 *         value; Pair.second: iff true, the found value is a perfect match;
	 *         iff false, it's merely a prefix match.
	 */
	public Pair<E, Boolean> findWithPrefix(final String keyPrefix) {
		// fast path hack
		if (this.map instanceof SortedArrayMap) {
			SortedArrayMap<String, E> sortedArrayMap = this.map;
			return sortedArrayMap.findWithPrefix(keyPrefix);
		}

		SortedMap<String, E> subMap = this.map.subMap(keyPrefix, keyPrefix + LAST_UNICODE_CHAR);
		if (subMap.isEmpty())
			return null;
		assert subMap.size() == 1;
		E e = subMap.values().iterator().next();
		String key = subMap.firstKey();
		if (key.equals(keyPrefix)) {
			return new Pair<E, Boolean>(e, true);
		} else {
			return new Pair<E, Boolean>(e, false);
		}
	}

	public Collection<E> values() {
		return this.map.values();
	}

}
