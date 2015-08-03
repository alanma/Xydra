package org.xydra.csv.impl.memory;

import java.util.HashSet;
import java.util.Set;

public class Shared {

	/**
	 * @param concatenated
	 * @param s
	 * @return true if the set composed of items 'aaa|bbb|ccc' contains the
	 *         string 's', e.g. is s == 'aaa'.
	 */
	public static boolean contains(final String concatenated, final String s) {
		if (concatenated == null) {
			return false;
		}

		return toSet(concatenated, true, true).contains(s.trim());
	}

	public static Set<String> toSet(final String concatenated, final boolean trim, final boolean lowercase) {
		final String[] uniques = concatenated.split("[|]");
		final Set<String> uniqueSet = new HashSet<String>();
		for (String t : uniques) {
			if (trim) {
				t = t.trim();
			}
			if (lowercase) {
				t = t.toLowerCase();
			}
			uniqueSet.add(t);
		}
		return uniqueSet;

	}

	public static boolean isNotNullOrEmpty(final String q) {
		return q != null && !q.trim().equals("") && !q.equals("null");
	}

}
