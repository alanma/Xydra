package de.xam.xindex.index.impl;

import de.xam.xindex.Factory;
import de.xam.xindex.index.AbstractTransitivePairIndexTest;
import de.xam.xindex.index.IPairIndex;


public class StoredTransitivePairIndexTest extends AbstractTransitivePairIndexTest {
	
	@Override
	public void setUp() {
		this.index = this.idx = new StoredTransitivePairIndex<Integer>(
		        new PairIndex<Integer,Integer>(), new Factory<IPairIndex<Integer,Integer>>() {
			        
			        public IPairIndex<Integer,Integer> createInstance() {
				        return new MapPairIndex<Integer,Integer>();
			        }
			        
		        });
	}
	
}
