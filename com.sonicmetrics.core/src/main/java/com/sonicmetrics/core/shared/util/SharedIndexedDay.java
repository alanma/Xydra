package com.sonicmetrics.core.shared.util;

import org.xydra.annotations.RunsInGWT;


/**
 * Converts from long UTC timestamps to indexed days. Days are indexes after
 * some defined 'BIG BANG'-like moment. Every day is 24 hours long. This implies
 * that wall clock time and indexed days differ more and more over time, as real
 * days on earth are sometimes longer than 24 hours, when there are leap
 * seconds. Usually there are no more than ca. 2-20 leap seconds per year.
 * 
 * Indexed day numbers are great for consistent caching of time-based stuff.
 * 
 * @author Andi
 * @author xamde
 */
@RunsInGWT(true)
public class SharedIndexedDay {
	
	public static final long MINUTES_PER_DAY = 24 * 60;
	
	public static final long MILLIS_PER_MINUTE = 60 * 1000;
	
	public static final long MILLIS_PER_DAY = MINUTES_PER_DAY * MILLIS_PER_MINUTE;
	
	protected static void assertAfterBigBang(long timeUtc, long bigBang) {
		if(timeUtc < bigBang) {
			throw new IllegalArgumentException("not a valid timeStamp - must be after " + (bigBang)
			        + ", was on " + (timeUtc));
		}
	}
	
	/**
	 * Returns the default (lower) indexed day.
	 * 
	 * @param timeUtc
	 * @param bigBang start of time. Should be used consistently with other
	 *            calls that use bigBang.
	 * @return the indexed day in which timeUtc happened
	 */
	public static int toIndexedDay(long timeUtc, long bigBang) {
		assertAfterBigBang(timeUtc, bigBang);
		long millisSinceBigBang = timeUtc - bigBang;
		double index = ((double)millisSinceBigBang) / ((double)MILLIS_PER_DAY);
		return (int)Math.floor(index);
	}
	
	/**
	 * Returns the upper indexed day.
	 * 
	 * @param timeUtc
	 * @param bigBang start of time. Should be used consistently with other
	 *            calls that use bigBang.
	 * @return the indexed day after the day in which timeUtc happened.
	 */
	public static int toUpperIndexedDay(long timeUtc, long bigBang) {
		assertAfterBigBang(timeUtc, bigBang);
		long millisSinceBigBang = timeUtc - bigBang;
		double index = ((double)millisSinceBigBang) / ((double)MILLIS_PER_DAY);
		return (int)Math.ceil(index);
	}
	
	/**
	 * @param timeUtc
	 * @param bigBang start of time. Should be used consistently with other
	 *            calls that use bigBang.
	 * @return the milliseconds within the indexed day of the given timeUtc
	 */
	public static long toTimeOnIndexedDay(long timeUtc, long bigBang) {
		assertAfterBigBang(timeUtc, bigBang);
		long millisSinceBigBang = timeUtc - bigBang;
		long millisOnDay = millisSinceBigBang % MILLIS_PER_DAY;
		return millisOnDay;
	}
	
	/**
	 * Return the UTC time at which the day following the indexed day started.
	 * This is the first millisecond excluding the time covered by the
	 * indexedDay.
	 * 
	 * @param indexedDay
	 * @param bigBang start of time. Should be used consistently with other
	 *            calls that use bigBang.
	 * @return the equivalent upper UTC-day-border-timeStamp
	 */
	public static long toUpperUtc(int indexedDay, long bigBang) {
		return ((long)indexedDay + 1) * MILLIS_PER_DAY + bigBang;
	}
	
	/**
	 * @param indexedDay
	 * @param bigBang start of time. Should be used consistently with other
	 *            calls that use bigBang.
	 * @return the equivalent lower UTC-day-border-timeStamp
	 */
	public static long toUtc(int indexedDay, long bigBang) {
		return ((long)indexedDay) * MILLIS_PER_DAY + bigBang;
	}
	
}
