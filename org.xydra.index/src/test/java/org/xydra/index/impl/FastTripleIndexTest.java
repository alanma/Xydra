package org.xydra.index.impl;

import org.xydra.index.AbstractTripleIndexTest;
import org.xydra.index.ITripleIndex;

public class FastTripleIndexTest extends AbstractTripleIndexTest<String, String, String> {

	@Override
	public String createS(final String label) {
		return label;
	}

	@Override
	public String createP(final String label) {
		return label;
	}

	@Override
	public String createO(final String label) {
		return label;
	}

	@Override
	public ITripleIndex<String, String, String> create() {
		final ITripleIndex<String, String, String> ti = new FastTripleIndex<String, String, String>();
		return ti;
	}
}
