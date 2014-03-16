package org.xydra.index.impl;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import org.xydra.index.IIntegerRangeIndex;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;


/**
 * Uses internally a sorted map. Fast; memory-efficient;
 * 
 * @author xamde
 */
public class IntegerRangeIndex implements IIntegerRangeIndex {
    
    private static final long serialVersionUID = -6793029187873016827L;
    
    private static final Logger log = LoggerFactory.getLogger(IntegerRangeIndex.class);
    
    @Override
    public boolean isInInterval(int i) {
        Entry<Integer,Integer> prev = this.sortedmap.floorEntry(i);
        return start(prev) <= i && i <= end(prev);
    }
    
    private TreeMap<Integer,Integer> sortedmap = new TreeMap<Integer,Integer>();
    
    /*
     * Runtime: O(1) + O(1) + O(contained intervals).
     * 
     * @see org.xydra.index.IIntegerRangeIndex#index(int, int)
     */
    @Override
    public void index(int start, int end) {
        assert start <= end;
        if(log.isTraceEnabled())
            log.trace("Index " + start + "," + end);
        
        int mergedStart = start;
        int mergedEnd = end;
        if(log.isTraceEnabled())
            log.trace("Current: " + mergedStart + "," + mergedEnd);
        
        /* merge with previous? */
        Entry<Integer,Integer> prev = this.sortedmap.floorEntry(start - 1);
        assert prev == null || start(prev) <= start - 1;
        // [1,5] & [3,9]
        if(prev != null && start - 1 <= end(prev)) {
            if(log.isTraceEnabled())
                log.trace("Merge with prev: " + prev);
            mergedStart = start(prev);
            mergedEnd = Math.max(end(prev), end);
            this.sortedmap.remove(start(prev));
            if(log.isTraceEnabled())
                log.trace("Current: " + mergedStart + "," + mergedEnd);
        }
        
        /* merge with next? */
        Entry<Integer,Integer> next = this.sortedmap.floorEntry(end + 1);
        assert next == null || start(next) <= end + 1;
        if(next != null && start(next) >= mergedStart && mergedEnd + 1 >= start(next)) {
            if(log.isTraceEnabled())
                log.trace("Merge with next: " + next);
            mergedEnd = Math.max(mergedEnd, end(next));
            this.sortedmap.remove(start(next));
            if(log.isTraceEnabled())
                log.trace("Current: " + mergedStart + "," + mergedEnd);
        }
        
        /* prune contained intervals? */
        int pruneStart = mergedStart + 1;
        int pruneEnd = mergedEnd - 1;
        if(pruneEnd - pruneStart > 1) {
            pruneRanges(pruneStart, pruneEnd);
        }
        
        this.sortedmap.put(mergedStart, mergedEnd);
    }
    
    @Override
    public void dump() {
        Iterator<Entry<Integer,Integer>> it = rangesIterator();
        while(it.hasNext()) {
            Map.Entry<Integer,Integer> entry = it.next();
            log.info("[" + start(entry) + ", " + end(entry) + "]");
        }
    }
    
    @Override
    public Iterator<Entry<Integer,Integer>> rangesIterator() {
        return this.sortedmap.entrySet().iterator();
    }
    
    @Override
    public void clear() {
        this.sortedmap.clear();
    }
    
    @Override
    public boolean isEmpty() {
        return this.sortedmap.isEmpty();
    }
    
    @Override
    public void deIndex(int start, int end) {
        assert start <= end;
        log.debug("De-index " + start + "," + end);
        
        /*
         * Cases: 1) split existing range, 2) adapt 1 range at start + adapt 1
         * range at end + delete ranges in the middle, 3) nothing changes
         */
        
        /*
         * prev = entry with the greatest key <= start, or null if there is no
         * such key
         */
        Entry<Integer,Integer> prev = this.sortedmap.floorEntry(start - 1);
        if(prev != null) {
            assert start(prev) < start : "start(prev)=" + start(prev) + " start=" + start;
            
            if(start <= end(prev)) {
                // prev needs to be trimmed/split
                
                if(end == end(prev)) {
                    // corner case: trim & done
                    
                    int oldStart = prev.getKey();
                    this.sortedmap.remove(prev.getKey());
                    this.sortedmap.put(oldStart, start - 1);
                    
                    return;
                } else if(end < end(prev)) {
                    // split & done
                    int oldEnd = end(prev);
                    
                    assert end < oldEnd;
                    assert start(prev) <= start - 1;
                    
                    int oldStart = start(prev);
                    this.sortedmap.remove(prev.getKey());
                    this.sortedmap.put(oldStart, start - 1);
                    
                    this.sortedmap.put(end + 1, oldEnd);
                    return;
                } else {
                    // trim & not done
                    int oldStart = start(prev);
                    this.sortedmap.remove(prev.getKey());
                    this.sortedmap.put(oldStart, start - 1);
                }
            }
        }
        
        /*
         * next = entry with the greatest key <= end, or null if there is no
         * such key
         */
        // assert prev was null || prev was trimmed
        Entry<Integer,Integer> next = this.sortedmap.floorEntry(end);
        if(next != null) {
            assert start(next) <= end;
            
            if(end < end(next)) {
                // trim start of next
                int oldEnd = end(next);
                this.sortedmap.remove(next.getKey());
                this.sortedmap.put(end + 1, oldEnd);
            } else {
                assert end >= end(next);
                // delete next, will happen in next loop anyway
            }
        }
        
        /* delete inner ranges */
        
        /* prune contained intervals? */
        if(end - start > 1) {
            pruneRanges(start, end);
        }
    }
    
    private void pruneRanges(int start, int end) {
        if(log.isTraceEnabled())
            log.trace("pruning in range [" + (start) + "," + (end) + "]");
        SortedMap<Integer,Integer> sub = this.sortedmap.subMap(start, end + 1);
        Iterator<Entry<Integer,Integer>> it = sub.entrySet().iterator();
        while(it.hasNext()) {
            Entry<Integer,Integer> e = it.next();
            if(log.isTraceEnabled())
                log.trace("pruning entry [" + (start(e)) + "," + (end(e)) + "]");
            it.remove();
        }
    }
    
    private static int start(Entry<Integer,Integer> entry) {
        return entry.getKey();
    }
    
    private static int end(Entry<Integer,Integer> entry) {
        return entry.getValue();
    }
    
    /**
     * @return number of stored ranges, NOT number of contained integer numbers
     */
    public int size() {
        return this.sortedmap.size();
    }
    
    public void addAll(IIntegerRangeIndex other) {
        Iterator<Entry<Integer,Integer>> it = other.rangesIterator();
        while(it.hasNext()) {
            Map.Entry<Integer,Integer> entry = it.next();
            index(entry.getKey(), entry.getValue());
        }
    }
}
