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
	
	/**
	 * @return only if on AppEngine development mode (i.e. locally)
	 */
	public static boolean inDevelopment() {
		return SystemProperty.environment.get() != null
		        && SystemProperty.environment.value().equals(
		                SystemProperty.Environment.Value.Development);
	}
	
	/**
	 * @return true if on AppEngine (regardless whether in production or in
	 *         development mode)
	 */
	public static boolean onAppEngine() {
		return inProduction() || inDevelopment();
	}
	
	/**
	 * format: user-chosen-versionId-from-appengine-xml '.' timestamp
	 */
	public static String getVersion() {
		
		return SystemProperty.applicationVersion.get();
	}
	
}
