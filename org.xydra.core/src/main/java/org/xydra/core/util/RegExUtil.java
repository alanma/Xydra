package org.xydra.core.util;

import java.util.regex.Pattern;

import org.xydra.annotations.RunsInGWT;


/**
 * Syntax defined in <a
 * href="http://www.w3.org/TR/xpath-functions/#regex-syntax">here</a> and most
 * is taken from <a href="http://www.w3.org/TR/xmlschema-2/#regexs">here</a>.
 */
@RunsInGWT(true)
public class RegExUtil {
	
	public static final String METACHARS = "\\|.?*+{}()[]";
	
	/**
	 * Checks for partial matches.
	 * 
	 * @param s string in which to search
	 * @param regex regular expression
	 * @param caseSensitive if false, flag "i" for case-insensitive matching is
	 *            turned on and performed in a Unicode standard way.
	 * @return true if a match of regex is found in s.
	 */
	public static boolean isFound(String s, String regex, boolean caseSensitive) {
		boolean result = Pattern
		        .compile(regex,
		                caseSensitive ? 0 : (Pattern.CASE_INSENSITIVE + Pattern.UNICODE_CASE))
		        .matcher(s).matches(); // we used 'find()' in earlier versions
		return result;
	}
	
	/**
	 * @param raw a regular expression (syntax see class comment) , never null
	 * @return an encoded regular expression
	 */
	public static String regexEncode(String raw) {
		if(raw == null)
			throw new IllegalArgumentException("raw may not be null");
		
		String result = raw;
		// escape all meta characters
		for(int i = 0; i < METACHARS.length(); i++) {
			char c = METACHARS.charAt(i);
			result = result.replace("" + c, "" + '\\' + c);
		}
		return result;
	}
	
}
