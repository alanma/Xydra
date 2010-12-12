package org.xydra.wikisyntax;

/**
 * Syntax defined in <a
 * href="http://www.w3.org/TR/xpath-functions/#regex-syntax">here</a> and most
 * is taken from <a href="http://www.w3.org/TR/xmlschema-2/#regexs">here</a>.
 */
public class RegExUtil {
	
	public static final String METACHARS = "\\|.?*+{}()[]";
	
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
