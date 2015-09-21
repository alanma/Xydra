package org.xydra.index.impl;

import org.xydra.index.IIndex;

/**
 * A map that maps entries to a scoring value. Only positive counts are remembered, so use {@link #deIndex(Object)} carefully.
 *
 * @author xamde
 * @param <T>
 */
public class ScoringMap<T> extends AbstractCountingMap<T, Double>implements IIndex {

	@Override
	protected Double add(final Double i, final Double increment) {
		return i + increment;
	}

	@Override
	protected boolean equalsZero(final Double a) {
		return a==0;
	}

	@Override
	protected Double plusOne() {
		return 1d;
	}

	@Override
	protected Double zero() {
		return 0d;
	}

	@Override
	protected Double minusOne() {
		return -1d;
	}

	@Override
	protected Double subtract(final Double a, final Double b) {
		return a-b;
	}

	@Override
	protected int toSignumInt(final Double a) {
		return (int) Math.signum(a);
	}

}
