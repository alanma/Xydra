package org.xydra.store.impl.gae;

import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;


/**
 * Provides functionality similar to the Java 'assert' keyword on Google
 * AppEngine.
 * 
 * @author xamde
 */
public class GaeAssert {
	
	private static final Logger log = LoggerFactory.getLogger(GaeAssert.class);
	
	/**
	 * Runtime-config setting to enable or disable assertion on this instance
	 */
	private static boolean enabled = false;
	
	public static void enable() {
		log.debug("GaeAssert is on");
		enabled = true;
	}
	
	public static boolean isEnabled() {
		return enabled;
	}
	
	public static void setEnabled(boolean enabled_) {
		enabled = enabled_;
	}
	
	/**
	 * This method just returns, doing nothing if {@link GaeAssert#enable()} has
	 * not been called from main code.
	 * 
	 * @param condition should evaluate to true if things run OK
	 * @param message can be null
	 * @throws RuntimeException if GaeAssertions are on and the condition is
	 *             false
	 */
	public static void gaeAssert(boolean condition, String message) {
		if(!enabled) {
			return;
		}
		
		if(!condition) {
			log.warn("Assertion failed: " + message);
			throw new RuntimeException("Assert failed: " + message);
		}
	}
	
}
