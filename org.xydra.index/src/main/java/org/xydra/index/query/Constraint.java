package org.xydra.index.query;

/**
 * A constraint can either be an {@link EqualsConstraint} or a {@link Wildcard}.
 * 
 * @author voelkel
 * 
 * @param <E> entity type
 */
public interface Constraint<E> {
	
	/**
	 * This information is used for optimisations.
	 * 
	 * @return true if this constraint is bound in any way, i.e. it's not a
	 *         wild-card
	 */
	boolean isStar();
	
	/**
	 * @param element
	 * @return true if the constraint matches the element, i.e. if the element
	 *         should appear in a result iterator.
	 */
	boolean matches(E element);
	
}
