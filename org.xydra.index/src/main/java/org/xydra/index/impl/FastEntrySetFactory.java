package org.xydra.index.impl;

import java.io.Serializable;

import org.xydra.index.Factory;
import org.xydra.index.IEntrySet;

/**
 * A factory for creating fast (but larger) {@link IEntrySet}s
 *
 * @author voelkel
 *
 * @param <E>
 *            entity type
 */
public class FastEntrySetFactory<E> implements Factory<IEntrySet<E>>, Serializable {

	private boolean concurrent;

	public FastEntrySetFactory() {
		this(false);
	}

	public FastEntrySetFactory(final boolean concurrent) {
		this.concurrent = concurrent;
	}

	@Override
	public IEntrySet<E> createInstance() {
		return new FastSetIndex<E>(this.concurrent);
	}

}
