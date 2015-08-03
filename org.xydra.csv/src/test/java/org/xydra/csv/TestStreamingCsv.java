package org.xydra.csv;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import org.junit.Test;

public class TestStreamingCsv {

	@Test
	public void testReadStreamAndWroteStream() throws IllegalStateException, IOException {
		final Iterator<IReadableRow> rowIt = new RowDataGenerator();
		final IRowHandler rowHandler = new RowHandler();
		while (rowIt.hasNext()) {
			final IReadableRow row = rowIt.next();
			rowHandler.handleRow("rowName", row);
		}
	}

	class RowHandler implements IRowHandler {

		@Override
		public void handleRow(final String rowName, final IReadableRow readableRow) {
			System.out.println("row: " + rowName + " " + readableRow.toString());
		}

		@Override
		public void handleHeaderRow(final Collection<String> columnNames) {
			System.out.println("HEADER " + columnNames);
		}

	}

}
