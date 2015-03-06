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

	private Map<T, Integer> map = new HashMap<T, Integer>();

	@Override
	public void clear() {
		this.map.clear();
	}

	public void index(T key) {
		Integer i = this.map.get(key);
		if (i == null) {
			this.map.put(key, 1);
		} else {
			this.map.put(key, i + 1);
		}
	}

	/**
	 * Subtracts 1 from the count for this key; If the new count is 0, all
	 * information about the key is forgotten.
	 * 
	 * @param key
	 * @return true if there was a positive count
	 */
	public boolean deIndex(T key) {
		Integer i = this.map.get(key);
		if (i == null) {
			return false;
		} else {
			int newCount = -1;
			if (newCount == 0) {
				this.map.remove(key);
			} else {
				this.map.put(key, newCount);
			}
			return true;
		}
	}

	public Set<T> keySet() {
		return this.map.keySet();
	}

	public Set<Entry<T, Integer>> entrySet() {
		return this.map.entrySet();
	}

	/**
	 * @param key
	 * @return 0 if key is unknown, exact count otherwise
	 */
	public int getCount(T key) {
		Integer i = this.map.get(key);
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
	public List<T> getTop_k_SortedBy(int numberOfResults, final boolean highestCountsFirst) {

		List<Entry<T, Integer>> list = new ArrayList<Map.Entry<T, Integer>>(this.map.size());
		list.addAll(this.map.entrySet());
		Collections.sort(list, new Comparator<Entry<T, Integer>>() {

			@SuppressWarnings({ "unchecked", "rawtypes" })
			@Override
			public int compare(Entry<T, Integer> a, Entry<T, Integer> b) {

				int i = b.getValue() - a.getValue();
				if (!highestCountsFirst) {
					i *= -1;
				}
				if (i != 0)
					return i;

				if (a.getKey() instanceof Comparable && b.getKey() instanceof Comparable) {
					return ((Comparable) a).compareTo(b);
				}

				return 0;
			}
		});

		int resSize = Math.min(list.size(), numberOfResults);
		List<T> result = new ArrayList<T>(resSize);
		for (int i = 0; i < resSize; i++) {
			result.add(list.get(i).getKey());
		}
		return result;
	}
}