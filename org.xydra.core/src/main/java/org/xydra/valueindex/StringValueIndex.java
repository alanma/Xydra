package org.xydra.valueindex;

import java.util.Iterator;

import org.xydra.base.XAddress;
import org.xydra.base.value.XValue;
import org.xydra.index.IMapSetIndex;
import org.xydra.index.query.Constraint;
import org.xydra.index.query.EqualsConstraint;
import org.xydra.index.query.KeyEntryTuple;


/*
 * TODO Implement & Document
 */

public class StringValueIndex implements ValueIndex {
	private static final long serialVersionUID = -5366154192053454730L;
	
	private StringMap map;
	
	public StringValueIndex(StringMap map) {
		this.map = map;
	}
	
	@Override
	public void clear() {
		throw new UnsupportedOperationException("clear() is not supported by StringValueIndex");
	}
	
	@Override
	public boolean isEmpty() {
		throw new UnsupportedOperationException("empty() is not supported by StringValueIndex");
	}
	
	@Override
	public Iterator<ValueIndexEntry> constraintIterator(Constraint<String> c) {
		if(!(c instanceof EqualsConstraint)) {
			throw new UnsupportedOperationException(
			        "StringValueIndex only supports EqualsConstraints");
		}
		EqualsConstraint<String> eq = (EqualsConstraint<String>)c;
		
		String key = eq.getKey();
		String result = this.map.get(key);
		
		ValueIndexEntry[] entries = ValueIndexEntryUtils.getArrayFromString(result);
		SimpleArrayIterator<ValueIndexEntry> it = new SimpleArrayIterator<ValueIndexEntry>(entries);
		return it;
	}
	
	@Override
	public boolean contains(Constraint<String> c, Constraint<ValueIndexEntry> entryConstraint) {
		if(!(entryConstraint instanceof EqualsConstraint)) {
			throw new UnsupportedOperationException(
			        "StringValueIndex only supports EqualsConstraints");
		}
		
		EqualsConstraint<ValueIndexEntry> entryEq = (EqualsConstraint<ValueIndexEntry>)entryConstraint;
		ValueIndexEntry entry = entryEq.getKey();
		XAddress address = entry.getAddress();
		XValue value = entry.getValue();
		
		Iterator<ValueIndexEntry> it = this.constraintIterator(c);
		
		while(it.hasNext()) {
			ValueIndexEntry e = it.next();
			
			if(e.equalAddressAndValue(address, value)) {
				/*
				 * TODO check counter too?
				 */
				return true;
			}
		}
		
		return false;
	}
	
	@Override
	public boolean containsKey(String key) {
		String result = this.map.get(key);
		
		return result != null;
	}
	
	@Override
	public void deIndex(String key, ValueIndexEntry entry) {
		if(!(this.containsKey(key))) {
			// do nothing
			return;
		}
		
		XAddress address = entry.getAddress();
		XValue value = entry.getValue();
		
		String entriesString = this.map.get(key);
		ValueIndexEntry[] entryArray = ValueIndexEntryUtils.getArrayFromString(entriesString);
		
		boolean found = false;
		for(int i = 0; i < entryArray.length && !found; i++) {
			/*
			 * TODO make this faster, for example by implementing an order on
			 * ValueIndexEntries and using binary search
			 */

			if(entryArray[i].equalAddressAndValue(address, value)) {
				found = true;
				
				entryArray[i].decrementCounter();
				
				if(entryArray[i].getCounter() == 0) {
					entryArray[i] = null;
					
					/*
					 * setting the entry to null will result in deleting it from
					 * the stored set, since {@link ValueIndexEntryUtils} only
					 * writes entries which are not null into the string
					 */
				}
			}
		}
		
		if(found) {
			/*
			 * counter of one entry was decremented -> rewrite the stored string
			 * to represent the change
			 */
			String newEntriesString = ValueIndexEntryUtils.serializeAsString(entryArray);
			this.map.put(key, newEntriesString);
			
		}
		// else: do nothing, since nothing was changed
	}
	
	@Override
	public void deIndex(String key) {
		this.map.remove(key);
	}
	
	@Override
	public void index(String key, ValueIndexEntry entry) {
		XAddress address = entry.getAddress();
		XValue value = entry.getValue();
		
		if(!(this.containsKey(key))) {
			ValueIndexEntry[] newEntries = new ValueIndexEntry[1];
			newEntries[0] = new ValueIndexEntry(address, value, 1);
			
			String entriesString = ValueIndexEntryUtils.serializeAsString(newEntries);
			
			this.map.put(key, entriesString);
		} else {
			
			String entriesString = this.map.get(key);
			ValueIndexEntry[] entryArray = ValueIndexEntryUtils.getArrayFromString(entriesString);
			
			boolean found = false;
			for(int i = 0; i < entryArray.length && !found; i++) {
				/*
				 * TODO make this faster, for example by implementing an order
				 * on ValueIndexEntries and using binary search
				 */

				if(entryArray[i].equalAddressAndValue(address, value)) {
					found = true;
					
					entryArray[i].incrementCounter();
				}
			}
			
			if(found) {
				/*
				 * counter of one entry was decremented -> rewrite the stored
				 * string to represent the change
				 */
				String newEntriesString = ValueIndexEntryUtils.serializeAsString(entryArray);
				this.map.put(key, newEntriesString);
				
			} else {
				/*
				 * no entry was found, so we'll have to add it
				 */
				ValueIndexEntry newEntry = new ValueIndexEntry(address, value, 1);
				
				String newEntriesString = ValueIndexEntryUtils.serializeAsString(entryArray,
				        newEntry);
				this.map.put(key, newEntriesString);
			}
			
		}
	}
	
	@Override
	public Iterator<KeyEntryTuple<String,ValueIndexEntry>> tupleIterator(Constraint<String> c1,
	        Constraint<ValueIndexEntry> entryConstraint) {
		throw new UnsupportedOperationException("StringValueIndex does not support tupleIterator()");
	}
	
	@Override
	public Iterator<String> keyIterator() {
		throw new UnsupportedOperationException("StringValueIndex does not support keyIterator()");
	}
	
	@Override
	public org.xydra.index.IMapSetIndex.IMapSetDiff<String,ValueIndexEntry> computeDiff(
	        IMapSetIndex<String,ValueIndexEntry> otherFuture) {
		throw new UnsupportedOperationException("StringValueIndex does not support computeDiff");
	}
	
}
