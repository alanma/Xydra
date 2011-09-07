package org.xydra.index.impl;

import org.xydra.index.Factory;
import org.xydra.index.IEntrySet;


/**
 * A factory creating small (but slow) IEntrySet
 * 
 * @author voelkel
 * 
 * @param <E> entity type
 */
public class SmallEntrySetFactory<E> implements Factory<IEntrySet<E>> {
	
	@Override
    public IEntrySet<E> createInstance() {
		return new SmallSetIndex<E>();
	}
	
}
