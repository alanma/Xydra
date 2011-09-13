package org.xydra.restless.utils;

/**
 * A simple named clock for measuring time. Nanosecond precision.
 * 
 * @author xamde
 * 
 */
public class Clock {
	
	private StringBuffer stats = new StringBuffer();
	
	/** -1 = not running */
	private long start = -1;
	
	/**
	 * @return itself to allow elegant fluent code like this
	 *         <code>Clock c = new Clock().start();</code>
	 */
	public Clock start() {
		this.start = System.nanoTime();
		return this;
	}
	
	public void reset() {
		this.start = -1;
		this.stats = new StringBuffer();
	}
	
	/**
	 * @param name for the statistics
	 */
	public Clock stop(String name) {
		stopAndGetDuration(name);
		return this;
	}
	
	/**
	 * @param name for clock entry
	 * @return duration since last start in milliseconds
	 */
	public long stopAndGetDuration(String name) {
		if(this.start == -1) {
			throw new IllegalStateException("Cannot stop a clock that was never started.");
		}
		long stop = System.nanoTime();
		double durationInMs = (stop - this.start) / 1000000d;
		this.stats.append(name).append("=").append(durationInMs).append("ms <br />\n");
		this.start = -1;
		return (long)durationInMs;
	}
	
	public void stopAndStart(String name) {
		stop(name);
		start();
	}
	
	public String getStats() {
		return this.stats.toString();
	}
	
	public static void main(String[] args) {
		Clock c = new Clock();
		c.start();
		c.stop("a");
		c.start();
		c.stop("b");
		c.start();
		c.stop("c");
		c.start();
		c.stop("d");
		System.out.println(c.getStats());
	}
	
	public Clock append(String s) {
		this.stats.append(s);
		return this;
	}
}
