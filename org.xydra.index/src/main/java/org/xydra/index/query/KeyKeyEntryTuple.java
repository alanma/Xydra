package org.xydra.index.query;

import org.xydra.index.impl.DebugUtils;

/**
 * A generic triple
 *
 * @author xamde
 * @param <K>
 * @param <L>
 * @param <E>
 */
public class KeyKeyEntryTuple<K, L, E> implements ITriple<K, L, E>, HasEntry<E> {

	private final E entry;

	private final K key1;

	private final L key2;

	public KeyKeyEntryTuple(final K key1, final L key2, final E entry) {
		this.key1 = key1;
		this.key2 = key2;
		this.entry = entry;
	}

	@Override
	public E getEntry() {
		return this.entry;
	}

	@Override
	public K getKey1() {
		return this.key1;
	}

	@Override
	public L getKey2() {
		return this.key2;
	}

	@Override
	public String toString() {
		return "("

		+ DebugUtils.toLimitedString(this.key1, 20) + ","

		+ DebugUtils.toLimitedString(this.key2, 20) + ","

		+ DebugUtils.toLimitedString(this.entry, 20)

		+ ")";
	}

	@Override
	public int hashCode() {
		return hashCode(this.key1, this.key2, this.entry);
	}

	/**
	 * A universal template for hash codes on triples
	 *
	 * @param s
	 * @param p
	 * @param o
	 * @return
	 */
	public static <K, L, E> int hashCode(final K s, final L p, final E o) {
		int hash = 17;
		hash = hash * 31 + s.hashCode();
		hash = hash * 31 + p.hashCode();
		hash = hash * 31 + o.hashCode();
		return hash;
	}

	@Override
	public boolean equals(final Object other) {
		return other instanceof ITriple<?, ?, ?>

		&& this.key1.equals(((ITriple<?, ?, ?>) other).getKey1())

		&& this.key2.equals(((ITriple<?, ?, ?>) other).getKey2())

		&& this.entry.equals(((ITriple<?, ?, ?>) other).getEntry());
	}

	@Override
	public K s() {
		return this.key1;
	}

	@Override
	public L p() {
		return this.key2;
	}

	@Override
	public E o() {
		return this.entry;
	}

	public static <K, L, E> boolean equals(final K s1, final K s2, final L p1, final L p2, final E o1, final E o2) {
		if (!s1.equals(s2)) {
			return false;
		}

		if (!p1.equals(p2)) {
			return false;
		}

		if (!o1.equals(o2)) {
			return false;
		}

		return true;
	}

}
