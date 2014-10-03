package org.xydra.index.impl;

import org.xydra.index.AbstractMapIndexTest;
import org.xydra.index.impl.trie.SmallSortedStringMap;

public class SmallSortedStringMapIndexTest extends AbstractMapIndexTest<String, Integer> {

	public SmallSortedStringMapIndexTest() {
		super(new SmallSortedStringMap<Integer>());
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

}
