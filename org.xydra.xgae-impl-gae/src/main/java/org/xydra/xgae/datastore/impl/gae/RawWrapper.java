package org.xydra.xgae.datastore.impl.gae;

import java.util.Iterator;

import org.xydra.index.iterator.ITransformer;
import org.xydra.index.iterator.TransformingIterator;
import org.xydra.xgae.datastore.api.SWrapper;

/**
 * Design decision for nulls: Don't wrap nulls
 *
 * @author xamde
 *
 * @param <R>
 * @param <S>
 */
public class RawWrapper<R, S extends SWrapper> {

	private final R raw;

	public RawWrapper(final R raw) {
		if (raw == null) {
			throw new IllegalArgumentException();
		}

		this.raw = raw;
	}

	public R raw() {
		return this.raw;
	}

	private static class UnwrappedIterable<R, S extends SWrapper> implements Iterable<R> {

		public UnwrappedIterable(final Iterable<S> it) {
			super();
			this.iterable = it;
		}

		private final Iterable<S> iterable;

		@Override
		public Iterator<R> iterator() {
			return new TransformingIterator<S, R>(this.iterable.iterator(),
					new ITransformer<S, R>() {

						@SuppressWarnings("unchecked")
						@Override
						public R transform(final S in) {
							return (R) in.raw();
						}
					});
		}

	}

	protected static <R, S extends SWrapper> Iterable<R> _unwrap(final Iterable<S> it) {
		return new UnwrappedIterable<R, S>(it);
	}

}
