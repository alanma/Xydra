package org.xydra.core.util;

/**
 * The Variable {@link #ANY} denotes a wild-card in queries for XId or XValue.
 * 
 * Currently used in {@link ModelIndex} only.
 */
public interface Variable {
	
	public static final Variable ANY = new Variable() {
	};
	
}
