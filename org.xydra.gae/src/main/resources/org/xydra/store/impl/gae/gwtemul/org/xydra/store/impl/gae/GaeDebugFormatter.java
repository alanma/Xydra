package org.xydra.store.impl.gae;

/**
 * GAE-specific parts of {@link DebugFormatter}. Separating them allows for
 * re-use in GWT.
 * 
 * This is the GWT-version.
 * 
 * @author xamde
 * 
 */
public class GaeDebugFormatter {
	
	public static boolean canHandle(Object o) {
		return false;
	}
	
	public static String toString(Object value) {
		throw new RuntimeException("Cannot handle this, check via canHandle() before");
	}
	
}
