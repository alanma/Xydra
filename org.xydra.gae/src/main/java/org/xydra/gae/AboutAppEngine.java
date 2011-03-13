package org.xydra.gae;

import com.google.appengine.api.utils.SystemProperty;


/**
 * Information about AppEngine
 * 
 * @author voelkel
 */
public class AboutAppEngine {
	
	/**
	 * @return true if app is running on a real remote GAE server
	 */
	public static boolean inProduction() {
		return SystemProperty.environment.get() != null
		        && SystemProperty.environment.value().equals(
		                SystemProperty.Environment.Value.Production);
	}
	
	public static boolean inDevelopment() {
		return SystemProperty.environment.get() != null
		        && SystemProperty.environment.value().equals(
		                SystemProperty.Environment.Value.Development);
	}
	
	public static boolean onAppEngine() {
		return inProduction() || inDevelopment();
	}
	
}
