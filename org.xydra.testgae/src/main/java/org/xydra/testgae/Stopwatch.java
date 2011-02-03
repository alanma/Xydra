package org.xydra.testgae;

public class Stopwatch {
	
	private long stop;
	private long start;
	
	public Stopwatch start() {
		this.start = System.currentTimeMillis();
		return this;
	}
	
	public void stop() {
		this.stop = System.currentTimeMillis();
	}
	
	public String getResult() {
		return this.stop - this.start + " ms";
	}
	
	public String getFormattedResult(String operation, int count) {
		if(this.stop == 0) {
			throw new IllegalStateException("stop watch has not been stopped");
		}
		return (count == 0 ? "--" : (getDuration() / count)) + " ms per '" + operation
		        + "' based on " + count + " runs.";
	}
	
	public long getDuration() {
		return this.stop - this.start;
	}
}
