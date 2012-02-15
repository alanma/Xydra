package org.xydra.valueindex;

import java.util.Iterator;

import org.xydra.index.IMapSetIndex;
import org.xydra.index.query.Constraint;
import org.xydra.index.query.EqualsConstraint;
import org.xydra.index.query.KeyEntryTuple;


/*
 * TODO Implement & Document
 */

public class StringValueIndex implements IMapSetIndex<String,ValueIndexEntry> {
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
	public boolean contains(Constraint<String> c1, Constraint<ValueIndexEntry> entryConstraint) {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public boolean containsKey(String key) {
		String result = this.map.get(key);
		
		return result != null;
	}
	
	@Override
	public void deIndex(String key1, ValueIndexEntry entry) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void deIndex(String key) {
		this.map.remove(key);
		
	}
	
	@Override
	public void index(String key, ValueIndexEntry entry) {
		// String result = this.map.get(key);
		// String entryString =
	}
	
	@Override
	public Iterator<KeyEntryTuple<String,ValueIndexEntry>> tupleIterator(Constraint<String> c1,
	        Constraint<ValueIndexEntry> entryConstraint) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public Iterator<String> keyIterator() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public org.xydra.index.IMapSetIndex.IMapSetDiff<String,ValueIndexEntry> computeDiff(
	        IMapSetIndex<String,ValueIndexEntry> otherFuture) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
