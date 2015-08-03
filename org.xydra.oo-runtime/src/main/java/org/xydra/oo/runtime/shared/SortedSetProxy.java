package org.xydra.oo.runtime.shared;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.SortedSet;

import org.xydra.annotations.RunsInGWT;
import org.xydra.base.XId;
import org.xydra.base.rmof.XWritableField;
import org.xydra.base.rmof.XWritableObject;
import org.xydra.base.value.XCollectionValue;

/**
 * A typed, modifiable Xydra-backed SortedSet
 *
 * @author xamde
 *
 * @param <X>
 * @param <T>
 * @param <J>
 * @param <C>
 */
@RunsInGWT(true)
public class SortedSetProxy<X extends XCollectionValue<T>, T, J, C> extends
		CollectionProxy<X, T, J, C> implements SortedSet<C> {

	public SortedSetProxy(final XWritableObject xo, final XId fieldId,
			final CollectionProxy.IComponentTransformer<X, T, J, C> t) {
		super(xo, fieldId, t);
	}

	@Override
	public boolean add(final C j) {
		final boolean changes = super.add(j);
		if (changes && size() > 1) {
			/* IMPROVE more efficient */
			final XWritableField f = this.xo.getField(this.fieldId);

			@SuppressWarnings("unchecked")
			XCollectionValue<T> col = (XCollectionValue<T>) f.getValue();
			final List<T> list = new ArrayList<T>();
			list.addAll(Arrays.asList(col.toArray()));
			Collections.sort(list, new Comparator<T>() {

				@Override
				public int compare(final T o1, final T o2) {
					return o1.toString().compareTo(o2.toString());
				}
			});
			col = this.componentTransformer.createCollection();

			for (final T element : list) {
				col = col.add(element);
			}
			f.setValue(col);
		}

		return changes;
	}

	@Override
	public boolean containsAll(final Collection<?> c) {
		throw new UnsupportedOperationException("Not implemented yet");
	}

	@Override
	public boolean addAll(final Collection<? extends C> c) {
		throw new UnsupportedOperationException("Not implemented yet");
	}

	@Override
	public boolean retainAll(final Collection<?> c) {
		throw new UnsupportedOperationException("Not implemented yet");
	}

	@Override
	public boolean removeAll(final Collection<?> c) {
		throw new UnsupportedOperationException("Not implemented yet");
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException("Not implemented yet");
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
	public Comparator<? super C> comparator() {
		throw new UnsupportedOperationException("Not implemented yet");
	}

	@Override
	public SortedSet<C> subSet(final C fromElement, final C toElement) {
		throw new UnsupportedOperationException("Not implemented yet");
	}

	@Override
	public SortedSet<C> headSet(final C toElement) {
		throw new UnsupportedOperationException("Not implemented yet");
	}

	@Override
	public SortedSet<C> tailSet(final C fromElement) {
		throw new UnsupportedOperationException("Not implemented yet");
	}

	@Override
	public C first() {
		throw new UnsupportedOperationException("Not implemented yet");
	}

	@Override
	public C last() {
		throw new UnsupportedOperationException("Not implemented yet");
	}

}
