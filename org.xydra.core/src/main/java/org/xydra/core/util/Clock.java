package org.xydra.core.util;

import org.xydra.annotations.RunsInGWT;


/**
 * A simple named clock for measuring time. Millisecond precision.
 *
 * @author xamde
 */
@RunsInGWT(true)
public class Clock {

	private StringBuffer stats = new StringBuffer();

	/** -1 = not running */
	private long start = -1;

	private long firstStart = -1;

	/**
	 * @return itself to allow elegant fluent code like this
	 *         <code>Clock c = new Clock().start();</code>
	 */
	public Clock start() {
		this.start = System.currentTimeMillis();
		this.firstStart = this.start;
		return this;
	}

	public void reset() {
		this.start = -1;
		this.stats = new StringBuffer();
	}

	/**
	 * @param name for the statistics
	 * @return this instance for fluent API usage
	 */
	public Clock stop(final String name) {
		stopAndGetDuration(name);
		return this;
	}

	/**
	 * @param name for clock entry
	 * @return duration since last start in milliseconds
	 */
	public long stopAndGetDuration(final String name) {
		if(this.start == -1) {
			throw new IllegalStateException("Cannot stop a clock that was never started.");
		}
		final long stop = System.currentTimeMillis();
		final double durationInMs = stop - this.start;
		this.stats.append(name).append("=").append(durationInMs).append("ms <br />\n");
		this.start = -1;
		return (long)durationInMs;
	}

	public void stopAndStart(final String name) {
		stop(name);
		start();
	}

	public String getStats() {
		return this.stats.toString();
	}

	public static void main(final String[] args) {
		final Clock c = new Clock();
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

	public Clock append(final String s) {
		this.stats.append(s);
		return this;
	}

	/**
	 * @return milliseconds elapsed since first start of this clock. -1 = clock
	 *         was not started ever.
	 */
	public long getDurationSinceStart() {
		return System.currentTimeMillis() - this.firstStart;
	}
}
