package org.xydra.core.util;

public class ConfigUtils {
	
	/**
	 * @param booleanString can be null
	 * @return true if string is not null and equals true (case ignored).
	 */
	public static boolean isTrue(String booleanString) {
		return booleanString != null && booleanString.equalsIgnoreCase("true");
	}
	
}
