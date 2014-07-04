package org.xydra.sharedutils;

/** The GWT version of nanotime */
public class SystemUtils {
	
	public static long nanoTime() {
		return System.currentTimeMillis() * 1000000;
	}
	
}
