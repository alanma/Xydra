package org.xydra.csv;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.xydra.csv.impl.memory.SingleRow;

public class RowDataGenerator implements Iterator<IReadableRow> {

	private long delivered = 0;

	@Override
	public boolean hasNext() {
		return this.delivered < 100;
	}

	@Override
	public IReadableRow next() {
		if (!hasNext()) {
			throw new NoSuchElementException();
		}
		this.delivered++;
		return new SingleRow("test1", new String[][] {

		new String[] { "h1", "c1" },

		new String[] { "h2", "c2" },

		new String[] { "h3", "c3" },

		new String[] { "time", "" + System.currentTimeMillis() },

		new String[] { "rnd", "" + Math.random() },

		});
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

}
