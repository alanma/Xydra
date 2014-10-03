package org.xydra.index.impl;

import org.xydra.index.AbstractPairIndexTest;
import org.xydra.index.impl.MapPairIndex;

public class MapPairIndexTest extends AbstractPairIndexTest {

	@Override
	public void setUp() {
		this.index = new MapPairIndex<Integer, Integer>();
	}

}
