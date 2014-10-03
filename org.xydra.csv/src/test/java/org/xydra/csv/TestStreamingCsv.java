package org.xydra.csv;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import org.junit.Test;

public class TestStreamingCsv {

	@Test
	public void testReadStreamAndWroteStream() throws IllegalStateException, IOException {
		Iterator<IReadableRow> rowIt = new RowDataGenerator();
		IRowHandler rowHandler = new RowHandler();
		while (rowIt.hasNext()) {
			IReadableRow row = rowIt.next();
			rowHandler.handleRow("rowName", row);
		}
	}

	class RowHandler implements IRowHandler {

		@Override
		public void handleRow(String rowName, IReadableRow readableRow) {
			System.out.println("row: " + rowName + " " + readableRow.toString());
		}

		@Override
		public void handleHeaderRow(Collection<String> columnNames) {
			System.out.println("HEADER " + columnNames);
		}

	}

}
