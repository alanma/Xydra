package org.xydra.base.id;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public class SimpleUTF8 {

	/**
	 * @param anyString @NeverNull
	 * @return utf-8 encoding
	 */
	public static byte[] toUtf8Bytes_fromAnyString(final String anyString) {
		if (isSimpleUtf8CompatibleString(anyString)) {
			return toUtf8Bytes_fromSimpleString(anyString);
		} else {
			try {
				return anyString.getBytes("UTF-8");
			} catch (final UnsupportedEncodingException e) {
				throw new RuntimeException("Error", e);
			}
		}
	}

	/**
	 * @param simpleString @NeverNull
	 * @return a string using only characters a-z, A-Z, '-', '_', '.', 0-9 and
	 *         starts with a-zA-Z --> can be converted to UTF-8 by just taking
	 *         the bytes
	 */
	public static byte[] toUtf8Bytes_fromSimpleString(final String simpleString) {
		final byte[] bytes = new byte[simpleString.length()];
		for (int i = 0; i < bytes.length; i++) {
			final char c = simpleString.charAt(i);
			assert isXmlNameCharAndWithinAscii(c) : "Non-xmlnamechar '" + c + "' in '"
					+ simpleString + "'";
			assert i > 0 || isXmlNameStartCharAndWithinAscii(c);
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
	public static boolean isSimpleUtf8CompatibleString(final String s) {
		if (s == null) {
			throw new IllegalArgumentException("string was null");
		}
		for (int i = 0; i < s.length();) {
			final int c = s.codePointAt(i);
			i += Character.charCount(c);
			if (i == 0) {
				if (!isXmlNameStartCharAndWithinAscii((char) c)) {
					return false;
				}
			} else {
				if (!isXmlNameCharAndWithinAscii((char) c)) {
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
	public static boolean isXmlNameCharAndWithinAscii(final int c) {
		return isXmlNameStartCharAndWithinAscii(c)// .
				|| '0' <= c && c <= '9'// .
				|| c == '-'// .
				|| c == '.'// .
		;
	}

	/**
	 * Legal chars: a-z, A-Z, '_',
	 *
	 * @param c
	 * @return true for valid XML 1.0 NAMESTART_CHAR (excluding ':' colon)
	 */
	public static boolean isXmlNameStartCharAndWithinAscii(final int c) {
		return 'a' <= c && c <= 'z' // .
				|| 'A' <= c && c <= 'Z'// .
				|| c == '_'// .
		;
	}

	public static char[] toUtf8Chars_fromAnyBytes(final byte[] bytes) {

		if (isSimpleUtf8CompatibleBytes(bytes)) {
			return toUtf8Chars_fromSimpleBytes(bytes);
		} else {

			final ByteBuffer buf = ByteBuffer.wrap(bytes);
			return Charset.forName("UTF-8").decode(buf).array();
		}
	}

	public static char[] toUtf8Chars_fromSimpleBytes(final byte[] bytes) {
		final char[] chars = new char[bytes.length];
		for (int i = 0; i < chars.length; i++) {
			final char c = (char) bytes[i];
			assert isXmlNameCharAndWithinAscii(c) : "Otherwise simple UTF8 does not work for sure";
			assert i > 0 || isXmlNameStartCharAndWithinAscii(c);
			chars[i] = c;
		}
		return chars;
	}

	/**
	 * @param bytes
	 * @return
	 */
	public static String toUtf8String(final byte[] bytes) {
		return new String(toUtf8Chars_fromAnyBytes(bytes));
	}

	/**
	 * @param bytes
	 * @return true if essentially the bytes contain a valid XML 1.0 name in
	 *         us-ascii
	 */
	public static boolean isSimpleUtf8CompatibleBytes(final byte[] bytes) {
		for (int i = 0; i < bytes.length; i++) {
			final byte b = bytes[i];
			if (i == 0) {
				if (!isXmlNameStartCharAndWithinAscii((char) b)) {
					return false;
				}
			} else {
				if (!isXmlNameCharAndWithinAscii((char) b)) {
					return false;
				}
			}
		}
		return true;
	}

}
