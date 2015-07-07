package org.xydra.index.impl.trie;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.xydra.index.AbstractMapSetIndexTest;
import org.xydra.index.IEntrySet;
import org.xydra.index.impl.SmallEntrySetFactory;
import org.xydra.index.impl.TestDataGenerator;
import org.xydra.index.impl.trie.SmallStringSetTrie.KeyFragments;
import org.xydra.index.iterator.Iterators;
import org.xydra.index.query.KeyEntryTuple;
import org.xydra.index.query.Wildcard;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;

public class SmallStringSetTrieTest extends AbstractMapSetIndexTest<String, Integer> {

	private static final Logger log = LoggerFactory.getLogger(SmallStringSetTrieTest.class);

	public SmallStringSetTrieTest() {
		super(new SmallStringSetTrie<Integer>(new SmallEntrySetFactory<Integer>()));
	}

	private static int i = 1;

	private static int s = 1;

	@Override
	protected Integer createEntry() {
		return i++;
	}

	@Override
	protected String createKey() {
		return "s" + s++;
	}

	protected SmallStringSetTrie<Integer> trie() {
		return (SmallStringSetTrie<Integer>) this.mapSetIndex;
	}

	@Test
	public void testPrefixSearch() {
		trie().index("aca", 5);
		trie().index("abl", 3);
		trie().index("aaa", 1);
		trie().index("abg", 2);
		trie().index("abu", 4);

		List<KeyEntryTuple<String, Integer>> list = Iterators.toList(trie().search("ab"));
		// for(KeyEntryTuple<String,Integer> a : list) {
		// System.out.println(a);
		// }
		assertEquals("abg", list.get(0).getKey());
		assertEquals("abl", list.get(1).getKey());
		assertEquals("abu", list.get(2).getKey());
		assertEquals((Integer) 2, list.get(0).getEntry());
		assertEquals((Integer) 3, list.get(1).getEntry());
		assertEquals((Integer) 4, list.get(2).getEntry());
	}

	@Test
	public void testSpecialCases() {
		trie().index("somefragment", 1);
		trie().index("someother", 2);
		trie().index("some", 3);

		IEntrySet<Integer> set = trie().lookup("some");
		assertTrue(set.contains(3));
		assertEquals(1, set.size());
	}

	@Test
	public void testPrefixSearchDupes() {
		trie().index("helloworld", 2);
		trie().index("helloyou", 3);
		trie().index("hello", 3);
		trie().index("hello", 2);

		List<KeyEntryTuple<String, Integer>> list = Iterators.toList(trie().search("hell"));
		// for(KeyEntryTuple<String,Integer> a : list) {
		// System.out.println(a);
		// }
		assertEquals("hello", list.get(0).getKey());
		assertEquals("hello", list.get(1).getKey());
		assertEquals("helloworld", list.get(2).getKey());
		assertEquals("helloyou", list.get(3).getKey());
		assertEquals((Integer) 3, list.get(0).getEntry());
		assertEquals((Integer) 2, list.get(1).getEntry());
		assertEquals((Integer) 2, list.get(2).getEntry());
		assertEquals((Integer) 3, list.get(3).getEntry());
	}

	/**
	 * TreeMap: Total = 1320ms; insert= 7675 ns each; query= 5526 ns each
	 * 
	 * SortedArrayMap: Total = 1107ms; insert= 6356 ns each; query= 4721 ns each
	 */
	@Test
	public void testPerformance() {
		final int dataSize = 1000000;
		log.info("Preparing random datasets");
		int ITEMS = dataSize;
		String[] names = new String[ITEMS];
		Set<String> uniqueNames = new HashSet<String>();
		for (int i = 0; i < ITEMS; i++) {
			String name = TestDataGenerator.generateRandomKatakanaString(1, 10);
			while (uniqueNames.contains(name)) {
				name += TestDataGenerator.generateRandomKatakanaString(1, 10);
			}
			uniqueNames.add(name);
			names[i] = name;
		}

		log.info("test performance now");
		long start = System.nanoTime();
		for (int i = 0; i < ITEMS; i++) {
			Integer id = i % (ITEMS / 10);
			String name = names[i];

			this.mapSetIndex.index(name, id);
		}
		long insert = System.nanoTime();
		// query
		int QUERY = dataSize;
		for (int i = 0; i < QUERY; i++) {
			String name = TestDataGenerator.randomFromList(names);
			IEntrySet<Integer> set = this.mapSetIndex.lookup(name);
			assert !set.isEmpty();
		}
		long query = System.nanoTime();
		System.out.println("Total = " + (query - start) / (1000 * 1000) + "ms; insert= "
				+ (insert - start) / ITEMS + " ns each; query= " + (query - insert) / QUERY
				+ " ns each");
	}

	@Test
	public void testLongestMatch() {
		trie().index("helloworld", 2);
		trie().index("helloyou", 3);
		trie().index("hello", 3);
		trie().index("hello", 2);

		int l = trie().getLongestMatch("And then hellover, what next", 9).getFirst();
		assertEquals(5, l);

		assertNull(trie().getLongestMatch("And then he llover, what next", 9));
	}

	@Test
	public void test1() {
		int sharedLen = SmallStringSetTrie.getSharedPrefixLength("Hello World",
				"Hell a lot of work");
		assertEquals(4, sharedLen);

		SmallStringSetTrie<Integer> st = new SmallStringSetTrie<Integer>(
				new SmallEntrySetFactory<Integer>());
		st.index("Hello World", 13);
		st.index("Hell", 11);
		st.index("Hell a lot of work", 666);

		st.dump();

		List<Integer> ints = new ArrayList<Integer>();
		Iterators.addAll(st.iterator(), ints);
		System.out.println(ints);

		List<String> keys = new ArrayList<String>();
		Iterators.addAll(st.keyIterator(), keys);
		System.out.println(keys);

		List<KeyEntryTuple<String, Integer>> tuples = new ArrayList<KeyEntryTuple<String, Integer>>();
		Iterators.addAll(st.tupleIterator(new Wildcard<String>(), new Wildcard<Integer>()), tuples);
		System.out.println(tuples);

		st.deIndex("Hello");
		st.deIndex("Hell");

		// next test
		SmallStringSetTrie<Void> st2 = new SmallStringSetTrie<Void>(
				new SmallEntrySetFactory<Void>());
		st2.indexKey("aaabbccc");
		st2.indexKey("aaaddeee");
		st2.indexKey("aaabbcff");
		System.out.println("-----");
		st2.dump();
		KeyFragments ky = st2.getKeyFragmentsFor("aaabbcg");
		assert ky.matched.size() == 2;
		assert ky.matched.get(0).equals("aaa");
		assert ky.matched.get(1).equals("bbc");
		assert ky.remainder.matches("g");
		ky = st2.getKeyFragmentsFor("aaabb");
		assert ky.matched.size() == 2;
		assert ky.matched.get(0).equals("aaa");
		assert ky.matched.get(1).equals("bb");
		assert ky.remainder.matches("");
	}

}
