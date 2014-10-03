package org.xydra.index.impl;

import org.xydra.index.AbstractMapIndexTest;

public class MapIndexTest extends AbstractMapIndexTest<String, Integer> {

	public MapIndexTest() {
		super(new MapIndex<String, Integer>());
	}

	private static int i = 1;

	private static int s = 1;

	@Override
	protected Integer createEntry() {
		return i++;
	}

	@Override
	protected String createKey() {
		return "s" + s++;
	}

}
