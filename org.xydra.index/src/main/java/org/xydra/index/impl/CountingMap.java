package org.xydra.index.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.xydra.index.IIndex;

public class CountingMap<T> implements IIndex {

	private Map<T, Integer> map = new HashMap<T, Integer>();

	@Override
	public void clear() {
		this.map.clear();
	}

	public void count(T key) {
		Integer i = this.map.get(key);
		if (i == null) {
			this.map.put(key, 1);
		} else {
			this.map.put(key, i + 1);
		}
	}

	public Set<T> keySet() {
		return this.map.keySet();
	}

	public Set<Entry<T, Integer>> entrySet() {
		return this.map.entrySet();
	}

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

}
