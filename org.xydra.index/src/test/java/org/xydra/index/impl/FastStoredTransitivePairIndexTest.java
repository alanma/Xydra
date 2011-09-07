package org.xydra.index.impl;

import org.xydra.index.AbstractTransitivePairIndexTest;
import org.xydra.index.Factory;
import org.xydra.index.IPairIndex;
import org.xydra.index.impl.FastStoredTransitivePairIndex;
import org.xydra.index.impl.MapPairIndex;
import org.xydra.index.impl.PairIndex;



public class FastStoredTransitivePairIndexTest extends AbstractTransitivePairIndexTest {
	
	@Override
	public void setUp() {
		this.index = this.idx = new FastStoredTransitivePairIndex<Integer>(
		        new PairIndex<Integer,Integer>(), new Factory<IPairIndex<Integer,Integer>>() {
			        
			        @Override
                    public IPairIndex<Integer,Integer> createInstance() {
				        return new MapPairIndex<Integer,Integer>();
			        }
			        
		        });
	}
	
}
