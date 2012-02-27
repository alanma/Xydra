package org.xydra.valueindex;

import java.util.Iterator;

import org.xydra.base.XAddress;
import org.xydra.base.XX;
import org.xydra.base.value.XValue;
import org.xydra.index.query.EqualsConstraint;


/**
 * Implementation of {@link ValueIndex} which does not store maps of
 * {@link ValueIndexEntry ValueIndexEntries}, but represents and stores them as
 * Strings.
 * 
 * Warning: Many methods are not supported in this implementation!
 * 
 * @author Kaidel
 * 
 */

/*
 * TODO Maybe rewrite the whole interface and don't use IMapSetIndex, since
 * almost all methods don't work anyway?
 */

/*
 * TODO implement methods that check the size of the value first and only add it
 * if its small enough and null otherwise
 */
public class StringValueIndex implements ValueIndex {
	
	private StringMap map;
	private boolean checkEntrySize;
	private int maxEntrySize;
	
	public StringValueIndex(StringMap map) {
		this.map = map;
	}
	
	public StringValueIndex(StringMap map, int maxEntrySize) {
		this(map);
		
		this.maxEntrySize = maxEntrySize;
		this.checkEntrySize = true;
	}
	
	@Override
	public Iterator<ValueIndexEntry> constraintIterator(EqualsConstraint<String> c) {
		String key = c.getKey();
		String result = this.map.get(key);
		
		ValueIndexEntry[] entries = ValueIndexEntryUtils.getArrayFromString(result);
		SimpleArrayIterator<ValueIndexEntry> it = new SimpleArrayIterator<ValueIndexEntry>(entries);
		return it;
	}
	
	@Override
	public boolean contains(EqualsConstraint<String> c,
	        EqualsConstraint<ValueIndexEntry> entryConstraint) {
		ValueIndexEntry entry = entryConstraint.getKey();
		XAddress address = entry.getAddress();
		XValue value = entry.getValue();
		
		Iterator<ValueIndexEntry> it = this.constraintIterator(c);
		
		while(it.hasNext()) {
			ValueIndexEntry e = it.next();
			
			if(e.equalAddressAndValue(address, value)) {
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
	
	private XValue getValueForIndex(XAddress fieldAddress, XValue value) {
		if(!this.checkEntrySize) {
			return value;
		}
		
		ValueIndexEntry entry = new ValueIndexEntry(fieldAddress, value, 1);
		String entryString = ValueIndexEntryUtils.serializeAsString(entry);
		
		int entrySize = this.estimateStringSize(entryString);
		
		if(entrySize > this.maxEntrySize) {
			// use the fieldAddress as the value
			
			return fieldAddress;
		} else {
			// using the given value in the entry is okay
			
			return value;
		}
	}
	
	private int estimateStringSize(String s) {
		int size = s.length();
		return size * 16;
	}
	
	@Override
	public void deIndex(String key, XAddress fieldAddress, XValue value) {
		
		if(!(this.containsKey(key))) {
			// do nothing
			return;
		}
		
		XValue usedValue = this.getValueForIndex(fieldAddress, value);
		
		XAddress objectAddress = XX.resolveObject(fieldAddress.getRepository(),
		        fieldAddress.getModel(), fieldAddress.getObject());
		
		String entriesString = this.map.get(key);
		ValueIndexEntry[] entryArray = ValueIndexEntryUtils.getArrayFromString(entriesString);
		
		boolean found = false;
		for(int i = 0; i < entryArray.length && !found; i++) {
			/*
			 * TODO make this faster, for example by implementing an order on
			 * ValueIndexEntries and using binary search (which makes adding new
			 * entries slower... what's more important?)
			 * 
			 * Problem: there's no real "obvious" relation between
			 * ValueIndexEntries which could be used for ordering
			 */
			XAddress entryAddress = entryArray[i].getAddress();
			XValue entryValue = entryArray[i].getValue();
			
			boolean sameValue = false;
			if(entryValue == null) {
				sameValue = (usedValue == null);
			} else {
				sameValue = entryValue.equals(usedValue);
			}
			
			if(sameValue && entryAddress.equals(objectAddress)) {
				
				found = true;
				
				entryArray[i].decrementCounter();
				
				if(entryArray[i].getCounter() == 0) {
					entryArray[i] = null;
					
					/*
					 * setting the entry to null will result in deleting it from
					 * the stored set, since {@link
					 * ValueIndexEntryUtils#serializeAsString
					 * (ValueIndexEntry[])} only writes entries which are not
					 * null into the string
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
	public void index(String key, XAddress fieldAddress, XValue value) {
		XValue usedValue = this.getValueForIndex(fieldAddress, value);
		
		XAddress objectAddress = XX.resolveObject(fieldAddress.getRepository(),
		        fieldAddress.getModel(), fieldAddress.getObject());
		
		if(!(this.containsKey(key))) {
			ValueIndexEntry[] newEntries = new ValueIndexEntry[1];
			newEntries[0] = new ValueIndexEntry(objectAddress, usedValue, 1);
			
			String entriesString = ValueIndexEntryUtils.serializeAsString(newEntries);
			
			this.map.put(key, entriesString);
		} else {
			
			String entriesString = this.map.get(key);
			ValueIndexEntry[] entryArray = ValueIndexEntryUtils.getArrayFromString(entriesString);
			
			boolean found = false;
			for(int i = 0; i < entryArray.length && !found; i++) {
				/*
				 * TODO make this faster, for example by implementing an order
				 * on ValueIndexEntries and using binary search (which makes
				 * adding new entries slower... what's more important?)
				 * 
				 * Problem: there's no real "obvious" relation between
				 * ValueIndexEntries which could be used for ordering
				 */
				
				if(entryArray[i].equalAddressAndValue(objectAddress, usedValue)) {
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
				ValueIndexEntry newEntry = new ValueIndexEntry(objectAddress, usedValue, 1);
				
				String newEntriesString = ValueIndexEntryUtils.serializeAsString(entryArray,
				        newEntry);
				this.map.put(key, newEntriesString);
			}
			
		}
	}
}
