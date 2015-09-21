package org.xydra.index.impl;

import org.xydra.index.AbstractMapIndexTest;
import org.xydra.index.impl.trie.SortedStringMapIndex;

public class SortedStringMapIndexTest extends AbstractMapIndexTest<String, Integer> {

	public SortedStringMapIndexTest() {
		super(new SortedStringMapIndex<Integer>());
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
