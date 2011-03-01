package org.xydra.csv;

import java.util.Iterator;

import org.junit.Test;


public class TestStreamingCsv {
	
	@Test
	public void testReadStreamAndWroteStream() {
		Iterator<IReadableRow> rowIt = new RowDataGenerator();
		IRowHandler rowHandler = new RowHandler();
		while(rowIt.hasNext()) {
			IReadableRow row = rowIt.next();
			rowHandler.handleRow("rowName", row);
		}
	}
	
	class RowHandler implements IRowHandler {
		
		@Override
		public void handleRow(String rowName, IReadableRow readableRow) {
			System.out.println("row: " + rowName + " " + readableRow.toString());
		}
		
	}
	
}
