package org.xydra.index.impl;

import org.xydra.index.AbstractPairIndexTest;

public class MapPairIndexTest extends AbstractPairIndexTest {

	@Override
	public void setUp() {
		this.index = new MapPairIndex<Integer, Integer>();
	}

}
