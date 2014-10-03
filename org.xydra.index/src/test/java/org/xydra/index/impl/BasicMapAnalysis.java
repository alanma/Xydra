package org.xydra.index.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class BasicMapAnalysis {

	public static void main(String[] args) {
		/* How does a normal map handle null keys? It depends. */

		testNullHandling(new HashMap<String, String>());
		testNullHandling(new TreeMap<String, String>());
	}

	public static void testNullHandling(Map<String, String> map) {
		map.put(null, "test");
		assert map.keySet() != null;
		assert map.keySet().contains(null);
		assert map.values().contains("test");
	}

}
