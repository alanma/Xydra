package org.xydra.index;

import java.util.HashSet;
import java.util.Set;

public class CollectionUtils {

	/**
	 * Same performance, regardless which is the larger set
	 *
	 * @param a
	 * @param b
	 * @return a new HashSet which contains the set-intersection of a and b
	 */
	public static <E> Set<E> intersect(final Set<E> a, final Set<E> b) {
		final Set<E> result = new HashSet<E>();
		if (a.size() < b.size()) {
			result.addAll(a);
			result.retainAll(b);
		} else {
			result.addAll(b);
			result.retainAll(a);
		}
		return result;
	}

	/**
	 * @param a
	 * @param b
	 * @return true iff both sets contain the same elements
	 */
	public static <E> boolean equalSets(final Set<E> a, final Set<E> b) {
		if (a.size() != b.size()) {
			return false;
		}

		for (final E e : a) {
			if (!b.contains(e)) {
				return false;
			}
		}
		return true;
	}

}
