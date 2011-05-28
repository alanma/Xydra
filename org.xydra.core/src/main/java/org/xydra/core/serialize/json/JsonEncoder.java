/*
 * Created on 16.05.2005
 */
package org.xydra.core.serialize.json;

public class JsonEncoder {
	
	public static String encode(String in) {
		String result = in;
		result = result.replace("\\", "\\\\");
		result = result.replace("\"", "\\\"");
		return result;
	}
}
