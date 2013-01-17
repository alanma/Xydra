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
		if(in == null) {
			return null;
		}
		
		String result = in;
		result = result.replace("&", "&amp;");
		result = result.replace("<", "&lt;");
		result = result.replace(">", "&gt;");
		result = result.replace("'", "&apos;");
		result = result.replace("\"", "&quot;");
		return result;
	}
	
	public static final String XMAP_ELEMENT = "xmap";
	public static final String XARRAY_ELEMENT = "xarray";
	public static final String XNULL_ELEMENT = "xnull";
	public static final String XVALUE_ELEMENT = "xvalue";
	public static final String NULL_ATTRIBUTE = "isNull";
	public static final String NULL_VALUE = "true";
	public static final String NULL_CONTENT_ATTRIBUTE = "nullContent";
	public static final String NULL_CONTENT_VALUE = "true";
	
}