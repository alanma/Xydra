package de.xam.xindex.index.impl;

import static org.junit.Assert.*;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

import de.xam.xindex.Factory;
import de.xam.xindex.index.IEntrySet;
import de.xam.xindex.index.IMapMapSetIndex.IMapMapSetDiff;
import de.xam.xindex.index.IMapSetIndex.IMapSetDiff;
import de.xam.xindex.query.KeyEntryTuple;
import de.xam.xindex.query.KeyKeyEntryTuple;
import de.xam.xindex.query.Wildcard;


public class TestIndexes {
	
	public static <E> List<E> toList(Iterator<E> it) {
		LinkedList<E> list = new LinkedList<E>();
		while(it.hasNext()) {
			list.add(it.next());
		}
		return list;
	}
	
	@Test
	public void testMapSetIndex() {
		Factory<IEntrySet<String>> entrySetFactory = new FastEntrySetFactory<String>();
		MapSetIndex<String,String> mapSetIndex1 = new MapSetIndex<String,String>(entrySetFactory);
		mapSetIndex1.index("-1", "1");
		mapSetIndex1.index("=2", "2");
		mapSetIndex1.index("-4", "4");
		
		MapSetIndex<String,String> mapSetIndex2 = new MapSetIndex<String,String>(entrySetFactory);
		mapSetIndex2.index("=2", "2");
		mapSetIndex2.index("+3", "3");
		mapSetIndex2.index("+4", "5");
		
		IMapSetDiff<String,String> diff = mapSetIndex1.computeDiff(mapSetIndex2);
		
		Iterator<KeyEntryTuple<String,String>> it = diff.getAdded().tupleIterator(
		        new Wildcard<String>(), new Wildcard<String>());
		
		List<KeyEntryTuple<String,String>> addList = toList(it);
		assertEquals(2, addList.size());
		assertTrue(addList.contains(new KeyEntryTuple<String,String>("+3", "3")));
		assertTrue(addList.contains(new KeyEntryTuple<String,String>("+4", "5")));
		
		it = diff.getRemoved().tupleIterator(new Wildcard<String>(), new Wildcard<String>());
		List<KeyEntryTuple<String,String>> remList = toList(it);
		assertEquals(2, remList.size());
		assertTrue(remList.contains(new KeyEntryTuple<String,String>("-1", "1")));
		assertTrue(remList.contains(new KeyEntryTuple<String,String>("-4", "4")));
		
	}
	
	@Test
	public void testMapMapSetIndex() {
		Factory<IEntrySet<String>> entrySetFactory = new FastEntrySetFactory<String>();
		MapMapSetIndex<String,String,String> mapMapSetIndex1 = new MapMapSetIndex<String,String,String>(
		        entrySetFactory);
		mapMapSetIndex1.index("-1", "1", "1"); // = removed
		mapMapSetIndex1.index("=2", "2", "2"); // = same
		mapMapSetIndex1.index("-3", "4", "4"); // removed
		
		MapMapSetIndex<String,String,String> mapMapSetIndex2 = new MapMapSetIndex<String,String,String>(
		        entrySetFactory);
		
		mapMapSetIndex2.index("=2", "2", "2"); // = same
		mapMapSetIndex2.index("+4", "4", "4"); // = added
		mapMapSetIndex2.index("+3", "5", "5"); // added
		
		IMapMapSetDiff<String,String,String> diff = mapMapSetIndex1.computeDiff(mapMapSetIndex2);
		
		Iterator<KeyKeyEntryTuple<String,String,String>> it = diff.getAdded().tupleIterator(
		        new Wildcard<String>(), new Wildcard<String>(), new Wildcard<String>());
		List<KeyKeyEntryTuple<String,String,String>> addList = toList(it);
		assertEquals(2, addList.size());
		assertTrue(addList.contains(new KeyKeyEntryTuple<String,String,String>("+3", "5", "5")));
		assertTrue(addList.contains(new KeyKeyEntryTuple<String,String,String>("+4", "4", "4")));
		
		it = diff.getRemoved().tupleIterator(new Wildcard<String>(), new Wildcard<String>(),
		        new Wildcard<String>());
		List<KeyKeyEntryTuple<String,String,String>> remList = toList(it);
		assertEquals(2, remList.size());
		assertTrue(remList.contains(new KeyKeyEntryTuple<String,String,String>("-1", "1", "1")));
		assertTrue(remList.contains(new KeyKeyEntryTuple<String,String,String>("-3", "4", "4")));
	}
}
