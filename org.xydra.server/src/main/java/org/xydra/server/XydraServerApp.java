package org.xydra.server;

/**
 * Implementations must have a zero-arg constructor.
 * 
 * @author xamde
 */
public interface XydraServerApp {

	/**
	 * Set up internal parts to handle a single (or multiple) requests
	 */
	void init();

}
