package de.xam.xindex.query;

/**
 * A constraint that matches any object.
 * 
 * @param <K>
 */
public class Wildcard<K> implements Constraint<K> {
	
	public boolean isStar() {
		return true;
	}
	
	public boolean matches(K element) {
		return true;
	}
	
}
