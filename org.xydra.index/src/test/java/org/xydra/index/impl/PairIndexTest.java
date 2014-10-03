package org.xydra.index.impl;

import org.xydra.index.AbstractPairIndexTest;
import org.xydra.index.impl.PairIndex;

public class PairIndexTest extends AbstractPairIndexTest {

	@Override
	public void setUp() {
		this.index = new PairIndex<Integer, Integer>();
	}

}
