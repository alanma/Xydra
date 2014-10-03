package com.sonicmetrics.core.shared.query;

import java.util.HashSet;
import java.util.Set;

import org.xydra.annotations.NeverNull;

public class PropertyMetadataResult<T> {

	private final int max;

	public PropertyMetadataResult(int max) {
		this.max = max;
	}

	/**
	 * @return true if the result contains all used values in the queries
	 *         interval. If false, there were too many results and this is only
	 *         a fraction of them.
	 */
	public boolean isComplete() {
		return !isFull();
	}

	private final Set<T> usedValues = new HashSet<T>();

	public void add(T s) {
		if (!isFull()) {
			this.usedValues.add(s);
		}
	}

	private boolean isFull() {
		return this.usedValues.size() == this.max;
	}

	public @NeverNull Set<T> getUsedValues() {
		return this.usedValues;
	}
}
