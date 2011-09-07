package org.xydra.index.query;

public class KeyKeyEntryTuple<K, L, E> implements HasEntry<E> {
	
	private E entry;
	private K key1;
	private L key2;
	
	public KeyKeyEntryTuple(K key1, L key2, E entry) {
		this.key1 = key1;
		this.key2 = key2;
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
	
	@Override
	public String toString() {
		return "(" + this.key1 + "," + this.key2 + "," + this.entry + ")";
	}
	
	@Override
	public int hashCode() {
		return this.key1.hashCode() + this.key2.hashCode() + this.entry.hashCode();
	}
	
	@Override
	public boolean equals(Object other) {
		return other instanceof KeyKeyEntryTuple<?,?,?>

		&& this.key1.equals(((KeyKeyEntryTuple<?,?,?>)other).getKey1())

		&& this.key2.equals(((KeyKeyEntryTuple<?,?,?>)other).getKey2())

		&& this.entry.equals(((KeyKeyEntryTuple<?,?,?>)other).getEntry());
	}
	
}
