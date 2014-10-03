package org.xydra.csv;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;

import org.xydra.annotations.RunsInGWT;
import org.xydra.csv.impl.memory.CsvTable;

@RunsInGWT(false)
public class CsvRowHandler implements IRowHandler {

	private Writer writer;
	private Collection<String> columnNames;

	public CsvRowHandler(Writer writer) {
		this.writer = writer;
	}

	@Override
	public void handleRow(String rowName, IReadableRow readableRow) throws IOException {
		if (this.columnNames == null) {
			throw new IllegalStateException();
		}
		CsvTable.writeRow(this.writer, this.columnNames, rowName, readableRow);
	}

	@Override
	public void handleHeaderRow(Collection<String> columnNames) throws IOException {
		this.columnNames = columnNames;
		CsvTable.writeHeaderRow(this.writer, columnNames);
	}

}
