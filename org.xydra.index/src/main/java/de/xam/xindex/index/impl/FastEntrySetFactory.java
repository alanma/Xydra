package de.xam.xindex.index.impl;

import de.xam.xindex.Factory;
import de.xam.xindex.index.IEntrySet;


/**
 * A factory for creating fast (but larger) {@link IEntrySet}s
 * 
 * @author voelkel
 * 
 * @param <E>
 */
public class FastEntrySetFactory<E> implements Factory<IEntrySet<E>> {
	
	public IEntrySet<E> createInstance() {
		return new FastSetIndex<E>();
	}
	
}
