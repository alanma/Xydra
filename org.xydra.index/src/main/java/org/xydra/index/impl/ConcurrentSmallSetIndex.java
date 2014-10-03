package org.xydra.index.impl;

import org.xydra.index.IEntrySet;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Based on a simple linked list - much more memory efficient than based on a
 * HashSet - and much slower, too.
 * 
 * @author voelkel
 * 
 * @param <E>
 *            entity type
 */
public class ConcurrentSmallSetIndex<E> extends SmallSetIndex<E> implements IEntrySet<E> {

	private static final long serialVersionUID = 1L;

	@Override
	public synchronized Iterator<E> iterator() {
		List<E> list;
		synchronized (this) {
			list = new ArrayList<E>(this.size());
			list.addAll(this);
		}
		return list.iterator();
	}

}
