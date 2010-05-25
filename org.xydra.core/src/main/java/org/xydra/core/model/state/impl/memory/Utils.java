package org.xydra.core.model.state.impl.memory;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import org.xydra.core.model.XID;



public class Utils {
	
	/**
	 * A fast, memory efficient comparison
	 * 
	 * @param <T>
	 * @param aIterator should be the smaller iterator of the two
	 * @param bIterator
	 * @return
	 */
	public static <T> boolean equals(Iterator<T> aIterator, Iterator<T> bIterator) {
		Collection<T> aCollection = new HashSet<T>();
		while(aIterator.hasNext()) {
			aCollection.add(aIterator.next());
		}
		
		int bCount = 0;
		while(bIterator.hasNext()) {
			T b = bIterator.next();
			if(!aCollection.contains(b)) {
				return false;
			} else {
				bCount++;
			}
		}
		if(bCount != aCollection.size()) {
			return false;
		}
		
		return true;
	}
	
	public static String toString(Iterator<XID> idIterator, String separator) {
		StringBuffer buf = new StringBuffer();
		while(idIterator.hasNext()) {
			XID xid = idIterator.next();
			buf.append(xid.toString());
			buf.append(separator);
		}
		return buf.toString();
	}
	
}
