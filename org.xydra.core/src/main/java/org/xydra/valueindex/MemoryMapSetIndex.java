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


/**
 * A simple in-memory implementation of {@link ValueIndex}, for testing
 * purposes.
 * 
 * Warning: Some methods are not supported.
 * 
 * @author Kaidel
 * 
 */

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
	public void deIndex(String key, XAddress objectAddress, XValue value) {
		HashSet<ValueIndexEntry> set = this.map.get(key);
		Iterator<ValueIndexEntry> iterator = set.iterator();
		
		boolean found = false;
		while(!found && iterator.hasNext()) {
			ValueIndexEntry triple = iterator.next();
			
			if(triple.equalAddressAndValue(objectAddress, value)) {
				found = true;
				triple.decrementCounter();
				
				if(triple.getCounter() == 0) {
					set.remove(triple);
				}
			}
		}
		
		if(set.size() == 0) {
			deIndex(key);
		}
		
	}
	
	@Override
	public void deIndex(String key, ValueIndexEntry entry) {
		XAddress address = entry.getAddress();
		XValue value = entry.getValue();
		// counter is ignored!
		
		deIndex(key, address, value);
	}
	
	@Override
	public void deIndex(String key) {
		this.map.remove(key);
	}
	
	@Override
	public void index(String key, XAddress objectAddress, XValue value) {
		if(!this.map.containsKey(key)) {
			this.map.put(key, new HashSet<ValueIndexEntry>());
		}
		
		HashSet<ValueIndexEntry> set = this.map.get(key);
		assert set != null;
		
		Iterator<ValueIndexEntry> iterator = set.iterator();
		
		boolean found = false;
		while(!found && iterator.hasNext()) {
			ValueIndexEntry triple = iterator.next();
			
			if(triple.equalAddressAndValue(objectAddress, value)) {
				found = true;
				triple.incrementCounter();
			}
		}
		
		if(!found) {
			// no entry found -> add one
			ValueIndexEntry newEntry = new ValueIndexEntry(objectAddress, value, 1);
			set.add(newEntry);
		}
	}
	
	@Override
	public void index(String key, ValueIndexEntry entry) {
		XAddress address = entry.getAddress();
		XValue value = entry.getValue();
		// counter is ignored!
		
		index(key, address, value);
	}
	
	/**
	 * @throws UnsupportedOperationException this method is not supported by
	 *             this implementation
	 */
	@Override
	public Iterator<KeyEntryTuple<String,ValueIndexEntry>> tupleIterator(Constraint<String> c1,
	        Constraint<ValueIndexEntry> entryConstraint) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public Iterator<String> keyIterator() {
		return this.map.keySet().iterator();
	}
	
	/**
	 * @throws UnsupportedOperationException this method is not supported by
	 *             this implementation
	 */
	@Override
	public org.xydra.index.IMapSetIndex.IMapSetDiff<String,ValueIndexEntry> computeDiff(
	        IMapSetIndex<String,ValueIndexEntry> otherFuture) {
		throw new UnsupportedOperationException();
	}
	
}
