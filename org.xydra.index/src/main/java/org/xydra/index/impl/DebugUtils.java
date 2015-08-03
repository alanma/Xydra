package org.xydra.index.impl;

public class DebugUtils {

	/**
	 * @param object
	 * @CanBeNull
	 * @param maxLen use -1 for unlimited
	 * @return a String with max len 'maxLen'
	 */
	public static String toLimitedString(final Object object, final int maxLen) {
		if (object == null) {
			return "-null-";
		}
		final String s = object.toString();
		return toLimited(s, maxLen);
	}

	public static String toLimited(final String s, final int maxLen) {
		assert s != null;
		if (maxLen >= 0 && s.length() > maxLen) {
			if (maxLen >= "[...]".length()) {
				return s.substring(0, maxLen - "[...]".length()) + "[...]";
			} else {
				// return strictly cut off
				return s.substring(0, maxLen);
			}
		} else {
			return s;
		}
	}

}
