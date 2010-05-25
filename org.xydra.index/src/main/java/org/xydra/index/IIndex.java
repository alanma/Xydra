package org.xydra.index;

import java.io.Serializable;


/**
 * Generic super-interface for all kinds of indexes.
 * 
 * @author voelkel
 * 
 */
public interface IIndex extends Serializable {
	
	void clear();
	
	boolean isEmpty();
	
}
