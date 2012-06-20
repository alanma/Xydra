package org.xydra.googleanalytics.httpclient;

import java.util.concurrent.Future;


/**
 * Minimal bridge for Apache commons HttpClient and GAE UrlFetch service
 * 
 * Users should define {@link #setAutoRetry(boolean)},
 * {@link #setUserAgentIdentifier(String)}, {@link #setConnectionTimeout(int)}.
 * 
 * @author voelkel
 * 
 */
public interface HttpUserAgent {
	
	void setUserAgentIdentifier(String userAgent);
	
	/**
	 * @param maxMillis how long to wait maximal, in milliseconds.
	 */
	void setConnectionTimeout(int maxMillis);
	
	void setAutoRetry(boolean autoRetry);
	
	/**
	 * @param url
	 * @return the response code
	 */
	Future<Integer> GET(String url);
	
}
