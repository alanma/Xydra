package org.xydra.googleanalytics;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.xydra.googleanalytics.httpclienht.HttpUserAgent;
import org.xydra.googleanalytics.httpclienht.HttpUserAgentFactory;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;


/**
 * Google Analytics Limit: For each visit (user session), a maximum of
 * approximately 500 combined GATC requests (both events and page views) can be
 * tracked.
 * 
 * @author voelkel
 * 
 */
public class Tracker {
	
	public static final String CLIENT_ID = "$Revision$".replace(" ", "_");
	
	public static final String HTTP_HEADER_SET_COOKIE = "Set-Cookie";
	
	public static final String HTTP_HEADER_USER_AGENT = "User-agent";
	
	public static final String OS_NAME = System.getProperty("os.name");
	
	public static final String OS_VERSION = System.getProperty("os.version");
	
	public static String USER_AGENT;
	
	static {
		String osShortName;
		if(OS_NAME.contains("Windows")) {
			osShortName = "Windows";
		} else if(OS_NAME.contains("Mac OS")) {
			osShortName = "Macintosh";
		} else if(OS_NAME.contains("nix") || OS_NAME.contains("nux")) {
			osShortName = "Linux";
		} else {
			osShortName = "UnknownOS";
		}
		USER_AGENT = "Tracker/" + CLIENT_ID + " (" + osShortName + "; " + OS_VERSION + ")";
	}
	
	private static Logger log = LoggerFactory.getLogger(Tracker.class);
	
	private String trackerCode;
	
	private int trackCount = 0;
	
	private HttpUserAgent httpClient;
	
	public Tracker(String trackerCode) {
		this.trackerCode = trackerCode;
		
		this.httpClient = HttpUserAgentFactory.createHttpUserAgent();
		this.httpClient.setUserAgentIdentifier(USER_AGENT);
		// wait up to 5 seconds for an OK
		this.httpClient.setConnectionTimeout(5000);
		// do not retry
		this.httpClient.setAutoRetry(false);
	}
	
	boolean warnedAbout500Events = false;
	
	/**
	 * FIXME use only one thread that works on a queue of URLs
	 * 
	 * Track asynchronously
	 * 
	 * @param focusPoint
	 * @param refererURL
	 * @param userinfo
	 * @param gaEvent can be null
	 */
	public void track(FocusPoint focusPoint, String refererURL, UserInfo userinfo, GaEvent gaEvent) {
		log.debug("Tracking asynchronously focusPoint=" + focusPoint.getContentTitle());
		
		UrchinCookie cookie = new UrchinCookie(userinfo);
		String hostname = userinfo.getHostName();
		String url = UrchinUrl.toURL(hostname, focusPoint, refererURL, cookie, this.trackerCode,
		        gaEvent);
		
		Future<Integer> result = this.httpClient.GET(url);
		this.trackCount++;
		
		// check every 50 requests if they work
		if(this.trackCount % 50 == 1) {
			try {
				result.get(5000, TimeUnit.MILLISECONDS);
			} catch(InterruptedException e) {
			} catch(ExecutionException e) {
				log.warn("Could not finish HTTP GET", e);
			} catch(TimeoutException e) {
				log.warn("Could not finish HTTP GET in time", e);
			}
		}
		
		if(this.trackCount > 500 && !this.warnedAbout500Events) {
			log.warn("Sent over 500 requests in this session to Google Analytics "
			        + "- they don't track more");
			this.warnedAbout500Events = true;
		}
	}
	
}
