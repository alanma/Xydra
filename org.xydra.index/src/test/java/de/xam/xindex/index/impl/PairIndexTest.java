package de.xam.xindex.index.impl;

import de.xam.xindex.index.AbstractPairIndexTest;


public class PairIndexTest extends AbstractPairIndexTest {
	
	@Override
	public void setUp() {
		this.index = new PairIndex<Integer,Integer>();
	}
	
}
