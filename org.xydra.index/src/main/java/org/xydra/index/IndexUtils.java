package org.xydra.index;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.xydra.index.impl.MapMapIndex;
import org.xydra.index.query.Constraint;
import org.xydra.index.query.KeyKeyEntryTuple;

import com.google.common.collect.Sets;

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
	 * @param mapMapIndex
	 *            where to deIndex from
	 * @param c1
	 *            constraint for first tuple position
	 * @param c2
	 *            constraint for second tuple position
	 */
	public static <K, L, V> void deIndex(MapMapIndex<K, L, V> mapMapIndex, Constraint<K> c1,
			Constraint<L> c2) {
		Iterator<KeyKeyEntryTuple<K, L, V>> it = mapMapIndex.tupleIterator(c1, c2);
		Set<KeyKeyEntryTuple<K, L, V>> toDelete = new HashSet<KeyKeyEntryTuple<K, L, V>>();
		while (it.hasNext()) {
			KeyKeyEntryTuple<K, L, V> entry = it.next();
			toDelete.add(entry);
		}
		for (KeyKeyEntryTuple<K, L, V> entry : toDelete) {
			mapMapIndex.deIndex(entry.getKey1(), entry.getKey2());
		}
	}

	/**
	 * @param <E>
	 *            ..
	 * @param it
	 *            ..
	 * @return a HashSet containing all entries of the iterator
	 */
	public static <E> Set<E> toSet(Iterator<E> it) {
		Set<E> set = new HashSet<E>();
		while (it.hasNext()) {
			set.add(it.next());
		}
		return set;
	}

	/**
	 * @param <T>
	 *            any type
	 * @param base
	 *            ..
	 * @param added
	 *            ..
	 * @param removed
	 *            ..
	 * @return all elements present in base, minus those in removed, plus those
	 *         in added.
	 */
	public static <T> Set<T> diff(Iterator<T> base, Iterator<T> added, Iterator<T> removed) {
		Set<T> set = toSet(base);
		while (removed.hasNext()) {
			set.remove(removed.next());
		}
		while (added.hasNext()) {
			set.add(added.next());
		}
		return set;
	}

	/**
	 * A diff of two sets
	 * 
	 * @param <E>
	 */
	public static interface ISetDiff<E> {
		/**
		 * @return all added tuples; writes to this data have no effect.
		 */
		Set<E> getAdded();

		/**
		 * @return all removed tuples; writes to this data have no effect.
		 */
		Set<E> getRemoved();
	}

	public static class SetDiff<E> implements ISetDiff<E> {

		public SetDiff(Set<E> added, Set<E> removed) {
			super();
			this.added = added;
			this.removed = removed;
		}

		@Override
		public String toString() {
			return "SetDiff [added=" + this.added + ", removed=" + this.removed + "]";
		}

		private Set<E> added;
		private Set<E> removed;

		@Override
		public Set<E> getAdded() {
			return this.added;
		}

		@Override
		public Set<E> getRemoved() {
			return this.removed;
		}

	}

	/**
	 * @param a
	 * @NeverNull
	 * @param b
	 * @NeverNull
	 * @return a diff with all elements added (not present in a, but present in
	 *         b); and all elements remove (present in a, but no longer present
	 *         in b)
	 */
	public static <T> ISetDiff<T> diff(Set<T> a, Set<T> b) {
		assert a != null;
		assert b != null;

		Set<T> added = new HashSet<T>();
		Set<T> removed = new HashSet<T>();
		if (a.size() > b.size()) {
			// b is small(er)
			for (T elemB : b) {
				if (!a.contains(elemB)) {
					added.add(elemB);
				}
			}
			removed.addAll(a);
			removed.removeAll(b);
		} else {
			// a is small(er)
			added.addAll(b);
			added.removeAll(a);
			for (T elemA : a) {
				if (!b.contains(elemA)) {
					removed.add(elemA);
				}
			}
		}
		return new SetDiff<T>(added, removed);
	}

	public static void main(String[] args) {
		Set<String> a = Sets.newHashSet("1", "2", "3", "4");
		Set<String> b = Sets.newHashSet("3", "4", "5", "6");
		ISetDiff<String> diff = diff(a, b);
		System.out.println(diff.toString());
	}

	/**
	 * @param s
	 * @param p
	 * @param o
	 * @return a string in syntax '(*, 'foo', 'bar)', '(*, *, *)' and similar
	 */
	public static <K, L, M> String asQuery(K s, L p, M o) {
		StringBuffer buf = new StringBuffer();
		buf.append("(");
		buf.append(s == null ? "*" : "'" + s + "'");
		buf.append(", ");
		buf.append(p == null ? "*" : "'" + p + "'");
		buf.append(", ");
		buf.append(o == null ? "*" : "'" + o + "'");
		buf.append(")");
		return buf.toString();
	}

	/**
	 * @param cS
	 * @param cP
	 * @param cO
	 * @return (....)
	 */
	public static <K, L, M> String asQuery(Constraint<K> cS, Constraint<L> cP, Constraint<M> cO) {
		StringBuffer buf = new StringBuffer();
		buf.append("(");
		buf.append(cS == null ? "*" : cS.toString());
		buf.append(", ");
		buf.append(cP == null ? "*" : cP.toString());
		buf.append(", ");
		buf.append(cO == null ? "*" : cO.toString());
		buf.append(")");
		return buf.toString();
	}

}
