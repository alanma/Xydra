package org.xydra.googleanalytics;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.xydra.googleanalytics.logsink.GALogListener;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;


public class TestGA {
	
	// ID for 'test.xydra.org'
	public static final String GOOGLE_ANALYTICS_ID = "UA-271022-28";
	
	@Test
	public void testGA() throws InterruptedException {
		Tracker tracker = new Tracker(GOOGLE_ANALYTICS_ID);
		tracker.track(new FocusPoint("click1"), "-", new UserInfoImpl("test.xydra.org"), null);
		// wait for HTTP to terminate
		Thread.sleep(1000);
		
		Tracker tracker2 = new Tracker(GOOGLE_ANALYTICS_ID);
		
		GaEvent event = new GaEvent("JUnit Test", "Testing");
		
		tracker2.track(new FocusPoint("click2"), "-", new UserInfoImpl("example.com"), event);
	}
	
	@Test
	public void testEvent() {
		Tracker tracker = new Tracker(GOOGLE_ANALYTICS_ID);
		tracker.track(new FocusPoint("eventpage"), "-", new UserInfoImpl("test.xydra.org"),
		        new GaEvent("the category", "the action", "the optionalLabel", 3));
	}
	
	@Test
	public void testLoggingEvent() {
		
		GALogListener logListener = new GALogListener(GOOGLE_ANALYTICS_ID, "test.xydra.org");
		LoggerFactory.addLogListener(logListener);
		Logger log = LoggerFactory.getLogger(TestGA.class);
		log.debug("should not be logged in GA");
		log.warn("should be logged in GA as warn");
		log.error("should be logged in GA as error");
		log.error("should be logged in GA as error with exception", new RuntimeException(
		        "the runtime exception message"));
		
		try {
			throw new RuntimeException("a thrown exception");
		} catch(RuntimeException e) {
			log.warn("catching the exception", e);
		}
		
		Logger log2 = LoggerFactory.getLogger(UserInfoImpl.class);
		log2.warn("should be logged in GA as warn from the UserInfoImpl log");
		
	}
	
	@Test
	public void testResendCookie() {
		/*
		 * <code>__utma=58334141.238842114.1303286004.1306757340.1306762313.37;
		 * __utmz
		 * =58334141.1306704704.35.2.utmcsr=backward.latest.cxmserver.appspot
		 * .com|utmccn=(referral)|utmcmd=referral|utmcct=/hop/f7nrPL5</code>
		 */

		Tracker tracker = new Tracker(GOOGLE_ANALYTICS_ID);
		String utmaCookie = "58334141.238842114.1303286004.1306757340.1306762313.37";
		String utmzCookie = "58334141.1306704704.35.2.utmcsr=backward.latest.cxmserver.appspot.com|utmccn=(referral)|utmcmd=referral|utmcct=/hop/f7nrPL5";
		UrchinCookie cookie = new UrchinCookie(utmaCookie, utmzCookie);
		
		assertEquals("58334141", cookie.utma.domainHash);
		assertEquals("58334141", cookie.utmz.domainHash);
		
		tracker.trackLowLevel("resend.example.com", new FocusPoint("resendCookieTest-aaa"),
		        "http://a1.example.com", cookie.getCookieString(), null);
		cookie.setCurrentSessionStartTimeToNow();
		String url = tracker.trackLowLevel("resend.example.com",
		        new FocusPoint("resendCookieTest2"), "http://a1.example.com",
		        cookie.getCookieString(), null);
		System.out.println(url);
	}
}
