package org.xydra.webadmin;

import org.xydra.core.util.Clock;


public class Progress {
	private Clock clock = new Clock();
	private long count = 0;
	
	public void startTime() {
		this.clock.start();
	}
	
	public void makeProgress(long howMuch) {
		this.count += howMuch;
	}
	
	public double getMsPerProgress() {
		return ((double)this.clock.getDurationSinceStart()) / ((double)this.count);
	}
	
	public long willTakeMsUntilProgressIs(long howMuchTotal) {
		return (long)(howMuchTotal * getMsPerProgress()) - getMsSinceStart();
	}
	
	public long getProgress() {
		return this.count;
	}
	
	public long getMsSinceStart() {
		return this.clock.getDurationSinceStart();
	}
	
	@Override
	public String toString() {
		return "Running for " + getMsSinceStart() + "ms and made " + this.count
		        + " progress; that's " + getMsPerProgress() + "ms for each.";
		
	}
}
