package com.sonicmetrics.core.shared.util;

import org.xydra.annotations.RunsInGWT;


/**
 * Based on parts of org.stringtree.json.JSONWriter, licensed under APL and
 * LGPL. We've chosen APL (see above). The original code was written by Frank
 * Carver.
 * 
 * @author xamde
 */
@RunsInGWT(true)
public class JsonUtils {
	
	/**
	 * @param raw
	 * @return null as 'null', every other strings as '"abc"' with proper
	 *         escaping if needed.
	 */
	public static StringBuilder string(String raw) {
		StringBuilder b = new StringBuilder();
		/* handle null */
		if(raw == null) {
			b.append("null");
			return b;
		}
		
		b.append('"');
		for(int i = 0; i < raw.length(); i++) {
			char c = raw.charAt(i);
			if(c == '"')
				b.append("\\\"");
			else if(c == '\\')
				b.append("\\\\");
			else if(c == '/')
				b.append("\\/");
			else if(c == '\b')
				b.append("\\b");
			else if(c == '\f')
				b.append("\\f");
			else if(c == '\n')
				b.append("\\n");
			else if(c == '\r')
				b.append("\\r");
			else if(c == '\t')
				b.append("\\t");
			else if(isISOControl(c)) {
				b.append(unicode(c));
			} else {
				b.append(c);
			}
		}
		b.append('"');
		return b;
		
	}
	
	/**
	 * The GWT version for Character.isISOControl(c).
	 * 
	 * Determines if the specified character is an ISO control character. A
	 * character is considered to be an ISO control character if its code is in
	 * the range '\u0000' through '\u001F' or in the range '\u007F' through
	 * '\u009F'.
	 * 
	 * 
	 * GWT's Character knows only: // isDigit(char), // isHighSurrogate(char),
	 * // isLetter(char), // isLetterOrDigit(char), // isLowerCase(char), //
	 * isLowSurrogate(char), // isSpace(char), // isSupplementaryCodePoint(int),
	 * // isSurrogatePair(char, char), // isUpperCase(char), //
	 * isValidCodePoint(int),
	 * 
	 * @param c
	 * @return true if Java would return true
	 */
	public static boolean isISOControl(char c) {
		return ('\u0000' <= c && c <= '\u001F') || ('\u007F' <= c && c <= '\u009F');
	}
	
	protected static StringBuilder unicode(char c) {
		StringBuilder b = new StringBuilder();
		
		b.append("\\u");
		int n = c;
		for(int i = 0; i < 4; ++i) {
			int digit = (n & 0xf000) >> 12;
			b.append(hex[digit]);
			n <<= 4;
		}
		return b;
	}
	
	static char[] hex = "0123456789ABCDEF".toCharArray();
	
	public static void appendKeyValue(StringBuilder b, String key, String value) {
		b.append(string(key).toString()).append(": ").append(string(value).toString());
	}
	
	public static void appendKeyValue(StringBuilder b, String key, long value) {
		b.append(string(key).toString()).append(": ").append(value);
	}
	
}
