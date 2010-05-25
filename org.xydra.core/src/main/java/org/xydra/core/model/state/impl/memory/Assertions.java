package org.xydra.core.model.state.impl.memory;

/**
 * Small helper to determine if java assertions are enabled.
 * 
 * @author voelkel
 * 
 */
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
