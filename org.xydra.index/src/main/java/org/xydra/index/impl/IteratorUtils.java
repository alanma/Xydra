package org.xydra.index.impl;

import java.util.Collection;
import java.util.Iterator;


public class IteratorUtils {
	
	/**
	 * @param <T> type of both
	 * @param it never null
	 * @param collection to which elements are added
	 * @return as a convenience, the supplied collection
	 */
	public static <T> Collection<T> addAll(Iterator<T> it, Collection<T> collection) {
		while(it.hasNext()) {
			T t = it.next();
			collection.add(t);
		}
		return collection;
	}
	
}
