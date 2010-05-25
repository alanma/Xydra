package org.xydra.index.impl;

import org.xydra.index.Factory;
import org.xydra.index.IEntrySet;


/**
 * A factory creating small (but slow) IEntrySet
 * 
 * @author voelkel
 * 
 * @param <E>
 */
public class SmallEntrySetFactory<E> implements Factory<IEntrySet<E>> {
	
	public IEntrySet<E> createInstance() {
		return new SmallSetIndex<E>();
	}
	
}
