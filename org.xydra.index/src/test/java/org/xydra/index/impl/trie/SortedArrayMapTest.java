package org.xydra.index.impl.trie;

import java.util.SortedMap;


public class SortedArrayMapTest extends SortedMapTest {
    
    @Override
    protected SortedMap<String,Integer> createMap() {
        return new SortedArrayMap<String,Integer>();
    }
    
}
