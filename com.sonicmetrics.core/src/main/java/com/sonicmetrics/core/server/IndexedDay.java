package com.sonicmetrics.core.server;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

import com.sonicmetrics.core.shared.util.SharedIndexedDay;

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
public class IndexedDay extends SharedIndexedDay {

	private static final SimpleDateFormat ISO_DATE_FORMAT = new SimpleDateFormat("yyyy'-'MM'-'dd");

	/** 2007-12-24T18:21Z */
	private static final SimpleDateFormat ISO_DATETIME_FORMAT = new SimpleDateFormat(
			"yyyy'-'MM'-'dd'T'HH':'mm':'ss','SSS'Z'");

	protected static void assertAfterBigBang(long timeUtc, long bigBang) {
		if (timeUtc < bigBang) {
			throw new IllegalArgumentException("not a valid timeStamp - must be after "
					+ toIsoDateTimeString(bigBang) + ", was on " + toIsoDateTimeString(timeUtc));
		}
	}

	public static String toUpperIsoDateString(int indexedDay, long bigBang) {
		return toIsoDateString(toUpperUtc(indexedDay, bigBang));
	}

	public static String toIsoDateString(int indexedDay, long bigBang) {
		return toIsoDateString(toUtc(indexedDay, bigBang));
	}

	/**
	 * @param utcTime
	 * @return the UTC time as a ISO8601 date string in the format yyyy-MM-dd
	 */
	public static String toIsoDateString(long utcTime) {
		return ISO_DATE_FORMAT.format(new Date(utcTime));
	}

	public static String toIsoDateTimeString(long utcTime) {
		return ISO_DATETIME_FORMAT.format(new Date(utcTime));
	}

	/**
	 * @param isoDate
	 *            in format yyyy-MM-dd
	 * @param bigBang
	 * @return the default (lower) indexed day
	 */
	public static int parseIsoDateString(String isoDate, long bigBang) {
		try {
			String yearStr = isoDate.substring(0, 4);
			int year = Integer.parseInt(yearStr);
			String monthStr = isoDate.substring(5, 7);
			int month = Integer.parseInt(monthStr) - 1;
			String dayStr = isoDate.substring(8, 10);
			int day = Integer.parseInt(dayStr);
			GregorianCalendar gc = new GregorianCalendar();
			gc.set(year, month, day, 0, 0, 0);
			long timeInMillis = gc.getTimeInMillis();

			return toIndexedDay(timeInMillis, bigBang);
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Could not parse '" + isoDate + "'", e);
		}
	}

}
