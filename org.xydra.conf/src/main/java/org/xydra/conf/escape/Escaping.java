package org.xydra.conf.escape;

public class Escaping {

	static final int UNICODE_RESERVED_END = Integer.parseInt("DFFF", 16);
	static final int UNICODE_RESERVED_START = Integer.parseInt("D800", 16);

	/**
	 * @param b @NeverNull
	 * @param codepoint must be below xD800 (55296 as int)
	 */
	public static void appendAsUnicodeEscapeSequence(final StringBuilder b, final int codepoint) {
		/*
		 * let's be clever here: encoding in range 0..255 is represented as
		 * ENCODING_CHAR + 2 hex;
		 *
		 * 255.. 65535 is ENCODING_CHAR + ENCODING_CHAR + 4 hex
		 *
		 * range 65536..1114111 is represented as ENCODING_CHAR + ENCODING_CHAR
		 * + ENCODING_CHAR + 6 hex
		 */

		if (codepoint >= UNICODE_RESERVED_START && codepoint <= UNICODE_RESERVED_END) {
			throw new IllegalArgumentException("Codepoints in range xD800-xDFFF are reserved.");
		}

		if (codepoint < UNICODE_RESERVED_START) {
			// hack to get padding with leading zeroes
			b.append("\\u");
			b.append(Integer.toHexString(0x10000 | codepoint).substring(1).toUpperCase());
		} else {
			/*
			 * Wikipedia: A codepoint between 0x10000 and 0x10FFFF above the BMP
			 * is encoded as follows. 0x10000 is subtracted from the codepoint,
			 * leaving a value between 0 and 0xFFFFF (220−1). This is split into
			 * two 10-bit halves. The high-order half is added to 0xD800 (giving
			 * an encoding unit in the range 0xD800..0xDBFF), and the low-order
			 * half is added to 0xDC00 (for an encoding unit in the range
			 * 0xDC00..0xDFFF). The first encoding unit is the high surrogate,
			 * or leading surrogate; the second the low surrogate or trailing
			 * surrogate. The high surrogate followed by the low surrogate is
			 * the UTF-16 encoding of the codepoint.
			 */

			final int x = codepoint - Integer.parseInt("10000", 16);
			assert x <= Integer.parseInt("FFFFF", 16);

			final int high = x >> 10;
			final int low = x - (high << 10);

			final int surrogateHigh = high + Integer.parseInt("D800", 16);
			assert surrogateHigh >= Integer.parseInt("D800", 16);
			assert surrogateHigh <= Integer.parseInt("DBFF", 16);
			final int surrogateLow = low + Integer.parseInt("DC00", 16);
			assert surrogateLow >= Integer.parseInt("DC00", 16);
			assert surrogateLow <= Integer.parseInt("DFFF", 16);

			b.append("\\u");
			b.append(Integer.toHexString(0x10000 | surrogateHigh).substring(1).toUpperCase());
			b.append("\\u");
			b.append(Integer.toHexString(0x10000 | surrogateLow).substring(1).toUpperCase());
		}
	}

	/**
	 * Escape the characters (written as regex char group without
	 * backslash-escaping) [\\n\t\r:= ]
	 *
	 * @param raw
	 * @param escapeColonSpaceEquals TODO
	 * @param escapeQuotes TODO
	 * @return ...
	 */
	public static String escape(final String raw, final boolean escapeColonSpaceEquals, final boolean escapeQuotes) {
		assert raw != null;

		final StringBuilder esc = new StringBuilder();
		int i = 0;
		while (i < raw.length()) {
			final int c = raw.codePointAt(i);
			i += Character.charCount(c);

			switch (c) {
			case '\\':
				// simple escaping
				esc.append('\\');
				esc.appendCodePoint(c);
				break;
			case ':':
			case '=':
			case ' ':
				if (escapeColonSpaceEquals) {
					// simple escaping
					esc.append('\\');
					esc.appendCodePoint(c);
				} else {
					// no escaping
					esc.appendCodePoint(c);
				}
				break;
			case '\"':
				if (escapeQuotes) {
					// simple escaping
					esc.append('\\');
					esc.appendCodePoint(c);
				} else {
					// no escaping
					esc.appendCodePoint(c);
				}
				break;
			case '\n':
				esc.append('\\');
				esc.append('n');
				break;
			case '\t':
				esc.append('\\');
				esc.append('t');
				break;
			case '\r':
				esc.append('\\');
				esc.append('r');
				break;
			default:
				if (c < 0x0020 || c > 0x007e) {
					// unicode
					appendAsUnicodeEscapeSequence(esc, c);
				} else {
					esc.appendCodePoint(c);
				}
			}
		}

		return esc.toString();
	}

	public static String escapeUnicode(final String raw) {
		return escape(raw, false, false);
	}

	public static String escapeUnicodeAndQuotes(final String raw) {
		return escape(raw, false, true);
	}

	/**
	 * @param escaped
	 * @param i
	 * @param unescaped
	 * @param swallowBackslashNewline if true, do what Java Property File
	 *            reading mandates: when reading [backslash][newline] just emit
	 *            nothing instead. This allows long lines to broken in the
	 *            syntax and still get a single line-break free string
	 * @param escapeColonSpaceEqual if true, colon, space and equal sign are
	 *            escaped: [:] = [\\][:], [ ] = [\\][ ], [=] = [\\][=].
	 * @return number of interpreted characters
	 */
	private static int materializeBackslashEscapes(final String escaped, final int i, final StringBuilder unescaped,
			final boolean swallowBackslashNewline, final boolean escapeColonSpaceEqual) {
		final int c = escaped.codePointAt(i);
		switch (c) {
		case '\n':
			if (swallowBackslashNewline) {
				// special java property behaviour: backslash-newline = not
				// there
				break;
			} else {
				// ignore escaping and write back verbatim
				unescaped.append('\\');
				unescaped.appendCodePoint(c);
				break;
			}
		case '\\':
		case ':':
		case '=':
		case ' ':
			if (escapeColonSpaceEqual) {
				// just un-escape
				unescaped.appendCodePoint(c);
				break;
			} else {
				// ignore escaping and write back verbatim
				unescaped.append('\\');
				unescaped.appendCodePoint(c);
				break;
			}
		case 't':
			unescaped.append('\t');
			break;
		case 'r':
			unescaped.append('\r');
			break;
		case 'n':
			unescaped.append('\n');
			break;
		case 'u':
			return 1 + materializeUnicode(escaped, i + 1, unescaped);
		default:
			// ignore escaping and write back verbatim
			unescaped.append('\\');
			unescaped.appendCodePoint(c);
		}
		return 1;
	}

	/**
	 * @param escaped a string which may contain '\\', '\n', '\t', '\ r', or
	 *            Unicode '\ uXXXX' where XXXX is hex. The space is not there.
	 * @param swallowBackslashNewline if true, do what Java Property File
	 *            reading mandates: when reading [backslash][newline] just emit
	 *            nothing instead. This allows long lines to broken in the
	 *            syntax and still get a single line-break free string
	 * @param escapeColonSpaceEqual if true, colon, space and equal sign are
	 *            escaped: [:] = [\\][:], [ ] = [\\][ ], [=] = [\\][=].
	 * @return a string in which java and unicode escapes have been replaced
	 *         with the correct unicode codepoint
	 */
	public static String materializeEscapes(final String escaped, final boolean swallowBackslashNewline,
			final boolean escapeColonSpaceEqual) {
		assert escaped != null;

		final StringBuilder unescaped = new StringBuilder();
		int i = 0;
		while (i < escaped.length()) {
			final int c = escaped.codePointAt(i);
			i += Character.charCount(c);
			switch (c) {
			case '\\':
				if (i < escaped.length()) {
					// process
					i += materializeBackslashEscapes(escaped, i, unescaped,
							swallowBackslashNewline, true);
				} else {
					// we're at the end
					unescaped.appendCodePoint(c);
				}
				break;
			default:
				unescaped.appendCodePoint(c);
			}
		}

		return unescaped.toString();
	}

	public static String materializeUnicode(final String escaped) {
		final StringBuilder unescaped = new StringBuilder();
		int i = 0;
		while (i < escaped.length()) {
			final int c = escaped.codePointAt(i);
			i += Character.charCount(c);
			switch (c) {
			case '\\':
				if (i < escaped.length()) {
					// process
					final int j = materializeUnicode(escaped, i + 1, unescaped);
					if (j > 0) {
						i += j + 1;
					} else {
						// false alarm
						unescaped.appendCodePoint(c);
					}
				} else {
					// we're at the end
					unescaped.appendCodePoint(c);
				}
				break;
			default:
				unescaped.appendCodePoint(c);
			}
		}
		return unescaped.toString();
	}

	/**
	 * Surrogate pairs not supported.
	 *
	 * @param escapedSource
	 * @param i position of hex chars after the '\ u'
	 * @param unescaped
	 * @return how many chars used
	 */
	public static int materializeUnicode(final String escapedSource, final int i, final StringBuilder unescaped) {
		// try to get hex chars
		if (i + 4 > escapedSource.length()) {
			// parse nothing
			return 0;
		}

		final String hex4 = escapedSource.substring(i, i + 4);
		// TODO optimisation potential
		if (hex4.matches("[a-fA-F0-9]{4}")) {
			final int codepointNumber = Integer.parseInt(hex4, 16);
			unescaped.appendCodePoint(codepointNumber);
			return 4;
		} else {
			// there was just any string with ...'single backslash followed
			// by character u'... in it
			return 0;
		}
	}

	/**
	 * Not optimized for speed, but handy for debugging.
	 *
	 * @param s
	 * @return a very readable string
	 */
	public static String toCodepoints(final String s) {
		String res = "";
		int i = 0;
		while (i < s.length()) {
			final int c = s.codePointAt(i);
			i += Character.charCount(c);
			res += "[" + Integer.toString(c) + "='" + (char) c + "']";
		}
		return res;
	}

	public static void main(final String[] args) {
		final StringBuilder b = new StringBuilder();
		// b.append("foo");
		// appendAsUnicodeEscapeSequence(b, 254);
		// b.append("foo");
		// appendAsUnicodeEscapeSequence(b, 50000);
		b.append("foo");
		System.out.println(b);
		System.out.println(materializeUnicode("0298", 0, b));
		System.out.println("'" + b + "'");
		final String enc = escapeUnicode("Völkel");
		System.out.println(enc);
		System.out.println(materializeUnicode(enc));
	}

}
