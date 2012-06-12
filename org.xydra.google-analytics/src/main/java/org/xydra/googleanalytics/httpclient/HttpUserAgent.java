package org.xydra.googleanalytics.httpclient;

import java.util.concurrent.Future;


/**
 * Minimal bridge for apache commons httpclient and GAE UrlFetch service
 * 
 * @author voelkel
 * 
 */
public interface HttpUserAgent {
	
	void setUserAgentIdentifier(String userAgent);
	
	void setConnectionTimeout(int maxMillis);
	
	void setAutoRetry(boolean autoRetry);
	
	/**
	 * @param url
	 * @return the response code
	 */
	Future<Integer> GET(String url);
	
}
