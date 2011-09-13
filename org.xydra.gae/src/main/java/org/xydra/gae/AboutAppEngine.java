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
	 * @return The major version number for the currently running version of the
	 *         application plus a timestamp at which it was deployed. This is
	 *         not just the version identifier string you specify in
	 *         appengine-web.xml.
	 * 
	 *         Format: user-chosen-versionId-from-appengine-xml '.' timestamp
	 */
	public static String getVersion() {
		String version = SystemProperty.applicationVersion.get();
		if(version == null) {
			version = "devmode";
		}
		return version;
	}
	
	@SuppressWarnings("deprecation")
	public static String getInstanceId() {
		return SystemProperty.instanceReplicaId.get();
	}
	
	public static String getThreadInfo() {
		Thread ct = Thread.currentThread();
		return ct.getId() + "-'" + ct.getName() + "'";
	}
	
}
