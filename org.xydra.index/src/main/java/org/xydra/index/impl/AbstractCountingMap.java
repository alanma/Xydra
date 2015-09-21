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
 * A map that maps entries to a count. Only positive counts are remembered, so use {@link #deIndex(Object)} carefully.
 *
 * @author xamde
 * @param <T>
 */
public abstract class AbstractCountingMap<T, N> implements IIndex {

	/**
	 * @param countingMap
	 * @return
	 */
	public static <T, N> Map<T, N> toHashMap(final AbstractCountingMap<T, N> countingMap) {
		final Map<T, N> map = new HashMap<>();
		for (final Entry<T, N> e : countingMap.entrySet()) {
			map.put(e.getKey(), e.getValue());
		}
		return map;
	}

	private final Map<T, N> map = new HashMap<T, N>();

	protected abstract N add(N i, N increment);

	@Override
	public void clear() {
		this.map.clear();
	}

	public boolean containsKey(final T key) {
		return this.map.containsKey(key);
	}

	private Comparator<? super Entry<?, N>> createEntryComparator(final boolean highestCountsFirst) {
		return new Comparator<Entry<?, N>>() {
			@SuppressWarnings({ "unchecked", "rawtypes" })
			@Override
			public int compare(final Entry<?, N> a, final Entry<?, N> b) {
				int i = toSignumInt(subtract(b.getValue(), a.getValue()));
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
		};
	}


	/**
	 * Subtracts 1 from the count for this key; If the new count is 0, all information about the key is forgotten.
	 *
	 * @param key
	 * @return true if there was a positive count
	 */
	public boolean deIndex(final T key) {
		final N i = this.map.get(key);
		if (i == null) {
			return false;
		} else {
			final N newCount = add(i, minusOne());
			if (equalsZero(newCount)) {
				this.map.remove(key);
			} else {
				this.map.put(key, newCount);
			}
			return true;
		}
	}

	/**
	 * @return the maps entry set
	 */
	public Set<Entry<T, N>> entrySet() {
		return this.map.entrySet();
	}

	protected abstract boolean equalsZero(N a);

	/**
	 * @param key
	 * @return 0 if key is unknown, exact count otherwise
	 */
	public N getCount(final T key) {
		final N i = this.map.get(key);
		if (i == null) {
			return zero();
		} else {
			return i;
		}
	}

	/**
	 * Return the highest (or lowest) n entries. Sorted by (1) their count, (2) if comparable, sorted secondary by the
	 * natural ordering of the keys.
	 *
	 * @param numberOfResults
	 * @param highestCountsFirst if false, lowest entries are returned
	 *
	 * @return @NeverNull
	 */
	public List<T> getTop_k_SortedBy(final int numberOfResults, final boolean highestCountsFirst) {
		final List<Entry<T, N>> list = new ArrayList<Map.Entry<T, N>>(this.map.size());
		list.addAll(this.map.entrySet());
		final Comparator<? super Entry<?, N>> comp = createEntryComparator(highestCountsFirst);
		Collections.sort(list, comp);

		final int resSize = Math.min(list.size(), numberOfResults);
		final List<T> result = new ArrayList<T>(resSize);
		for (int i = 0; i < resSize; i++) {
			result.add(list.get(i).getKey());
		}
		return result;
	}

	/**
	 * @param highestCountsFirst
	 * @return all results that are as good as the first
	 */
	public List<T> getTop_SortedBy(final boolean highestCountsFirst) {
		final List<Entry<T, N>> list = new ArrayList<Map.Entry<T, N>>(this.map.size());
		list.addAll(this.map.entrySet());
		final Comparator<? super Entry<?, N>> comp = createEntryComparator(highestCountsFirst);
		Collections.sort(list, comp);

		if (list.isEmpty()) {
			return Collections.EMPTY_LIST;
		}

		final N best = list.get(0).getValue();
		final List<T> result = new ArrayList<T>();
		int i = 0;
		while (i < list.size() && list.get(i).getValue() == best) {
			result.add(list.get(i).getKey());
			i++;
		}
		return result;
	}

	/**
	 * @param key
	 */
	public void index(final T key) {
		index(key, plusOne());
	}

	/**
	 * @param key
	 * @param increment
	 */
	public void index(final T key, final N increment) {
		final N i = this.map.get(key);
		if (i == null) {
			this.map.put(key, increment);
		} else {
			this.map.put(key, add(i, increment));
		}
	}

	@Override
	public boolean isEmpty() {
		return this.map.isEmpty();
	}

	/**
	 * @return all indexed keys. Keys that have a count == 0 are not included.
	 */
	public Set<T> keySet() {
		return this.map.keySet();
	}

	protected abstract N minusOne();

	protected abstract N plusOne();

	public int size() {
		return this.map.size();
	}

	protected abstract N subtract(N a, N b);

	protected abstract int toSignumInt(N a);

	@Override
	public String toString() {
		final StringBuilder b = new StringBuilder();

		for (final Entry<T, N> e : this.map.entrySet()) {
			b.append(e.getValue() + " : " + e.getKey() + "\n");
		}

		return b.toString();
	}

	protected abstract N zero();

}
