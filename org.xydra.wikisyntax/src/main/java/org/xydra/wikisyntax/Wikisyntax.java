package org.xydra.wikisyntax;

/**
 * Parses a mix of a subset of MediaWiki and JSPWiki.
 * 
 * @author User
 * 
 */
public class Wikisyntax {
	
	/**
	 * Inline.
	 */
	public static final String bold = RegExUtil.regexEncode("__");
	/**
	 * Inline.
	 */
	public static final String italic = RegExUtil.regexEncode("''");
	/**
	 * Inline.
	 */
	public static final String linkStart = RegExUtil.regexEncode("[");
	/**
	 * Inline.
	 */
	public static final String linkEnd = RegExUtil.regexEncode("]");
	/**
	 * Inline. A separator that separates the URL in a link from a human
	 * readable label. We allow space (as in MediaWiki) and pipe (as in JSPWiki)
	 */
	public static final String linkLabelSep = "(?: |" + RegExUtil.regexEncode("|") + ")";
	
	/**
	 * Begin of line.
	 */
	public static final String item = RegExUtil.regexEncode("*") + " ?";
	
	/**
	 * Begin of line.
	 */
	public static final String h2 = RegExUtil.regexEncode("==") + "|"
	        + RegExUtil.regexEncode("!!!");
	
	public static final String h3 = RegExUtil.regexEncode("===") + "|"
	        + RegExUtil.regexEncode("!!");
	
	public static final String h4 = RegExUtil.regexEncode("====") + "|"
	        + RegExUtil.regexEncode("!");
	
	public static final String START_OF_LINE = "^";
	public static final String END_OF_LINE = "$";
	
	private static final String MULTILINEMODE = "(?sm)";
	
	/**
	 * Parses
	 * 
	 * <pre>
	 * __bold__ and ''italic''
	 * </pre>
	 * 
	 * @param wikisyntax
	 * @return
	 */
	public static String toHtml(String wikisyntax) {
		String result = wikisyntax;
		
		// begin of line stuff
		result = result.replaceAll(MULTILINEMODE + START_OF_LINE + item + "(.*?)" + END_OF_LINE,
		        "<li>$1</li>");
		
		// wrap lists in <ul>
		result = result.replaceAll(MULTILINEMODE + "(" + START_OF_LINE + "<li>.*?</li>" + "\\s*"
		        + ")++", "<ul>$0</ul>");
		
		// inline stuff
		result = result.replaceAll(MULTILINEMODE + bold + "(.*?)" + bold, "<strong>$1</strong>");
		result = result.replaceAll(MULTILINEMODE + italic + "(.*?)" + italic, "<emph>$1</emph>");
		// links with label
		
		result = result.replaceAll(MULTILINEMODE + linkStart + "(.+?)" + linkLabelSep + "(.+?)"
		        + linkEnd, "<a href=\"$1\">$2</a>");
		
		// links without label
		result = result.replaceAll(MULTILINEMODE + linkStart + "(.+?)" + linkEnd,
		        "<a href=\"$1\">$1</a>");
		
		return result;
		
	}
}
