package de.xam.xindex.query;

public class KeyKeyKeyEntryTuple<K, L, M, E> implements HasEntry<E> {
	
	private E entry;
	private K key1;
	private L key2;
	private M key3;
	
	public KeyKeyKeyEntryTuple(K key1, L key2, M key3, E entry) {
		this.key1 = key1;
		this.key2 = key2;
		this.key3 = key3;
		this.entry = entry;
	}
	
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
	
}
