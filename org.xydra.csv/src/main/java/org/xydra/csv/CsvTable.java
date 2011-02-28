package org.xydra.csv;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeSet;

import org.xydra.csv.CsvTable.Row.Cell;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;


/**
 * Maintains a sparse table, organized by rows and columns.
 * 
 * Insertion order is preserved.
 * 
 * You are trying to open a file that contains more than 65,536 rows or 256
 * columns.
 * 
 * @author voelkel
 * 
 */
public class CsvTable implements Iterable<CsvTable.Row> {
	
	private Collection<String> columnNames;
	
	private boolean restrictToExcelSize = false;
	
	/* maintain row insertion order */
	private List<String> rowNames = new LinkedList<String>();
	
	private boolean splitWhenWritingLargeFiles = false;
	
	private Map<String,Row> table = new HashMap<String,Row>();
	
	private int readMaxRows = -1;
	
	private IRowInsertionHandler rowInsertionHandler;
	
	private boolean aggregateStrings = true;
	
	// TODO make setParam-able
	private String defaultEncoding = "ISO-8859-1";
	
	public CsvTable() {
		this.columnNames = new TreeSet<String>();
	}
	
	/**
	 * @param maintainColumnInsertionOrder if true, the insertion order of
	 *            columns is maintained. If false, the default automatic
	 *            alphabetic sorting is on.
	 */
	public CsvTable(boolean maintainColumnInsertionOrder) {
		if(maintainColumnInsertionOrder) {
			this.columnNames = new LinkedHashSet<String>();
		} else {
			this.columnNames = new TreeSet<String>();
		}
	}
	
	/**
	 * A Row is a sparse representation of a table row.
	 * 
	 * @author voelkel
	 */
	public class Row extends HashMap<String,CsvTable.Row.Cell> {
		
		private static final long serialVersionUID = -1859613946021005526L;
		private static final String ROW_KEY = "ROW";
		
		/**
		 * @param columnName for which to get the value in this row
		 * @return the value or the String "null"
		 */
		public String getValue(String columnName) {
			CsvTable.Row.Cell cell = getOrCreateCell(columnName, false);
			return cell == null ? "null" : cell.getValue();
		}
		
		private CsvTable.Row.Cell getOrCreateCell(String column, boolean create) {
			CsvTable.Row.Cell c = get(column);
			if(c == null && create) {
				if(CsvTable.this.columnNames.size() == EXCEL_MAX_COLS) {
					log.warn("Cannot add the " + EXCEL_MAX_COLS
					        + "th column - that is Excels limit");
					if(CsvTable.this.restrictToExcelSize)
						throw new ExcelLimitException("Column limit reached");
				}
				c = new Cell();
				put(column, c);
				// maintain index of column name
				CsvTable.this.columnNames.add(column);
			}
			return c;
		}
		
		/**
		 * Aggregate given row into this row
		 * 
		 * @param row never null
		 */
		public void aggregate(Row row, String[] keyColumnNames) {
			assert row != null;
			for(String colName : CsvTable.this.columnNames) {
				if(colName.equals(ROW_KEY)) {
					// skip row key
				} else if(!contains(keyColumnNames, colName)) {
					// skip key columns
				} else {
					String thisValue = this.getValue(colName);
					String otherValue = row.getValue(colName);
					if(otherValue == null || otherValue.equals("")) {
						// ignore
					} else {
						// try as number
						try {
							long thisLong;
							if(thisValue == null) {
								thisLong = 0;
							} else {
								thisLong = Long.parseLong(thisValue);
							}
							long otherLong = Long.parseLong(otherValue);
							long sum = thisLong + otherLong;
							if(sum == 0) {
								this.removeValue(colName);
							} else {
								this.setValue(colName, "" + sum);
							}
						} catch(NumberFormatException e) {
							// string concatenation
							if(otherValue.equals("")) {
								// do nothing
							} else {
								if(thisValue == null || thisValue.equals("")) {
									this.setValue(colName, otherValue);
								} else {
									if(CsvTable.this.aggregateStrings) {
										String concat = thisValue + "|" + otherValue;
										this.setValue(colName, concat);
									}
								}
							}
						}
					}
				}
			}
		}
		
		private void removeValue(String colName) {
			this.remove(colName);
			// IMPROVE: colName remains indexed, we don't know where else the
			// colName is used
		}
		
		private void setValue(String columnName, String value) {
			CsvTable.Row.Cell c = getOrCreateCell(columnName, true);
			c.setValue(value, false);
		}
		
		/**
		 * @param array
		 * @param value
		 * @return true if array of String contains value
		 */
		private boolean contains(String[] array, String value) {
			for(int i = 0; i < array.length; i++) {
				if(array[i].equals(value))
					return true;
			}
			return false;
		}
		
		// public String getKey() {
		// return this.getValue(ROW_KEY);
		// }
		//
		// public void setKey(String rowKey) {
		// setValue(ROW_KEY, rowKey);
		// }
		
		/**
		 * @param columnName never null
		 * @param value may be null
		 * @param initial if false and there was a already a value.
		 * @throws IllegalStateException if 'initial' is true and there was a
		 *             previous value
		 */
		public void setValue(String columnName, String value, boolean initial) {
			if(value == null) {
				// nothing to set, keep table sparse
			} else {
				Cell cell = getOrCreateCell(columnName, true);
				try {
					cell.setValue(value, initial);
				} catch(IllegalStateException e) {
					throw new IllegalStateException("Could not set value in column (" + columnName
					        + ")", e);
				}
			}
		}
		
		public void setValue(String columnName, long value, boolean initial) {
			if(value == 0) {
				// nothing to set, keep table sparse
			} else {
				setValue(columnName, "" + value, initial);
			}
		}
		
		public void incrementValue(String columnName, int increment) {
			if(increment == 0) {
				// nothing to set, keep table sparse
			} else {
				Cell cell = getOrCreateCell(columnName, true);
				try {
					cell.incrementValue(increment);
				} catch(IllegalStateException e) {
					throw new IllegalStateException("Could not increment value in column ("
					        + columnName + "). Stored was " + this.getValue(columnName), e);
				}
			}
		}
		
		public void appendValue(String columnName, String value, int maximalFieldLength) {
			if(value == null || value.equals("")) {
				// nothing to set, keep table sparse
			} else {
				Cell cell = getOrCreateCell(columnName, true);
				try {
					cell.appendString(value, maximalFieldLength);
				} catch(IllegalStateException e) {
					throw new IllegalStateException("Could not append value in column ("
					        + columnName + "). Stored was " + this.getValue(columnName), e);
				}
			}
		}
		
		/**
		 * @param columnName never null
		 * @return 0 if value is not set.
		 * @throws WrongDatatypeException if value was set, but could not be
		 *             parsed as long.
		 */
		public long getValueAsLong(String columnName) {
			Cell cell = getOrCreateCell(columnName, false);
			
			if(cell == null) {
				return 0;
			} else {
				return cell.getValueAsLong();
			}
		}
		
		public double getValueAsDouble(String columnName) {
			Cell cell = getOrCreateCell(columnName, false);
			
			if(cell == null) {
				return 0;
			} else {
				return cell.getValueAsDouble();
			}
		}
		
		class Cell {
			
			private String s;
			
			public void appendString(String s, int maximalFieldLength) {
				int sLen = this.s == null ? 0 : this.s.length();
				// if we have any space left
				if(sLen < maximalFieldLength) {
					// initialise
					if(this.s == null) {
						this.s = "";
					}
					// append
					this.s += s.substring(0, Math.min(s.length(), maximalFieldLength - sLen));
				}
			}
			
			public long getValueAsLong() {
				if(this.s == null) {
					return 0;
				} else {
					try {
						return Long.parseLong(this.s);
					} catch(NumberFormatException e) {
						throw new WrongDatatypeException("Content was '" + this.s
						        + "'. Could not parse as long.", e);
					}
				}
			}
			
			public double getValueAsDouble() {
				if(this.s == null) {
					return 0;
				} else {
					try {
						return Double.parseDouble(this.s);
					} catch(NumberFormatException e) {
						// retry with ',' as '.'
						String usVersion = this.s.replace(",", ".");
						try {
							return Double.parseDouble(usVersion);
						} catch(NumberFormatException e2) {
							throw new WrongDatatypeException("Content was '" + this.s
							        + "'. Could not parse as double. Even tried to parse as '"
							        + usVersion + "'", e);
						}
					}
				}
			}
			
			public String getValue() {
				return this.s;
			}
			
			public void incrementValue(int increment) throws WrongDatatypeException {
				long l = getValueAsLong();
				l = l + increment;
				this.s = "" + l;
			}
			
			/**
			 * @param value may be null, to store a null.
			 * @param initial if true, throws an {@link IllegalStateException}
			 *            if there was already a value
			 * @throws IllegalStateException if there was already a value
			 */
			public void setValue(String value, boolean initial) {
				if(initial && this.s != null) {
					throw new IllegalStateException("Value was not null but '" + this.s
					        + "' so could not set to '" + value + "'");
				}
				this.s = value;
			}
			
		}
		
		public Iterator<Cell> iterator() {
			return this.values().iterator();
		}
		
		public Collection<String> getColumnNames() {
			return this.keySet();
		}
		
		/**
		 * Add all values from the otherRow to this Row
		 * 
		 * @param otherRow from which to add all cells
		 */
		public void addAll(Row otherRow) {
			for(String colName : otherRow.getColumnNames()) {
				this.setValue(colName, otherRow.getValue(colName), true);
			}
		}
	}
	
	public static final int EXCEL_MAX_COLS = 255;
	
	public static final int EXCEL_MAX_ROWS = 65535;
	
	private static final String COLUMNNAME_ROW = "ROW";
	
	private static Logger log = LoggerFactory.getLogger(CsvTable.class);
	private static final String SEMICOLON = ";";
	
	private static String excelDecode(String encoded) {
		if(encoded.equals("") || encoded.equals("\"\"")) {
			return null;
		}
		
		String decoded = encoded;
		if(encoded.startsWith("\"") && encoded.endsWith("\"")) {
			decoded = decoded.substring(1, encoded.length() - 1);
		}
		// unescape
		decoded = decoded.replace("\"\"", "\"");
		return decoded;
	}
	
	/**
	 * Use safe CSV encoding, that is, surround by " and escape all quotes.
	 * 
	 * Enclosing all fields in quotes is not strictly required, but lets Excel
	 * open an CSV file with default settings without mangling e.g. dates.
	 * 
	 * @param value
	 */
	private static String excelEncode(String value) {
		if(value == null) {
			return "\"\"";
		}
		
		String escaped;
		if(value.contains("\"")) {
			escaped = value.replace("\"", "\"\"");
		} else {
			escaped = value;
		}
		
		escaped = escaped.replace("\n", "§N");
		escaped = escaped.replace("\r", "§R");
		
		return "\"" + escaped + "\"";
	}
	
	/**
	 * Used only by tests.
	 * 
	 * @param row
	 * @param column
	 * @param s
	 * @param maximalFieldLength
	 */
	void appendString(String row, String column, String s, int maximalFieldLength) {
		Row r = getOrCreateRow(row, true);
		Cell c = r.getOrCreateCell(column, true);
		c.appendString(s, maximalFieldLength);
	}
	
	/**
	 * @param row never null
	 * @param column never null
	 * @return the table value at cell ('row','column')
	 */
	public String getValue(String row, String column) {
		Row r = getOrCreateRow(row, false);
		if(r == null) {
			return null;
		}
		Cell c = r.getOrCreateCell(column, false);
		if(c == null) {
			return null;
		}
		return c.getValue();
	}
	
	/**
	 * Increments a value at (row,column). Creates the cell before, if
	 * necessary.
	 * 
	 * @param row never null
	 * @param column never null
	 * @param increment the increment, often '1' is used.
	 * @throws WrongDatatypeException if the stored value cannot be cast to a
	 *             long.
	 */
	public void incrementValue(String row, String column, int increment)
	        throws WrongDatatypeException {
		Row r = getOrCreateRow(row, true);
		Cell c = r.getOrCreateCell(column, true);
		c.incrementValue(increment);
	}
	
	/**
	 * Add the content given in the reader as CSV to this CvsTable. Multiple
	 * CvsTable can be read in, the result is a merge.
	 * 
	 * The first row of the CVS file is interpreted as header names. The first
	 * column is interpreted as row names.
	 * 
	 * @param f from which to read
	 * @throws IOException if file reading fails
	 */
	public void readFrom(File f) throws IOException {
		log.info("Reading CSV table from " + f.getAbsolutePath() + " Before: " + this.rowCount()
		        + " rows and " + this.colCount() + " columns");
		FileInputStream fos = new FileInputStream(f);
		Reader reader = new InputStreamReader(fos, Charset.forName(this.defaultEncoding));
		readFrom(reader);
		reader.close();
	}
	
	/**
	 * Aggregates all rows that share the same values for the given keys.
	 * 
	 * Aggregation is simple: If the values can be parsed as numbers, they are
	 * added. Otherwise string concatenation with a separator is used.
	 * 
	 * Example: Aggregating a table with columns "a", "b" and "c" using
	 * keyColumnNames=("a","b") will add all numbers of "c" where "a" and "b"
	 * are equal. Given the table
	 * <table>
	 * <tr>
	 * <th>a</th>
	 * <th>b</th>
	 * <th>c</th>
	 * </tr>
	 * <tr>
	 * <td>blue</td>
	 * <td>on</td>
	 * <td>1</td>
	 * </tr>
	 * <tr>
	 * <td>red</td>
	 * <td>on</td>
	 * <td>3</td>
	 * </tr>
	 * <tr>
	 * <td>blue</td>
	 * <td>off</td>
	 * <td>7</td>
	 * </tr>
	 * <tr>
	 * <td>red</td>
	 * <td>on</td>
	 * <td>11</td>
	 * </tr>
	 * <tr>
	 * <td>green</td>
	 * <td>on</td>
	 * <td>13</td>
	 * </tr>
	 * </table>
	 * results in
	 * <table>
	 * <tr>
	 * <th>a</th>
	 * <th>b</th>
	 * <th>c</th>
	 * </tr>
	 * <tr>
	 * <td>blue</td>
	 * <td>on</td>
	 * <td>1</td>
	 * </tr>
	 * <tr>
	 * <td>blue</td>
	 * <td>off</td>
	 * <td>7</td>
	 * </tr>
	 * <tr>
	 * <td>red</td>
	 * <td>on</td>
	 * <td>14</td>
	 * </tr>
	 * <tr>
	 * <td>green</td>
	 * <td>on</td>
	 * <td>13</td>
	 * </tr>
	 * </table>
	 * 
	 * Existing sorting order of table is ignored.
	 * 
	 * @param keyColumnNames an array of column names which is used for
	 *            aggregation.
	 */
	public void aggregate(String[] keyColumnNames) {
		// temporary index of occurring keys and their row
		Map<String,Row> compoundKeys2row = new HashMap<String,Row>(this.rowCount());
		Iterator<Row> rowIt = this.table.values().iterator();
		// Collection<String> rowKeysToBeRemoved = new LinkedList<String>();
		long processed = 0;
		long aggregated = 0;
		while(rowIt.hasNext()) {
			CsvTable.Row row = rowIt.next();
			
			// calculate compound key FIXME takes 50% of performance
			StringBuffer compoundKeyBuffer = new StringBuffer(100);
			for(String keyColumnName : keyColumnNames) {
				String value = row.getValue(keyColumnName);
				if(value != null) {
					compoundKeyBuffer.append(value);
				}
			}
			String compoundKey = compoundKeyBuffer.toString();
			
			if(compoundKeys2row.containsKey(compoundKey)) {
				// aggregate row into existing row
				Row masterRow = compoundKeys2row.get(compoundKey);
				masterRow.aggregate(row, keyColumnNames);
				aggregated++;
				
				// delete row
				rowIt.remove();
				// rowKeysToBeRemoved.add(row.getKey());
			} else {
				// just mark key as seen
				compoundKeys2row.put(compoundKey, row);
			}
			
			processed++;
			if(processed % 1000 == 0) {
				log.info("Aggregate processed " + processed + " rows, aggregated " + aggregated);
			}
		}
		// // delete
		// log.info("Deleting " + rowKeysToBeRemoved.size() + " rows");
		// for (String rowKeyToBeRemoved : rowKeysToBeRemoved) {
		// this.table.remove(rowKeyToBeRemoved);
		// }
		log.info(this.rowCount() + " rows with aggregated data remain");
	}
	
	private static String[] splitAtUnquotedSemicolon(String line) {
		boolean inQuote = false;
		int index = 0;
		List<String> result = new LinkedList<String>();
		StringBuffer currentString = new StringBuffer();
		while(index < line.length()) {
			char c = line.charAt(index);
			switch(c) {
			case '\"': {
				if(inQuote) {
					inQuote = false;
				} else {
					inQuote = true;
				}
				currentString.append(c);
			}
				break;
			case ';': {
				if(inQuote) {
					// just copy over
					currentString.append(c);
				} else {
					// terminate current string
					result.add(currentString.toString());
					currentString = new StringBuffer();
				}
			}
				break;
			default: {
				// just copy over
				currentString.append(c);
			}
				break;
			}
			index++;
		}
		result.add(currentString.toString());
		return result.toArray(new String[result.size()]);
	}
	
	/**
	 * Add the content given in the reader as CSV to this CvsTable. Multiple
	 * CvsTable can be read in, the result is a merge.
	 * 
	 * The first row of the CVS file is interpreted as header names. The first
	 * column is interpreted as row names.
	 * 
	 * @param r from which to read
	 * @throws IOException from the underyling reader
	 */
	public void readFrom(Reader r) throws IOException {
		BufferedReader br = new BufferedReader(r);
		String line = br.readLine();
		long lineNumber = 1;
		if(line == null) {
			throw new IllegalArgumentException("CVS file has no content");
		}
		// read header
		String[] headers = splitAtUnquotedSemicolon(line);
		if(headers.length < 1) {
			throw new IllegalArgumentException("Found no first column");
		}
		
		// read data
		line = br.readLine();
		lineNumber++;
		while(line != null && /*
							 * maybe we have to limit the number of read lines
							 */
		(this.readMaxRows == -1 || lineNumber < this.readMaxRows)) {
			String[] datas = splitAtUnquotedSemicolon(line);
			if(headers.length != datas.length) {
				throw new IllegalArgumentException("Line " + lineNumber + ": Header length ("
				        + headers.length + ") is different from data length (" + datas.length + ")");
			}
			
			// prepare row
			String rowName = excelDecode(datas[0]);
			Row row = new Row();
			for(int i = 1; i < headers.length; i++) {
				try {
					String value = excelDecode(datas[i]);
					String colName = excelDecode(headers[i]);
					row.setValue(colName, value, true);
				} catch(IllegalStateException e) {
					throw new IllegalArgumentException("Line " + lineNumber + "> Could not parse '"
					        + line + "'", e);
				}
			}
			
			// add row
			addRow(rowName, row);
			
			line = br.readLine();
			lineNumber++;
			
			if(lineNumber % 10000 == 0) {
				log.info("Parsed " + lineNumber + " lines...");
			}
		}
		
	}
	
	/**
	 * Add checked
	 * 
	 * @param rowName
	 * @param row
	 */
	private void addRow(String rowName, Row row) {
		boolean insert = true;
		if(this.rowInsertionHandler != null) {
			insert &= this.rowInsertionHandler.beforeRowInsertion(row);
		}
		if(insert) {
			this.insertRow(rowName, row);
		}
	}
	
	/**
	 * Removes all rows that match the given RowFilter.
	 * 
	 * @param rowFilter never null
	 */
	public void removeRowsMatching(RowFilter rowFilter) {
		Iterator<Entry<String,Row>> it = this.table.entrySet().iterator();
		while(it.hasNext()) {
			Map.Entry<String,CsvTable.Row> entry = it.next();
			if(rowFilter.matches(entry.getValue())) {
				it.remove();
			}
		}
	}
	
	/**
	 * @param b if true, the table restricts itself to a size Excel can handle
	 *            and ignores too much data. Warnings are logged in this case.
	 * 
	 *            Default is false.
	 */
	public void setParamRestrictToExcelSize(boolean b) {
		this.restrictToExcelSize = b;
	}
	
	/**
	 * @param b Default is false. If true, file writing is split into files with
	 *            65535 records each, so that Excel can handle it.
	 */
	public void setParamSplitWhenWritingLargeFiles(boolean b) {
		this.splitWhenWritingLargeFiles = b;
	}
	
	/**
	 * Set an initial value to cell (rowName, columnName)
	 * 
	 * @param rowName never null
	 * @param columnName never null
	 * @param value may be null
	 * @throws IllegalStateException if there was already a value
	 */
	public void setValueInitial(String rowName, String columnName, String value)
	        throws IllegalStateException {
		Row row = getOrCreateRow(rowName, true);
		row.setValue(columnName, value, true);
	}
	
	public void writeTo(File f) throws FileNotFoundException {
		log.info("Writing CSV table to " + f.getAbsolutePath());
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(f);
		} catch(FileNotFoundException e) {
			// Java's way of saying, Windows locked the file
			File f2 = new File(f.getAbsolutePath() + "-COULD-NOT-OVERWRITE");
			fos = new FileOutputStream(f2);
		}
		Writer writer = new OutputStreamWriter(fos, Charset.forName(this.defaultEncoding));
		try {
			if(this.splitWhenWritingLargeFiles) {
				log.info("Will write as " + ((this.table.size() / EXCEL_MAX_ROWS) + 1) + " file(s)");
				int startRow = 0;
				int endRow = Math.min(EXCEL_MAX_ROWS, this.table.size());
				writeTo(writer, startRow, endRow);
				int writtenRows = endRow - startRow;
				int fileNumber = 1;
				while(writtenRows < this.table.size()) {
					// split in several files
					writer.flush();
					writer.close();
					
					// shift start
					startRow += EXCEL_MAX_ROWS;
					endRow = Math.min(endRow + EXCEL_MAX_ROWS, this.table.size());
					
					File f1 = new File(f.getAbsolutePath() + "-part-" + fileNumber);
					fos = new FileOutputStream(f1);
					writer = new OutputStreamWriter(fos, Charset.forName(this.defaultEncoding));
					fileNumber++;
					writeTo(writer, startRow, endRow);
					writtenRows += endRow - startRow;
				}
				
			} else {
				writeTo(writer);
			}
		} catch(IOException e) {
			log.warn("", e);
		} finally {
			try {
				writer.flush();
				writer.close();
			} catch(IOException e1) {
				log.warn("", e1);
			}
		}
	}
	
	/**
	 * Writes a non-sparse CSV version of this tables contents
	 * 
	 * @param w to which to write the CSV
	 * @throws IOException from the writer
	 */
	public void writeTo(Writer w) throws IOException {
		log.info("Writing " + this.table.size() + " rows with " + this.columnNames.size()
		        + " columns");
		writeTo(w, 0, this.rowCount());
	}
	
	/**
	 * Writes a non-sparse CSV version of this tables contents. Writes only the
	 * data in [startRow,endRow).
	 * 
	 * @param w to which to write the CSV
	 * @param startRow inclusive
	 * @param endRow exclusive
	 * @throws IOException from the writer
	 * @throws ExcelLimitException if using more than 65535 rows or more than
	 *             255 columns.
	 */
	public void writeTo(Writer w, int startRow, int endRow) throws IOException, ExcelLimitException {
		if(endRow - startRow > EXCEL_MAX_ROWS) {
			throw new ExcelLimitException("Exceeding Excels limit of " + EXCEL_MAX_ROWS + " rows");
		}
		log.debug("Writing rows " + startRow + " to " + endRow + " of " + this.rowCount()
		        + " rows with " + this.colCount() + " columns");
		int writtenRows = 0;
		int writtenCols = 0;
		
		// header
		w.write(excelEncode(COLUMNNAME_ROW));
		
		Iterator<String> colIt = this.columnNames.iterator();
		while(colIt.hasNext() && writtenCols < EXCEL_MAX_COLS) {
			String columnName = colIt.next();
			w.write(SEMICOLON + excelEncode(columnName));
			writtenCols++;
			if(writtenCols == EXCEL_MAX_COLS) {
				log.warn("Reached Excels limit of " + EXCEL_MAX_COLS + " columns");
			}
		}
		w.write(SEMICOLON + "\n");
		writtenRows++;
		
		// data
		List<String> selectedRowNames = this.rowNames.subList(startRow, endRow);
		Iterator<String> rowIt = selectedRowNames.iterator();
		while(rowIt.hasNext() && writtenRows < EXCEL_MAX_ROWS) {
			String rowName = rowIt.next();
			w.write(excelEncode(rowName));
			colIt = this.columnNames.iterator();
			writtenCols = 0;
			while(colIt.hasNext() && writtenCols < EXCEL_MAX_COLS) {
				String columnName = colIt.next();
				w.write(SEMICOLON + excelEncode(this.getValue(rowName, columnName)));
				writtenCols++;
				if(writtenCols == EXCEL_MAX_COLS) {
					log.warn("Reached Excels limit of " + EXCEL_MAX_COLS + " columns");
				}
			}
			w.write(SEMICOLON + "\n");
			writtenRows++;
		}
	}
	
	private int colCount() {
		return this.columnNames.size();
	}
	
	/**
	 * Returns an existing row or creates a new one. The new row is guaranteed
	 * to be inserted into the table.
	 * 
	 * @param rowName a String that uniquely identifies a {@link Row}. Never
	 *            null.
	 * @param create if true, creates a new {@link Row} if there is none yet. If
	 *            false and there was now row, null is returned.
	 * @return a new or exiting {@link Row} or null (if 'create' was false and
	 *         no {@link Row} with given 'rowName' exists).
	 */
	public Row getOrCreateRow(String rowName, boolean create) {
		Row row = this.table.get(rowName);
		if(row == null && create) {
			row = new Row();
			this.insertRow(rowName, row);
		}
		return row;
	}
	
	private void insertRow(String rowName, Row row) {
		if(this.table.size() == EXCEL_MAX_ROWS) {
			log.warn("Adding the " + EXCEL_MAX_ROWS + "th row - that is Excels limit");
			if(this.restrictToExcelSize)
				throw new ExcelLimitException("Row limit reached");
		}
		/* new key? */
		if(this.table.containsKey(rowName)) {
			// attempt a merge
			Row existingRow = this.getOrCreateRow(rowName, false);
			
			for(Map.Entry<String,Cell> entry : row.entrySet()) {
				try {
					existingRow.setValue(entry.getKey(), entry.getValue().getValue(), true);
				} catch(IllegalStateException ex) {
					throw new IllegalStateException("Table contains already a row named '"
					        + rowName + "' and for key '" + entry.getKey()
					        + "' there was already a value.", ex);
				}
			}
		} else {
			assert !this.rowNames.contains(rowName);
			this.rowNames.add(rowName);
			this.table.put(rowName, row);
		}
	}
	
	/**
	 * @return the number of rows in the table. A very fast operation.
	 */
	public int rowCount() {
		return this.table.size();
	}
	
	/**
	 * How many rows should maximally be read? Default is -1 = unlimited. Note
	 * that more rows can be added later.
	 * 
	 * @param readMaxRows set to -1 for unlimited (default)
	 */
	public void setParamReadMaxRows(int readMaxRows) {
		this.readMaxRows = readMaxRows;
	}
	
	/**
	 * Set an {@link IRowInsertionHandler} which is called before and after each
	 * row insertion (either during read or create).
	 * 
	 * @see IRowInsertionHandler
	 * @param rowInsertionHandler never null
	 */
	public void setRowInsertionHandler(IRowInsertionHandler rowInsertionHandler) {
		this.rowInsertionHandler = rowInsertionHandler;
	}
	
	/**
	 * @param aggregateStrings Default is true. If false, strings are not
	 *            aggregated.
	 */
	public void setParamAggregateStrings(boolean aggregateStrings) {
		this.aggregateStrings = aggregateStrings;
	}
	
	/**
	 * Handlers to execute before and after row insertion. The handler can
	 * reject rows from being inserted by returning false in the
	 * {@link #beforeRowInsertion(Row)} method.
	 * 
	 * @author voelkel
	 */
	interface IRowInsertionHandler {
		/**
		 * @param row
		 * @return if the row should really be inserted
		 */
		boolean beforeRowInsertion(Row row);
		
		void afterRowInsertion(Row row);
		
	}
	
	/**
	 * Dump table to System.out
	 * 
	 * @throws IOException from System.out
	 */
	public void dump() throws IOException {
		Writer writer = new OutputStreamWriter(System.out);
		writeTo(writer);
		writer.flush();
	}
	
	/**
	 * Deletes all data, but remembers the column names. This allows to generate
	 * several tables with the same layout, even if data is cleared in between.
	 */
	public void clear() {
		this.rowNames.clear();
		this.table.clear();
	}
	
	public Iterator<Row> iterator() {
		return this.table.values().iterator();
	}
	
	/**
	 * Create a sub-table
	 * 
	 * @param key never null
	 * @param value may be null
	 * @return a sub-table where column key has the given value
	 */
	public CsvTable filter(String key, String value) {
		CsvTable target = new CsvTable();
		for(String rowName : this.rowNames) {
			Row sourceRow = this.getOrCreateRow(rowName, false);
			if(sourceRow.getValue(key).equals(value)) {
				// copy row
				Row targetRow = target.getOrCreateRow(rowName, true);
				for(String colName : sourceRow.keySet()) {
					targetRow.setValue(colName, sourceRow.getValue(colName), true);
				}
			}
		}
		return target;
	}
	
	/**
	 * Create a sub-table
	 * 
	 * @param key never null
	 * @param value may be null
	 * @return a sub-table where no column has for the given key the given value
	 */
	public CsvTable drop(String key, String value) {
		CsvTable target = new CsvTable();
		for(String rowName : this.rowNames) {
			Row sourceRow = this.getOrCreateRow(rowName, false);
			if(!sourceRow.getValue(key).equals(value)) {
				// copy row
				Row targetRow = target.getOrCreateRow(rowName, true);
				for(String colName : sourceRow.keySet()) {
					targetRow.setValue(colName, sourceRow.getValue(colName), true);
				}
			}
		}
		return target;
	}
	
	/**
	 * Dump table to System.out in LaTeX syntax
	 * 
	 * @throws IOException from System.out
	 */
	public void dumpToLaTeX() throws IOException {
		OutputStreamWriter osw = new OutputStreamWriter(System.out);
		toLaTeX(osw);
	}
	
	public void toLaTeX(Writer w) throws IOException {
		// determine padding
		Map<String,Integer> colName2maxLength = new HashMap<String,Integer>();
		for(Row row : this) {
			for(String colName : this.columnNames) {
				int oldmax = colName2maxLength.get(colName) == null ? 0 : colName2maxLength
				        .get(colName);
				String value = row.getValue(colName);
				int newmax = Math.max(oldmax, value.length());
				colName2maxLength.put(colName, newmax);
			}
		}
		
		// table control header
		w.write("\\begin{tabular}{|");
		
		for(int i = 0; i < this.columnNames.size(); i++) {
			w.write("l|");
		}
		w.write("}\n");
		// table header row
		w.write("\\hline\n");
		
		// calculate the last columns name
		String lastColumnName = null;
		Iterator<String> colNameIt = this.columnNames.iterator();
		while(colNameIt.hasNext()) {
			lastColumnName = colNameIt.next();
		}
		
		for(String colName : this.columnNames) {
			w.write(latexEncode(colName, colName2maxLength.get(colName)));
			if(colName.equals(lastColumnName)) {
				w.write(" \\\\\n");
			} else {
				w.write(" & ");
			}
		}
		w.write("\\hline\n");
		
		for(String rowName : this.rowNames) {
			Row row = this.getOrCreateRow(rowName, false);
			for(String colName : this.columnNames) {
				w.write(latexEncode(row.getValue(colName), colName2maxLength.get(colName)));
				if(colName.equals(lastColumnName)) {
					w.write(" \\\\\n");
				} else {
					w.write(" & ");
				}
			}
		}
		w.write("\\hline\n");
		w.write("\\end{tabular}\n");
		w.flush();
	}
	
	/**
	 * @param s
	 * @param fieldLength number of characters to be used to write the filed,
	 *            excess characters are truncated, missing characters are filled
	 *            with spaces.
	 * @return a String in LaTeX-safe encoding
	 */
	static String latexEncode(String s, int fieldLength) {
		String s2 = s;
		if(s == null) {
			s2 = "";
		} else if(s.equals("null")) {
			s2 = "";
		}
		
		while(s2.length() < fieldLength) {
			s2 = " " + s2;
		}
		
		return s2;
	}
	
	/**
	 * Add all values from the other table into this table
	 * 
	 * @param other never null
	 */
	public void addAll(CsvTable other) {
		for(Entry<String,Row> entry : other.table.entrySet()) {
			Row thisRow = this.getOrCreateRow(entry.getKey(), true);
			for(Entry<String,Cell> rowEntry : entry.getValue().entrySet()) {
				thisRow.setValue(rowEntry.getKey(), rowEntry.getValue().getValue());
			}
		}
	}
	
	/**
	 * For every change in 'colName' a new sub-table is created with the rows
	 * having the same values for 'colName'. Sorting is always performed
	 * automatically before executing this command.
	 * 
	 * @param colName never null
	 * @return a Map from String (the value of column 'colName') to
	 *         {@link CsvTable}
	 */
	public Map<String,CsvTable> split(String colName) {
		Map<String,CsvTable> map = new HashMap<String,CsvTable>();
		
		for(String rowName : this.rowNames) {
			Row row = this.getOrCreateRow(rowName, false);
			String currentValue = row.getValue(colName);
			
			CsvTable table = map.get(currentValue);
			if(table == null) {
				table = new CsvTable();
				map.put(currentValue, table);
			}
			
			Row copyRow = table.getOrCreateRow(rowName, true);
			copyRow.addAll(row);
		}
		
		return map;
	}
	
	/**
	 * Visit all rows with an {@link IRowVisitor}.
	 * 
	 * @param rowVisitor never null
	 */
	public void visitRows(IRowVisitor rowVisitor) {
		for(Row row : this) {
			rowVisitor.visit(row);
		}
	}
}
