package de.xam.xindex.index.impl;

import de.xam.xindex.Factory;
import de.xam.xindex.index.IEntrySet;


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
