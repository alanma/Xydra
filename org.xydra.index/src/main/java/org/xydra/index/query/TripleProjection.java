package org.xydra.index.query;

import java.util.Iterator;

import org.xydra.index.iterator.ITransformer;
import org.xydra.index.iterator.Iterators;

/**
 * Utility class to project parts of an {@link ITriple}.
 *
 * @author xamde
 */
public class TripleProjection {

	private static final ITransformer<ITriple<?, ?, ?>, ?> PROJECT_O = new ITransformer<ITriple<?, ?, ?>, Object>() {

		@Override
		public Object transform(final ITriple<?, ?, ?> in) {
			return in.getEntry();
		}

	};

	private static final ITransformer<ITriple<?, ?, ?>, ?> PROJECT_P = new ITransformer<ITriple<?, ?, ?>, Object>() {

		@Override
		public Object transform(final ITriple<?, ?, ?> in) {
			return in.getKey2();
		}

	};

	private static final ITransformer<ITriple<?, ?, ?>, ?> PROJECT_S = new ITransformer<ITriple<?, ?, ?>, Object>() {

		@Override
		public Object transform(final ITriple<?, ?, ?> in) {
			return in.getKey1();
		}

	};

	@SuppressWarnings("unchecked")
	public static <T> Iterator<T> o(final Iterator<? extends ITriple<?, ?, T>> it) {
		return (Iterator<T>) Iterators.transform(it, PROJECT_O);
	}

	@SuppressWarnings("unchecked")
	public static <T> Iterator<T> p(final Iterator<? extends ITriple<?, T, ?>> it) {
		return (Iterator<T>) Iterators.transform(it, PROJECT_P);

	}

	@SuppressWarnings("unchecked")
	public static <T> Iterator<T> s(final Iterator<? extends ITriple<T, ?, ?>> it) {
		return (Iterator<T>) Iterators.transform(it, PROJECT_S);
	}

}
