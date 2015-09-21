package org.xydra.index.query;

public class KeyKeyKeyEntryTuple<K, L, M, E> implements HasEntry<E> {

	private final E entry;
	private final K key1;
	private final L key2;
	private final M key3;

	public KeyKeyKeyEntryTuple(final K key1, final L key2, final M key3, final E entry) {
		this.key1 = key1;
		this.key2 = key2;
		this.key3 = key3;
		this.entry = entry;
	}

	@Override
	public E getEntry() {
		return this.entry;
	}

	public K getKey1() {
		return this.key1;
	}

	public L getKey2() {
		return this.key2;
	}

	public M getKey3() {
		return this.key3;
	}

	public K s() {
		return this.key1;
	}

	public L p() {
		return this.key2;
	}

	public M o() {
		return this.key3;
	}

}
