/*
 * Created on 16.05.2005
 */
package org.xydra.core.xml.impl;

/**
 * Helper class for XML encoding and decoding of XML escaped characters
 */
public class XmlEncoder {
	
	public static String XML_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
	
	public static String xmldecode(String in) {
		String result = in;
		result = result.replace("&amp;", "&");
		result = result.replace("&lt;", "<");
		result = result.replace("&gt;", ">");
		result = result.replace("&apos;", "'");
		result = result.replace("&quot;", "\"");
		return result;
		
	}
	
	public static String xmlencode(String in) {
		String result = in;
		result = result.replace("&", "&amp;");
		result = result.replace("<", "&lt;");
		result = result.replace(">", "&gt;");
		result = result.replace("'", "&apos;");
		result = result.replace("\"", "&quot;");
		return result;
	}
	
}
