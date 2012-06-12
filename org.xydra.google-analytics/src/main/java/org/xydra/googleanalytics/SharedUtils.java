package org.xydra.googleanalytics;

import java.util.Random;


/**
 * Runs in GWT
 */
public class SharedUtils {
	
	private static final Random random = new Random();
	
	public static int random31bitInteger() {
		return random.nextInt(2147483647) - 1;
	}
	
	public static int random32bitInteger() {
		return random.nextInt();
	}
	
	public static long getCurrentTimeInSeconds() {
		return millisInSecons(System.currentTimeMillis());
	}
	
	public static long millisInSecons(long millis) {
		return millis / 1000;
	}
	
}
