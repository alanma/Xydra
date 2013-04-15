package org.xydra.core.util;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


public class DebugUtils {
	
	public static void dumpStacktrace() {
		try {
			throw new RuntimeException("CALLER");
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static <T> Set<T> toSet(Iterator<T> iterator) {
		HashSet<T> set = new HashSet<T>();
		while(iterator.hasNext()) {
			set.add(iterator.next());
		}
		return set;
	}
	
}
