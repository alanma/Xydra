/*
 * Created on 16.05.2005
 */
package org.xydra.core.serialize.json;

import org.xydra.annotations.RequiresAppEngine;
import org.xydra.annotations.RunsInAppEngine;
import org.xydra.annotations.RunsInGWT;
import org.xydra.core.serialize.LineBreaks;


/**
 * Encode string values as valid JSON
 * 
 * @author xamde
 */
@RunsInGWT(true)
@RunsInAppEngine(true)
@RequiresAppEngine(false)
public class JsonEncoder {
	
	public static String encode(String in) {
		String result = LineBreaks.normalizeLinebreaks(in);
		result = result.replace("\\", "\\\\");
		result = result.replace("\"", "\\\"");
		result = result.replace("\n", "\\n");
		return result;
	}
	
	public static final String PROPERTY_TYPE = "$t";
}
