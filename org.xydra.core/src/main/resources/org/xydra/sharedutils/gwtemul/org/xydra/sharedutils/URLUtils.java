package org.xydra.sharedutils;

import com.google.gwt.http.client.URL;


/**
 * Replacement on GWT for {@link org.xydra.sharedutils.URLUtils}.
 * 
 * @author xamde
 */
public class URLUtils {
	
	/**
	 * @param coded string containing encoded URL encoded sequences - can be null
	 * @return a string where all URL escape sequences have been converted back
	 *         to their original character representations.
	 * @throws IllegalArgumentException if could not be decoded
	 */
	public static String decode(String coded) throws IllegalArgumentException {
		if (coded == null) return null;
		return URL.decode(coded);
	}
	
	/**
	 * @param raw a String - can be null
	 * @return a string where all characters that are not valid for a complete
	 *         URL have been escaped. The escaping of a character is done by
	 *         converting it into its UTF-8 encoding and then encoding each of
	 *         the resulting bytes as a %xx hexadecimal escape sequence.
	 */
	public static String encode(String raw) {
		if (raw == null) return null;
		return URL.encode(raw);
	}
	
}
