package de.xam.xindex.iterator;

import java.util.Iterator;


/**
 * An AbstractTransformingIterator that simply delegates the transformation to
 * an extra helper object, a {@link Transformer}
 * 
 * @author voelkel
 * 
 * @param <I>
 * @param <O>
 */
public class TransformingIterator<I, O> extends AbstractTransformingIterator<I,O> {
	
	/**
     * 
     */
    private static final long serialVersionUID = 5011114245818703216L;

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
