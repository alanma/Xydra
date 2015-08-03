package org.xydra.base.value.impl.memory;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.xydra.base.value.XSetDiffable;
import org.xydra.base.value.XSetValue;


/**
 * A generic implementation for most parts of a {@link XSetValue}, backed by a
 * {@link HashSet}.
 *
 * @author dscharrer
 *
 * @param <E> collection base type, e.g. XInteger
 */
public abstract class MemorySetValue<E> implements XSetValue<E>, XSetDiffable<E>, Serializable {

    private static final long serialVersionUID = -5921208010230423103L;

    // non-final to be GWT-Serializable
    protected Set<E> set = new HashSet<E>();

    // empty constructor for GWT-Serializable
    protected MemorySetValue() {
    }

    public MemorySetValue(final Collection<E> contents) {
        this.set.addAll(contents);
    }

    public MemorySetValue(final E[] contents) {
        this(Arrays.asList(contents));
    }

    public boolean checkEquals(final XSetValue<E> other) {
        if(other instanceof MemorySetValue<?>) {
            return this.set.equals(((MemorySetValue<?>)other).set);
        }
        if(size() != other.size()) {
            return false;
        }
        for(final E e : other) {
            if(!contains(e)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean contains(final E elem) {
        return this.set.contains(elem);
    }

    protected int getHashCode() {
        return this.set.hashCode();
    }

    @Override
    public boolean isEmpty() {
        return this.set.isEmpty();
    }

    @Override
    public Iterator<E> iterator() {
        final Iterator<E> it = this.set.iterator();
        return new Iterator<E>() {

            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @Override
            public E next() {
                return it.next();
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }

        };
    }

    @Override
    public int size() {
        return this.set.size();
    }

    protected E[] toArray(final E[] a) {
        return this.set.toArray(a);
    }

    @Override
    public Set<E> toSet() {
        final Set<E> copy = new HashSet<E>();
        copy.addAll(this.set);
        return copy;
    }

    @Override
    public String toString() {
        return this.set.toString();
    }

    @Override
    public XSetDiff<E> computeDiff(final XSetValue<E> otherSet) {
        final SetDiff<E> diff = new SetDiff<E>();
        if(otherSet instanceof MemorySetValue) {
            // fast way
            final MemorySetValue<E> otherMemSet = (MemorySetValue<E>)otherSet;
            for(final E e : this.set) {
                if(!otherMemSet.set.contains(e)) {
                    diff.removed.add(e);
                }
            }
            for(final E e : otherMemSet.set) {
                if(!this.set.contains(e)) {
                    diff.added.add(e);
                }
            }
        } else {
            // generic way
            diff.added.addAll(otherSet.toSet());
            diff.removed.addAll(this.set);
            // remove (this set-intersection other) from added and removed
            diff.removed.removeAll(diff.added);
            diff.added.removeAll(this.set);
        }
        return diff;
    }

    public static class SetDiff<E> implements XSetDiff<E> {

        public Set<E> removed = new HashSet<E>();
        public Set<E> added = new HashSet<E>();

        @Override
        public Set<E> getAdded() {
            return this.added;
        }

        @Override
        public Set<E> getRemoved() {
            return this.removed;
        }

    }

}
