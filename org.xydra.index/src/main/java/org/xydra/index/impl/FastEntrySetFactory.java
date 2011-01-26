package org.xydra.index.impl;

import org.xydra.index.Factory;
import org.xydra.index.IEntrySet;


/**
 * A factory for creating fast (but larger) {@link IEntrySet}s
 * 
 * @author voelkel
 * 
 * @param <E> entity type
 */
public class FastEntrySetFactory<E> implements Factory<IEntrySet<E>> {
	
	public IEntrySet<E> createInstance() {
		return new FastSetIndex<E>();
	}
	
}
