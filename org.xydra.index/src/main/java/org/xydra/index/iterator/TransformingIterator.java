package org.xydra.index.iterator;

import java.util.Iterator;


/**
 * An AbstractTransformingIterator that simply delegates the transformation to
 * an extra helper object, a {@link Transformer}
 * 
 * @author voelkel
 * 
 * @param <I> input type
 * @param <O> output type
 */
public class TransformingIterator<I, O> extends AbstractTransformingIterator<I,O> {
	
	public interface Transformer<I, O> {
		
		O transform(I in);
		
	}
	
	private Transformer<I,O> transformer;
	
	public TransformingIterator(Iterator<? extends I> base, Transformer<I,O> transformer) {
		super(base);
		this.transformer = transformer;
	}
	
	@Override
	public O transform(I in) {
		return this.transformer.transform(in);
	}
	
}
