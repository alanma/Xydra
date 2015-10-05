package org.xydra.index.impl;

import org.xydra.index.Factory;
import org.xydra.index.IEntrySet;

public class EntrySets {

	/**
	 * This code is neither brilliant nor fast. But at least we have a central location for it.
	 *
	 * @param a @CanBeNull
	 * @param b @CanBeNull
	 * @param entrySetFactory
	 * @return
	 */
	public static <E> IEntrySet<E> merge(final IEntrySet<E> a, final IEntrySet<E> b, final Factory<IEntrySet<E>> entrySetFactory) {
		if (a == null) {
			// no need to merge; @CanBeNull
			return b;
		} else if (b == null) {
			// no need to merge
			return a;
		}

		// merge
		final IEntrySet<E> merged = entrySetFactory.createInstance();
		for (final E e : a) {
			merged.index(e);
		}
		for (final E e : b) {
			merged.index(e);
		}
		return merged;
	}

}
