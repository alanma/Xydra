package org.xydra.core.util;

import static org.xydra.core.util.RegExUtil.containsCodepoint;

import org.junit.Test;

public class RegExUtilTest {

	@Test
	public void test() {
		assert containsCodepoint(new StringBuffer("äöü"), 'ä');
		assert containsCodepoint(new StringBuffer("äöü"), 'ö');
		assert containsCodepoint(new StringBuffer("äöü"), 'ü');
		assert !containsCodepoint(new StringBuffer("äöü"), 'a');

	}

}
