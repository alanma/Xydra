package org.xydra.googleanalytics.httpclient;

import com.google.appengine.api.utils.SystemProperty;


public class HttpUserAgentFactory {
	
	/**
	 * @return a new instance of a {@link HttpUserAgent} implementation
	 */
	public static HttpUserAgent createHttpUserAgent() {
		if(onAppEngine()) {
			return new HttpUserAgentUrlFetch();
		} else {
			return new HttpUserAgentApacheCommons();
		}
	}
	
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
