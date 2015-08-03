package org.xydra.log.howto.inpurejava;

import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;

/**
 * You can ignore all other classes in this project for logging in plain Java.
 *
 * TODO explain how to configure log levels
 *
 * @author xamde
 *
 */
public class HowToLogInPureJava {

	private static Logger log = LoggerFactory.getLogger(HowToLogInPureJava.class);

	public static void main(final String[] args) {

		// default logger level is always 'info'
		log.info("hey info");
		log.debug("hey debug");
	}

}
