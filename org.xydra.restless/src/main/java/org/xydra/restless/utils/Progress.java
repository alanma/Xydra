package org.xydra.restless.utils;

import org.xydra.annotations.RunsInGWT;
import org.xydra.common.NanoClock;

/**
 * Count for a given operation the throughput per time unit.
 *
 * Make sure to call {@link #startTime()}.
 *
 * @author xamde
 */
@RunsInGWT(true)
public class Progress {

	private final NanoClock clock = new NanoClock();

	private long count = 0;

	/**
	 * Starts the progress calculation.
	 */
	public void startTime() {
		this.clock.start();
	}

	/**
	 * @param howMuch number of operations done since last call
	 */
	public void makeProgress(final long howMuch) {
		this.count += howMuch;
	}

	/**
	 * @return number of ms it takes to make 1 operation on average since start
	 */
	public double getMsPerProgress() {
		return (double) this.clock.getDurationSinceStart() / (double) this.count;
	}

	/**
	 * @param howMuchTotal
	 * @return an estimate in ms when the given number of operations will be
	 *         done
	 */
	public long willTakeMsUntilProgressIs(final long howMuchTotal) {
		return (long) (howMuchTotal * getMsPerProgress()) - getMsSinceStart();
	}

	/**
	 * @return current operation count
	 */
	public long getProgress() {
		return this.count;
	}

	/**
	 * @return the duraction in ms since the start
	 */
	public long getMsSinceStart() {
		return this.clock.getDurationSinceStart();
	}

	@Override
	public String toString() {
		return "Running for " + getMsSinceStart() + "ms and made " + this.count
				+ " progress; that's " + getMsPerProgress() + "ms for each.";

	}

	public String info(final long totalCount) {
		return "Running for " + getMsSinceStart() + "ms. " + getMsPerProgress() + "ms for each. "
				+ willTakeMsUntilProgressIs(totalCount) + " ms ("
				+ willTakeMsUntilProgressIs(totalCount) / 60000 + "min) until finished.";
	}
}
