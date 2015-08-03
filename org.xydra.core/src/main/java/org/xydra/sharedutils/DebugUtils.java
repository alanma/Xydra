package org.xydra.sharedutils;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.xydra.annotations.RunsInGWT;

@RunsInGWT(true)
public class DebugUtils {

	/**
	 * Print stack trace of current thread to standard error stream
	 */
	public static void dumpStacktrace() {
		try {
			throw new RuntimeException("CALLER");
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	public static <T> Set<T> toSet(final Iterator<T> iterator) {
		final HashSet<T> set = new HashSet<T>();
		while (iterator.hasNext()) {
			set.add(iterator.next());
		}
		return set;
	}

	public static String toIndent(final String indent, final int count) {
		final StringBuffer buf = new StringBuffer();
		for (int i = 0; i < count; i++) {
			buf.append(indent);
		}
		return buf.toString();
	}

	/**
	 * @param label
	 * @param booleanFlag
	 * @return a compact string for debugging
	 */
	public static String flagToString(final String label, final boolean booleanFlag) {
		return (booleanFlag ? "+" : "-") + label;
	}

	public static <T> String toString(final Iterable<T> c) {
		return toString(c.iterator());
	}

	public static <T> String toString(final Iterator<T> it) {
		final StringBuffer buf = new StringBuffer();
		buf.append("{");
		while (it.hasNext()) {
			final T t = it.next();
			buf.append(t.toString());
			if (it.hasNext()) {
				buf.append(", ");
			}
		}
		buf.append("}");
		return buf.toString();
	}
}
