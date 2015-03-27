package org.xydra.base.id;

import org.xydra.annotations.RunsInGWT;
import org.xydra.base.XId;
import org.xydra.base.XIdProvider;
import org.xydra.core.XX;
import org.xydra.index.impl.IntegerRangeIndex;

/**
 * Static methods.
 * 
 * Encodes strings as XIds (within possible limits)
 * 
 * @author xamde
 */
@RunsInGWT(false)
// TODO make it work in GWT
public class XidCodec {

	/** May not be one of the lower-case hex characters! */
	private static final int ENCODING_CHAR = '_';

	/** just here as documentation */
	@SuppressWarnings("unused")
	private static final int maxUnicodeCodepoint = Integer.parseInt("10FFFF", 16);

	static IntegerRangeIndex NEEDS_NO_ENCODING_firstChar;

	static IntegerRangeIndex NEEDS_NO_ENCODING_nthChar;

	static {
		NEEDS_NO_ENCODING_firstChar = new IntegerRangeIndex();
		NEEDS_NO_ENCODING_firstChar.addAll(BaseStringIDProvider.RANGEINDEX_nameStartChar);
		assert BaseStringIDProvider.RANGEINDEX_nameStartChar.isInInterval(ENCODING_CHAR);
		NEEDS_NO_ENCODING_firstChar.deIndex(ENCODING_CHAR, ENCODING_CHAR);

		NEEDS_NO_ENCODING_nthChar = new IntegerRangeIndex();
		NEEDS_NO_ENCODING_nthChar.addAll(BaseStringIDProvider.RANGEINDEX_nameChar);
		assert BaseStringIDProvider.RANGEINDEX_nameChar.isInInterval(ENCODING_CHAR);
		NEEDS_NO_ENCODING_nthChar.deIndex(ENCODING_CHAR, ENCODING_CHAR);
	}

	private static void appendEncoded(StringBuilder b, int codepoint) {
		/*
		 * let's be clever here: encoding in range 0..255 is represented as
		 * ENCODING_CHAR + 2 hex;
		 * 
		 * 255.. 65535 is ENCODING_CHAR + ENCODING_CHAR + 4 hex
		 * 
		 * range 65536..1114111 is represented as ENCODING_CHAR + ENCODING_CHAR
		 * + ENCODING_CHAR + 6 hex
		 */

		if (codepoint <= 255) {
			b.appendCodePoint(ENCODING_CHAR);
			// hack to get padding with leading zeroes
			b.append(Integer.toHexString(0x100 | codepoint).substring(1));

		} else if (codepoint <= 65535) {
			b.appendCodePoint(ENCODING_CHAR);
			b.appendCodePoint(ENCODING_CHAR);
			// hack to get padding with leading zeroes
			b.append(Integer.toHexString(0x10000 | codepoint).substring(1));
		} else {
			b.appendCodePoint(ENCODING_CHAR);
			b.appendCodePoint(ENCODING_CHAR);
			b.appendCodePoint(ENCODING_CHAR);
			b.append(Integer.toHexString(0x1000000 | codepoint).substring(1));
		}

	}

	/**
	 * Decode a potentially XidEncoded string. XidEncoding uses '_hh' where h is
	 * a hex character. Or '__hhhh' or '___hhhhhh'. The string might also
	 * contain '_' for other reasons. So if this method is invoked on a
	 * non-encoded string containing '_'s, the original string should be
	 * returned.
	 * 
	 * @param encId @NeverNull
	 * @return a decoded idString
	 */
	public static String decode(String encId) {
		assert encId != null && encId.length() > 0;

		StringBuilder b = new StringBuilder();

		int i = 0;
		while (i < encId.length()) {
			int codePoint = encId.codePointAt(i);

			if (codePoint == ENCODING_CHAR) {
				/* find out how many hex chars to expect */
				int markers = 0;
				do {
					i += Character.charCount(codePoint);
					if (i >= encId.length()) {
						// was not really a xid-encoded id
						return encId;
					}
					codePoint = encId.codePointAt(i);
					markers++;
				} while (codePoint == ENCODING_CHAR);
				// we moved past the markers

				if (markers > 3) {
					throw new IllegalArgumentException("String has too many '" + ENCODING_CHAR
							+ "', we allow at most 3 for the hex-encoding. String is '" + encId
							+ "'");
				}
				int expectHex = 2 * markers;
				// hex chars are code point count = char count
				if (i - Character.charCount(codePoint) + expectHex < encId.length()) {
					String hex = encId.substring(i, i + expectHex);
					i += expectHex;
					try {
						int decodedCodepoint = Integer.parseInt(hex, 16);
						b.appendCodePoint(decodedCodepoint);
					} catch (NumberFormatException e) {
						// not really encoded? copy verbatim.
						// was not really a xid-encoded id
						return encId;
					} catch (IllegalArgumentException e) {
						// not really encoded? copy verbatim.
						// was not really a xid-encoded id
						return encId;
					}
				} else {
					// cannot be a proper hex encoding, roll-back
					// was not really a xid-encoded id
					return encId;
				}
			} else {
				// copy over
				b.appendCodePoint(codePoint);
				i += Character.charCount(codePoint);
			}
		}
		return b.toString();
	}

	/**
	 * Helps debugging
	 * 
	 * @param xid
	 * @return the decoded id string
	 */
	public static String decodeFromXId(XId xid) {
		String s = xid.toString();

		if (s == null || s.length() == 0)
			throw new IllegalArgumentException("Cannot decode null or empty");

		return decode(s);
	}

	public static String encode(String s, int maxLen) {
		StringBuilder enc = new StringBuilder();

		// first char is pretty strict
		int firstCodepoint = s.codePointAt(0);
		if (NEEDS_NO_ENCODING_firstChar.isInInterval(firstCodepoint)) {
			// first char is legal, copy over
			enc.appendCodePoint(firstCodepoint);
		} else {
			// needs encoding
			appendEncoded(enc, firstCodepoint);
		}

		// nth chars are a bit more liberal
		int i = Character.charCount(firstCodepoint);
		while (i < s.length() && enc.length() < maxLen) {
			int nthCodepoint = s.codePointAt(i);
			if (NEEDS_NO_ENCODING_nthChar.isInInterval(nthCodepoint)) {
				// copy over
				enc.appendCodePoint(nthCodepoint);
			} else {
				// needs encoding
				appendEncoded(enc, nthCodepoint);
			}
			i += Character.charCount(nthCodepoint);
		}

		// drastic: truncate to max length
		if (enc.length() > maxLen) {
			enc.setLength(maxLen);
		}

		if (enc.length() > XIdProvider.MAX_LENGTH) {
			throw new IllegalArgumentException("Input string '" + s
					+ "' results in overly long encoded string '" + enc
					+ "', cannot create XId from it.");
		}

		String encString = enc.toString();
		assert MemoryStringIDProvider.isValidId(encString) : "Failed to encode '" + s + "' as '"
				+ encString + "'";
		return encString;
	}

	/**
	 * Encodes only those characters that need it.
	 * 
	 * @param s @NeverNull
	 * @param maxLen use a number longer than {@link XIdProvider#MAX_LENGTH} to
	 *            get an exception when string is too long
	 * @return s encoded @NeverNull
	 * @throws IllegalArgumentException if input string is null, empty or too
	 *             long
	 */
	public static XId encodeAsXId(String s, int maxLen) throws IllegalArgumentException {
		if (s == null) {
			throw new IllegalArgumentException("Given string is null");
		}
		if (s.length() == 0) {
			throw new IllegalArgumentException("Given string is empty");
		}

		String encString = encode(s, maxLen);
		// IMPROVE do without checks
		return XX.toId(encString);
	}

	public static void main(String[] args) {

		System.out.println("159 = '" + ((char) 159) + "' = a unicode control character");
		System.out.println("159 = '" + ((char) 159) + "'");

		StringBuilder b = new StringBuilder();
		appendEncoded(b, 1);
		b.append(" | ");
		appendEncoded(b, 1042);
		b.append(" | ");
		appendEncoded(b, 65699);
		System.out.println(b);

		System.out.println(XidCodec.encodeAsXId("40", XIdProvider.MAX_LENGTH));

		String[] s = new String[] {

		"Self-organized+Reuse+of+Software+Engineering+Knowledge+Supported+by+Semantic+Wikis",
				"CallAutomata?)>\\" };
		for (String a : s) {
			System.out.println(XidCodec.encodeAsXId(a, XIdProvider.MAX_LENGTH));
		}
		System.out.println(decode("_30install"));
		System.out.println(decode("_meta"));
	}

	// // combine truncated (=easy debugging) + hash (=better
	// uniqueness)
	//
	// // encoding produces less than 2 chars for one
	// String surrogate = s.substring(0, (XIdProvider.MAX_LENGTH / 2) -
	// 20) + "_"
	// + s.hashCode();
	// enc = encode(surrogate);

	// byte[] base64 = Base64.urlDecode(dec);
	// try {
	// dec = new String(base64, UTF8);
	// } catch(UnsupportedEncodingException e) {
	// throw new AssertionError();
	// }
	// enc = Base64.urlEncode(s.getBytes(UTF8));
}
