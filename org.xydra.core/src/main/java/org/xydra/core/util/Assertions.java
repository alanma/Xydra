package org.xydra.core.util;

/**
 * Small helper to determine if java assertions are enabled.
 * 
 * @author voelkel
 * 
 */
@Deprecated
public class Assertions {
	
	private static boolean assertionsEnabled = false;
	
	static {
		assert assertionsAreEnabled();
	}
	
	private static boolean assertionsAreEnabled() {
		// side-effect: remember
		assertionsEnabled = true;
		return true;
	}
	
	public static boolean enabled() {
		return assertionsEnabled;
	}
	
}
