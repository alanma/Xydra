package org.xydra.valueindex;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.xydra.base.XAddress;
import org.xydra.base.value.XValue;
import org.xydra.index.IMapSetIndex;
import org.xydra.index.query.Constraint;
import org.xydra.index.query.EqualsConstraint;
import org.xydra.index.query.KeyEntryTuple;


public class MemoryMapSetIndex implements ValueIndex {
	/**
     * 
     */
	private static final long serialVersionUID = 8240211898556269887L;
	private HashMap<String,HashSet<ValueIndexEntry>> map;
	
	public MemoryMapSetIndex() {
		this.map = new HashMap<String,HashSet<ValueIndexEntry>>();
	}
	
	@Override
	public void clear() {
		this.map.clear();
	}
	
	@Override
	public boolean isEmpty() {
		return this.map.isEmpty();
	}
	
	@Override
	public Iterator<ValueIndexEntry> constraintIterator(Constraint<String> c1) {
		if(!(c1 instanceof EqualsConstraint<?>)) {
			throw new UnsupportedOperationException("only EqualsConstraints supported");
		}
		
		EqualsConstraint<String> eq = (EqualsConstraint<String>)c1;
		
		HashSet<ValueIndexEntry> result = new HashSet<ValueIndexEntry>();
		
		for(String key : this.map.keySet()) {
			if(eq.matches(key)) {
				HashSet<ValueIndexEntry> entries = this.map.get(key);
				
				result.addAll(entries);
			}
		}
		
		return result.iterator();
	}
	
	@Override
	public boolean contains(Constraint<String> c1, Constraint<ValueIndexEntry> entryConstraint) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public boolean containsKey(String key) {
		return this.map.containsKey(key);
	}
	
	@Override
	public void deIndex(String key1, ValueIndexEntry entry) {
		HashSet<ValueIndexEntry> set = this.map.get(key1);
		
		XAddress address = entry.getAddress();
		XValue value = entry.getValue();
		// counter is ignored!
		
		Iterator<ValueIndexEntry> iterator = set.iterator();
		
		boolean found = false;
		while(!found && iterator.hasNext()) {
			ValueIndexEntry triple = iterator.next();
			
			if(triple.equalAddressAndValue(address, value)) {
				found = true;
				triple.decrementCounter();
				
				if(triple.getCounter() == 0) {
					set.remove(triple);
				}
			}
		}
		
		if(set.size() == 0) {
			deIndex(key1);
		}
		
	}
	
	@Override
	public void deIndex(String key1) {
		this.map.remove(key1);
	}
	
	@Override
	public void index(String key1, ValueIndexEntry entry) {
		if(!this.map.containsKey(key1)) {
			this.map.put(key1, new HashSet<ValueIndexEntry>());
		}
		
		HashSet<ValueIndexEntry> set = this.map.get(key1);
		assert set != null;
		
		XAddress address = entry.getAddress();
		XValue value = entry.getValue();
		// counter is ignored!
		
		Iterator<ValueIndexEntry> iterator = set.iterator();
		
		boolean found = false;
		while(!found && iterator.hasNext()) {
			ValueIndexEntry triple = iterator.next();
			
			if(triple.equalAddressAndValue(address, value)) {
				found = true;
				triple.incrementCounter();
			}
		}
		
		if(!found) {
			// no entry found -> add one
			ValueIndexEntry newEntry = new ValueIndexEntry(address, value, 1);
			set.add(newEntry);
		}
	}
	
	@Override
	public Iterator<KeyEntryTuple<String,ValueIndexEntry>> tupleIterator(Constraint<String> c1,
	        Constraint<ValueIndexEntry> entryConstraint) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public Iterator<String> keyIterator() {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public org.xydra.index.IMapSetIndex.IMapSetDiff<String,ValueIndexEntry> computeDiff(
	        IMapSetIndex<String,ValueIndexEntry> otherFuture) {
		throw new UnsupportedOperationException();
	}
	
}