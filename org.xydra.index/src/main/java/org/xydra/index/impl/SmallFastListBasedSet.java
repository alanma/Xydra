package org.xydra.index.impl;

import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Wrap a fast list into a Set to avoid using hashes
 * 
 * @author xamde
 * 
 * @param <E>
 */
public class SmallFastListBasedSet<E> extends AbstractSet<E> {

	private ArrayList<E> elements;

	public SmallFastListBasedSet(int size) {
		this.elements = new ArrayList<E>(size);
	}

	/**
	 * No checks regarding set semantics are done
	 * 
	 * @param element
	 */
	@Override
	public boolean add(E element) {
		return this.elements.add(element);
	}

	@Override
	public Iterator<E> iterator() {
		return this.elements.iterator();
	}

	@Override
	public int size() {
		return this.elements.size();
	}

}
