package org.xydra.index.query;

import org.xydra.index.iterator.IFilter;


/**
 * A constraint can either be an {@link EqualsConstraint} or a {@link Wildcard}.
 * 
 * @author voelkel
 * 
 * @param <E> entity type
 */
public interface Constraint<E> extends IFilter<E> {
    
    /**
     * This information is used for optimisations.
     * 
     * @return false if this constraint is bound in any way, i.e. it's not a
     *         wild-card
     */
    boolean isStar();
    
    /**
     * @return true if this constraint targets exactly 1 result
     */
    boolean isExact();
    
    /**
     * Inherited from {@link IFilter}.
     * 
     * @param element @CanBeNull
     * @return true if the constraint matches the element, i.e. if the element
     *         should appear in a result iterator.
     */
    boolean matches(E element);
    
    /**
     * @return the expected object; @CanBeNull if this is a (kind of) wild card
     *         or if you search for nulls
     */
    E getExpected();
    
}
