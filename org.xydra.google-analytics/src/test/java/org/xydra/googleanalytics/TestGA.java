package org.xydra.googleanalytics;

import org.junit.Test;


public class TestGA {
	
	// ID for test.xydra.org
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
	
}
