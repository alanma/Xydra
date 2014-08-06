package org.xydra.index.query;

/**
 * A Constraint that matches objects that are equal to a given object.
 * 
 * @param <E> entity type
 */
public class EqualsConstraint<E> implements Constraint<E> {
    
    protected final E expect;
    
    /**
     * @param expect @CanBeNull if you search for nulls
     */
    public EqualsConstraint(E expect) {
        super();
        this.expect = expect;
    }
    
    /**
     * @return @CanBeNull
     */
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
    
    @Override
    public int hashCode() {
        return this.expect.hashCode();
    }
    
    @Override
    public boolean equals(Object other) {
        return other instanceof EqualsConstraint
                && ((EqualsConstraint<?>)other).expect.equals(this.expect);
    }
    
    @Override
    public E getExpected() {
        return this.expect;
    }
    
    public String toString() {
        return "'" + this.expect.toString() + "'";
    }
    
    @Override
    public boolean isExact() {
        return true;
    }
    
}
