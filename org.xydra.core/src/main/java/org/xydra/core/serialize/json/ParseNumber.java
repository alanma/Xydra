package org.xydra.core.serialize.json;

public class ParseNumber {

	/*
	 * work around Blackberry bug
	 * http://code.google.com/p/google-web-toolkit/issues/detail?id=7291
	 *
	 * by using Long.parseLong instead
	 */
	public static int parseInt(final String s) {
		return (int)Long.parseLong(s);
	}

	public static int parseInt(final String s, final int radix) {
		return (int)Long.parseLong(s, radix);
	}
}
