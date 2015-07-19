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

}
