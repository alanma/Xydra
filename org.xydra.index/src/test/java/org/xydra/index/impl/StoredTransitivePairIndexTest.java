package org.xydra.index.impl;

import org.xydra.index.AbstractTransitivePairIndexTest;
import org.xydra.index.Factory;
import org.xydra.index.IPairIndex;

public class StoredTransitivePairIndexTest extends AbstractTransitivePairIndexTest {

	@Override
	public void setUp() {
		this.index = this.idx = new StoredTransitivePairIndex<Integer>(
				new PairIndex<Integer, Integer>(), new Factory<IPairIndex<Integer, Integer>>() {

					@Override
					public IPairIndex<Integer, Integer> createInstance() {
						return new MapPairIndex<Integer, Integer>();
					}

				});
	}

}
