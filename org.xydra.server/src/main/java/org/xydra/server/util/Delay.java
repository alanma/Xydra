package org.xydra.server.util;

import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;


/**
 * Simulate typical web-delays to make dev-mode feel more like real usage.
 * 
 * @author xamde
 */
public class Delay {
	
	private static final Logger log = LoggerFactory.getLogger(Delay.class);
	
	private static int ajaxDelayTimeMs = 0;
	
	public static boolean isSimulateDelay() {
		return ajaxDelayTimeMs > 0;
	}
	
	/**
	 * Simulate a typical delay for an AJAX request
	 */
	public static void ajax() {
		delay(ajaxDelayTimeMs);
	}
	
	public static void delay(int ms) {
		log.info("~~~ Artificial delay of " + ms + " ms");
		try {
			Thread.sleep(ms);
		} catch(InterruptedException e) {
			throw new RuntimeException(e);
		}
		
	}
	
	public static void setAjaxDelayMs(int ms) {
		assert ms >= 0;
		ajaxDelayTimeMs = ms;
	}
	
}
