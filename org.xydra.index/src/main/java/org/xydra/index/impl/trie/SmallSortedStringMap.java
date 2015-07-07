package org.xydra.index.impl.trie;

import java.nio.charset.Charset;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import org.xydra.index.IMapIndex;
import org.xydra.index.iterator.IFilter;
import org.xydra.index.iterator.ITransformer;
import org.xydra.index.iterator.Iterators;
import org.xydra.index.query.Constraint;
import org.xydra.index.query.KeyEntryTuple;

/**
 * A {@link TreeMap} put under an {@link IMapIndex}<String,E> interface.
 *
 * @author xamde
 *
 * @param <E>
 */
public class SmallSortedStringMap<E> implements IMapIndex<String, E> {

	private static final long serialVersionUID = 1L;

	private static final Charset UTF8 = Charset.forName("utf-8");

	private final TreeMap<byte[], E> map = new TreeMap<byte[], E>(new Comparator<byte[]>() {

		@Override
		public int compare(final byte[] a, final byte[] b) {
			for (int i = 0; i < Math.min(a.length, b.length); i++) {
				final int c = b[i] - a[i];
				if (c != 0) {
					return c;
				}
			}

			final int c = a.length - b.length;
			return c;
		}
	});

	@Override
	public void index(final String key, final E value) {
		this.map.put(toBytes(key), value);
	}

	private static byte[] toBytes(final String string) {
		return string.getBytes(UTF8);
	}

	private static String toString(final byte[] bytes) {
		return new String(bytes, UTF8);
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
		return this.map.containsKey(toBytes(key));
	}

	@Override
	public void deIndex(final String key) {
		this.map.remove(toBytes(key));
	}

	@Override
	public Iterator<E> iterator() {
		return this.map.values().iterator();
	}

	@Override
	public E lookup(final String key) {
		return this.map.get(toBytes(key));
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
		Iterator<Entry<byte[], E>> mapentryIt = this.map.entrySet().iterator();
		mapentryIt = Iterators.filter(mapentryIt, new IFilter<Entry<byte[], E>>() {

			@Override
			public boolean matches(final Entry<byte[], E> mapentry) {
				return keyConstraint.matches(SmallSortedStringMap.toString(mapentry.getKey()));
			}
		});
		return Iterators.transform(mapentryIt,
				new ITransformer<Map.Entry<byte[], E>, KeyEntryTuple<String, E>>() {

			@Override
			public KeyEntryTuple<String, E> transform(final Entry<byte[], E> mapentry) {
				return new KeyEntryTuple<String, E>(SmallSortedStringMap.toString(mapentry
						.getKey()), mapentry.getValue());
			}
		});
	}

	private static ITransformer<byte[], String> BYTE2STRING = new ITransformer<byte[], String>() {

		@Override
		public String transform(final byte[] in) {
			return SmallSortedStringMap.toString(in);
		}
	};

	@Override
	public Iterator<String> keyIterator() {
		return Iterators.transform(this.map.keySet().iterator(), BYTE2STRING);
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
		final SortedMap<byte[], E> subMap = this.map.subMap(toBytes(keyPrefix), toBytes(keyPrefix
				+ LAST_UNICODE_CHAR));
		return !subMap.isEmpty();
	}

	/**
	 * @param keyPrefix
	 * @return all entries which have been indexed at a key starting with the
	 *         given prefix. Collects the results of potentially many such keys.
	 */
	public Iterator<E> lookupStartingWith(final String keyPrefix) {
		final SortedMap<byte[], E> subMap = this.map.subMap(toBytes(keyPrefix), toBytes(keyPrefix
				+ LAST_UNICODE_CHAR));
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
		final SortedMap<byte[], E> subMap = this.map.subMap(toBytes(keyPrefix), toBytes(keyPrefix
				+ LAST_UNICODE_CHAR));
		return subMap.isEmpty() ? null : toString(subMap.firstKey());
	}

	/**
	 * @return number of key-value mappings
	 */
	public int size() {
		return this.map.size();
	}

	@Override
	public String toString() {
		return this.map.toString();
	}

}
