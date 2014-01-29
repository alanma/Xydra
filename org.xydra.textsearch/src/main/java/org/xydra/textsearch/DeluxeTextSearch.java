package org.xydra.textsearch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.xydra.index.IEntrySet;
import org.xydra.index.impl.IteratorUtils;
import org.xydra.index.impl.MapSetIndex;
import org.xydra.index.impl.SmallEntrySetFactory;
import org.xydra.index.iterator.IFilter;
import org.xydra.textsearch.PragmaticTextSearch.Normaliser;
import org.xydra.textsearch.impl.PTSImpl;


public class DeluxeTextSearch<V> {
    
    private PragmaticTextSearch<V> pts;
    @SuppressWarnings("unused")
    private String tokenizer;
    private String wordTokenizer;
    private String splitRegex;
    private Normaliser normaliser;
    private IContentResolver<V> contentResolver;
    
    /**
     * TODO use Match objects in higher levels
     * 
     * @param contentResolver
     */
    public DeluxeTextSearch(IContentResolver<V> contentResolver) {
        this.pts = new PTSImpl<V>();
        this.contentResolver = contentResolver;
        this.splitRegex = "[ ]";
        this.wordTokenizer = "[ -./]";
        this.normaliser = new Normaliser() {
            
            @Override
            public String normalise(String raw) {
                return raw.toLowerCase();
            }
        };
        this.pts.configure(this.splitRegex, this.wordTokenizer, this.normaliser);
    }
    
    /**
     * Resolves an Id to its content string for search engine indexing purposes
     * 
     * @param <V>
     */
    public interface IContentResolver<V> {
        /**
         * @param value
         * @return the content of the given value or @CanBeNull if not found
         */
        String getContent(V value);
    }
    
    public static class Match<V> implements Comparable<Match<V>> {
        
        private IContentResolver<V> contentResolver;
        
        // case matters
        private Set<String> matchedTokens;
        
        public Match(V value, IContentResolver<V> contentResolver) {
            super();
            this.value = value;
            this.contentResolver = contentResolver;
            this.matchedTokens = new HashSet<String>();
        }
        
        private V value;
        
        public String toString() {
            return this.value + " matches:" + this.matchedTokens;
        }
        
        @Override
        public int compareTo(Match<V> other) {
            int i = other.matchedTokens.size() - this.matchedTokens.size();
            if(i != 0)
                return i;
            
            String thisContent = this.contentResolver.getContent(this.getValue());
            String otherContent = this.contentResolver.getContent(other.getValue());
            
            int thisExactContained = 0;
            int thisOtherContained = 0;
            for(String token : this.matchedTokens) {
                if(thisContent.contains(token)) {
                    thisExactContained += token.length();
                } else if(thisContent.toLowerCase().contains(token.toLowerCase())) {
                    thisOtherContained += token.length();
                }
            }
            
            int otherExactContained = 0;
            int otherOtherContained = 0;
            for(String token : other.matchedTokens) {
                if(otherContent.contains(token)) {
                    otherExactContained += token.length();
                } else if(otherContent.toLowerCase().contains(token.toLowerCase())) {
                    otherOtherContained += token.length();
                }
            }
            
            int thisContained = thisExactContained + thisOtherContained;
            int otherContained = otherExactContained + otherOtherContained;
            
            i = otherContained - thisContained;
            if(i != 0)
                return i;
            
            i = thisContent.length() - otherContent.length();
            if(i != 0)
                return i;
            
            i = otherExactContained - thisExactContained;
            if(i != 0)
                return i;
            
            i = otherOtherContained - thisOtherContained;
            if(i != 0)
                return i;
            
            return thisContent.compareTo(otherContent);
            // best possible match: 100% of characters match, same case
            
            // 2nd best possible match: 100% of characters match, just case
            // differs
            
            // other matches: same with less matching characters
        }
        
        public V getValue() {
            return this.value;
        }
        
        public String getContent() {
            return this.contentResolver.getContent(getValue());
        }
    }
    
    /**
     * @param query casing matters
     * @param maxResults use a large number for 'unlimited'
     * @param filter
     * @return a list of matches, <em>sorted</em> by relevance
     */
    public List<Match<V>> search(String query, int maxResults, IFilter<V> filter) {
        // IMPROVE re-use regex
        String[] tokensWithCase = query.trim().split(this.splitRegex);
        // get results
        MapSetIndex<V,String> found = new MapSetIndex<V,String>(new SmallEntrySetFactory<String>());
        for(String tokenWithCase : tokensWithCase) {
            int count = 0;
            Iterator<V> res = this.pts.search(this.normaliser.normalise(tokenWithCase));
            /*
             * we accept a 10 x overhead to increase the chance to have really
             * good matches. As we search for tokens "A" and "B" separately, we
             * want to return "A B" even if many items with only "A" and others
             * with only "B" are present. -- 10 x is empirically
             */
            while(res.hasNext() && count < (maxResults * 10)) {
                V value = res.next();
                if(filter.matches(value)) {
                    found.index(value, tokenWithCase);
                    count++;
                }
            }
        }
        /*
         * add more token matches which are present but were not found by search
         * engine, e.g. short, in-string matches. We use them for better
         * ranking.
         */
        for(Entry<V,IEntrySet<String>> entry : found.getEntries()) {
            String contentLowercase = this.contentResolver.getContent(entry.getKey()).toLowerCase();
            for(String tokenWithCase : tokensWithCase) {
                if(contentLowercase.contains(tokenWithCase.toLowerCase())) {
                    entry.getValue().index(tokenWithCase);
                }
            }
        }
        
        // rank them
        List<Match<V>> list = new ArrayList<Match<V>>();
        Iterator<Entry<V,IEntrySet<String>>> it = found.getEntries().iterator();
        while(it.hasNext()) {
            Entry<V,IEntrySet<String>> entry = it.next();
            Match<V> match = new Match<V>(entry.getKey(), this.contentResolver);
            list.add(match);
            Iterator<String> tokenWithCaseIt = entry.getValue().iterator();
            while(tokenWithCaseIt.hasNext()) {
                String token = tokenWithCaseIt.next();
                match.matchedTokens.add(token);
            }
        }
        
        // FIXME
        System.out.println(Arrays.toString(list.toArray()));
        
        Collections.sort(list);
        list.subList(0, Math.min(list.size(), maxResults));
        return list;
    }
    
    @SuppressWarnings("unused")
    private static final <T> int size(IEntrySet<T> entrySet) {
        return IteratorUtils.count(entrySet.iterator());
    }
    
    public void index(V value, String text) {
        this.pts.index(value, text);
    }
    
    public void deIndex(V value, String text) {
        this.pts.deIndex(value, text);
    }
}
