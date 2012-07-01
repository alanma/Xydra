package com.sonicmetrics.core.shared.query;

import java.io.Serializable;

import org.xydra.annotations.RunsInGWT;


/**
 * @author xamde
 * 
 */
@RunsInGWT(true)
public class TimeConstraint implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	public static final TimeConstraint ALL_UNTIL_NOW = new TimeConstraint(0l, Long.MAX_VALUE, null);
	
	private TimeConstraint(long startInclusive, long endInclusive, String lastkey) {
		this.start = startInclusive;
		this.end = endInclusive;
		this.lastKey = lastkey;
	}
	
	/**
	 * @param startInclusive first matching time-stamp (=inclusive)
	 * @param endInclusive last matching time-stamp (=inclusive)
	 * @return a time constraint for an interval
	 */
	public static TimeConstraint fromTo(long startInclusive, long endInclusive) {
		return new TimeConstraint(startInclusive, endInclusive, null);
	}
	
	/**
	 * @param startInclusive first matching time-stamp (=inclusive)
	 * @return for all events since a certain date until now
	 */
	public static TimeConstraint from(long startInclusive) {
		return new TimeConstraint(startInclusive, Long.MAX_VALUE, null);
	}
	
	/**
	 * @param lastKey last known key (exclusive)
	 * @return all events since a known last key until now
	 */
	public static TimeConstraint sinceLastkey(String lastKey) {
		return new TimeConstraint(0, Long.MAX_VALUE, null);
	}
	
	public boolean isConstraining() {
		return this.start > 0 || this.lastKey != null || this.end < Long.MAX_VALUE;
	}
	
	/** Exclusive */
	public final String lastKey;
	
	/** Inclusive. Ranges from 0 to Long.MAX_VALUE. */
	public final long start;
	
	/** Inclusive. Long.MAX_VALUE = current server time */
	public final long end;
	
	@Override
	public String toString() {
		return (this.lastKey == null ? "[" + this.start : "(key=" + this.lastKey) + "," + this.end
		        + "]";
	}
	
}
