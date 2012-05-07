package org.xydra.valueindex;

import java.util.Iterator;

import org.xydra.base.XAddress;
import org.xydra.base.XType;
import org.xydra.base.rmof.XReadableField;
import org.xydra.base.value.XValue;
import org.xydra.index.query.EqualsConstraint;
import org.xydra.sharedutils.XyAssert;


/**
 * Implementation of {@link ValueIndex} which does not store maps of
 * {@link ValueIndexEntry ValueIndexEntries}, but represents and stores them as
 * Strings.
 * 
 * Since {@link XValue XValues} can be pretty big, this implementation offers
 * the possibility to set a maximum size for the index entries. If an
 * {@link XValue} is bigger than the given maximum size, the {@link XAddress} of
 * the {@link XReadableField} holding the value will be stored instead of the
 * value itself.
 * 
 * 
 * 
 * @author Kaidel
 * 
 */

public class StringValueIndex implements ValueIndex {
	
	private StringMap map;
	private boolean checkEntrySize;
	private int maxEntrySize;
	
	/**
	 * Creates a new StringValueIndex, which does not check the size of its
	 * entries.
	 * 
	 * @param map the {@link StringMap} which will be used for indexing.
	 */
	public StringValueIndex(StringMap map) {
		this.map = map;
	}
	
	/**
	 * Creates a new StringValueIndex, which will compare the size of its
	 * entries to the given maximum size. Since the size of most types of
	 * {@link XValue XValues} can only be estimated, this is not exact, but
	 * should usually work. If an entry with the given {@link XValue} is bigger
	 * than the given maximum size, the {@link XAddress} of the
	 * {@link XReadableField} holding the value will be stored instead of the
	 * value itself. The size of the entry with the {@link XAddress} will not be
	 * checked again, so you need to make sure that the {@link XAddress
	 * XAddresses} actually are not bigger than the {@link XValue XValues} you
	 * are using to actually save memory.
	 * 
	 * @param map the {@link StringMap} which will be used for indexing.
	 * @param maxEntrySize The approximate maximum size of an
	 *            {@link ValueIndexEntry} in this index storing an
	 *            {@link XValue}.
	 */
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
		XyAssert.xyAssert(fieldAddress.getAddressedType() == XType.XFIELD);
		
		if(!this.checkEntrySize) {
			return value;
		}
		
		ValueIndexEntry entry = new ValueIndexEntry(fieldAddress, value);
		String entryString = ValueIndexEntryUtils.serializeAsString(entry);
		
		int entrySize = StringValueIndex.estimateStringSize(entryString);
		
		if(entrySize > this.maxEntrySize) {
			// use the fieldAddress as the value
			
			return fieldAddress;
		} else {
			// using the given value in the entry is okay
			
			return value;
		}
	}
	
	private static int estimateStringSize(String s) {
		int size = s.length();
		return size * 16;
	}
	
	@Override
	public void deIndex(String key, XAddress fieldAddress, XValue value) {
		if(fieldAddress.getAddressedType() != XType.XFIELD) {
			throw new RuntimeException("The given fieldAddress was no address of a field, but an "
			        + fieldAddress.getAddressedType() + "type address!");
		}
		
		if(!(this.containsKey(key))) {
			// do nothing
			return;
		}
		
		XValue usedValue = this.getValueForIndex(fieldAddress, value);
		
		ValueIndexEntry entry = new ValueIndexEntry(fieldAddress, usedValue);
		String arrayString = this.map.get(key);
		
		String newArrayString = ValueIndexEntryUtils.removeEntryFromArrayString(arrayString, entry);
		
		if(newArrayString != null) {
			if(newArrayString.equals("")) {
				this.map.remove(key);
			} else {
				this.map.put(key, newArrayString);
			}
		}
		/*
		 * else: (newArrayString == null) indicates that nothing was changed, so
		 * we need to do nothing
		 */
		
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
		
		XValue usedValue = this.getValueForIndex(fieldAddress, value);
		
		if(!(this.containsKey(key))) {
			ValueIndexEntry[] newEntries = new ValueIndexEntry[1];
			newEntries[0] = new ValueIndexEntry(fieldAddress, usedValue);
			
			String entriesString = ValueIndexEntryUtils.serializeAsString(newEntries);
			
			this.map.put(key, entriesString);
		} else {
			
			String arrayString = this.map.get(key);
			ValueIndexEntry entry = new ValueIndexEntry(fieldAddress, usedValue);
			
			String newArrayString = ValueIndexEntryUtils.addEntryToArrayString(arrayString, entry);
			
			assert newArrayString != null;
			
			this.map.put(key, newArrayString);
			
		}
	}
}
