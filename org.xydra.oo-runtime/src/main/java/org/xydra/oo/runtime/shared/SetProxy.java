package org.xydra.oo.runtime.shared;

import java.util.Collection;
import java.util.Set;

import org.xydra.annotations.RunsInGWT;
import org.xydra.base.XId;
import org.xydra.base.rmof.XWritableField;
import org.xydra.base.rmof.XWritableObject;
import org.xydra.base.value.XCollectionValue;

/**
 * A typed, modifiable Xydra-backed Set
 *
 * @author xamde
 *
 * @param <X>
 * @param <T>
 * @param <J>
 * @param <C>
 */
@RunsInGWT(true)
public class SetProxy<X extends XCollectionValue<T>, T, J, C> extends CollectionProxy<X, T, J, C>
		implements Set<C> {

	public SetProxy(final XWritableObject xo, final XId fieldId,
			final CollectionProxy.IComponentTransformer<X, T, J, C> t) {
		super(xo, fieldId, t);
	}

	@Override
	public boolean containsAll(final Collection<?> c) {
		throw new UnsupportedOperationException("Not implemented yet");
	}

	@Override
	public boolean addAll(final Collection<? extends C> c) {
		// TODO add whole collection in one step
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
		final XWritableField f = this.xo.getField(this.fieldId);
		if (f != null) {
			f.setValue(this.componentTransformer.createCollection());
		}
	}

	@Override
	public Object[] toArray() {
		throw new UnsupportedOperationException("Not implemented yet");
	}

	@Override
	public <R> R[] toArray(final R[] a) {
		throw new UnsupportedOperationException("Not implemented yet");
	}

}
