package org.xydra.index.impl;

import org.xydra.index.Factory;
import org.xydra.index.IEntrySet;

/**
 * A factory for creating fast, weak (but larger) {@link IEntrySet}s
 * 
 * @author voelkel
 * 
 * @param <E> entity type
 */
public class FastWeakEntrySetFactory<E> implements Factory<IEntrySet<E>> {

	private boolean concurrent;

	public FastWeakEntrySetFactory() {
		this(false);
	}

	public FastWeakEntrySetFactory(boolean concurrent) {
		this.concurrent = concurrent;
	}

	@Override
	public IEntrySet<E> createInstance() {
		return new FastWeakSetIndex<E>(this.concurrent);
	}

}
