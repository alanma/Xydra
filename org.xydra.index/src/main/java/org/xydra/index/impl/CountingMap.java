package org.xydra.index.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.xydra.index.IIndex;

/**
 * A map that maps entries to a count. Only positive counts are remembered, so
 * use {@link #deIndex(Object)} carefully.
 *
 * @author xamde
 * @param <T>
 */
public class CountingMap<T> implements IIndex {

	private final Map<T, Integer> map = new HashMap<T, Integer>();

	@Override
	public void clear() {
		this.map.clear();
	}

	/**
	 * @param key
	 */
	public void index(final T key) {
		index(key,1);
	}

	/**
	 * @param key
	 * @param increment
	 */
	public void index(final T key, final int increment) {
		final Integer i = this.map.get(key);
		if (i == null) {
			this.map.put(key, increment);
		} else {
			this.map.put(key, i + increment);
		}
	}

	/**
	 * Subtracts 1 from the count for this key; If the new count is 0, all
	 * information about the key is forgotten.
	 *
	 * @param key
	 * @return true if there was a positive count
	 */
	public boolean deIndex(final T key) {
		final Integer i = this.map.get(key);
		if (i == null) {
			return false;
		} else {
			final int newCount = i-1;
			if (newCount == 0) {
				this.map.remove(key);
			} else {
				this.map.put(key, newCount);
			}
			return true;
		}
	}

	/**
	 * @return all indexed keys. Keys that have a count == 0 are not included.
	 */
	public Set<T> keySet() {
		return this.map.keySet();
	}

	/**
	 * @return the maps entry set
	 */
	public Set<Entry<T, Integer>> entrySet() {
		return this.map.entrySet();
	}

	/**
	 * @param key
	 * @return 0 if key is unknown, exact count otherwise
	 */
	public int getCount(final T key) {
		final Integer i = this.map.get(key);
		if (i == null) {
			return 0;
		} else {
			return i;
		}
	}

	@Override
	public boolean isEmpty() {
		return this.map.isEmpty();
	}

	/**
	 * Return the highest (or lowest) n entries. Sorted by (1) their count, (2)
	 * if comparable, sorted secondary by the natural ordering of the keys.
	 *
	 * @param numberOfResults
	 * @param highestCountsFirst if false, lowest entries are returned
	 *
	 * @return @NeverNull
	 */
	public List<T> getTop_k_SortedBy(final int numberOfResults, final boolean highestCountsFirst) {

		final List<Entry<T, Integer>> list = new ArrayList<Map.Entry<T, Integer>>(this.map.size());
		list.addAll(this.map.entrySet());
		Collections.sort(list, new Comparator<Entry<T, Integer>>() {

			@SuppressWarnings({ "unchecked", "rawtypes" })
			@Override
			public int compare(final Entry<T, Integer> a, final Entry<T, Integer> b) {

				int i = b.getValue() - a.getValue();
				if (!highestCountsFirst) {
					i *= -1;
				}
				if (i != 0) {
					return i;
				}

				if (a.getKey() instanceof Comparable && b.getKey() instanceof Comparable) {
					return ((Comparable) a.getKey()).compareTo(b.getKey());
				}

				return 0;
			}
		});

		final int resSize = Math.min(list.size(), numberOfResults);
		final List<T> result = new ArrayList<T>(resSize);
		for (int i = 0; i < resSize; i++) {
			result.add(list.get(i).getKey());
		}
		return result;
	}

	/**
	 * @param countingMap
	 * @return
	 */
	public static <T> Map<T, Integer> toHashMap(final CountingMap<T> countingMap) {
		final Map<T, Integer> map = new HashMap<>();
		for( final Entry<T, Integer> e :countingMap.entrySet()) {
			map.put(e.getKey(), e.getValue());
		}
		return map;
	}

}
