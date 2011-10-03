package org.xydra.core.util;

public class DebugUtils {
	
	public static void dumpStacktrace() {
		try {
			throw new RuntimeException("CALLER");
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
}
