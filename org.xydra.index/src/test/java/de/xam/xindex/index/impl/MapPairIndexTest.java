package de.xam.xindex.index.impl;

import de.xam.xindex.index.AbstractPairIndexTest;


public class MapPairIndexTest extends AbstractPairIndexTest {
	
	@Override
	public void setUp() {
		this.index = new MapPairIndex<Integer,Integer>();
	}
	
}
