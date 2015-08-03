package org.xydra.oo.runtime.shared;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.xydra.annotations.RunsInGWT;
import org.xydra.base.XId;
import org.xydra.base.rmof.XWritableObject;
import org.xydra.base.value.XCollectionValue;

/**
 * A typed, modifiable Xydra-backed List
 *
 * @author xamde
 *
 * @param <X>
 * @param <T>
 * @param <J>
 * @param <C>
 */
@RunsInGWT(true)
public class ListProxy<X extends XCollectionValue<T>, T, J, C> extends CollectionProxy<X, T, J, C>
		implements List<C> {

	/**
	 * @param xo
	 * @param fieldId
	 * @param componentTransformer
	 *            @NeverNull
	 */
	public ListProxy(final XWritableObject xo, final XId fieldId,
			final CollectionProxy.IComponentTransformer<X, T, J, C> componentTransformer) {
		super(xo, fieldId, componentTransformer);
	}

	@Override
	public Object[] toArray() {
		throw new UnsupportedOperationException("Not implemented yet");
	}

	@Override
	public <R> R[] toArray(final R[] a) {
		throw new UnsupportedOperationException("Not implemented yet");
	}

	@Override
	public boolean containsAll(final Collection<?> c) {
		throw new UnsupportedOperationException("Not implemented yet");
	}

	@Override
	public boolean addAll(final Collection<? extends C> c) {
		final Iterator<? extends C> it = c.iterator();
		boolean changed = false;
		while (it.hasNext()) {
			final C cItem = it.next();
			changed |= super.add(cItem);
		}
		return changed;
	}

	@Override
	public boolean addAll(final int index, final Collection<? extends C> c) {
		throw new UnsupportedOperationException("Not implemented yet");
	}

	@Override
	public boolean removeAll(final Collection<?> c) {
		throw new UnsupportedOperationException("Not implemented yet");
	}

	@Override
	public boolean retainAll(final Collection<?> c) {
		throw new UnsupportedOperationException("Not implemented yet");
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException("Not implemented yet");
	}

	@Override
	public C get(final int index) {
		throw new UnsupportedOperationException("Not implemented yet");
	}

	@Override
	public C set(final int index, final C element) {
		throw new UnsupportedOperationException("Not implemented yet");
	}

	@Override
	public void add(final int index, final C element) {
		throw new UnsupportedOperationException("Not implemented yet");
	}

	@Override
	public C remove(final int index) {
		throw new UnsupportedOperationException("Not implemented yet");
	}

	@Override
	public int indexOf(final Object o) {
		throw new UnsupportedOperationException("Not implemented yet");
	}

	@Override
	public int lastIndexOf(final Object o) {
		throw new UnsupportedOperationException("Not implemented yet");
	}

	@Override
	public ListIterator<C> listIterator() {
		throw new UnsupportedOperationException("Not implemented yet");
	}

	@Override
	public ListIterator<C> listIterator(final int index) {
		throw new UnsupportedOperationException("Not implemented yet");
	}

	@Override
	public List<C> subList(final int fromIndex, final int toIndex) {
		throw new UnsupportedOperationException("Not implemented yet");
	}

}
