/* Based on code Copyright 2009 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License. */
package org.xydra.index.impl.trie;

import java.io.Serializable;
import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedMap;

import org.xydra.annotations.LicenseApache;
import org.xydra.annotations.ModificationOperation;
import org.xydra.index.query.Pair;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;

/**
 * A memory-efficient sorted map based on two simple sorted arrays.
 *
 * Arrays resizing is decoupled from insert/remove, as long as capacity allows it.
 *
 * @param <K> the key type
 * @param <V> the value type
 */
@LicenseApache(copyright = "Copyright 2009 Google Inc.")
public class SortedArrayMap<K, V> implements SortedMap<K, V>, Serializable {

	private static final Logger log = LoggerFactory.getLogger(SortedArrayMap.class);

	private static final long serialVersionUID = 1L;

	private static final int INITIAL_TABLE_SIZE = 0;

	private abstract class BaseIterator<E> implements Iterator<E> {

		private final Object[] coModCheckKeys = SortedArrayMap.this.keys;
		private int index = SortedArrayMap.this.min;
		private int last = -1;

		@Override
		public boolean hasNext() {
			if (this.coModCheckKeys != SortedArrayMap.this.keys) {
				throw new ConcurrentModificationException();
			}
			advanceToItem();
			return this.index < SortedArrayMap.this.max();
		}

		@Override
		public E next() {
			if (!hasNext()) {
				throw new NoSuchElementException();
			}
			this.last = this.index;
			return iteratorItem(this.index++);
		}

		@Override
		public void remove() {
			if (this.last < 0) {
				throw new IllegalStateException();
			}
			if (this.coModCheckKeys != SortedArrayMap.this.keys) {
				throw new ConcurrentModificationException();
			}
			internalRemove(this.last);
			if (SortedArrayMap.this.keys[this.last] != null) {
				// Hole was plugged.
				this.index = this.last;
			}
			this.last = -1;
		}

		protected abstract E iteratorItem(int index);

		private void advanceToItem() {
			for (; this.index < SortedArrayMap.this.max(); ++this.index) {
				if (SortedArrayMap.this.keys[this.index] != null) {
					return;
				}
			}
		}
	}

	private class EntryIterator extends BaseIterator<Entry<K, V>> {
		@Override
		protected Entry<K, V> iteratorItem(final int index) {
			return new MapEntryImpl(index);
		}
	}

	private class EntrySet extends AbstractSet<Entry<K, V>> {
		@Override
		public boolean add(final Entry<K, V> entry) {
			final boolean result = !SortedArrayMap.this.containsKey(entry.getKey());
			SortedArrayMap.this.put(entry.getKey(), entry.getValue());
			return result;
		}

		@Override
		public boolean addAll(final Collection<? extends Entry<K, V>> c) {
			SortedArrayMap.this.resizeForJoin(c.size());
			return super.addAll(c);
		}

		@Override
		public void clear() {
			SortedArrayMap.this.clear();
		}

		@Override
		@SuppressWarnings("unchecked")
		public boolean contains(final Object o) {
			if (!(o instanceof Entry)) {
				return false;
			}
			final Entry<K, V> entry = (Entry<K, V>) o;
			final V value = SortedArrayMap.this.get(entry.getKey());
			return SortedArrayMap.valueEquals(value, entry.getValue());
		}

		@Override
		public int hashCode() {
			return SortedArrayMap.this.hashCode();
		}

		@Override
		public Iterator<java.util.Map.Entry<K, V>> iterator() {
			return new EntryIterator();
		}

		@Override
		@SuppressWarnings("unchecked")
		public boolean remove(final Object o) {
			if (!(o instanceof Entry)) {
				return false;
			}
			final Entry<K, V> entry = (Entry<K, V>) o;
			final int index = findKey(entry.getKey());
			if (index >= 0 && valueEquals(SortedArrayMap.this.vals[index], entry.getValue())) {
				internalRemove(index);
				return true;
			}
			return false;
		}

		@Override
		public boolean removeAll(final Collection<?> c) {
			boolean didRemove = false;
			for (final Object o : c) {
				didRemove |= remove(o);
			}
			return didRemove;
		}

		@Override
		public int size() {
			return SortedArrayMap.this.size;
		}
	}

	private class MapEntryImpl implements Entry<K, V> {
		private final int index;

		public MapEntryImpl(final int index) {
			this.index = index;
		}

		@Override
		@SuppressWarnings("unchecked")
		public boolean equals(final Object o) {
			if (!(o instanceof Entry)) {
				return false;
			}
			final Entry<K, V> entry = (Entry<K, V>) o;
			return keyEquals(getKey(), entry.getKey()) && valueEquals(getValue(), entry.getValue());
		}

		@Override
		@SuppressWarnings("unchecked")
		public K getKey() {
			return (K) SortedArrayMap.this.keys[this.index];
		}

		@Override
		@SuppressWarnings("unchecked")
		public V getValue() {
			return (V) SortedArrayMap.this.vals[this.index];
		}

		@Override
		public int hashCode() {
			return keyHashCode(getKey()) ^ valueHashCode(getValue());
		}

		@Override
		@SuppressWarnings("unchecked")
		public V setValue(final V value) {
			final V previous = (V) SortedArrayMap.this.vals[this.index];
			SortedArrayMap.this.vals[this.index] = value;
			return previous;
		}

		@Override
		public String toString() {
			return getKey() + "=" + getValue();
		}
	}

	private class KeyIterator extends BaseIterator<K> {
		@SuppressWarnings("unchecked")
		@Override
		protected K iteratorItem(final int index) {
			return (K) SortedArrayMap.this.keys[index];
		}
	}

	private class KeySet extends AbstractSet<K> {
		@Override
		public void clear() {
			SortedArrayMap.this.clear();
		}

		@Override
		public boolean contains(final Object o) {
			return SortedArrayMap.this.containsKey(o);
		}

		@Override
		public int hashCode() {
			int result = 0;
			for (int i = SortedArrayMap.this.min; i < SortedArrayMap.this.max(); ++i) {
				final Object key = SortedArrayMap.this.keys[i];
				if (key != null) {
					result += keyHashCode(key);
				}
			}
			return result;
		}

		@Override
		public Iterator<K> iterator() {
			return new KeyIterator();
		}

		@Override
		public boolean remove(final Object o) {
			final int index = findKey(o);
			if (index >= 0) {
				internalRemove(index);
				return true;
			}
			return false;
		}

		@Override
		public boolean removeAll(final Collection<?> c) {
			boolean didRemove = false;
			for (final Object o : c) {
				didRemove |= remove(o);
			}
			return didRemove;
		}

		@Override
		public int size() {
			return SortedArrayMap.this.size;
		}
	}

	private class ValueIterator extends BaseIterator<V> {
		@SuppressWarnings("unchecked")
		@Override
		protected V iteratorItem(final int index) {
			return (V) SortedArrayMap.this.vals[index];
		}
	}

	private class Values extends AbstractCollection<V> {
		@Override
		public void clear() {
			SortedArrayMap.this.clear();
		}

		@Override
		public boolean contains(final Object o) {
			return SortedArrayMap.this.containsValue(o);
		}

		@Override
		public int hashCode() {
			int result = 0;
			for (int i = SortedArrayMap.this.min; i < SortedArrayMap.this.max(); ++i) {
				if (SortedArrayMap.this.keys[i] != null) {
					result += valueHashCode(SortedArrayMap.this.vals[i]);
				}
			}
			return result;
		}

		@Override
		public Iterator<V> iterator() {
			return new ValueIterator();
		}

		@Override
		public boolean remove(final Object o) {
			if (o == null) {
				for (int i = SortedArrayMap.this.min; i < SortedArrayMap.this.max(); ++i) {
					if (SortedArrayMap.this.keys[i] != null && SortedArrayMap.this.vals[i] == null) {
						internalRemove(i);
						return true;
					}
				}
			} else {
				for (int i = SortedArrayMap.this.min; i < SortedArrayMap.this.max(); ++i) {
					if (valueEquals(SortedArrayMap.this.vals[i], o)) {
						internalRemove(i);
						return true;
					}
				}
			}
			return false;
		}

		@Override
		public boolean removeAll(final Collection<?> c) {
			boolean didRemove = false;
			for (final Object o : c) {
				didRemove |= remove(o);
			}
			return didRemove;
		}

		@Override
		public int size() {
			return SortedArrayMap.this.size;
		}
	}

	/**
	 * Backing store for all the keys. Default access to avoid synthetic accessors from inner classes.
	 */
	private Object[] keys;

	/**
	 * Number of pairs in this set. This is managed explicitly to let the arrays being longer than required. This allows
	 * for better performance if frequent add/remove operations are performed towards the end. Can also enable faster
	 * access on pre-sorted keys.
	 *
	 * Default access to avoid synthetic accessors from inner classes.
	 */
	private int size = 0;

	/**
	 * Backing store for all the values. Default access to avoid synthetic accessors from inner classes.
	 */
	private Object[] vals;

	private boolean isSubMap() {
		return this.max != -1;
	}

	/** used for sub-maps, default is 0 */
	private int min;

	/** used for sub-maps. -1 == unbounded, use size() */
	private int max;

	public SortedArrayMap() {
		this(INITIAL_TABLE_SIZE);
	}

	public SortedArrayMap(final int initialCapacity) {
		initTable(initialCapacity);
		this.min = 0;
		this.max = -1;
		this.size = 0;
	}

	/**
	 * To create sub-maps
	 *
	 * @param keys
	 * @param values
	 * @param min inclusive
	 * @param max exclusive
	 */
	private SortedArrayMap(final Object[] keys, final Object[] values, final int min, final int max) {
		assert values.length == keys.length;
		assert keys.length >= max : "requesting a sub-map with a max (" + max + ") greater than available lenght ("
				+ keys.length + ")";

		this.keys = keys;
		this.vals = values;
		this.min = min;
		this.max = max;
		this.size = max - min;

		assert this.size == 0 || this.keys[this.size - 1] != null;
	}

	public SortedArrayMap(final Map<? extends K, ? extends V> m) {
		int newCapacity = INITIAL_TABLE_SIZE;
		final int expectedSize = m.size();
		while (newCapacity * 3 < expectedSize * 4) {
			newCapacity <<= 1;
		}
		initTable(newCapacity);
		this.min = 0;
		this.max = -1;
		this.size = 0;

		putAll(m);
	}

	@Override
	@ModificationOperation
	public void clear() {
		// TODO fix for submap case
		if (isSubMap()) {
			throw new RuntimeException("not yet impl");
		}
		initTable(INITIAL_TABLE_SIZE);
		this.size = 0;
	}

	/* Respects submaps. */
	@Override
	public boolean containsKey(final Object key) {
		return findKey(key) >= 0;
	}

	@Override
	public boolean containsValue(final Object value) {
		// TODO fix for submap case
		if (isSubMap()) {
			throw new RuntimeException("not yet impl");
		}
		if (value == null) {
			for (int i = this.min; i < this.max(); ++i) {
				if (this.keys[i] != null && this.vals[i] == null) {
					return true;
				}
			}
		} else {
			for (final Object existing : this.vals) {
				if (valueEquals(existing, value)) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public Set<Entry<K, V>> entrySet() {
		return new EntrySet();
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean equals(final Object o) {
		if (!(o instanceof Map)) {
			return false;
		}
		final Map<K, V> other = (Map<K, V>) o;
		return entrySet().equals(other.entrySet());
	}

	@Override
	@SuppressWarnings("unchecked")
	public V get(final Object key) {
		final int index = findKey(key);
		return index < 0 ? null : (V) this.vals[index];
	}

	@Override
	public int hashCode() {
		int result = 0;
		for (int i = this.min; i < this.max(); ++i) {
			final Object key = this.keys[i];
			if (key != null) {
				result += keyHashCode(key) ^ valueHashCode(this.vals[i]);
			}
		}
		return result;
	}

	@Override
	public boolean isEmpty() {
		return this.size == 0;
	}

	@Override
	public Set<K> keySet() {
		return new KeySet();
	}

	@Override
	@SuppressWarnings("unchecked")
	@ModificationOperation
	public V put(final K key, final V value) {
		assert key != null;
		assert this.keys.length >= this.size;
		assert this.size == 0 || this.keys[this.size - 1] != null;

		if (log.isTraceEnabled()) {
			log.trace("Put '" + key + "' into " + Arrays.toString(this.keys) + " " + ((Object) this.keys).hashCode());
		}

		int index = binarySearch(key);
		if (index >= 0 && index < this.size) {
			// update existing value
			if (index < this.min || index > max()) {
				throw new IllegalArgumentException("Cannot put index=" + index + " outside of structural bounds ["
						+ this.min + "," + this.max + "]");
			}
			final Object previousValue = this.vals[index];
			this.vals[index] = value;
			assert this.size == 0 || this.keys[this.size - 1] != null;
			return (V) previousValue;
		} else {
			// insert new value
			if (isSubMap()) {
				throw new IllegalArgumentException(
						"Cannot insert value; have structural bounds [" + this.min + "," + this.max + "]");
			}
			index = -(index + 1);

			this.size++;
			// grow arrays if necessary
			if (this.size <= this.keys.length) {
				// fast path, re-use arrays

				// 'pre' remains as it is

				// 'post' is shifted to the right
				System.arraycopy(this.keys, index, this.keys, index + 1, this.size() - index - 1);
				System.arraycopy(this.vals, index, this.vals, index + 1, this.size() - index - 1);

				// put element
				this.keys[index] = key;
				this.vals[index] = value;
			} else {
				// slow path, create new arrays
				final Object[] oldKeys = this.keys;
				final Object[] oldVals = this.vals;
				initTable(this.size + 1);
				// copy 'pre'
				System.arraycopy(oldKeys, 0, this.keys, 0, index);
				System.arraycopy(oldVals, 0, this.vals, 0, index);

				// put element
				this.keys[index] = key;
				this.vals[index] = value;

				// copy 'post'
				System.arraycopy(oldKeys, index, this.keys, index + 1, oldKeys.length - index);
				System.arraycopy(oldVals, index, this.vals, index + 1, oldVals.length - index);
			}

			if (log.isTraceEnabled()) {
				log.trace("Done " + key + " into " + Arrays.toString(this.keys));
			}

			assert this.size == 0 || this.keys[this.size - 1] != null;
			return null;
		}
	}

	@Override
	@ModificationOperation
	public void putAll(final Map<? extends K, ? extends V> m) {
		resizeForJoin(m.size());
		for (final Entry<? extends K, ? extends V> entry : m.entrySet()) {
			put(entry.getKey(), entry.getValue());
		}
		assert this.size == 0 || this.keys[this.size - 1] != null;
	}

	@Override
	@SuppressWarnings("unchecked")
	@ModificationOperation
	public V remove(final Object key) {
		final int index = findKey(key);
		if (index < 0) {
			return null;
		}
		final Object previousValue = this.vals[index];
		internalRemove(index);
		return (V) previousValue;
	}

	@Override
	public int size() {
		return this.size;
	}

	@Override
	public String toString() {
		if (this.size == 0) {
			return stats() + " {}";
		}
		final StringBuilder buf = new StringBuilder(32 * size());
		buf.append(stats());
		buf.append(" {");

		boolean needComma = false;
		for (int i = this.min; i < this.max(); ++i) {
			final Object key = this.keys[i];
			if (key != null) {
				if (needComma) {
					buf.append(',').append(' ');
				}
				final Object value = this.vals[i];
				buf.append(key == this ? "(this Map)" : key).append('=').append(value == this ? "(this Map)" : value);
				needComma = true;
			}
		}
		buf.append('}');
		return buf.toString();
	}

	private String stats() {
		return " min=" + this.min + " max=" + this.max + " size=" + this.size + " capacity=" + this.keys.length;
	}

	@Override
	public Collection<V> values() {
		return new Values();
	}

	/**
	 * Returns whether two keys are equal for the purposes of this set.
	 */
	private static boolean keyEquals(final Object a, final Object b) {
		return a == null ? b == null : a.equals(b);
	}

	/**
	 * Returns the hashCode for a key.
	 */
	private static int keyHashCode(final Object k) {
		return k == null ? 0 : k.hashCode();
	}

	/**
	 * Returns whether two values are equal for the purposes of this set.
	 */
	private static boolean valueEquals(final Object a, final Object b) {
		return a == null ? b == null : a.equals(b);
	}

	/**
	 * Returns the hashCode for a value.
	 */
	private static int valueHashCode(final Object v) {
		return v == null ? 0 : v.hashCode();
	}

	/**
	 * Ensures the map is large enough to contain the specified number of entries. Default access to avoid synthetic
	 * accessors from inner classes.
	 */
	private @Deprecated boolean ensureSizeFor(final int expectedSize) {
		if (this.keys.length * 3 >= expectedSize * 4) {
			return false;
		}

		int newCapacity = this.keys.length << 1;
		while (newCapacity * 3 < expectedSize * 4) {
			newCapacity <<= 1;
		}

		final Object[] oldKeys = this.keys;
		final Object[] oldValues = this.vals;
		initTable(newCapacity);
		System.arraycopy(oldKeys, 0, this.keys, 0, oldKeys.length);
		System.arraycopy(oldValues, 0, this.vals, 0, oldValues.length);
		return true;
	}

	/**
	 * Respects submaps.
	 *
	 * @return the index in the key table at which a particular key resides, or -1 if the key is not in the table.
	 *         Default access to avoid synthetic accessors from inner classes.
	 */
	private int findKey(final Object k) {
		final int index = binarySearch(k);
		if (index < 0) {
			return -1;
		} else {
			return index;
		}
	}

	/**
	 * Respects submaps.
	 *
	 * @param key
	 * @return x >= 0 if value was found, -(x+1) if not found and x is insertion point. The +1 disambiguates the 0.
	 */
	private int binarySearch(final Object key) {
		assert key != null;
		if (size() == 0) {
			return -1;
		}

		// binary search
		int low = this.min;
		int high = max() - 1;

		while (true) {
			final int mid = low + high >>> 1;

			if (log.isTraceEnabled()) {
				log.trace("low=" + low + " high=" + high + " mid=" + mid + " size=" + this.size() + " keys="
						+ Arrays.toString(this.keys));
			}

			@SuppressWarnings("rawtypes") final Comparable midVal = (Comparable) this.keys[mid];
			@SuppressWarnings("unchecked") final
			// [ cmp > 0 ] [ cmp == 0 ] [ cmp < 0 ]
			int cmp = midVal.compareTo(key);

			if (cmp == 0) {
				return mid; // key found
			}

			if (low >= high) {
				assert mid == low;
				assert mid == high;

				// insertion point depends on cmp

				if (cmp < 0) {
					// insert at low + 1 (+1 offset for disambiguation from 0)
					return -(low + 1 + 1); // key not found
				} else {
					assert cmp > 0;
					// insert at low (+1 offset for disambiguation from 0)
					return -(low + 1); // key not found
				}
			}

			if (cmp < 0) {
				low = mid + 1;
			} else {
				high = mid;
			}
		}
	}

	public void dump() {
		System.out.println("Size = " + this.size);
		for (int i = this.min; i < max(); i++) {
			System.out.println(this.keys[i] + " = " + this.vals[i]);
		}
	}

	/**
	 * Returns the index in the key table at which a particular key resides, or the index of an empty slot in the table
	 * where this key should be inserted if it is not already in the table. Default access to avoid synthetic accessors
	 * from inner classes.
	 */
	int findKeyOrEmpty(final Object k) {
		final int index = binarySearch(k);
		return Math.abs(index);
	}

	/**
	 * Removes the entry at the specified index, and performs internal management to make sure we don't wind up with a
	 * hole in the table. Default access to avoid synthetic accessors from inner classes.
	 */
	private void internalRemove(final int index) {
		assert index >= this.min;
		assert index < this.max();
		assert this.keys.length >= this.size : "keys.len=" + this.keys.length + " vs. size=" + this.size;
		assert this.keys[this.size - 1] != null;

		this.keys[index] = null;
		this.vals[index] = null;
		this.size--;
		// move elements to the front
		System.arraycopy(this.keys, index + 1, this.keys, index, this.keys.length - index - 1);
		System.arraycopy(this.vals, index + 1, this.vals, index, this.vals.length - index - 1);

		// TODO implement shrinking

		if (log.isTraceEnabled()) {
			log.trace("Removed at " + index + " keys=" + Arrays.toString(this.keys) + " size=" + this.size);
		}

		assert this.size == 0 || this.keys[this.size - 1] != null;
	}

	/**
	 * Resizes this map to accommodate the minimum size required to join this map with another map. This is an
	 * optimization to prevent multiple resizes during the join operation. Naively, it would seem like we should resize
	 * to hold {@code (size + otherSize)}. However, the incoming map might have duplicates with this map; it might even
	 * be all duplicates. The correct behavior when the incoming map is all duplicates is NOT to resize, and therefore
	 * not to invalidate any iterators.
	 * <p>
	 * In practice, this strategy results in a worst-case of two resizes. In the worst case, where {@code size} and
	 * {@code otherSize} are roughly equal and the sets are completely disjoint, we might do 1 initial rehash and then 1
	 * additional rehash down the road. But this is an edge case that requires getting unlucky on both boundaries. Most
	 * of the time, we do either 1 initial rehash or 1 down the road, because doubling the capacity generally allows
	 * this map to absorb an equally-sized disjoint map.
	 */
	private boolean resizeForJoin(final int sizeOther) {
		return ensureSizeFor(Math.max(this.size, sizeOther));
	}

	private void initTable(final int capacity) {
		this.keys = new Object[capacity];
		this.vals = new Object[capacity];
	}

	@Override
	public Comparator<? super K> comparator() {
		return null;
	}

	@Override
	/** FIXME sub-map write are not always reflected back to original map */
	public SortedMap<K, V> subMap(final K fromKey, final K toKey) {
		if (isEmpty()) {
			// FIXME double-check this
			return new SortedArrayMap<K, V>(this.keys, this.vals, 0, 0);
		}

		int fromIndex = binarySearch(fromKey);
		if (fromIndex < 0) {
			fromIndex = -fromIndex - 1;
		}
		if (fromIndex >= size()) {
			fromIndex = size();
		}

		assert fromIndex >= 0 && fromIndex <= size() : "fromKey=" + fromKey + " fromIndex=" + fromIndex;

		int toIndex = binarySearch(toKey);
		if (toIndex < 0) {
			toIndex = -toIndex - 1;
		}
		if (toIndex > size()) {
			toIndex = size();
		}
		assert toIndex >= 0 && toIndex <= size() : "toKey=" + toKey + " toIndex=" + toIndex + " size()=" + size();

		// FIXME double-check this
		return new SortedArrayMap<K, V>(this.keys, this.vals, fromIndex, toIndex);
	}

	/**
	 * @param keyPrefix
	 * @return @CanBeNull if no entry starts with the given keyPrefix. If not null, a Pair is returned.
	 *         Pair.first: @CanBeNull, is the found value; Pair.second: iff true, the found value is a perfect match;
	 *         iff false, it's merely a prefix match.
	 */
	@SuppressWarnings("unchecked")
	public Pair<V, Boolean> findWithPrefix(final String keyPrefix) {
		final int i = binarySearch(keyPrefix);

		if (i == -1) {
			return null;
		}
		if (i >= 0) {
			// perfect match
			return new Pair<V, Boolean>((V) this.vals[i], true);
		} else {
			assert i < -1;
			/* the found key is longer than the keyPrefix and since keys stored here are prefix-free among each other,
			 * the found key is the only one which contains nodes with the searched prefix */
			final int insertionPoint = -(i + 1);
			return new Pair<V, Boolean>((V) this.vals[insertionPoint - 1], false);
		}
	}

	@SuppressWarnings("unchecked")
	public K tailMapFirstKey(final K fromKey, final K toKey) {
		final int index = tailMapFirstKeyIndex(fromKey, toKey);
		return (K) (index == -1 ? null : this.keys[index]);
	}

	@SuppressWarnings("unchecked")
	public V tailMapFirstEntry(final K fromKey, final K toKey) {
		final int index = tailMapFirstKeyIndex(fromKey, toKey);
		return (V) (index == -1 ? null : this.vals[index]);
	}

	/**
	 * @param fromKey
	 * @param toKey
	 * @return -1 if not found
	 */
	private int tailMapFirstKeyIndex(final K fromKey, final K toKey) {
		if (isEmpty()) {
			return -1;
		}

		int fromIndex = binarySearch(fromKey);
		if (fromIndex < 0) {
			fromIndex = -fromIndex - 1;
		}
		if (fromIndex >= size()) {
			fromIndex = size();
		}

		assert fromIndex >= 0 && fromIndex <= size() : "fromKey=" + fromKey + " fromIndex=" + fromIndex;

		int toIndex = binarySearch(toKey);
		if (toIndex < 0) {
			toIndex = -toIndex - 1;
		}
		if (toIndex > size()) {
			toIndex = size();
		}
		assert toIndex >= 0 && toIndex <= size() : "toKey=" + toKey + " toIndex=" + toIndex + " size()=" + size();

		if (fromIndex == toIndex) {
			return -1;
		}

		return fromIndex;
	}

	@Override
	/** FIXME sub-map write are not always reflected back to original map */
	public SortedMap<K, V> headMap(final K toKey) {
		if (isEmpty()) {
			// FIXME double-check this
			return new SortedArrayMap<K, V>(this.keys, this.vals, 0, 0);
		}

		int toIndex = binarySearch(toKey);
		if (toIndex < 0) {
			toIndex = -toIndex;
		}
		if (toIndex > size()) {
			toIndex = size();
		}
		assert toIndex >= 0 && toIndex <= size() : "toKey=" + toKey + " toIndex=" + toIndex + " size()=" + size();
		// FIXME double-check this
		return new SortedArrayMap<K, V>(this.keys, this.vals, 0, toIndex);
	}

	@Override
	/** FIXME sub-map write are not always reflected back to original map */
	public SortedMap<K, V> tailMap(final K fromKey) {
		if (isEmpty()) {
			// FIXME double-check this
			return new SortedArrayMap<K, V>(this.keys, this.vals, 0, 0);
		}

		int fromIndex = binarySearch(fromKey);
		if (fromIndex < 0) {
			fromIndex = -fromIndex;
		}
		if (fromIndex > size() - 1) {
			fromIndex = size() - 1;
		}

		assert fromIndex >= 0 && fromIndex <= size() - 1 : "fromKey=" + fromKey + " fromIndex=" + fromIndex;
		// FIXME double-check this
		return new SortedArrayMap<K, V>(this.keys, this.vals, fromIndex, this.size);
	}

	@SuppressWarnings("unchecked")
	@Override
	public K firstKey() {
		if (isEmpty()) {
			return null;
		}
		return (K) this.keys[this.min];
	}

	@SuppressWarnings("unchecked")
	@Override
	public K lastKey() {
		if (isEmpty()) {
			return null;
		}

		assert this.keys[this.size - 1] != null;

		final K lastKey = (K) this.keys[max() - 1];
		assert lastKey != null;
		return lastKey;
	}

	/**
	 * @return
	 */
	private int max() {
		if (this.max == -1) {
			return this.size;
		} else {
			return this.max;
		}
	}

}
