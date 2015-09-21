package org.xydra.index.impl;

import org.xydra.index.IIndex;

/**
 * A map that maps entries to a count. Only positive counts are remembered, so use {@link #deIndex(Object)} carefully.
 *
 * @author xamde
 * @param <T>
 */
public class CountingMap<T> extends AbstractCountingMap<T, Integer>implements IIndex {

	@Override
	protected Integer add(final Integer i, final Integer increment) {
		return i + increment;
	}

	@Override
	protected boolean equalsZero(final Integer a) {
		return a==0;
	}

	@Override
	protected Integer plusOne() {
		return 1;
	}

	@Override
	protected Integer zero() {
		return 0;
	}

	@Override
	protected Integer minusOne() {
		return -1;
	}

	@Override
	protected Integer subtract(final Integer a, final Integer b) {
		return a-b;
	}

	@Override
	protected int toSignumInt(final Integer a) {
		return a;
	}

}
