package org.xydra.valueindex;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.xydra.base.XAddress;
import org.xydra.base.XType;
import org.xydra.base.value.XValue;
import org.xydra.index.query.EqualsConstraint;
import org.xydra.sharedutils.XyAssert;


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
	private HashMap<String,HashSet<ValueIndexEntry>> map;
	
	public MemoryMapSetIndex() {
		this.map = new HashMap<String,HashSet<ValueIndexEntry>>();
	}
	
	@Override
	public Iterator<ValueIndexEntry> constraintIterator(EqualsConstraint<String> c1) {
		HashSet<ValueIndexEntry> result = new HashSet<ValueIndexEntry>();
		
		for(String key : this.map.keySet()) {
			if(c1.matches(key)) {
				HashSet<ValueIndexEntry> entries = this.map.get(key);
				
				result.addAll(entries);
			}
		}
		
		return result.iterator();
	}
	
	@Override
	public boolean contains(EqualsConstraint<String> c1,
	        EqualsConstraint<ValueIndexEntry> entryConstraint) {
		// TODO maybe implement...
		throw new UnsupportedOperationException();
	}
	
	@Override
	public boolean containsKey(String key) {
		return this.map.containsKey(key);
	}
	
	@Override
	public void deIndex(String key, XAddress fieldAddress, XValue value) {
		if(fieldAddress.getAddressedType() != XType.XFIELD) {
			throw new RuntimeException("The given fieldAddress was no address of a field, but an "
			        + fieldAddress.getAddressedType() + "type address!");
		}
		
		HashSet<ValueIndexEntry> set = this.map.get(key);
		
		if(set == null) {
			return;
		}
		
		Iterator<ValueIndexEntry> iterator = set.iterator();
		
		boolean found = false;
		while(!found && iterator.hasNext()) {
			ValueIndexEntry entry = iterator.next();
			
			if(entry.equalAddressAndValue(fieldAddress, value)) {
				found = true;
				set.remove(entry);
			}
		}
		
		if(set.size() == 0) {
			deIndex(key);
		}
		
	}
	
	@Override
	public void deIndex(String key) {
		this.map.remove(key);
	}
	
	@Override
	public void index(String key, XAddress fieldAddress, XValue value) {
		if(fieldAddress.getAddressedType() != XType.XFIELD) {
			throw new RuntimeException("The given fieldAddress was no address of a field, but an "
			        + fieldAddress.getAddressedType() + "type address!");
		}
		
		if(!this.map.containsKey(key)) {
			this.map.put(key, new HashSet<ValueIndexEntry>());
		}
		
		HashSet<ValueIndexEntry> set = this.map.get(key);
		XyAssert.xyAssert(set != null); assert set != null;
		
		Iterator<ValueIndexEntry> iterator = set.iterator();
		
		boolean found = false;
		while(!found && iterator.hasNext()) {
			ValueIndexEntry triple = iterator.next();
			
			if(triple.equalAddressAndValue(fieldAddress, value)) {
				found = true;
			}
		}
		
		if(!found) {
			// no entry found -> add one
			ValueIndexEntry newEntry = new ValueIndexEntry(fieldAddress, value);
			set.add(newEntry);
		}
	}
	
	public Iterator<String> keyIterator() {
		return this.map.keySet().iterator();
	}
	
}