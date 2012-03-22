package org.xydra.index;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.xydra.index.impl.MapMapIndex;
import org.xydra.index.query.Constraint;
import org.xydra.index.query.KeyKeyEntryTuple;


/**
 * Some static methods performing simple or complex operations that work on top
 * of defined interfaces.
 * 
 * @author xamde
 * 
 */
public class IndexUtils {
	
	/**
	 * DeIndex all entries matching the given query.
	 * 
	 * IMPROVE consider removing while iterating
	 * 
	 * @param mapMapIndex where to deIndex from
	 * @param c1 constraint for first tuple position
	 * @param c2 constraint for second tuple position
	 */
	public static <K, L, V> void deIndex(MapMapIndex<K,L,V> mapMapIndex, Constraint<K> c1,
	        Constraint<L> c2) {
		Iterator<KeyKeyEntryTuple<K,L,V>> it = mapMapIndex.tupleIterator(c1, c2);
		Set<KeyKeyEntryTuple<K,L,V>> toDelete = new HashSet<KeyKeyEntryTuple<K,L,V>>();
		while(it.hasNext()) {
			KeyKeyEntryTuple<K,L,V> entry = it.next();
			toDelete.add(entry);
		}
		for(KeyKeyEntryTuple<K,L,V> entry : toDelete) {
			mapMapIndex.deIndex(entry.getKey1(), entry.getKey2());
		}
	}
	
	/**
	 * @param <E> ..
	 * @param it ..
	 * @return a HashSet containing all entries of the iterator
	 */
	public static <E> Set<E> toSet(Iterator<E> it) {
		Set<E> set = new HashSet<E>();
		while(it.hasNext()) {
			set.add(it.next());
		}
		return set;
	}
	
	/**
	 * @param <T> any type
	 * @param base ..
	 * @param added ..
	 * @param removed ..
	 * @return all elements present in base, minus those in removed, plus those
	 *         in added.
	 */
	public static <T> Set<T> diff(Iterator<T> base, Iterator<T> added, Iterator<T> removed) {
		Set<T> set = toSet(base);
		while(removed.hasNext()) {
			set.remove(removed.next());
		}
		while(added.hasNext()) {
			set.add(added.next());
		}
		return set;
	}
	
}
