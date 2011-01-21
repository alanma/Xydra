package org.xydra.index;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


/**
 * Commonly used helper functions.
 * 
 * @author dscharrer
 * 
 */
public class XI {
	
	/**
	 * Check if the two objects are equal. The given objects can be null. Two
	 * null objects are equals and any non-null object is not equal to null.
	 */
	public static boolean equals(Object a, Object b) {
		return a == b || (a != null && a.equals(b));
	}
	
	/**
	 * Checks if two iterators have the same elements in the same order.
	 * 
	 * @return true if both iterators have the same number of elements and each
	 *         pair of elements is equal.
	 */
	static public boolean equalsIterator(Iterator<?> a, Iterator<?> b) {
		while(a.hasNext()) {
			if(!b.hasNext()) {
				return false;
			}
			if(!XI.equals(a.next(), b.next())) {
				return false;
			}
		}
		return !b.hasNext();
	}
	
	@SuppressWarnings("unchecked")
	public static <T> Set<T> toJavaSet(IEntrySet<T> entrySet) {
		if(entrySet instanceof Set) {
			/* if created with FastEntrySetFactory, this works */
			return (Set<T>)entrySet;
		} // else: convert
		Iterator<T> it = entrySet.iterator();
		Set<T> result = new HashSet<T>();
		while(it.hasNext()) {
			T xid = it.next();
			result.add(xid);
		}
		return result;
	}
	
}
