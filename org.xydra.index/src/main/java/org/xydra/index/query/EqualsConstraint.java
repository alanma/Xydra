package org.xydra.index.query;

/**
 * A Constraint that matches objects that are equal to a given object.
 * 
 * @param <E> entity type
 */
public class EqualsConstraint<E> implements Constraint<E> {
	
	private E expect;
	
	public EqualsConstraint(E expect) {
		super();
		this.expect = expect;
	}
	
	public E getKey() {
		return this.expect;
	}
	
	@Override
    public boolean isStar() {
		return false;
	}
	
	@Override
    public boolean matches(E element) {
		return this.expect == element || (this.expect != null && this.expect.equals(element));
	}
	
}
