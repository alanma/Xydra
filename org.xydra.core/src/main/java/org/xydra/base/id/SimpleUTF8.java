package org.xydra.base.id;

public class SimpleUTF8 {

	/**
	 * @param simpleString @NeverNull
	 * @return a string using only characters a-z, A-Z, '-', '_', '.', 0-9 and
	 *         starts with a-zA-Z --> can be converted to UTF-8 by just taking
	 *         the bytes
	 */
	public static byte[] toUtf8Bytes(String simpleString) {
		byte[] bytes = new byte[simpleString.length()];
		for (int i = 0; i < bytes.length; i++) {
			char c = simpleString.charAt(i);
			assert isXmlNameChar(c);
			assert i > 0 || isXmlNameStartChar(c);
			bytes[i] = (byte) c;
		}
		return bytes;
	}

	/**
	 * @param s
	 * @return true if string s contains only characters which are the same byte
	 *         value as in UTF-8 encoding (i.e. they are in lower 127 bits of
	 *         ASCII) AND they are contained in the legal XML name char fragment
	 *         (which is a strict subset of the first 127 bits of ASCII)
	 */
	public static boolean isSimpleUtf8CompatibleString(String s) {
		if (s == null) {
			throw new IllegalArgumentException("string was null");
		}
		for (int i = 0; i < s.length();) {
			int c = s.codePointAt(i);
			i += Character.charCount(c);
			if (i == 0) {
				if (!isXmlNameStartChar((char) c)) {
					return false;
				}
			} else {
				if (!isXmlNameChar((char) c)) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Legal chars: a-z, A-Z, '_', '-', '.', 0-9
	 * 
	 * @param c
	 * @return true for valid XML 1.0 NAME_CHAR (excluding ':' colon)
	 */
	public static boolean isXmlNameChar(int c) {
		return isXmlNameStartChar(c)// .
				|| ('0' <= c && c <= '9')// .
				|| (c == '-')// .
				|| (c == '.')// .
		;
	}

	/**
	 * Legal chars: a-z, A-Z, '_',
	 * 
	 * @param c
	 * @return true for valid XML 1.0 NAMESTART_CHAR (excluding ':' colon)
	 */
	public static boolean isXmlNameStartChar(int c) {
		return ('a' <= c && c <= 'z') // .
				|| ('A' <= c && c <= 'Z')// .
				|| (c == '_')// .
		;
	}

	public static char[] toUtf8Chars(byte[] bytes) {
		char[] chars = new char[bytes.length];
		for (int i = 0; i < chars.length; i++) {
			char c = (char) bytes[i];
			assert isXmlNameChar(c) : "Otherwise simple UTF8 does not work for sure";
			assert i > 0 || isXmlNameStartChar(c);
			chars[i] = c;
		}
		return chars;
	}

	/**
	 * @param bytes
	 * @return
	 */
	public static String toUtf8String(byte[] bytes) {
		return new String(toUtf8Chars(bytes));
	}

	/**
	 * @param bytes
	 * @return true if essentially the bytes contain a valid XML 1.0 name in
	 *         us-ascii
	 */
	public static boolean isSimpleUtf8CompatibleBytes(byte[] bytes) {
		for (int i = 0; i < bytes.length; i++) {
			byte b = bytes[i];
			if (i == 0) {
				if (!isXmlNameStartChar((char) b)) {
					return false;
				}
			} else {
				if (!isXmlNameChar((char) b)) {
					return false;
				}
			}
		}
		return true;
	}

}
