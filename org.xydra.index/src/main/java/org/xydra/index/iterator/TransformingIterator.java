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

	private ITransformer<I, O> transformer;

	public TransformingIterator(Iterator<? extends I> base, ITransformer<I, O> transformer) {
		super(base);
		this.transformer = transformer;
	}

	@Override
	public O transform(I in) {
		return this.transformer.transform(in);
	}

}
