package org.xydra.core.value.impl.memory;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.xydra.core.value.XSetValue;


/**
 * A generic implementation for most parts of a {@link XSetValue}, backed by a
 * {@link HashSet}.
 * 
 * @author dscharrer
 * 
 * @param <E>
 */
public abstract class MemorySetValue<E> implements XSetValue<E> {
	
	private static final long serialVersionUID = -5921208010230423103L;
	
	private final Set<E> set = new HashSet<E>();
	
	public MemorySetValue(E[] contents) {
		this(Arrays.asList(contents));
	}
	
	public MemorySetValue(Collection<E> contents) {
		this.set.addAll(contents);
	}
	
	public Set<E> toSet() {
		Set<E> copy = new HashSet<E>();
		copy.addAll(this.set);
		return copy;
	}
	
	public boolean contains(E elem) {
		return this.set.contains(elem);
	}
	
	public boolean isEmpty() {
		return this.set.isEmpty();
	}
	
	public int size() {
		return this.set.size();
	}
	
	protected E[] toArray(E[] a) {
		return this.set.toArray(a);
	}
	
	public Iterator<E> iterator() {
		final Iterator<E> it = this.set.iterator();
		return new Iterator<E>() {
			
			public boolean hasNext() {
				return it.hasNext();
			}
			
			public E next() {
				return it.next();
			}
			
			public void remove() {
				throw new UnsupportedOperationException();
			}
			
		};
	}
	
	public boolean checkEquals(XSetValue<E> other) {
		if(other instanceof MemorySetValue<?>) {
			return this.set.equals(((MemorySetValue<?>)other).set);
		}
		if(size() != other.size()) {
			return false;
		}
		for(E e : other) {
			if(!contains(e)) {
				return false;
			}
		}
		return true;
	}
	
	protected int getHashCode() {
		return this.set.hashCode();
	}
	
	@Override
	public String toString() {
		return this.set.toString();
	}
	
}
