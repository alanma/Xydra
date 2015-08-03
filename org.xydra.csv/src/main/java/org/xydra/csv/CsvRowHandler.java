package org.xydra.csv;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;

import org.xydra.annotations.RunsInGWT;
import org.xydra.csv.impl.memory.CsvTable;

@RunsInGWT(false)
public class CsvRowHandler implements IRowHandler {

	private final Writer writer;
	private Collection<String> columnNames;

	public CsvRowHandler(final Writer writer) {
		this.writer = writer;
	}

	@Override
	public void handleRow(final String rowName, final IReadableRow readableRow) throws IOException {
		if (this.columnNames == null) {
			throw new IllegalStateException();
		}
		CsvTable.writeRow(this.writer, this.columnNames, rowName, readableRow);
	}

	@Override
	public void handleHeaderRow(final Collection<String> columnNames) throws IOException {
		this.columnNames = columnNames;
		CsvTable.writeHeaderRow(this.writer, columnNames);
	}

}
