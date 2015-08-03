package org.xydra.index.iterator;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Turns two iterators (one probably smaller than the other one) into a single
 * iterator, which can contain duplicates.
 *
 * @author voelkel
 *
 * @param <E>
 *            entity type
 */
public class BagUnionIterator<E> extends AbstractCascadedIterator<Iterator<? extends E>, E>
		implements ClosableIterator<E> {

	private Iterator<? extends E> smallIt;
	private Iterator<? extends E> largeIt;

	public BagUnionIterator(final Iterator<? extends E> smallIt, final Iterator<? extends E> largeIt) {
		super(Arrays.asList(smallIt, largeIt).iterator());
		this.smallIt = smallIt;
		this.largeIt = largeIt;
	}

	/**
	 * @param iterators
	 *            none of them is closed
	 */
	@SuppressWarnings({ "unchecked" })
	public BagUnionIterator(final Iterator<? extends E>... iterators) {
		super(Arrays.asList(iterators).iterator());
	}

	/**
	 * @param iterators
	 *            none of them is closed
	 */
	public BagUnionIterator(final List<Iterator<? extends E>> iterators) {
		super(iterators.iterator());
	}

	@Override
	protected Iterator<? extends E> toIterator(final Iterator<? extends E> element) {
		return element;
	}

	@Override
	public void close() {
		if (this.smallIt instanceof ClosableIterator<?>) {
			((ClosableIterator<? extends E>) this.smallIt).close();
		}
		if (this.largeIt instanceof ClosableIterator<?>) {
			((ClosableIterator<? extends E>) this.largeIt).close();
		}
	}

}
