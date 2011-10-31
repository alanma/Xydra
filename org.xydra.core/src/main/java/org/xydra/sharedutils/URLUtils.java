package org.xydra.sharedutils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;


public class URLUtils {
	
	private static final String UTF8 = "utf-8";
	
	/**
	 * @param coded string containing encoded URL encoded sequences
	 * @return a string where all URL escape sequences have been converted back
	 *         to their original character representations.
	 * @throws IllegalArgumentException if could not be decoded
	 */
	public static String decode(String coded) throws IllegalArgumentException {
		try {
			return URLDecoder.decode(coded, UTF8);
		} catch(UnsupportedEncodingException e) {
			throw new AssertionError("expected UTF8 on this platform");
		}
	}
	
	/**
	 * @param raw a String
	 * @return a string where all characters that are not valid for a complete
	 *         URL have been escaped. The escaping of a character is done by
	 *         converting it into its UTF-8 encoding and then encoding each of
	 *         the resulting bytes as a %xx hexadecimal escape sequence.
	 */
	public static String encode(String raw) {
		try {
			return URLEncoder.encode(raw, UTF8);
		} catch(UnsupportedEncodingException e) {
			throw new AssertionError("expected UTF8 on this platform");
		}
	}
	
}
