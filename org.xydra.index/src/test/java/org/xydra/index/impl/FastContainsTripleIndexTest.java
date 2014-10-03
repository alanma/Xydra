package org.xydra.index.impl;

import org.xydra.index.AbstractTripleIndexTest;
import org.xydra.index.ITripleIndex;

public class FastContainsTripleIndexTest extends AbstractTripleIndexTest<String, String, String> {

	@Override
	public String createS(String label) {
		return label;
	}

	@Override
	public String createP(String label) {
		return label;
	}

	@Override
	public String createO(String label) {
		return label;
	}

	@Override
	public ITripleIndex<String, String, String> create() {
		ITripleIndex<String, String, String> ti = new FastContainsTripleIndex<String, String, String>();
		return ti;
	}
}
