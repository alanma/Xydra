package org.xydra.index.impl.trie;

import java.util.SortedMap;

import org.junit.Test;

import com.google.gwt.editor.client.Editor.Ignore;

public class SortedArrayMapTest extends SortedMapTest {

	@Override
	protected SortedMap<String, Integer> createMap() {
		return new SortedArrayMap<String, Integer>();
	}

	@Test
	@Ignore
	public void testBinarySearch() {

	}

}
