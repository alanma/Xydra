package org.xydra.index.iterator;

/**
 * A standard transformer to be used in transforming iterators etc.
 * 
 * @author xamde
 * 
 * @param <I>
 *            input type
 * @param <O>
 *            output type
 */
public interface ITransformer<I, O> {

	/**
	 * @param in
	 * @return ipout type instance of type I transformed to the output type O
	 */
	O transform(I in);

}
