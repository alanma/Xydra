package org.xydra.textsearch.impl;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;


public class PrefixIndex<V> extends AbstractTokenIndex<V> {
    
    private int minPrefixLength;
    private int maxPrefixLength;
    
    public PrefixIndex(int minPrefixLength, int maxPrefixLength) {
        super();
        this.minPrefixLength = minPrefixLength;
        this.maxPrefixLength = maxPrefixLength;
    }
    
    @Override
    protected Collection<String> generateTokenFragments(String token) {
        return generatePrefixes(token, this.minPrefixLength, this.maxPrefixLength);
    }
    
    /**
     * @param t
     * @param minimalPrefixLength
     * @param maximalPrefixLength
     * @return substrings of given length, always starting at 0
     */
    private static List<String> generatePrefixes(String t, int minimalPrefixLength,
            int maximalPrefixLength) {
        List<String> result = new LinkedList<String>();
        if(t.length() < minimalPrefixLength) {
            return result;
        } else {
            int maxPrefixLen = Math.min(t.length(), maximalPrefixLength);
            
            for(int i = minimalPrefixLength; i <= maxPrefixLen; i++) {
                String subString = t.substring(0, i);
                result.add(subString);
            }
            return result;
        }
    }
    
}
