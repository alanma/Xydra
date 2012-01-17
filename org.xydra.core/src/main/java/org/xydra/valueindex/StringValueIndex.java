package org.xydra.valueindex;

import java.util.Iterator;

import org.xydra.index.IMapSetIndex;
import org.xydra.index.query.Constraint;
import org.xydra.index.query.KeyEntryTuple;


/*
 * TODO Implement & Document
 */

public class StringValueIndex implements IMapSetIndex<String,AddressValueCounterTriple> {
	private static final long serialVersionUID = -5366154192053454730L;
	
	private StringMap map;
	
	public StringValueIndex(StringMap map) {
		this.map = map;
	}
	
	@Override
	public void clear() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public Iterator<AddressValueCounterTriple> constraintIterator(Constraint<String> c1) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public boolean contains(Constraint<String> c1,
	        Constraint<AddressValueCounterTriple> entryConstraint) {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public boolean containsKey(String key) {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public void deIndex(String key1, AddressValueCounterTriple entry) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void deIndex(String key1) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void index(String key1, AddressValueCounterTriple entry) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public Iterator<KeyEntryTuple<String,AddressValueCounterTriple>> tupleIterator(
	        Constraint<String> c1, Constraint<AddressValueCounterTriple> entryConstraint) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public Iterator<String> keyIterator() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public org.xydra.index.IMapSetIndex.IMapSetDiff<String,AddressValueCounterTriple> computeDiff(
	        IMapSetIndex<String,AddressValueCounterTriple> otherFuture) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
