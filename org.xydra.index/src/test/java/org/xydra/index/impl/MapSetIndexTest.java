package org.xydra.index.impl;

import org.xydra.index.AbstractMapSetIndexTest;


public class MapSetIndexTest extends AbstractMapSetIndexTest<String,Integer> {
    
    public MapSetIndexTest() {
        super(new MapSetIndex<String,Integer>(new SmallEntrySetFactory<Integer>()));
    }
    
    private static int i = 1;
    
    private static int s = 1;
    
    @Override
    protected Integer createEntry() {
        return i++;
    }
    
    @Override
    protected String createKey() {
        return "s" + s++;
    }
    
}
