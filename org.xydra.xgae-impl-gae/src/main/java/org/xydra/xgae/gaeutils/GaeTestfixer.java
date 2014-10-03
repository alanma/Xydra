package org.xydra.xgae.gaeutils;

import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;
import org.xydra.log.util.ClassPathTool;

import com.google.appengine.api.utils.SystemProperty;

/**
 * A helper class to turn the simulated local AppEngine environment into a
 * singleton.
 * 
 * If we run in a JUnit test behind Jersey, which uses reflection, we are in a
 * different thread and need to take additional measure to tell this thread
 * about the GAE-test-setup
 * 
 * IMPROVE @Max: Remove class if AppEngine issue is ever fixed by Google
 * 
 * http://code.google.com/p/googleappengine/issues/detail?id=2201
 * 
 * @author xamde
 */
public class GaeTestfixer {

	private static final Logger log = LoggerFactory.getLogger(GaeTestfixer.class);

	private static boolean enabled = false;
	/** checking for production env only once makes this run faster */
	private static boolean checkedProduction = false;

	/**
	 * Set a static flag
	 */
	public static void enable() {
		log.debug("Enabling test fixer.");
		enabled = true;
		checkedProduction = false;
	}

	public static boolean isEnabled() {
		return enabled;
	}

	/**
	 * @return true if app is running on a real remote GAE server
	 */
	public static boolean inProduction() {
		return SystemProperty.environment.get() != null
				&& SystemProperty.environment.value().equals(
						SystemProperty.Environment.Value.Production);
	}

	/**
	 * Fix testing in development mode which spawns multiple threads, which
	 * cannot not happen on AppEngine in production mode.
	 * 
	 * This method just returns, doing nothing if {@link GaeTestfixer#enable()}
	 * is not called from main code.
	 */
	public static void initialiseHelperAndAttachToCurrentThread() {
		if (!enabled) {
			return;
		}

		/* if enabled and in production: self-disable */
		if (!checkedProduction) {
			log.debug("Testing if we are on the real GAE in production...");
			checkedProduction = true;
			if (inProduction()) {
				log.debug("Testfixer: Running on AppEngine in production: Auto-disabled test fixer.");
				enabled = false;
				return;
			} else {
				log.debug("Testfixer: Running locally");
			}

			/* second check: can we load this class: 'LocalServiceTestHelper' ? */
			try {
				ClassLoader cl = Thread.currentThread().getContextClassLoader();
				Class.forName(
						"com.google.appengine.tools.development.testing.LocalServiceTestHelper",
						false, cl);
				log.info("We can load the test classes.");
			} catch (ClassNotFoundException e) {
				// we're on AppEngine in production OR locally not running tests
				/* ah, we are in production */
				log.info(
						"We are in production or have no test-classpath (=a test jar is missing): Auto-disabled test fixer.\n"
								+ " Reason: 'com.google.appengine.tools.development.testing.LocalServiceTestHelper' not found in classpath",
						e);
				// FIXME kill
				ClassPathTool.dumpCurrentClasspath();
				enabled = false;
				return;
			} catch (NoClassDefFoundError e) {
				// we're on AppEngine in production OR locally not running tests
				/* ah, we are in production */
				log.warn(
						"We are in production or have no test-classpath (=a test jar is missing): Auto-disabled test fixer.",
						e);
				enabled = false;
				return;
			}
		}

		GaeTestFixer_LocalPart.initialiseHelperAndAttachToCurrentThread();
	}

	public static synchronized void tearDown() {
		GaeTestFixer_LocalPart.tearDown();
	}

}
