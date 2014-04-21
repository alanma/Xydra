package org.xydra.index.impl.trie;

import org.xydra.index.AbstractMapSetIndexTest;
import org.xydra.index.impl.SmallEntrySetFactory;
import org.xydra.index.impl.trie.SmallStringSetTrie;


public class SmallStringSetTrieTest extends AbstractMapSetIndexTest<String,Integer> {
    
    public SmallStringSetTrieTest() {
        super(new SmallStringSetTrie<Integer>(new SmallEntrySetFactory<Integer>()));
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
