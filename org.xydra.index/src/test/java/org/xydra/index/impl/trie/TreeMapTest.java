package org.xydra.index.impl.trie;

import java.util.SortedMap;
import java.util.TreeMap;


public class TreeMapTest extends SortedMapTest {
    
    @Override
    protected SortedMap<String,Integer> createMap() {
        return new TreeMap<String,Integer>();
    }
    
}
