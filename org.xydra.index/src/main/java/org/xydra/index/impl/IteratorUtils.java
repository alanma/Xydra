package org.xydra.index.impl;

import java.util.Collection;
import java.util.Iterator;


public class IteratorUtils {
	
	/**
	 * @param <T> type of both
	 * @param <C> a collection type of T
	 * @param it never null
	 * @param collection to which elements are added
	 * @return as a convenience, the supplied collection
	 */
	public static <C extends Collection<T>, T> C addAll(Iterator<? extends T> it, C collection) {
		while(it.hasNext()) {
			T t = it.next();
			collection.add(t);
		}
		return collection;
	}
	
	public static <T> boolean isEmpty(Iterable<T> iterable) {
		return isEmpty(iterable.iterator());
	}
	
	public static <T> boolean isEmpty(Iterator<T> it) {
		return !it.hasNext();
	}
	
	public static String toText(Collection<String> value) {
		StringBuffer buf = new StringBuffer();
		for(String s : value) {
			buf.append(s).append(",");
		}
		return buf.toString();
	}
}
