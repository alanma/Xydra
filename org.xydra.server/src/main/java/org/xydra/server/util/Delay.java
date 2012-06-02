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
	
	/**
	 * Simulate a typical delay for an AJAX request
	 */
	public static void ajax() {
		delay(10000);
	}
	
	private static void delay(int ms) {
		log.info("~~~ Artificial delay of " + ms + " ms");
		try {
			Thread.sleep(ms);
		} catch(InterruptedException e) {
			throw new RuntimeException(e);
		}
		
	}
	
}
