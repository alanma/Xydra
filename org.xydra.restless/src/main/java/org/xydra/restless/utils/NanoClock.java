package org.xydra.restless.utils;

import org.xydra.annotations.NeverNull;


/**
 * A simple named clock for measuring time. Nanosecond precision.
 * 
 * @author xamde
 * 
 */
public class NanoClock {
	
	/*
	 * TODO maybe declaring the methods as synchronized would suffice?
	 */
	
	private StringBuffer stats = new StringBuffer();
	
	/** -1 = not running */
	private long start = -1;
	
	private long firstStart = -1;
	
	/**
	 * @return itself to allow elegant fluent code like this
	 *         <code>Clock c = new Clock().start();</code>
	 */
	public NanoClock start() {
		synchronized(this) {
			this.start = System.nanoTime();
			this.firstStart = this.start;
			return this;
		}
	}
	
	public void reset() {
		synchronized(this) {
			this.start = -1;
			this.stats = new StringBuffer();
		}
	}
	
	/**
	 * @param name for the statistics
	 * @return this instance for API chaining
	 */
	public NanoClock stop(@NeverNull String name) {
		synchronized(this) {
			stopAndGetDuration(name);
			return this;
		}
	}
	
	/**
	 * @param name for clock entry
	 * @return duration since last start in milliseconds
	 */
	public long stopAndGetDuration(@NeverNull String name) {
		synchronized(this) {
			if(this.start == -1) {
				/*
				 * TODO what if the clock was started at some point in time and
				 * then stopped. Should trying to stop it really throw an
				 * exception in this case?
				 */
				throw new IllegalStateException("Cannot stop a clock that was never started.");
			}
			long stop = System.nanoTime();
			double durationInMs = (stop - this.start) / 1000000d;
			this.stats.append(name).append("=").append(durationInMs).append("ms <br />\n");
			this.start = -1;
			return (long)durationInMs;
		}
	}
	
	public void stopAndStart(@NeverNull String name) {
		synchronized(this) {
			stop(name);
			start();
		}
	}
	
	public String getStats() {
		synchronized(this) {
			return this.stats.toString();
		}
	}
	
	public static void main(String[] args) {
		NanoClock c = new NanoClock();
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
	
	/*
	 * TODO is this really supposed to be public?
	 */
	public NanoClock append(@NeverNull String s) {
		synchronized(this) {
			this.stats.append(s);
			return this;
		}
	}
	
	/**
	 * @return milliseconds elapsed since first start of this clock. -1 = clock
	 *         was not started ever.
	 */
	public long getDurationSinceStart() {
		return (System.nanoTime() - this.firstStart) / 1000000;
	}
}
