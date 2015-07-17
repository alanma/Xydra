package org.xydra.index.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.xydra.index.IEntrySet;

/**
 * Based on a simple linked list - much more memory efficient than based on a
 * HashSet - and much slower, too.
 *
 * @author voelkel
 *
 * @param <E> entity type
 */
public class ConcurrentSmallSetIndex<E> extends SmallSetIndex<E> implements IEntrySet<E>, Serializable {

	private static final long serialVersionUID = 1L;

	@Override
	public synchronized Iterator<E> iterator() {
		List<E> list;
		synchronized (this) {
			list = new ArrayList<E>(size());
			list.addAll(this);
		}
		return list.iterator();
	}

}
