/*
 * Created on 16.05.2005
 */
package org.xydra.core.xml.impl;

public class JsonEncoder {
	
	public static String encode(String in) {
		String result = in;
		result = result.replace("\\", "\\\\");
		result = result.replace("\"", "\\\"");
		return result;
	}
}
