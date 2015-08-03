package org.xydra.index;

/**
 * An interval which includes both end points. Start == end is legal.
 *
 * @param <T> usually a number or at least a comparable type
 */
public interface IRange<T> {

	public T getStart();

	public T getEnd();

}
