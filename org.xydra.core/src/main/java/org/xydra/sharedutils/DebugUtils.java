package org.xydra.sharedutils;

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

}
