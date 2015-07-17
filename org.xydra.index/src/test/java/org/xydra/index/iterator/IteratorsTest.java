package org.xydra.index.iterator;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * TODO refactor into a proper JUnit test
 * @author xamde
 */
public class IteratorsTest {

	public static void main(final String[] args) {
		final Map<String, Integer> map = new HashMap<>();
		map.put("five", 5);
		map.put("two", 2);
		map.put("one", 1);
		map.put("three", 3);
		map.put("four", 4);
		final Iterator<Entry<String, Integer>> it = Iterators.sortByValue(map, false);
		while (it.hasNext()) {
			System.out.println(it.next());
		}
	}

}
