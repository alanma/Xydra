package org.xydra.index.query;

/**
 * A constraint that matches any object.
 * 
 * @param <K> key type
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
	
}
