package org.xydra.index.query;

/**
 * A constraint that matches any object.
 * 
 * @param <K>
 *            key type
 */
public class Wildcard<K> implements Constraint<K> {

	@Override
	public boolean isStar() {
		return true;
	}

	@Override
	public boolean matches(K element) {
		return true;
	}

	@Override
	public int hashCode() {
		return 0;
	}

	@Override
	public boolean equals(Object other) {
		return other instanceof Wildcard;
	}

	@Override
	public K getExpected() {
		return null;
	}

	@Override
	public String toString() {
		return "*";
	}

	@Override
	public boolean isExact() {
		return false;
	}

}
