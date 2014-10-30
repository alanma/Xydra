package org.xydra.core;

import org.xydra.base.Base;
import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.rmof.XReadableModel;
import org.xydra.base.rmof.XReadableObject;
import org.xydra.base.rmof.XRevWritableObject;
import org.xydra.base.rmof.impl.XExistsRevWritableModel;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;
import org.xydra.core.model.impl.memory.MemoryModel;
import org.xydra.core.model.impl.memory.MemoryObject;

/**
 * A utility class for using {@link XId} and {@link XAddress}.
 * 
 * @author voelkel
 * @author Kaidel
 * @author dscharrer
 */
public class XX extends Base {

	/**
	 * Use {@link XCopyUtils#copyObject(XId, String, XReadableObject)} if the
	 * resulting object should not be backed by the XReadableObject.
	 * 
	 * @param actor
	 *            The session actor to use for the returned object.
	 * @param password
	 *            The password corresponding to the given actor.
	 * @param objectSnapshot
	 * @return an object with the same initial state as the given object
	 *         snapshot. The returned object may be backed by the provided
	 *         XReadableObject instance, so it should no longer be modified
	 *         directly or the behavior of the model is undefined.
	 */
	public static XObject wrap(XId actor, String password, XReadableObject objectSnapshot) {
		if (objectSnapshot instanceof XRevWritableObject) {
			return new MemoryObject(actor, password, (XRevWritableObject) objectSnapshot, null);
		} else {
			return XCopyUtils.copyObject(actor, password, objectSnapshot);
		}
	}

	/**
	 * Use {@link XCopyUtils#copyModel(XId, String, XReadableModel)} if the
	 * resulting model should not be backed by the XReadableModel.
	 * 
	 * @param actor
	 *            The session actor to use for the returned model.
	 * @param password
	 *            The password corresponding to the given actor.
	 * @param modelSnapshot
	 * @return a model with the same initial state as the given model snapshot.
	 *         The returned model may be backed by the provided XReadableModel
	 *         instance, so it should no longer be modified directly or the
	 *         behavior of the model is undefined.
	 */
	public static XModel wrap(XId actor, String password, XReadableModel modelSnapshot) {
		if (modelSnapshot instanceof XExistsRevWritableModel) {
			return new MemoryModel(actor, password, (XExistsRevWritableModel) modelSnapshot);
		} else {
			return XCopyUtils.copyModel(actor, password, modelSnapshot);
		}
	}

	private static final char ENCODING_CHAR = 'X';

	private static final String ENCODING_STRING = "" + ENCODING_CHAR;

	public static boolean isValidXmlNameStartChar(char c) {
		return

		(c == ':') ||

		(c >= 'A' && c <= 'Z') ||

		(c == '_') ||

		(c >= 'a' && c <= 'z') ||

		// a with accent
				(c >= '\u00C0' && c <= '\u00D6') ||

				(c >= '\u00D8' && c <= '\u00F6') ||

				(c >= '\u00F8' && c <= '\u02FF') ||

				(c >= '\u0370' && c <= '\u037D') ||

				(c >= '\u037F' && c <= '\u1FFF') ||

				(c >= '\u200C' && c <= '\u200D') ||

				(c >= '\u2070' && c <= '\u218F') ||

				(c >= '\u2C00' && c <= '\u2FEF') ||

				(c >= '\u2001' && c <= '\uD7FF') ||

				(c >= '\uF900' && c <= '\uFDCF') ||

				(c >= '\uFDF0' && c <= '\uFFFD');

		/*
		 * Java can handle only 16 bit unicode
		 * 
		 * (c >= '\u10000' && c <= '\uEFFFF') ||
		 */
	}

	public static boolean isValidXmlNameChar(char c) {
		return isValidXmlNameStartChar(c) ||

		(c == '-') ||

		(c == '.') ||

		(c >= '0' && c <= '9') ||

		// MIDDLE DOT
				(c == '\u00B7') ||

				(c >= '\u0300' && c <= '\u036F') ||

				(c >= '\u203F' && c <= '\u2040');
	}

	/**
	 * @param anyString
	 * @return a valid XId, if possible
	 * @throws IllegalArgumentException
	 *             if given string is null or too long
	 */
	public static XId encode(String anyString) throws IllegalArgumentException {
		if (anyString == null) {
			throw new IllegalArgumentException("'" + anyString + "' is null - cannot create XId");
		}
		if (anyString == "") {
			throw new IllegalArgumentException("'" + anyString + "' is empty - cannot create XId");
		}
		XId result;
		try {
			// pre-encode
			String s = anyString.replace(ENCODING_STRING, ENCODING_STRING
					+ toUnicodeFourDigits(ENCODING_CHAR));
			result = XX.toId(s);
		} catch (IllegalArgumentException e) {
			// string needs encoding
			StringBuffer buf = new StringBuffer();
			char first = anyString.charAt(0);
			if (first != ENCODING_CHAR && isValidXmlNameStartChar(first)) {
				// valid
				addDirect(buf, first);
			} else {
				// char needs encoding
				addEncoded(buf, first);
			}

			for (int i = 1; i < anyString.length(); i++) {
				char c = anyString.charAt(i);
				if (c != ENCODING_CHAR && isValidXmlNameChar(c)) {
					// valid
					addDirect(buf, c);
				} else {
					// char needs encoding
					addEncoded(buf, c);
				}

			}
			String s = buf.toString();
			result = XX.toId(s);
		}
		return result;
	}

	public static String toUnicodeFourDigits(char c) {
		return ("" + (10000 + c)).substring(1);
	}

	public static String decode(XId encoded) {
		String s = encoded.toString();
		if (s.contains(ENCODING_STRING)) {
			StringBuffer buf = new StringBuffer();
			for (int i = 0; i < s.length(); i++) {
				char c = s.charAt(i);
				if (c == ENCODING_CHAR) {
					// ignore this one

					// read next four
					String value = s.substring(i + 1, i + 1 + 4);
					int unicodeValue = Integer.parseInt(value);
					buf.append((char) unicodeValue);
					i += 4;
				} else {
					buf.append(c);
				}
			}
			return buf.toString();
		} else {
			return s;
		}
	}

	private static void addEncoded(StringBuffer buf, char c) {
		buf.append(ENCODING_CHAR + toUnicodeFourDigits(c));
	}

	private static void addDirect(StringBuffer buf, char c) {
		buf.append(c);
	}

	public static void main(String[] args) {
		System.out.println(decode(encode("foo")));
		System.out.println(decode(encode("öäü")));
		System.out.println(encode("XXX") + " == " + decode(encode("XXX")));
		System.out.println(encode("01234567890"));
	}

}
