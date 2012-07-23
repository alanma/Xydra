package com.sonicmetrics.core.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;


public class IndexedDayTest {
	
	public static void main(String[] args) {
		System.out.println(IndexedDay.MILLIS_PER_DAY);
	}
	
	/* at least over 3 days long to find more bugs */
	public static final long BIGBANG = 1000000000;
	
	/* 1000 millis after BIG-BANG - index = 0 */
	public static final long UTC1 = BIGBANG + 1000;
	
	public static final long UTC2 = BIGBANG + 10 * IndexedDay.MILLIS_PER_DAY + 1000;
	
	@Test
	public void testToIndexedDay() {
		assertConvertTwoWay(UTC1);
		assertConvertTwoWay(UTC2);
	}
	
	private static void assertConvertTwoWay(long utc1) {
		int idx1 = IndexedDay.toIndexedDay(utc1, BIGBANG);
		long utc2 = IndexedDay.toUtc(idx1, BIGBANG);
		int idx2 = IndexedDay.toIndexedDay(utc2, BIGBANG);
		assertEquals(idx1, idx2);
	}
	
	private static void assertConvertTwoWayUpper(long utc1) {
		int idx1 = IndexedDay.toUpperIndexedDay(utc1, BIGBANG);
		long utc2 = IndexedDay.toUtc(idx1, BIGBANG);
		int idx2 = IndexedDay.toUpperIndexedDay(utc2, BIGBANG);
		assertEquals(idx1, idx2);
	}
	
	@Test
	public void testToUpperIndexedDay() {
		assertConvertTwoWayUpper(UTC1);
		assertConvertTwoWayUpper(UTC2);
	}
	
	@Test
	public void testToTimeOnIndexedDay() {
		int idx1 = IndexedDay.toIndexedDay(UTC1, BIGBANG);
		long utc2 = IndexedDay.toUtc(idx1, BIGBANG);
		long utc2time = IndexedDay.toTimeOnIndexedDay(UTC1, BIGBANG);
		assertEquals(UTC1, utc2 + utc2time);
	}
	
	@Test
	public void testBasics() {
		assertEquals(0, IndexedDay.toIndexedDay(UTC1, BIGBANG));
	}
	
	@Test
	public void testToUpperUtc() {
		assertEquals(IndexedDay.toUpperIndexedDay(UTC1, BIGBANG),
		        1 + IndexedDay.toIndexedDay(UTC1, BIGBANG));
	}
	
	@Test
	public void testIsoParsing() {
		int index = IndexedDay.toIndexedDay(UTC2, BIGBANG);
		String iso = IndexedDay.toIsoDateString(UTC2);
		System.out.println(iso);
		int parsedIndex = IndexedDay.parseIsoDateString(iso, BIGBANG);
		assertTrue("parsed index looses time of day", parsedIndex <= index
		        && parsedIndex + 1 >= index);
	}
	
	@Test
	public void testToIsoDate() {
		String s = IndexedDay.toIsoDateTimeString(BIGBANG);
		System.out.println(s);
	}
	
}
