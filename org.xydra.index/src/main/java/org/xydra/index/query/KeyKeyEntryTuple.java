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
		return this.key1.hashCode() + this.key2.hashCode() + this.entry.hashCode();
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

}
