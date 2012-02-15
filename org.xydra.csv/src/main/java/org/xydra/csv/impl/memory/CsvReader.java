package org.xydra.csv.impl.memory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.xydra.csv.IReadableRow;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;


public class CsvReader {
	
	private static Logger log = LoggerFactory.getLogger(CsvTable.class);
	private int readMaxRows;
	private BufferedReader br;
	private List<String> colNames;
	private long lineNumber;
	
	/**
	 * @param reader from which to read CSV (1 header + n data rows)
	 * @param readMaxRows maximum number of rows to read. -1 = infinite.
	 */
	public CsvReader(Reader reader, int readMaxRows) {
		this.br = new BufferedReader(reader);
		this.readMaxRows = readMaxRows;
	}
	
	/**
	 * @return the names of all header rows. These rows are used for subsequent
	 *         calls.
	 * @throws IllegalStateException if this method has been called before.
	 * @throws IOException from underlying Reader
	 * @throws IllegalArgumentException if reader contains no valid header row
	 */
	public List<String> readHeaders() throws IllegalStateException, IOException,
	        IllegalArgumentException {
		String line = this.br.readLine();
		if(line == null) {
			throw new IllegalArgumentException("CSV file has no content, not even headers");
		}
		// read header
		String[] headers = CsvCodec.splitAtUnquotedSemicolon(line);
		if(headers.length < 1) {
			throw new IllegalArgumentException("Found no first column");
		}
		this.colNames = new ArrayList<String>();
		for(String s : headers) {
			String decoded = CsvCodec.excelDecode(s);
			this.colNames.add(decoded == null ? "NULL" : decoded);
		}
		this.lineNumber = 1;
		return this.colNames;
	}
	
	/**
	 * @return the next data row or null if there is none.
	 * @throws IllegalStateException if {@link #readHeaders()} has not been
	 *             called before, once.
	 * @throws IOException from underlying reader
	 */
	public IReadableRow readDataRow() throws IllegalStateException, IOException {
		String line = this.br.readLine();
		SingleRow row = null;
		if(line != null && /*
							 * maybe we have to limit the number of read lines
							 */
		(this.readMaxRows == -1 || this.lineNumber < this.readMaxRows)) {
			String[] datas = CsvCodec.splitAtUnquotedSemicolon(line);
			if(this.colNames.size() != datas.length) {
				throw new IllegalArgumentException("Line " + this.lineNumber + ": Header length ("
				        + this.colNames.size() + ") is different from data length (" + datas.length
				        + ")");
			}
			
			// prepare row
			String rowName = CsvCodec.excelDecode(datas[0]);
			row = new SingleRow(rowName);
			for(int i = 1; i < this.colNames.size(); i++) {
				try {
					String value = CsvCodec.excelDecode(datas[i]);
					String colName = CsvCodec.excelDecode(this.colNames.get(i));
					row.setValue(colName, value, true);
				} catch(IllegalStateException e) {
					throw new IllegalArgumentException("Line " + this.lineNumber
					        + "> Could not parse '" + line + "'", e);
				}
			}
			
			if(this.lineNumber % 10000 == 0) {
				log.info("Parsed " + this.lineNumber + " lines...");
			}
		}
		return row;
	}
}
