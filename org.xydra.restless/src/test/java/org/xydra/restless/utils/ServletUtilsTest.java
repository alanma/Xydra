package org.xydra.restless.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Map;
import java.util.SortedSet;

import org.junit.Test;


public class ServletUtilsTest {
	
	@Test
	public void testGetQueryStringAsMap() {
		String queryString = "aaa=bbb&ccc=ddd";
		Map<String,SortedSet<String>> map = ServletUtils.getQueryStringAsMap(queryString);
		assertTrue(map.containsKey("aaa"));
		assertEquals("bbb", map.get("aaa").first());
		assertTrue(map.containsKey("ccc"));
		assertEquals("ddd", map.get("ccc").first());
		assertEquals(2, map.size());
	}
	
	@Test
	public void testGetQueryStringAsMap2() {
		String queryString = "aaa=bbb==&ccc===ddd";
		Map<String,SortedSet<String>> map = ServletUtils.getQueryStringAsMap(queryString);
		assertTrue(map.containsKey("aaa"));
		assertEquals("bbb==", map.get("aaa").first());
		assertTrue(map.containsKey("ccc"));
		assertEquals("==ddd", map.get("ccc").first());
		assertEquals(2, map.size());
	}
	
	@Test
	public void testGetQueryStringAsMap3() {
		String queryString = "aaa=&ccc";
		Map<String,SortedSet<String>> map = ServletUtils.getQueryStringAsMap(queryString);
		assertTrue(map.containsKey("aaa"));
		assertEquals("", map.get("aaa").first());
		assertTrue(map.containsKey("ccc"));
		assertEquals(2, map.size());
	}
}
