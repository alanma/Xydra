package org.xydra.index.impl;

import org.xydra.index.AbstractMapMapSetIndexTest;

public class MapMapSetIndexTest extends AbstractMapMapSetIndexTest<String, Integer, Double> {

	public MapMapSetIndexTest() {
		super(new MapMapSetIndex<String, Integer, Double>(new SmallEntrySetFactory<Double>()));
	}

	private static double d = 1d;

	private static int i = 1;

	private static int s = 1;

	@Override
	protected Double createEntry() {
		return d++;
	}

	@Override
	protected String createKey_K() {
		return "s" + s++;
	}

	@Override
	protected Integer createKey_L() {
		return i++;
	}

}
