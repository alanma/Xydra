package org.xydra.index.query;

public class KeyEntryTuple<K, E> extends Pair<K,E> implements HasEntry<E> {
	
	public KeyEntryTuple(K key, E entry) {
		super(key, entry);
	}
	
	public E getEntry() {
		return getSecond();
	}
	
	public K getKey() {
		return getFirst();
	}
	
}
