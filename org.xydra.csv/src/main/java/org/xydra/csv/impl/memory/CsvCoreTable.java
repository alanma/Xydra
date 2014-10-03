package org.xydra.csv.impl.memory;

import java.io.IOException;

import org.xydra.csv.IRow;
import org.xydra.csv.IRowHandler;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;

public class CsvCoreTable extends SparseTable implements Iterable<Row> {

	@SuppressWarnings("unused")
	private static Logger log = LoggerFactory.getLogger(CsvCoreTable.class);

	protected static final String COLUMNNAME_ROW = "ROW";

	// TODO make setParam-able
	protected String defaultEncoding = "ISO-8859-1";

	protected boolean splitWhenWritingLargeFiles = false;

	protected int readMaxRows = -1;

	/**
	 * @param s
	 * @param fieldLength
	 *            number of characters to be used to write the filed, excess
	 *            characters are truncated, missing characters are filled with
	 *            spaces.
	 * @return a String in LaTeX-safe encoding
	 */
	static String latexEncode(String s, int fieldLength) {
		String s2 = s;
		if (s == null) {
			s2 = "";
		} else if (s.equals("null")) {
			s2 = "";
		}

		while (s2.length() < fieldLength) {
			s2 = " " + s2;
		}

		return s2;
	}

	public CsvCoreTable() {
		super();
	}

	/**
	 * @param maintainColumnInsertionOrder
	 *            if true, the insertion order of columns is maintained. If
	 *            false, the default automatic alphabetic sorting is on.
	 */
	public CsvCoreTable(boolean maintainColumnInsertionOrder) {
		super(maintainColumnInsertionOrder);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xydra.csv.ICsvTable#setParamReadMaxRows(int)
	 */
	public void setParamReadMaxRows(int readMaxRows) {
		this.readMaxRows = readMaxRows;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xydra.csv.ICsvTable#setParamSplitWhenWritingLargeFiles(boolean)
	 */
	public void setParamSplitWhenWritingLargeFiles(boolean b) {
		this.splitWhenWritingLargeFiles = b;
	}

	public void writeTo(IRowHandler rowHandler) throws IOException {
		rowHandler.handleHeaderRow(getColumnNames());
		for (String rowName : this.rowNamesIterable()) {
			IRow row = this.getOrCreateRow(rowName, false);
			rowHandler.handleRow(rowName, row);
		}
	}

}
