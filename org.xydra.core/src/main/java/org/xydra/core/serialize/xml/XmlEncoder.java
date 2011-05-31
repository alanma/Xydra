/*
 * Created on 16.05.2005
 */
package org.xydra.core.serialize.xml;

import org.xydra.annotations.RequiresAppEngine;
import org.xydra.annotations.RunsInAppEngine;
import org.xydra.annotations.RunsInGWT;


/**
 * Helper class for XML encoding and decoding of XML escaped characters
 */
@RunsInGWT(true)
@RunsInAppEngine(true)
@RequiresAppEngine(false)
public class XmlEncoder {
	
	/**
	 * Standard XML header with UTF-8 encoding.
	 */
	public static final String XML_DECLARATION = "<?xml version=\"1.0\" encoding=\"utf-8\"?>";
	
	public static String decode(String in) {
		String result = in;
		result = result.replace("&amp;", "&");
		result = result.replace("&lt;", "<");
		result = result.replace("&gt;", ">");
		result = result.replace("&apos;", "'");
		result = result.replace("&quot;", "\"");
		return result;
		
	}
	
	public static String encode(String in) {
		String result = in;
		result = result.replace("&", "&amp;");
		result = result.replace("<", "&lt;");
		result = result.replace(">", "&gt;");
		result = result.replace("'", "&apos;");
		result = result.replace("\"", "&quot;");
		return result;
	}
	
	static final String XMAP_ELEMENT = "xmap";
	static final String XARRAY_ELEMENT = "xarray";
	static final String XNULL_ELEMENT = "xnull";
	static final String NULL_ATTRIBUTE = "isNull";
	static final String NULL_VALUE = "true";
	static final String NULL_CONTENT_ATTRIBUTE = "nullContent";
	static final String NULL_CONTENT_VALUE = "true";
	
}
