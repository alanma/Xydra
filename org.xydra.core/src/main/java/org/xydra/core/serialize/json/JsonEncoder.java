/*
 * Created on 16.05.2005
 */
package org.xydra.core.serialize.json;

import org.xydra.annotations.RequiresAppEngine;
import org.xydra.annotations.RunsInAppEngine;
import org.xydra.annotations.RunsInGWT;


@RunsInGWT(true)
@RunsInAppEngine(true)
@RequiresAppEngine(false)
public class JsonEncoder {
	
	public static String encode(String in) {
		String result = in;
		result = result.replace("\\", "\\\\");
		result = result.replace("\"", "\\\"");
		// windows
		result = result.replace("\r\n", "\\n");
		// more or less standard
		result = result.replace("\n", "\\n");
		// old macs
		result = result.replace("\r", "\\n");
		return result;
	}
	
	public static final String PROPERTY_TYPE = "$t";
}
