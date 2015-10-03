package org.xydra.index.impl;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;
import org.xydra.index.Factory;
import org.xydra.index.IEntrySet;
import org.xydra.index.IMapMapSetIndex.IMapMapSetDiff;
import org.xydra.index.IMapSetIndex.IMapSetDiff;
import org.xydra.index.query.ITriple;
import org.xydra.index.query.KeyEntryTuple;
import org.xydra.index.query.KeyKeyEntryTuple;
import org.xydra.index.query.Wildcard;

import junit.framework.TestCase;

public class TestIndexes extends TestCase {

	public static <E> List<E> toList(final Iterator<E> it) {
		final LinkedList<E> list = new LinkedList<E>();
		while (it.hasNext()) {
			list.add(it.next());
		}
		return list;
	}

	@Test
	public void testMapSetIndex() {
		final Factory<IEntrySet<String>> entrySetFactory = new FastEntrySetFactory<String>();
		final SerializableMapSetIndex<String, String> mapSetIndex1 = new SerializableMapSetIndex<String, String>(entrySetFactory);
		mapSetIndex1.index("-1", "1");
		mapSetIndex1.index("=2", "2");
		mapSetIndex1.index("-4", "4");

		final SerializableMapSetIndex<String, String> mapSetIndex2 = new SerializableMapSetIndex<String, String>(entrySetFactory);
		mapSetIndex2.index("=2", "2");
		mapSetIndex2.index("+3", "3");
		mapSetIndex2.index("+4", "5");

		final IMapSetDiff<String, String> diff = mapSetIndex1.computeDiff(mapSetIndex2);

		Iterator<KeyEntryTuple<String, String>> it = diff.getAdded().tupleIterator(
				new Wildcard<String>(), new Wildcard<String>());

		final List<KeyEntryTuple<String, String>> addList = toList(it);
		assertEquals(2, addList.size());
		assertTrue(addList.contains(new KeyEntryTuple<String, String>("+3", "3")));
		assertTrue(addList.contains(new KeyEntryTuple<String, String>("+4", "5")));

		it = diff.getRemoved().tupleIterator(new Wildcard<String>(), new Wildcard<String>());
		final List<KeyEntryTuple<String, String>> remList = toList(it);
		assertEquals(2, remList.size());
		assertTrue(remList.contains(new KeyEntryTuple<String, String>("-1", "1")));
		assertTrue(remList.contains(new KeyEntryTuple<String, String>("-4", "4")));

	}

	@Test
	public void testMapMapSetIndex() {
		final Factory<IEntrySet<String>> entrySetFactory = new FastEntrySetFactory<String>();
		final SerializableMapMapSetIndex<String, String, String> mapMapSetIndex1 = new SerializableMapMapSetIndex<String, String, String>(
				entrySetFactory);
		mapMapSetIndex1.index("-1", "1", "1"); // = removed
		mapMapSetIndex1.index("=2", "2", "2"); // = same
		mapMapSetIndex1.index("-3", "4", "4"); // removed

		final SerializableMapMapSetIndex<String, String, String> mapMapSetIndex2 = new SerializableMapMapSetIndex<String, String, String>(
				entrySetFactory);

		mapMapSetIndex2.index("=2", "2", "2"); // = same
		mapMapSetIndex2.index("+4", "4", "4"); // = added
		mapMapSetIndex2.index("+3", "5", "5"); // added

		final IMapMapSetDiff<String, String, String> diff = mapMapSetIndex1.computeDiff(mapMapSetIndex2);

		Iterator<ITriple<String, String, String>> it = diff.getAdded().tupleIterator(
				new Wildcard<String>(), new Wildcard<String>(), new Wildcard<String>());
		final List<ITriple<String, String, String>> addList = toList(it);
		assertEquals(2, addList.size());
		assertTrue(addList.contains(new KeyKeyEntryTuple<String, String, String>("+3", "5", "5")));
		assertTrue(addList.contains(new KeyKeyEntryTuple<String, String, String>("+4", "4", "4")));

		it = diff.getRemoved().tupleIterator(new Wildcard<String>(), new Wildcard<String>(),
				new Wildcard<String>());
		final List<ITriple<String, String, String>> remList = toList(it);
		assertEquals(2, remList.size());
		assertTrue(remList.contains(new KeyKeyEntryTuple<String, String, String>("-1", "1", "1")));
		assertTrue(remList.contains(new KeyKeyEntryTuple<String, String, String>("-3", "4", "4")));
	}
}
