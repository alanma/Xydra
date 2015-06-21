package org.xydra.index.query;

import org.xydra.index.IPair;

/**
 * A tuple storing two objects.
 * 
 * @author dscharrer
 * @param <A>
 * @param <B>
 */
public class Pair<A, B> implements IPair<A, B> {

	private final A first;

	private final B second;

	public Pair(A first, B second) {
		this.first = first;
		this.second = second;
	}

	@Override
	public A getFirst() {
		return this.first;
	}

	@Override
	public B getSecond() {
		return this.second;
	}

	@Override
	public String toString() {
		return "('" + this.first + "', '" + this.second + "')";
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof Pair<?, ?>))
			return false;
		Pair<?, ?> p = (Pair<?, ?>) other;
		return (this.first == null ? p.first == null : this.first.equals(p.first))
				&& (this.second == null ? p.second == null : this.second.equals(p.second));
	}

	@Override
	public int hashCode() {
		return (this.first == null ? 0 : this.first.hashCode())
				+ (this.second == null ? 0 : this.second.hashCode());
	}

	public Pair<B, A> inverse() {
		return new Pair<B, A>(this.second, this.first);
	}

	public static <A, B> Pair<A, B> create(A a, B b) {
		return new Pair<A, B>(a, b);
	}

}
