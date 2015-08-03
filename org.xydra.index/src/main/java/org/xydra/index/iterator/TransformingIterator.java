package org.xydra.index.iterator;

import java.util.Iterator;

/**
 * An AbstractTransformingIterator that simply delegates the transformation to
 * an extra helper object, a {@link Transformer}
 *
 * @author voelkel
 *
 * @param <I>
 *            input type
 * @param <O>
 *            output type
 */
public class TransformingIterator<I, O> extends AbstractTransformingIterator<I, O> {

	/**
	 * @param <I>
	 *            input type
	 * @param <O>
	 *            output type
	 * @deprecated use {@link ITransformer} instead
	 */
	@Deprecated
	public interface Transformer<I, O> extends ITransformer<I, O> {

		@Override
		O transform(I in);

	}

	private final ITransformer<I, O> transformer;

	public TransformingIterator(final Iterator<? extends I> base, final ITransformer<I, O> transformer) {
		super(base);
		this.transformer = transformer;
	}

	@Override
	public O transform(final I in) {
		return this.transformer.transform(in);
	}

}
