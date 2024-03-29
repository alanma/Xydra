package org.xydra.index.impl;

import java.io.Serializable;

import org.xydra.index.Factory;
import org.xydra.index.IEntrySet;

/**
 * A factory creating small (but slow) IEntrySet
 *
 * @author voelkel
 *
 * @param <E> entity type
 */
public class SmallEntrySetFactory<E> implements Factory<IEntrySet<E>>, Serializable {

	public SmallEntrySetFactory() {
		this(false);
	}

	public SmallEntrySetFactory(final boolean concurrent) {
		this.concurrent = concurrent;
	}

	final boolean concurrent;

	@Override
	public IEntrySet<E> createInstance() {
		if (this.concurrent) {
			return new ConcurrentSmallSetIndex<E>();
		} else {
			return new SmallSetIndex<E>();
		}
	}

}
